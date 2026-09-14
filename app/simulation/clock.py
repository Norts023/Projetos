import asyncio
import logging

from sqlalchemy.orm import Session

from app import config
from app.database import STATE_LOCK, SessionLocal
from app.models import Company, GameClock
from app.simulation import bank, competitors, economy, land

logger = logging.getLogger("business_sim.clock")


def advance(session: Session, minutes: int) -> dict:
    """Advance the simulation by `minutes` game-minutes. Used by both the
    background real-time loop and the manual fast-forward endpoint.

    Market pricing uses a per-tick reversion/elasticity model that only
    stays numerically stable for small time steps, so large fast-forward
    jumps are broken into fixed-size sub-steps here rather than applied
    as a single huge step (which caused wild price oscillations)."""
    clock = session.get(GameClock, 1)
    company = session.get(Company, 1)

    old_day = clock.game_minutes // config.MINUTES_PER_DAY

    step = config.GAME_MINUTES_PER_TICK
    remaining = minutes
    produced = 0.0
    while remaining > 0:
        step_minutes = min(step, remaining)
        produced += economy.process_production(session, company, step_minutes)
        competitors.process_tick(session, step_minutes)
        economy.tick_market(session, step_minutes)
        remaining -= step_minutes

    clock.game_minutes += minutes
    new_day = clock.game_minutes // config.MINUTES_PER_DAY
    days_elapsed = new_day - old_day

    if days_elapsed > 0:
        land.charge_daily_upkeep(session, company, days_elapsed, clock.game_minutes)
        bank.process_due_loans(session, company, new_day, clock.game_minutes)
        competitors.apply_daily_growth(session, days_elapsed)

    session.commit()
    return {
        "game_minutes": clock.game_minutes,
        "day": new_day,
        "produced": produced,
        "days_elapsed": days_elapsed,
    }


async def run_background_clock() -> None:
    """Runs forever, ticking the game clock in real time while `running` is True."""
    while True:
        await asyncio.sleep(config.TICK_INTERVAL_SECONDS)
        async with STATE_LOCK:
            session = SessionLocal()
            try:
                clock = session.get(GameClock, 1)
                if clock is None or not clock.running:
                    continue
                minutes = max(1, round(config.GAME_MINUTES_PER_TICK * clock.speed_multiplier))
                advance(session, minutes)
            except Exception:
                logger.exception("Erro ao processar tick do relógio de jogo")
                session.rollback()
            finally:
                session.close()

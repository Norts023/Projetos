import asyncio
import logging

from sqlalchemy.orm import Session

from app import config
from app.database import SessionLocal
from app.models import Company, GameClock
from app.simulation import bank, economy, land

logger = logging.getLogger("business_sim.clock")


def advance(session: Session, minutes: int) -> dict:
    """Advance the simulation by `minutes` game-minutes. Used by both the
    background real-time loop and the manual fast-forward endpoint."""
    clock = session.get(GameClock, 1)
    company = session.get(Company, 1)

    old_day = clock.game_minutes // config.MINUTES_PER_DAY
    produced = economy.process_production(session, company, minutes)
    economy.tick_market(session, minutes)

    clock.game_minutes += minutes
    new_day = clock.game_minutes // config.MINUTES_PER_DAY
    days_elapsed = new_day - old_day

    if days_elapsed > 0:
        land.charge_daily_upkeep(session, company, days_elapsed, clock.game_minutes)
        bank.process_due_loans(session, company, new_day, clock.game_minutes)

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

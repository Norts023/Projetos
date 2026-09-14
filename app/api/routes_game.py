from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app import config
from app.api.deps import get_clock
from app.database import get_session
from app.models import GameClock
from app.schemas import AdvanceRequest, SpeedRequest
from app.simulation import clock as clock_sim

router = APIRouter(prefix="/api/game", tags=["game"])


@router.get("/state")
def get_state(clock: GameClock = Depends(get_clock)):
    day = clock.game_minutes // 1440
    return {
        "game_minutes": clock.game_minutes,
        "day": day,
        "running": clock.running,
        "speed_multiplier": clock.speed_multiplier,
        "beginner_boost_active": day < config.BEGINNER_BOOST_DAYS,
        "beginner_boost_days_left": max(0, config.BEGINNER_BOOST_DAYS - day),
    }


@router.post("/advance")
def advance(body: AdvanceRequest, session: Session = Depends(get_session)):
    return clock_sim.advance(session, body.minutes)


@router.post("/speed")
def set_speed(body: SpeedRequest, clock: GameClock = Depends(get_clock),
              session: Session = Depends(get_session)):
    clock.running = body.running
    clock.speed_multiplier = body.multiplier
    session.commit()
    return {"running": clock.running, "speed_multiplier": clock.speed_multiplier}

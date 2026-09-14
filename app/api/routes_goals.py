from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app import config
from app.database import get_session
from app.models import AchievedGoal

router = APIRouter(prefix="/api/goals", tags=["goals"])


@router.get("")
def list_goals(session: Session = Depends(get_session)):
    achieved = {row.goal_id: row.achieved_day for row in session.query(AchievedGoal).all()}
    return [
        {
            "id": goal["id"], "label": goal["label"], "description": goal["description"],
            "reward_cash": goal["reward_cash"], "achieved": goal["id"] in achieved,
            "achieved_day": achieved.get(goal["id"]),
        }
        for goal in config.GOALS
    ]

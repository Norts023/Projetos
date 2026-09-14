from sqlalchemy.orm import Session

from app.config import MINUTES_PER_DAY
from app.models import LedgerEntry


def record(session: Session, company_id: int, game_minutes: int, entry_type: str,
           category: str, amount: float, description: str = "") -> None:
    session.add(LedgerEntry(
        company_id=company_id,
        day=game_minutes // MINUTES_PER_DAY,
        game_minutes=game_minutes,
        entry_type=entry_type,
        category=category,
        amount=round(amount, 2),
        description=description,
    ))

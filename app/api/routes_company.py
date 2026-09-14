from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_clock, get_company
from app.database import get_session
from app.models import Company, GameClock

router = APIRouter(prefix="/api/company", tags=["company"])


@router.get("")
def get_state(company: Company = Depends(get_company), clock: GameClock = Depends(get_clock),
              session: Session = Depends(get_session)):
    return {
        "name": company.name,
        "cash": round(company.cash, 2),
        "credit_score": company.credit_score,
        "day": clock.game_minutes // 1440,
        "game_minutes": clock.game_minutes,
        "inventory": {item.good_name: round(item.quantity, 2) for item in company.inventory},
        "factory_count": len(company.factories),
        "land_owned": len(company.land_plots),
        "active_loans": len([loan for loan in company.loans if loan.status == "ACTIVE"]),
    }

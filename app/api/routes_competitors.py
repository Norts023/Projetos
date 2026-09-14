from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_company
from app.database import get_session
from app.models import Company, Competitor
from app.simulation import finance

router = APIRouter(prefix="/api/competitors", tags=["competitors"])


@router.get("")
def get_leaderboard(company: Company = Depends(get_company), session: Session = Depends(get_session)):
    balance = finance.balance_sheet(session, company)
    rows = [{
        "name": company.name,
        "is_player": True,
        "valuation": balance["equity"],
        "total_produced": None,
    }]
    for competitor in session.query(Competitor).all():
        rows.append({
            "name": competitor.name,
            "is_player": False,
            "valuation": round(competitor.cash, 2),
            "total_produced": round(competitor.total_produced, 1),
        })
    rows.sort(key=lambda r: r["valuation"], reverse=True)
    return rows

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_session
from app.models import MarketGoodState

router = APIRouter(prefix="/api/market", tags=["market"])


@router.get("")
def get_market(session: Session = Depends(get_session)):
    goods = session.query(MarketGoodState).all()
    return [
        {"name": g.name, "current_price": round(g.current_price, 2)}
        for g in goods
    ]

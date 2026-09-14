"""Developer-only endpoints to directly edit game state for testing.

Not linked from the normal game UI (only from static/admin.html). No
authentication: this game only ever runs locally on the developer's own
machine.
"""

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app import config
from app.api.deps import get_clock, get_company
from app.database import Base, SessionLocal, engine, get_session
from app.models import Company, Competitor, GameClock, InventoryItem, MarketGoodState
from app.schemas import AdminCompanyUpdate, AdminCompetitorUpdate, AdminGameUpdate, AdminMarketUpdate
from app.seed import seed_if_empty

router = APIRouter(prefix="/api/admin", tags=["admin"])


@router.get("/state")
def get_full_state(company: Company = Depends(get_company), clock: GameClock = Depends(get_clock),
                    session: Session = Depends(get_session)):
    return {
        "company": {"cash": company.cash, "credit_score": company.credit_score,
                    "inventory": {i.good_name: i.quantity for i in company.inventory}},
        "clock": {"game_minutes": clock.game_minutes, "day": clock.game_minutes // config.MINUTES_PER_DAY,
                  "running": clock.running, "speed_multiplier": clock.speed_multiplier},
        "market": [{"name": m.name, "current_price": m.current_price}
                   for m in session.query(MarketGoodState).all()],
        "competitors": [{"id": c.id, "name": c.name, "cash": c.cash,
                          "production_rate_per_hour": c.production_rate_per_hour}
                         for c in session.query(Competitor).all()],
    }


@router.post("/company")
def update_company(body: AdminCompanyUpdate, company: Company = Depends(get_company),
                    session: Session = Depends(get_session)):
    if body.cash is not None:
        company.cash = body.cash
    if body.credit_score is not None:
        company.credit_score = body.credit_score
    session.commit()
    return {"cash": company.cash, "credit_score": company.credit_score}


@router.post("/company/inventory/{good_name}")
def set_inventory(good_name: str, quantity: float, company: Company = Depends(get_company),
                   session: Session = Depends(get_session)):
    if quantity < 0:
        raise HTTPException(status_code=400, detail="Quantidade não pode ser negativa")
    item = session.query(InventoryItem).filter_by(company_id=company.id, good_name=good_name).one_or_none()
    if item is None:
        raise HTTPException(status_code=404, detail=f"Item de estoque '{good_name}' não encontrado")
    item.quantity = quantity
    session.commit()
    return {"good_name": good_name, "quantity": item.quantity}


@router.post("/market")
def set_market_price(body: AdminMarketUpdate, session: Session = Depends(get_session)):
    market = session.get(MarketGoodState, body.good_name)
    if market is None:
        raise HTTPException(status_code=404, detail=f"Mercadoria '{body.good_name}' não encontrada")
    market.current_price = body.price
    session.commit()
    return {"good_name": market.name, "current_price": market.current_price}


@router.post("/competitors/{competitor_id}")
def update_competitor(competitor_id: int, body: AdminCompetitorUpdate, session: Session = Depends(get_session)):
    competitor = session.get(Competitor, competitor_id)
    if competitor is None:
        raise HTTPException(status_code=404, detail="Concorrente não encontrado")
    if body.cash is not None:
        competitor.cash = body.cash
    if body.production_rate_per_hour is not None:
        competitor.production_rate_per_hour = body.production_rate_per_hour
    session.commit()
    return {"id": competitor.id, "cash": competitor.cash,
            "production_rate_per_hour": competitor.production_rate_per_hour}


@router.post("/game")
def set_game_time(body: AdminGameUpdate, clock: GameClock = Depends(get_clock),
                   session: Session = Depends(get_session)):
    if body.game_minutes is not None:
        clock.game_minutes = body.game_minutes
    session.commit()
    return {"game_minutes": clock.game_minutes, "day": clock.game_minutes // config.MINUTES_PER_DAY}


@router.post("/reset")
def reset_game():
    """Wipes all game data and reseeds a fresh start. Use with care."""
    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)
    session = SessionLocal()
    try:
        seed_if_empty(session)
    finally:
        session.close()
    return {"status": "reset"}

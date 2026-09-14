from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app import config
from app.api.deps import get_clock, get_company
from app.database import get_session
from app.models import Company, GameClock, MarketGoodState, RetailOrder
from app.schemas import RetailOrderRequest
from app.simulation import retail as retail_sim

router = APIRouter(prefix="/api/retail", tags=["retail"])


@router.get("/orders")
def list_orders(company: Company = Depends(get_company), session: Session = Depends(get_session)):
    orders = [o for o in company.retail_orders if o.status == "ACTIVE"]
    result = []
    for order in orders:
        market_price = session.get(MarketGoodState, order.good_name).current_price
        rate = retail_sim.estimate_rate_per_hour(market_price, order.price_per_unit)
        result.append({
            "id": order.id, "good_name": order.good_name,
            "good_label": config.GOODS[order.good_name]["label"], "price_per_unit": order.price_per_unit,
            "quantity_remaining": round(order.quantity_remaining, 2),
            "quantity_original": order.quantity_original,
            "estimated_rate_per_hour": round(rate, 2),
            "estimated_hours_left": round(order.quantity_remaining / rate, 1) if rate > 0 else None,
            "market_price": round(market_price, 2),
        })
    return result


@router.post("/orders")
def create_order(body: RetailOrderRequest, company: Company = Depends(get_company),
                  clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    current_day = clock.game_minutes // 1440
    try:
        order = retail_sim.create_order(session, company, body.good_name, body.quantity,
                                         body.price_per_unit, current_day)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    session.commit()
    return {"order_id": order.id}


@router.post("/orders/{order_id}/cancel")
def cancel_order(order_id: int, company: Company = Depends(get_company), session: Session = Depends(get_session)):
    order = session.get(RetailOrder, order_id)
    if order is None:
        raise HTTPException(status_code=404, detail="Pedido não encontrado")
    try:
        retail_sim.cancel_order(session, company, order)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    session.commit()
    return {"status": "cancelled"}


@router.get("/estimate")
def estimate(good_name: str, price: float, session: Session = Depends(get_session)):
    market = session.get(MarketGoodState, good_name)
    if market is None:
        raise HTTPException(status_code=404, detail="Bem não encontrado")
    rate = retail_sim.estimate_rate_per_hour(market.current_price, price)
    return {"market_price": round(market.current_price, 2), "estimated_rate_per_hour": round(rate, 2)}

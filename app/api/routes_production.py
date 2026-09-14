from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.api.deps import get_clock, get_company
from app.database import get_session
from app.models import Company, Factory, GameClock, LandPlot
from app.schemas import QuantityRequest
from app.simulation import land, trade

router = APIRouter(prefix="/api/production", tags=["production"])


@router.post("/buy-material")
def buy_material(body: QuantityRequest, company: Company = Depends(get_company),
                  clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    try:
        cost = trade.buy_material(session, company, body.quantity, clock.game_minutes)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    session.commit()
    return {"cost": round(cost, 2), "cash": round(company.cash, 2)}


@router.post("/sell-product")
def sell_product(body: QuantityRequest, company: Company = Depends(get_company),
                  clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    try:
        revenue = trade.sell_product(session, company, body.quantity, clock.game_minutes)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    session.commit()
    return {"revenue": round(revenue, 2), "cash": round(company.cash, 2)}


@router.get("/factories")
def list_factories(company: Company = Depends(get_company)):
    return [
        {"id": f.id, "land_plot": f.land_plot.name, "level": f.level}
        for f in company.factories
    ]


@router.post("/build-factory/{plot_id}")
def build_factory(plot_id: int, company: Company = Depends(get_company),
                   clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    plot = session.get(LandPlot, plot_id)
    if plot is None:
        raise HTTPException(status_code=404, detail="Terreno não encontrado")
    try:
        factory = land.build_factory(session, company, plot, clock.game_minutes)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    session.commit()
    return {"factory_id": factory.id, "cash": round(company.cash, 2)}

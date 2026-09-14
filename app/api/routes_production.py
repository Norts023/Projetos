from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app import config
from app.api.deps import get_clock, get_company
from app.database import get_session
from app.models import Company, GameClock, LandPlot
from app.schemas import BuildFactoryRequest, GoodQuantityRequest
from app.simulation import land, trade

router = APIRouter(prefix="/api/production", tags=["production"])


@router.post("/buy")
def buy_good(body: GoodQuantityRequest, company: Company = Depends(get_company),
             clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    try:
        cost = trade.buy_good(session, company, body.good_name, body.quantity, clock.game_minutes)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    session.commit()
    return {"cost": round(cost, 2), "cash": round(company.cash, 2)}


@router.post("/sell")
def sell_good(body: GoodQuantityRequest, company: Company = Depends(get_company),
              clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    try:
        revenue = trade.sell_good(session, company, body.good_name, body.quantity, clock.game_minutes)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    session.commit()
    return {"revenue": round(revenue, 2), "cash": round(company.cash, 2)}


@router.get("/factories")
def list_factories(company: Company = Depends(get_company)):
    return [
        {
            "id": f.id, "land_plot_id": f.land_plot_id, "land_plot": f.land_plot.name,
            "recipe_id": f.recipe_id, "recipe_label": config.RECIPES[f.recipe_id]["label"],
            "output_good": config.RECIPES[f.recipe_id]["output_good"],
            "output_label": config.GOODS[config.RECIPES[f.recipe_id]["output_good"]]["label"],
            "level": f.level,
        }
        for f in company.factories
    ]


@router.post("/build-factory/{plot_id}")
def build_factory(plot_id: int, body: BuildFactoryRequest, company: Company = Depends(get_company),
                   clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    plot = session.get(LandPlot, plot_id)
    if plot is None:
        raise HTTPException(status_code=404, detail="Terreno não encontrado")
    try:
        factory = land.build_factory(session, company, plot, body.recipe_id, clock.game_minutes)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    session.commit()
    return {"factory_id": factory.id, "cash": round(company.cash, 2)}

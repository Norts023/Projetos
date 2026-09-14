from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app import config
from app.api.deps import get_clock, get_company
from app.database import get_session
from app.models import Company, Factory, GameClock, LandPlot
from app.schemas import BuildFactoryRequest, GoodQuantityRequest
from app.simulation import construction, trade

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
def list_factories(company: Company = Depends(get_company), session: Session = Depends(get_session)):
    inventory = {item.good_name: item.quantity for item in company.inventory}
    result = []
    for f in company.factories:
        recipe = config.RECIPES[f.recipe_id]
        entry = {
            "id": f.id, "land_plot_id": f.land_plot_id, "land_plot": f.land_plot.name,
            "recipe_id": f.recipe_id, "recipe_label": recipe["label"],
            "output_good": recipe["output_good"], "output_label": config.GOODS[recipe["output_good"]]["label"],
            "sector": recipe["sector"], "sector_icon": recipe["sector_icon"],
            "level": f.level, "status": f.status,
            "construction_started_minutes": f.construction_started_minutes,
            "busy_until_minutes": f.busy_until_minutes,
            "inputs": [
                {"good": good, "label": config.GOODS[good]["label"], "stock": round(inventory.get(good, 0.0), 1)}
                for good in recipe["inputs"]
            ],
        }
        if f.status == "ACTIVE":
            next_level = f.level + 1
            upgrade_cost = recipe["build_cost"] * config.FACTORY_UPGRADE_COST_FACTOR * next_level
            entry["upgrade_plan"] = construction.compute_plan(session, upgrade_cost)
            entry["upgrade_plan"]["materials_labels"] = {
                config.GOODS[g]["label"]: q for g, q in entry["upgrade_plan"]["materials"].items()
            }
        result.append(entry)
    return result


@router.post("/build-factory/{plot_id}")
def build_factory(plot_id: int, body: BuildFactoryRequest, company: Company = Depends(get_company),
                   clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    plot = session.get(LandPlot, plot_id)
    if plot is None:
        raise HTTPException(status_code=404, detail="Terreno não encontrado")
    try:
        factory = construction.start_build(session, company, plot, body.recipe_id, clock.game_minutes)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    session.commit()
    return {"factory_id": factory.id, "cash": round(company.cash, 2),
            "busy_until_minutes": factory.busy_until_minutes}


@router.get("/build-plan")
def build_plan(recipe_id: str, session: Session = Depends(get_session)):
    if recipe_id not in config.RECIPES:
        raise HTTPException(status_code=404, detail="Receita não encontrada")
    return construction.compute_plan(session, config.RECIPES[recipe_id]["build_cost"])


@router.post("/factories/{factory_id}/upgrade")
def upgrade_factory(factory_id: int, company: Company = Depends(get_company),
                     clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    factory = session.get(Factory, factory_id)
    if factory is None or factory.company_id != company.id:
        raise HTTPException(status_code=404, detail="Fábrica não encontrada")
    try:
        plan = construction.start_upgrade(session, company, factory, clock.game_minutes)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    session.commit()
    return {"busy_until_minutes": factory.busy_until_minutes, "cash": round(company.cash, 2), "plan": plan}


@router.post("/factories/{factory_id}/rush")
def rush_factory(factory_id: int, company: Company = Depends(get_company),
                  clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    factory = session.get(Factory, factory_id)
    if factory is None or factory.company_id != company.id:
        raise HTTPException(status_code=404, detail="Fábrica não encontrada")
    try:
        cost = construction.rush(session, company, factory, clock.game_minutes)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    session.commit()
    return {"paid": cost, "cash": round(company.cash, 2), "status": factory.status, "level": factory.level}

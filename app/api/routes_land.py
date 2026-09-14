from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.api.deps import get_clock, get_company
from app.database import get_session
from app.models import Company, GameClock, LandPlot
from app.simulation import land as land_sim

router = APIRouter(prefix="/api/land", tags=["land"])


@router.get("")
def list_land(session: Session = Depends(get_session)):
    plots = session.query(LandPlot).all()
    return [
        {
            "id": p.id, "name": p.name, "region": p.region, "price": p.price,
            "terraforming_cost": p.terraforming_cost, "logistics_bonus": p.logistics_bonus,
            "capacity": p.capacity, "owned": p.owner_id is not None,
        }
        for p in plots
    ]


@router.post("/{plot_id}/buy")
def buy_land(plot_id: int, company: Company = Depends(get_company),
             clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    plot = session.get(LandPlot, plot_id)
    if plot is None:
        raise HTTPException(status_code=404, detail="Terreno não encontrado")
    try:
        land_sim.buy_land(session, company, plot, clock.game_minutes)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    session.commit()
    return {"plot_id": plot.id, "cash": round(company.cash, 2)}

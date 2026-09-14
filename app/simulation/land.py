from sqlalchemy.orm import Session

from app import config
from app.models import Company, LandPlot
from app.simulation import categories
from app.simulation.ledger import record


def buy_land(session: Session, company: Company, plot: LandPlot, game_minutes: int) -> None:
    if plot.owner_id is not None:
        raise ValueError("Terreno já possui dono")
    total_cost = plot.price + plot.terraforming_cost
    if company.cash < total_cost:
        raise ValueError(f"Caixa insuficiente (necessário {total_cost:.2f})")

    company.cash -= total_cost
    plot.owner_id = company.id
    plot.purchased_at_minutes = game_minutes
    record(session, company.id, game_minutes, categories.EXPENSE, categories.LAND_PURCHASE,
           total_cost, f"Compra de terreno: {plot.name}")


def charge_daily_upkeep(session: Session, company: Company, days_elapsed: int, game_minutes: int) -> None:
    if days_elapsed <= 0 or not company.factories:
        return
    total_upkeep = config.FACTORY_UPKEEP_PER_DAY * len(company.factories) * days_elapsed
    company.cash -= total_upkeep
    record(session, company.id, game_minutes, categories.EXPENSE, categories.UPKEEP,
           total_upkeep, f"Manutenção de {len(company.factories)} fábrica(s) por {days_elapsed} dia(s)")

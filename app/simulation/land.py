from sqlalchemy.orm import Session

from app import config
from app.models import Company, Factory, LandPlot
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


def build_factory(session: Session, company: Company, plot: LandPlot, game_minutes: int) -> Factory:
    if plot.owner_id != company.id:
        raise ValueError("Você não é dono deste terreno")
    existing = [f for f in company.factories if f.land_plot_id == plot.id]
    if len(existing) >= plot.capacity:
        raise ValueError("Capacidade do terreno esgotada")
    if company.cash < config.FACTORY_BUILD_COST:
        raise ValueError(f"Caixa insuficiente (necessário {config.FACTORY_BUILD_COST:.2f})")

    company.cash -= config.FACTORY_BUILD_COST
    factory = Factory(company_id=company.id, land_plot_id=plot.id, level=1, built_at_minutes=game_minutes)
    session.add(factory)
    record(session, company.id, game_minutes, categories.EXPENSE, categories.FACTORY_BUILD,
           config.FACTORY_BUILD_COST, f"Construção de fábrica em {plot.name}")
    return factory


def charge_daily_upkeep(session: Session, company: Company, days_elapsed: int, game_minutes: int) -> None:
    if days_elapsed <= 0 or not company.factories:
        return
    total_upkeep = config.FACTORY_UPKEEP_PER_DAY * len(company.factories) * days_elapsed
    company.cash -= total_upkeep
    record(session, company.id, game_minutes, categories.EXPENSE, categories.UPKEEP,
           total_upkeep, f"Manutenção de {len(company.factories)} fábrica(s) por {days_elapsed} dia(s)")

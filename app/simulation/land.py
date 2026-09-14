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


def build_factory(session: Session, company: Company, plot: LandPlot, recipe_id: str,
                   game_minutes: int) -> Factory:
    if recipe_id not in config.RECIPES:
        raise ValueError(f"Receita '{recipe_id}' não existe")
    if plot.owner_id != company.id:
        raise ValueError("Você não é dono deste terreno")
    existing = [f for f in company.factories if f.land_plot_id == plot.id]
    if len(existing) >= plot.capacity:
        raise ValueError("Capacidade do terreno esgotada")

    recipe = config.RECIPES[recipe_id]
    build_cost = recipe["build_cost"]
    if company.cash < build_cost:
        raise ValueError(f"Caixa insuficiente (necessário {build_cost:.2f})")

    company.cash -= build_cost
    factory = Factory(company_id=company.id, land_plot_id=plot.id, recipe_id=recipe_id,
                       level=1, built_at_minutes=game_minutes)
    session.add(factory)
    record(session, company.id, game_minutes, categories.EXPENSE, categories.FACTORY_BUILD,
           build_cost, f"Construção de {recipe['label']} em {plot.name}")
    return factory


def charge_daily_upkeep(session: Session, company: Company, days_elapsed: int, game_minutes: int) -> None:
    if days_elapsed <= 0 or not company.factories:
        return
    total_upkeep = config.FACTORY_UPKEEP_PER_DAY * len(company.factories) * days_elapsed
    company.cash -= total_upkeep
    record(session, company.id, game_minutes, categories.EXPENSE, categories.UPKEEP,
           total_upkeep, f"Manutenção de {len(company.factories)} fábrica(s) por {days_elapsed} dia(s)")

import math

from sqlalchemy.orm import Session

from app import config
from app.models import Company, Factory, LandPlot, MarketGoodState
from app.simulation import categories
from app.simulation.economy import get_inventory
from app.simulation.ledger import record


def compute_plan(session: Session, cost: float) -> dict:
    """Splits a $ cost into a cash portion and the 4 universal construction
    materials (at current market prices), plus how long it takes to build."""
    prices = {m: session.get(MarketGoodState, m).current_price for m in config.CONSTRUCTION_MATERIALS}
    cash_cost = cost * config.CONSTRUCTION_CASH_SHARE
    value_per_material = (cost * (1 - config.CONSTRUCTION_CASH_SHARE)) / len(config.CONSTRUCTION_MATERIALS)
    materials = {m: math.ceil(value_per_material / prices[m]) for m in config.CONSTRUCTION_MATERIALS}
    minutes = int(min(
        max(cost * config.CONSTRUCTION_MINUTES_PER_DOLLAR, config.CONSTRUCTION_MIN_MINUTES),
        config.CONSTRUCTION_MAX_MINUTES,
    ))
    return {"cash_cost": round(cash_cost, 2), "materials": materials, "minutes": minutes, "cost_basis": cost}


def _consume_resources(session: Session, company: Company, plan: dict, description: str, game_minutes: int) -> None:
    if company.cash < plan["cash_cost"]:
        raise ValueError(f"Caixa insuficiente (necessário {plan['cash_cost']:.2f} em dinheiro)")
    for good, qty in plan["materials"].items():
        inv = get_inventory(session, company.id, good)
        if inv.quantity < qty:
            raise ValueError(f"Falta {config.GOODS[good]['label']}: precisa de {qty}, tem {inv.quantity:.1f}")

    company.cash -= plan["cash_cost"]
    for good, qty in plan["materials"].items():
        get_inventory(session, company.id, good).quantity -= qty
    record(session, company.id, game_minutes, categories.EXPENSE, categories.FACTORY_BUILD,
           plan["cash_cost"], description)


def start_build(session: Session, company: Company, plot: LandPlot, recipe_id: str, game_minutes: int) -> Factory:
    if recipe_id not in config.RECIPES:
        raise ValueError(f"Receita '{recipe_id}' não existe")
    if plot.owner_id != company.id:
        raise ValueError("Você não é dono deste terreno")
    existing = [f for f in company.factories if f.land_plot_id == plot.id]
    if len(existing) >= plot.capacity:
        raise ValueError("Capacidade do terreno esgotada")

    recipe = config.RECIPES[recipe_id]
    plan = compute_plan(session, recipe["build_cost"])
    _consume_resources(session, company, plan, f"Construção de {recipe['label']} em {plot.name}", game_minutes)

    factory = Factory(
        company_id=company.id, land_plot_id=plot.id, recipe_id=recipe_id, level=1,
        built_at_minutes=game_minutes, status="BUILDING",
        construction_started_minutes=game_minutes, busy_until_minutes=game_minutes + plan["minutes"],
        construction_cost_basis=plan["cost_basis"],
    )
    session.add(factory)
    return factory


def start_upgrade(session: Session, company: Company, factory: Factory, game_minutes: int) -> dict:
    if factory.status != "ACTIVE":
        raise ValueError("Fábrica ocupada (em construção ou já em melhoria)")

    recipe = config.RECIPES[factory.recipe_id]
    next_level = factory.level + 1
    upgrade_cost = recipe["build_cost"] * config.FACTORY_UPGRADE_COST_FACTOR * next_level
    plan = compute_plan(session, upgrade_cost)
    _consume_resources(session, company, plan,
                        f"Melhoria de {recipe['label']} para nível {next_level}", game_minutes)

    factory.status = "UPGRADING"
    factory.construction_started_minutes = game_minutes
    factory.busy_until_minutes = game_minutes + plan["minutes"]
    factory.construction_cost_basis = plan["cost_basis"]
    return plan


def rush(session: Session, company: Company, factory: Factory, game_minutes: int) -> float:
    if factory.status == "ACTIVE" or factory.busy_until_minutes is None:
        raise ValueError("Fábrica não está em construção nem em melhoria")

    total_minutes = max(1, factory.busy_until_minutes - factory.construction_started_minutes)
    remaining_minutes = max(0, factory.busy_until_minutes - game_minutes)
    if remaining_minutes <= 0:
        raise ValueError("Construção já concluída")

    rush_cost = (remaining_minutes / total_minutes) * factory.construction_cost_basis * config.RUSH_PREMIUM_MULTIPLIER
    rush_cost = round(rush_cost, 2)
    if company.cash < rush_cost:
        raise ValueError(f"Caixa insuficiente para apressar (necessário {rush_cost:.2f})")

    company.cash -= rush_cost
    record(session, company.id, game_minutes, categories.EXPENSE, categories.FACTORY_BUILD,
           rush_cost, f"Pagamento para apressar construção/melhoria da fábrica #{factory.id}")
    _complete(factory)
    return rush_cost


def _complete(factory: Factory) -> None:
    if factory.status == "UPGRADING":
        factory.level += 1
    factory.status = "ACTIVE"
    factory.busy_until_minutes = None
    factory.construction_started_minutes = None
    factory.construction_cost_basis = None


def process_completions(session: Session, company: Company, game_minutes: int) -> list[int]:
    completed_ids = []
    for factory in company.factories:
        if factory.status != "ACTIVE" and factory.busy_until_minutes is not None \
                and factory.busy_until_minutes <= game_minutes:
            _complete(factory)
            completed_ids.append(factory.id)
    return completed_ids

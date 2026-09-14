from sqlalchemy.orm import Session

from app import config
from app.models import Company, InventoryItem, MarketGoodState


def get_inventory(session: Session, company_id: int, good_name: str) -> InventoryItem:
    item = session.query(InventoryItem).filter_by(company_id=company_id, good_name=good_name).one()
    return item


def get_inventory_map(session: Session, company_id: int) -> dict[str, InventoryItem]:
    items = session.query(InventoryItem).filter_by(company_id=company_id).all()
    return {item.good_name: item for item in items}


def process_production(session: Session, company: Company, minutes: int, boost_multiplier: float = 1.0) -> float:
    """Run every owned factory's recipe: consume its input goods (possibly more
    than one) and produce its output good. A factory that lacks enough of any
    input runs at a reduced rate, limited by the scarcest input.

    `boost_multiplier` applies the early-game "beginner boost" on top of the
    recipe's base rate (see BEGINNER_BOOST_DAYS in config).

    Returns total units produced across all factories (mixed goods).
    """
    if not company.factories:
        return 0.0

    inventory = get_inventory_map(session, company.id)
    total_produced = 0.0

    for factory in company.factories:
        if factory.status != "ACTIVE":
            continue
        recipe = config.RECIPES[factory.recipe_id]
        bonus = factory.land_plot.logistics_bonus
        desired_output = (
            recipe["output_rate_per_hour"] * factory.level * (minutes / 60.0)
            * (1 + bonus) * boost_multiplier
        )
        if desired_output <= 0:
            continue

        limiting_ratio = 1.0
        for input_good, ratio in recipe["inputs"].items():
            required = desired_output * ratio
            if required <= 0:
                continue
            available = inventory[input_good].quantity
            if available < required:
                limiting_ratio = min(limiting_ratio, available / required)

        actual_output = desired_output * limiting_ratio
        if actual_output <= 0:
            continue

        for input_good, ratio in recipe["inputs"].items():
            inventory[input_good].quantity = max(0.0, inventory[input_good].quantity - actual_output * ratio)

        inventory[recipe["output_good"]].quantity += actual_output
        total_produced += actual_output

    return total_produced


def _clamp_price(good_name: str, price: float) -> float:
    min_price = config.GOODS[good_name]["min_price"]
    base_price = config.GOODS[good_name]["base_price"]
    return max(min_price, min(price, base_price * 10))


def tick_market(session: Session, minutes: int) -> None:
    """Advance every market good's dynamic price based on accumulated supply/demand,
    with a gentle pull back toward the base price so pressure that eases off doesn't
    leave prices permanently pinned at an extreme."""
    for good_name in config.GOODS:
        market = session.get(MarketGoodState, good_name)
        hours = minutes / 60.0
        market.recent_demand += config.BASELINE_DEMAND_PER_HOUR.get(good_name, 0.0) * hours
        market.recent_supply += config.BASELINE_SUPPLY_PER_HOUR.get(good_name, 0.0) * hours

        imbalance = market.recent_demand - market.recent_supply
        adjustment = config.PRICE_ELASTICITY * (imbalance / 100.0)

        base_price = config.GOODS[good_name]["base_price"]
        reversion = -config.PRICE_REVERSION_PER_HOUR * hours * (market.current_price - base_price) / base_price

        market.current_price = _clamp_price(good_name, market.current_price * (1 + adjustment + reversion))

        # sliding-window decay so old trades stop influencing price forever
        market.recent_demand *= 0.95
        market.recent_supply *= 0.95


def add_flow(market: MarketGoodState, quantity: float, is_buy: bool) -> None:
    """Register background trade volume (e.g. AI competitors) that only nudges price
    through the next periodic tick_market call, without an immediate price shock."""
    if is_buy:
        market.recent_demand += quantity
    else:
        market.recent_supply += quantity


def apply_market_impact(market: MarketGoodState, quantity: float, is_buy: bool) -> None:
    """Immediate price impact of a deliberate player trade, on top of the periodic tick drift."""
    reference_volume = 50.0
    impact = config.PRICE_ELASTICITY * (quantity / reference_volume)
    if is_buy:
        market.current_price = _clamp_price(market.name, market.current_price * (1 + impact))
        market.recent_demand += quantity
    else:
        market.current_price = _clamp_price(market.name, market.current_price * (1 - impact))
        market.recent_supply += quantity

from sqlalchemy.orm import Session

from app import config
from app.models import Company, InventoryItem, MarketGoodState


def get_inventory(session: Session, company_id: int, good_name: str) -> InventoryItem:
    item = session.query(InventoryItem).filter_by(company_id=company_id, good_name=good_name).one()
    return item


def process_production(session: Session, company: Company, minutes: int) -> float:
    """Consume raw material and produce finished goods for every owned factory.

    Returns total units produced.
    """
    if not company.factories:
        return 0.0

    raw_inv = get_inventory(session, company.id, config.RAW_MATERIAL)
    product_inv = get_inventory(session, company.id, config.PRODUCT)

    total_produced = 0.0
    for factory in company.factories:
        bonus = factory.land_plot.logistics_bonus
        desired_output = (
            config.BASE_FACTORY_PRODUCTION_PER_HOUR * factory.level * (minutes / 60.0) * (1 + bonus)
        )
        required_material = desired_output * config.BASE_FACTORY_MATERIAL_CONSUMPTION_RATIO

        if required_material <= 0:
            continue

        if raw_inv.quantity < required_material:
            ratio = raw_inv.quantity / required_material
            actual_output = desired_output * ratio
            consumed = raw_inv.quantity
        else:
            actual_output = desired_output
            consumed = required_material

        raw_inv.quantity = max(0.0, raw_inv.quantity - consumed)
        total_produced += actual_output

    product_inv.quantity += total_produced
    return total_produced


def _clamp_price(good_name: str, price: float) -> float:
    min_price = config.MARKET_GOODS[good_name]["min_price"]
    base_price = config.MARKET_GOODS[good_name]["base_price"]
    return max(min_price, min(price, base_price * 10))


def tick_market(session: Session, minutes: int) -> None:
    """Advance every market good's dynamic price based on accumulated supply/demand."""
    for good_name in config.MARKET_GOODS:
        market = session.get(MarketGoodState, good_name)
        baseline_demand = config.BASELINE_DEMAND_PER_HOUR.get(good_name, 0.0) * (minutes / 60.0)
        market.recent_demand += baseline_demand

        imbalance = market.recent_demand - market.recent_supply
        adjustment = config.PRICE_ELASTICITY * (imbalance / 100.0)
        market.current_price = _clamp_price(good_name, market.current_price * (1 + adjustment))

        # sliding-window decay so old trades stop influencing price forever
        market.recent_demand *= 0.95
        market.recent_supply *= 0.95


def apply_market_impact(market: MarketGoodState, quantity: float, is_buy: bool) -> None:
    """Immediate price impact of a player trade, on top of the periodic tick drift."""
    reference_volume = 50.0
    impact = config.PRICE_ELASTICITY * (quantity / reference_volume)
    if is_buy:
        market.current_price = _clamp_price(market.name, market.current_price * (1 + impact))
        market.recent_demand += quantity
    else:
        market.current_price = _clamp_price(market.name, market.current_price * (1 - impact))
        market.recent_supply += quantity

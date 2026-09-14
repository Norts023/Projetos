from sqlalchemy.orm import Session

from app import config
from app.models import Company, MarketGoodState, RetailOrder
from app.simulation import categories
from app.simulation.economy import get_inventory
from app.simulation.ledger import record


def estimate_rate_per_hour(market_price: float, order_price: float) -> float:
    """Higher price than the market -> sells slower; lower -> sells faster.
    Mirrors the price/speed tradeoff described in the Sim Companies retail
    guide, without a separate simulated retail demand curve."""
    if order_price <= 0:
        return 0.0
    ratio = market_price / order_price
    rate = config.RETAIL_REFERENCE_RATE_PER_HOUR * (ratio ** config.RETAIL_PRICE_ELASTICITY_EXPONENT)
    return max(config.RETAIL_MIN_RATE_PER_HOUR, min(rate, config.RETAIL_MAX_RATE_PER_HOUR))


def create_order(session: Session, company: Company, good_name: str, quantity: float,
                  price_per_unit: float, current_day: int) -> RetailOrder:
    if good_name not in config.GOODS:
        raise ValueError(f"Bem '{good_name}' não existe")
    if quantity <= 0 or price_per_unit <= 0:
        raise ValueError("Quantidade e preço devem ser positivos")

    inv = get_inventory(session, company.id, good_name)
    if inv.quantity < quantity:
        raise ValueError(f"Estoque insuficiente (disponível {inv.quantity:.2f})")

    inv.quantity -= quantity
    order = RetailOrder(
        company_id=company.id, good_name=good_name, price_per_unit=price_per_unit,
        quantity_remaining=quantity, quantity_original=quantity, status="ACTIVE",
        created_day=current_day,
    )
    session.add(order)
    return order


def cancel_order(session: Session, company: Company, order: RetailOrder) -> None:
    if order.company_id != company.id:
        raise ValueError("Este pedido não pertence à sua empresa")
    if order.status != "ACTIVE":
        raise ValueError("Pedido não está ativo")

    inv = get_inventory(session, company.id, order.good_name)
    inv.quantity += order.quantity_remaining
    order.quantity_remaining = 0.0
    order.status = "CANCELLED"


def process_tick(session: Session, company: Company, minutes: int, game_minutes: int) -> None:
    hours = minutes / 60.0
    orders = session.query(RetailOrder).filter_by(company_id=company.id, status="ACTIVE").all()
    for order in orders:
        market_price = session.get(MarketGoodState, order.good_name).current_price
        rate_per_hour = estimate_rate_per_hour(market_price, order.price_per_unit)
        units_sold = min(order.quantity_remaining, rate_per_hour * hours)
        if units_sold <= 0:
            continue

        revenue = units_sold * order.price_per_unit
        company.cash += revenue
        order.quantity_remaining -= units_sold
        record(session, company.id, game_minutes, categories.INCOME, categories.SALES,
               revenue, f"Venda no varejo de {units_sold:.1f} un. de {config.GOODS[order.good_name]['label']}")

        if order.quantity_remaining <= 0.001:
            order.quantity_remaining = 0.0
            order.status = "COMPLETED"

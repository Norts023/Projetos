from sqlalchemy.orm import Session

from app import config
from app.models import Company, MarketGoodState
from app.simulation import categories
from app.simulation.economy import apply_market_impact, get_inventory
from app.simulation.ledger import record


def buy_material(session: Session, company: Company, quantity: float, game_minutes: int) -> float:
    if quantity <= 0:
        raise ValueError("Quantidade deve ser positiva")
    market = session.get(MarketGoodState, config.RAW_MATERIAL)
    cost = quantity * market.current_price
    if company.cash < cost:
        raise ValueError(f"Caixa insuficiente (necessário {cost:.2f})")

    company.cash -= cost
    inv = get_inventory(session, company.id, config.RAW_MATERIAL)
    inv.quantity += quantity
    apply_market_impact(market, quantity, is_buy=True)
    record(session, company.id, game_minutes, categories.EXPENSE, categories.MATERIAL_PURCHASE,
           cost, f"Compra de {quantity} un. de matéria-prima")
    return cost


def sell_product(session: Session, company: Company, quantity: float, game_minutes: int) -> float:
    if quantity <= 0:
        raise ValueError("Quantidade deve ser positiva")
    inv = get_inventory(session, company.id, config.PRODUCT)
    if inv.quantity < quantity:
        raise ValueError(f"Estoque insuficiente (disponível {inv.quantity:.2f})")

    market = session.get(MarketGoodState, config.PRODUCT)
    revenue = quantity * market.current_price
    inv.quantity -= quantity
    company.cash += revenue
    apply_market_impact(market, quantity, is_buy=False)
    record(session, company.id, game_minutes, categories.INCOME, categories.SALES,
           revenue, f"Venda de {quantity} un. de produto")
    return revenue

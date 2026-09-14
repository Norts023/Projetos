from sqlalchemy.orm import Session

from app import config
from app.models import Company, MarketGoodState
from app.simulation import categories
from app.simulation.economy import apply_market_impact, get_inventory
from app.simulation.ledger import record


def buy_good(session: Session, company: Company, good_name: str, quantity: float, game_minutes: int) -> float:
    if good_name not in config.GOODS:
        raise ValueError(f"Bem '{good_name}' não existe")
    if quantity <= 0:
        raise ValueError("Quantidade deve ser positiva")

    market = session.get(MarketGoodState, good_name)
    cost = quantity * market.current_price
    if company.cash < cost:
        raise ValueError(f"Caixa insuficiente (necessário {cost:.2f})")

    company.cash -= cost
    inv = get_inventory(session, company.id, good_name)
    inv.quantity += quantity
    apply_market_impact(market, quantity, is_buy=True)
    record(session, company.id, game_minutes, categories.EXPENSE, categories.MATERIAL_PURCHASE,
           cost, f"Compra de {quantity} un. de {config.GOODS[good_name]['label']}")
    return cost


def sell_good(session: Session, company: Company, good_name: str, quantity: float, game_minutes: int) -> float:
    if good_name not in config.GOODS:
        raise ValueError(f"Bem '{good_name}' não existe")
    if quantity <= 0:
        raise ValueError("Quantidade deve ser positiva")

    inv = get_inventory(session, company.id, good_name)
    if inv.quantity < quantity:
        raise ValueError(f"Estoque insuficiente (disponível {inv.quantity:.2f})")

    market = session.get(MarketGoodState, good_name)
    gross_revenue = quantity * market.current_price
    fee = gross_revenue * config.MARKET_FEE_RATE
    net_revenue = gross_revenue - fee

    inv.quantity -= quantity
    company.cash += net_revenue
    apply_market_impact(market, quantity, is_buy=False)
    record(session, company.id, game_minutes, categories.INCOME, categories.SALES,
           gross_revenue, f"Venda de {quantity} un. de {config.GOODS[good_name]['label']}")
    record(session, company.id, game_minutes, categories.EXPENSE, categories.MARKET_FEE,
           fee, f"Taxa de mercado sobre venda de {config.GOODS[good_name]['label']}")
    return net_revenue

import random

from sqlalchemy.orm import Session

from app import config
from app.models import Competitor, MarketGoodState
from app.simulation.economy import add_flow


def process_tick(session: Session, minutes: int) -> None:
    """Lightweight AI: each competitor produces at a randomized rate, buys the
    raw material and sells the finished product straight into the market,
    nudging prices the same way a real player's trades would."""
    raw_market = session.get(MarketGoodState, config.RAW_MATERIAL)
    product_market = session.get(MarketGoodState, config.PRODUCT)

    for competitor in session.query(Competitor).all():
        variation = random.uniform(0.8, 1.2)
        produced = competitor.production_rate_per_hour * (minutes / 60.0) * variation
        if produced <= 0:
            continue

        material_needed = produced * config.BASE_FACTORY_MATERIAL_CONSUMPTION_RATIO
        material_cost = material_needed * raw_market.current_price
        add_flow(raw_market, material_needed, is_buy=True)

        revenue = produced * product_market.current_price
        add_flow(product_market, produced, is_buy=False)

        competitor.total_produced += produced
        competitor.total_revenue += revenue
        competitor.cash += revenue - material_cost


def apply_daily_growth(session: Session, days_elapsed: int) -> None:
    if days_elapsed <= 0:
        return
    for competitor in session.query(Competitor).all():
        for _ in range(days_elapsed):
            growth = random.uniform(config.COMPETITOR_DAILY_GROWTH_MIN, config.COMPETITOR_DAILY_GROWTH_MAX)
            competitor.production_rate_per_hour *= growth

import random

from sqlalchemy.orm import Session

from app import config
from app.models import Competitor, MarketGoodState
from app.simulation.economy import add_flow


def process_tick(session: Session, minutes: int) -> None:
    """Lightweight AI: each competitor runs its own recipe at a randomized rate,
    buying that recipe's inputs and selling its output straight into the
    market, nudging prices the same way a real player's trades would."""
    for competitor in session.query(Competitor).all():
        recipe = config.RECIPES[competitor.recipe_id]
        variation = random.uniform(0.8, 1.2)
        produced = competitor.production_rate_per_hour * (minutes / 60.0) * variation
        if produced <= 0:
            continue

        input_cost = 0.0
        for input_good, ratio in recipe["inputs"].items():
            quantity_needed = produced * ratio
            input_market = session.get(MarketGoodState, input_good)
            input_cost += quantity_needed * input_market.current_price
            add_flow(input_market, quantity_needed, is_buy=True)

        output_market = session.get(MarketGoodState, recipe["output_good"])
        revenue = produced * output_market.current_price
        add_flow(output_market, produced, is_buy=False)

        competitor.total_produced += produced
        competitor.total_revenue += revenue
        competitor.cash += revenue - input_cost


def apply_daily_growth(session: Session, days_elapsed: int) -> None:
    if days_elapsed <= 0:
        return
    for competitor in session.query(Competitor).all():
        for _ in range(days_elapsed):
            growth = random.uniform(config.COMPETITOR_DAILY_GROWTH_MIN, config.COMPETITOR_DAILY_GROWTH_MAX)
            competitor.production_rate_per_hour *= growth

from sqlalchemy.orm import Session

from app import config
from app.models import Bank, Company, Competitor, GameClock, InventoryItem, LandPlot, MarketGoodState


def seed_if_empty(session: Session) -> None:
    if session.get(GameClock, 1) is None:
        session.add(GameClock(id=1, game_minutes=0, running=True, speed_multiplier=1.0))

    if session.get(Company, 1) is None:
        company = Company(id=1, name="Minha Empresa", cash=config.STARTING_CASH,
                           credit_score=config.STARTING_CREDIT_SCORE)
        session.add(company)
        session.flush()
        for good_name in config.MARKET_GOODS:
            session.add(InventoryItem(company_id=company.id, good_name=good_name, quantity=0.0))

    for good_name, data in config.MARKET_GOODS.items():
        if session.get(MarketGoodState, good_name) is None:
            session.add(MarketGoodState(name=good_name, current_price=data["base_price"]))

    if session.query(Bank).count() == 0:
        for bank_data in config.BANKS:
            session.add(Bank(**bank_data))

    if session.query(LandPlot).count() == 0:
        for plot_data in config.LAND_PLOTS:
            session.add(LandPlot(**plot_data))

    if session.query(Competitor).count() == 0:
        for comp_data in config.COMPETITORS:
            session.add(Competitor(cash=config.COMPETITOR_STARTING_CASH, **comp_data))

    session.commit()

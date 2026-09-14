import csv
import io

from sqlalchemy import select
from sqlalchemy.orm import Session

from app import config
from app.models import Company, LedgerEntry, MarketGoodState
from app.simulation import categories


def sum_category(session: Session, company_id: int, category: str, from_day: int = 0, to_day: int = 10**9) -> float:
    rows = session.scalars(
        select(LedgerEntry.amount).where(
            LedgerEntry.company_id == company_id,
            LedgerEntry.category == category,
            LedgerEntry.day >= from_day,
            LedgerEntry.day <= to_day,
        )
    ).all()
    return round(sum(rows), 2)


def income_statement(session: Session, company: Company, from_day: int, to_day: int) -> dict:
    revenue = sum_category(session, company.id, categories.SALES, from_day, to_day)
    cogs = sum_category(session, company.id, categories.MATERIAL_PURCHASE, from_day, to_day)
    upkeep = sum_category(session, company.id, categories.UPKEEP, from_day, to_day)
    interest = sum_category(session, company.id, categories.LOAN_INTEREST, from_day, to_day)
    market_fees = sum_category(session, company.id, categories.MARKET_FEE, from_day, to_day)
    goal_rewards = sum_category(session, company.id, categories.GOAL_REWARD, from_day, to_day)
    gross_profit = revenue - cogs
    operating_profit = gross_profit - upkeep - market_fees
    net_profit = operating_profit - interest + goal_rewards
    return {
        "from_day": from_day, "to_day": to_day,
        "revenue": revenue, "cogs": cogs, "gross_profit": round(gross_profit, 2),
        "opex_upkeep": upkeep, "market_fees": market_fees, "operating_profit": round(operating_profit, 2),
        "interest_expense": interest, "goal_rewards": goal_rewards, "net_profit": round(net_profit, 2),
    }


def balance_sheet(session: Session, company: Company) -> dict:
    prices = {m.name: m.current_price for m in session.query(MarketGoodState).all()}

    inventory_value = sum(item.quantity * prices[item.good_name] for item in company.inventory)
    land_value = sum(p.price + p.terraforming_cost for p in company.land_plots)
    factory_value = sum(config.RECIPES[f.recipe_id]["build_cost"] for f in company.factories)
    total_assets = company.cash + inventory_value + land_value + factory_value

    loans_balance = sum(loan.remaining_balance for loan in company.loans if loan.status == "ACTIVE")
    equity = total_assets - loans_balance

    return {
        "cash": round(company.cash, 2),
        "inventory_value": round(inventory_value, 2),
        "land_value": round(land_value, 2),
        "factory_value": round(factory_value, 2),
        "total_assets": round(total_assets, 2),
        "total_liabilities": round(loans_balance, 2),
        "equity": round(equity, 2),
    }


def cash_flow_statement(session: Session, company: Company, from_day: int, to_day: int) -> dict:
    revenue = sum_category(session, company.id, categories.SALES, from_day, to_day)
    cogs = sum_category(session, company.id, categories.MATERIAL_PURCHASE, from_day, to_day)
    upkeep = sum_category(session, company.id, categories.UPKEEP, from_day, to_day)
    market_fees = sum_category(session, company.id, categories.MARKET_FEE, from_day, to_day)
    goal_rewards = sum_category(session, company.id, categories.GOAL_REWARD, from_day, to_day)
    operating = round(revenue - cogs - upkeep - market_fees + goal_rewards, 2)

    loan_proceeds = sum_category(session, company.id, categories.LOAN_PROCEEDS, from_day, to_day)
    loan_principal = sum_category(session, company.id, categories.LOAN_PRINCIPAL, from_day, to_day)
    loan_interest = sum_category(session, company.id, categories.LOAN_INTEREST, from_day, to_day)
    loan_payoff = sum_category(session, company.id, categories.LOAN_PAYOFF, from_day, to_day)
    financing = round(loan_proceeds - loan_principal - loan_interest - loan_payoff, 2)

    land_purchase = sum_category(session, company.id, categories.LAND_PURCHASE, from_day, to_day)
    factory_build = sum_category(session, company.id, categories.FACTORY_BUILD, from_day, to_day)
    investing = round(-(land_purchase + factory_build), 2)

    return {
        "from_day": from_day, "to_day": to_day,
        "operating_activities": operating,
        "financing_activities": financing,
        "investing_activities": investing,
        "net_cash_flow": round(operating + financing + investing, 2),
    }


def recent_ledger(session: Session, company_id: int, query: str = "", limit: int = 50) -> list[dict]:
    stmt = select(LedgerEntry).where(LedgerEntry.company_id == company_id)
    if query:
        stmt = stmt.where(LedgerEntry.description.ilike(f"%{query}%"))
    rows = session.scalars(stmt.order_by(LedgerEntry.id.desc()).limit(limit)).all()
    return [
        {
            "day": r.day, "game_minutes": r.game_minutes, "entry_type": r.entry_type,
            "category": r.category, "amount": r.amount, "description": r.description,
        }
        for r in rows
    ]


def export_ledger_csv(session: Session, company: Company) -> str:
    rows = session.scalars(
        select(LedgerEntry).where(LedgerEntry.company_id == company.id).order_by(LedgerEntry.id)
    ).all()
    buffer = io.StringIO()
    writer = csv.writer(buffer)
    writer.writerow(["id", "day", "game_minutes", "entry_type", "category", "amount", "description"])
    for row in rows:
        writer.writerow([row.id, row.day, row.game_minutes, row.entry_type, row.category,
                          row.amount, row.description])
    return buffer.getvalue()

from datetime import datetime

from sqlalchemy import Boolean, DateTime, Float, ForeignKey, Integer, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base


class GameClock(Base):
    __tablename__ = "game_clock"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, default=1)
    game_minutes: Mapped[int] = mapped_column(Integer, default=0)
    running: Mapped[bool] = mapped_column(Boolean, default=True)
    speed_multiplier: Mapped[float] = mapped_column(Float, default=1.0)


class Company(Base):
    __tablename__ = "company"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, default=1)
    name: Mapped[str] = mapped_column(String, default="Minha Empresa")
    cash: Mapped[float] = mapped_column(Float, default=0.0)
    credit_score: Mapped[int] = mapped_column(Integer, default=600)
    company_level: Mapped[int] = mapped_column(Integer, default=100)
    total_produced: Mapped[float] = mapped_column(Float, default=0.0)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

    inventory: Mapped[list["InventoryItem"]] = relationship(back_populates="company")
    factories: Mapped[list["Factory"]] = relationship(back_populates="company")
    loans: Mapped[list["Loan"]] = relationship(back_populates="company")
    land_plots: Mapped[list["LandPlot"]] = relationship(back_populates="owner")
    retail_orders: Mapped[list["RetailOrder"]] = relationship(back_populates="company")
    ledger_entries: Mapped[list["LedgerEntry"]] = relationship(back_populates="company")


class InventoryItem(Base):
    __tablename__ = "inventory_item"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    company_id: Mapped[int] = mapped_column(ForeignKey("company.id"))
    good_name: Mapped[str] = mapped_column(String)
    quantity: Mapped[float] = mapped_column(Float, default=0.0)

    company: Mapped["Company"] = relationship(back_populates="inventory")


class MarketGoodState(Base):
    __tablename__ = "market_good_state"

    name: Mapped[str] = mapped_column(String, primary_key=True)
    current_price: Mapped[float] = mapped_column(Float)
    recent_supply: Mapped[float] = mapped_column(Float, default=0.0)
    recent_demand: Mapped[float] = mapped_column(Float, default=0.0)


class LandPlot(Base):
    __tablename__ = "land_plot"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    name: Mapped[str] = mapped_column(String)
    region: Mapped[str] = mapped_column(String)
    price: Mapped[float] = mapped_column(Float)
    terraforming_cost: Mapped[float] = mapped_column(Float)
    logistics_bonus: Mapped[float] = mapped_column(Float)
    capacity: Mapped[int] = mapped_column(Integer)
    owner_id: Mapped[int | None] = mapped_column(ForeignKey("company.id"), nullable=True)
    purchased_at_minutes: Mapped[int | None] = mapped_column(Integer, nullable=True)

    owner: Mapped["Company | None"] = relationship(back_populates="land_plots")
    factories: Mapped[list["Factory"]] = relationship(back_populates="land_plot")


class Factory(Base):
    __tablename__ = "factory"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    company_id: Mapped[int] = mapped_column(ForeignKey("company.id"))
    land_plot_id: Mapped[int] = mapped_column(ForeignKey("land_plot.id"))
    recipe_id: Mapped[str] = mapped_column(String)
    level: Mapped[int] = mapped_column(Integer, default=1)
    built_at_minutes: Mapped[int] = mapped_column(Integer, default=0)
    status: Mapped[str] = mapped_column(String, default="ACTIVE")  # ACTIVE, BUILDING, UPGRADING
    construction_started_minutes: Mapped[int | None] = mapped_column(Integer, nullable=True)
    busy_until_minutes: Mapped[int | None] = mapped_column(Integer, nullable=True)
    construction_cost_basis: Mapped[float | None] = mapped_column(Float, nullable=True)

    company: Mapped["Company"] = relationship(back_populates="factories")
    land_plot: Mapped["LandPlot"] = relationship(back_populates="factories")


class Bank(Base):
    __tablename__ = "bank"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    name: Mapped[str] = mapped_column(String)
    base_annual_rate: Mapped[float] = mapped_column(Float)
    min_credit_score: Mapped[int] = mapped_column(Integer)
    max_loan_to_cash_ratio: Mapped[float] = mapped_column(Float)
    max_term_months: Mapped[int] = mapped_column(Integer)

    loans: Mapped[list["Loan"]] = relationship(back_populates="bank")


class Loan(Base):
    __tablename__ = "loan"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    company_id: Mapped[int] = mapped_column(ForeignKey("company.id"))
    bank_id: Mapped[int] = mapped_column(ForeignKey("bank.id"))
    principal: Mapped[float] = mapped_column(Float)
    annual_rate: Mapped[float] = mapped_column(Float)
    term_months: Mapped[int] = mapped_column(Integer)
    payment_type: Mapped[str] = mapped_column(String)  # "PRICE" or "SAC"
    monthly_payment: Mapped[float] = mapped_column(Float)  # only meaningful for PRICE; SAC varies
    remaining_balance: Mapped[float] = mapped_column(Float)
    installments_paid: Mapped[int] = mapped_column(Integer, default=0)
    start_day: Mapped[int] = mapped_column(Integer)
    next_payment_day: Mapped[int] = mapped_column(Integer)
    status: Mapped[str] = mapped_column(String, default="ACTIVE")  # ACTIVE, PAID, DEFAULTED

    company: Mapped["Company"] = relationship(back_populates="loans")
    bank: Mapped["Bank"] = relationship(back_populates="loans")


class Competitor(Base):
    __tablename__ = "competitor"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    name: Mapped[str] = mapped_column(String)
    recipe_id: Mapped[str] = mapped_column(String)
    production_rate_per_hour: Mapped[float] = mapped_column(Float)
    cash: Mapped[float] = mapped_column(Float, default=0.0)
    total_produced: Mapped[float] = mapped_column(Float, default=0.0)
    total_revenue: Mapped[float] = mapped_column(Float, default=0.0)


class RetailOrder(Base):
    __tablename__ = "retail_order"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    company_id: Mapped[int] = mapped_column(ForeignKey("company.id"))
    good_name: Mapped[str] = mapped_column(String)
    price_per_unit: Mapped[float] = mapped_column(Float)
    quantity_remaining: Mapped[float] = mapped_column(Float)
    quantity_original: Mapped[float] = mapped_column(Float)
    status: Mapped[str] = mapped_column(String, default="ACTIVE")  # ACTIVE, COMPLETED, CANCELLED
    created_day: Mapped[int] = mapped_column(Integer)

    company: Mapped["Company"] = relationship(back_populates="retail_orders")


class LedgerEntry(Base):
    __tablename__ = "ledger_entry"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    company_id: Mapped[int] = mapped_column(ForeignKey("company.id"))
    day: Mapped[int] = mapped_column(Integer)
    game_minutes: Mapped[int] = mapped_column(Integer)
    entry_type: Mapped[str] = mapped_column(String)  # INCOME or EXPENSE
    category: Mapped[str] = mapped_column(String)
    amount: Mapped[float] = mapped_column(Float)
    description: Mapped[str] = mapped_column(String, default="")

    company: Mapped["Company"] = relationship(back_populates="ledger_entries")


class AchievedGoal(Base):
    __tablename__ = "achieved_goal"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    goal_id: Mapped[str] = mapped_column(String, unique=True)
    achieved_day: Mapped[int] = mapped_column(Integer)

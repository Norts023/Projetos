"""Central place for game balance constants."""

from pathlib import Path

DATA_DIR = Path(__file__).resolve().parent.parent / "data"
DATABASE_URL = f"sqlite:///{DATA_DIR / 'game.db'}"

# --- Time ---
MINUTES_PER_DAY = 24 * 60
TICK_INTERVAL_SECONDS = 1.0
GAME_MINUTES_PER_TICK = 10  # 1 game day passes every 144 real seconds at 1x speed
MAX_SPEED_MULTIPLIER = 8.0

# --- Starting conditions ---
STARTING_CASH = 50_000.0
STARTING_CREDIT_SCORE = 600  # 300-850 scale

# --- Production ---
RAW_MATERIAL = "materia_prima"
PRODUCT = "produto"
BASE_FACTORY_PRODUCTION_PER_HOUR = 5.0  # units of product per hour, per factory level
BASE_FACTORY_MATERIAL_CONSUMPTION_RATIO = 2.0  # units of raw material per unit of product
FACTORY_BUILD_COST = 20_000.0
FACTORY_UPKEEP_PER_DAY = 50.0

# --- Market / dynamic pricing ---
MARKET_GOODS = {
    RAW_MATERIAL: {"base_price": 10.0, "min_price": 1.0},
    PRODUCT: {"base_price": 45.0, "min_price": 5.0},
}
PRICE_ELASTICITY = 0.02  # how strongly price reacts to supply/demand imbalance per tick
PRICE_REVERSION_PER_HOUR = 0.05  # price drifts back toward base_price at this rate when imbalance eases
BASELINE_DEMAND_PER_HOUR = {
    RAW_MATERIAL: 0.0,  # only companies consume it
    PRODUCT: 8.0,  # simulated market demand for finished goods
}
BASELINE_SUPPLY_PER_HOUR = {
    RAW_MATERIAL: 25.0,  # external miners/suppliers keep feeding the market
    PRODUCT: 0.0,  # only companies supply it
}

# --- Banks / credit ---
CREDIT_SCORE_MIN = 300
CREDIT_SCORE_MAX = 850
CREDIT_SCORE_ON_TIME_PAYMENT = 4
CREDIT_SCORE_LATE_PAYMENT_PENALTY = 30
CREDIT_SCORE_MISSED_PAYMENT_PENALTY = 60
EARLY_PAYOFF_DISCOUNT = 0.05  # 5% discount on remaining balance if paid off early
LOAN_MISSED_RETRY_DAYS = 5

BANKS = [
    {
        "name": "Banco Popular",
        "base_annual_rate": 0.18,
        "min_credit_score": 300,
        "max_loan_to_cash_ratio": 3.0,
        "max_term_months": 24,
    },
    {
        "name": "Banco Nacional",
        "base_annual_rate": 0.12,
        "min_credit_score": 550,
        "max_loan_to_cash_ratio": 5.0,
        "max_term_months": 48,
    },
    {
        "name": "Banco Premium",
        "base_annual_rate": 0.08,
        "min_credit_score": 720,
        "max_loan_to_cash_ratio": 10.0,
        "max_term_months": 72,
    },
]

# --- Simulated competitors (lightweight AI) ---
COMPETITOR_STARTING_CASH = 40_000.0
COMPETITOR_DAILY_GROWTH_MIN = 1.000  # production_rate multiplier per day, applied on day rollover
COMPETITOR_DAILY_GROWTH_MAX = 1.010
COMPETITORS = [
    {"name": "Industrias Aurora", "production_rate_per_hour": 4.0},
    {"name": "Grupo Vantage", "production_rate_per_hour": 6.0},
    {"name": "Cooperativa Sertao", "production_rate_per_hour": 3.0},
]

# --- Land ---
LAND_PLOTS = [
    {"name": "Distrito Industrial Norte", "region": "Norte", "price": 30_000.0,
     "terraforming_cost": 2_000.0, "logistics_bonus": 0.05, "capacity": 2},
    {"name": "Zona Portuaria Sul", "region": "Sul", "price": 60_000.0,
     "terraforming_cost": 5_000.0, "logistics_bonus": 0.12, "capacity": 3},
    {"name": "Interior Central", "region": "Central", "price": 12_000.0,
     "terraforming_cost": 500.0, "logistics_bonus": 0.0, "capacity": 1},
    {"name": "Polo Logistico Leste", "region": "Leste", "price": 45_000.0,
     "terraforming_cost": 3_000.0, "logistics_bonus": 0.08, "capacity": 2},
    {"name": "Reserva Oeste (barata, distante)", "region": "Oeste", "price": 6_000.0,
     "terraforming_cost": 1_500.0, "logistics_bonus": -0.05, "capacity": 1},
]

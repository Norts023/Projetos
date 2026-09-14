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

# --- Goods catalog (multi-tier production chains, Sim Companies-style) ---
# Tier 0 = raw material (bought from the market, no recipe).
# Tier 1-3 = processed goods; each has a recipe (see RECIPES below) but can
# also be bought/sold on the market directly ("make vs. buy").
GOODS = {
    "minerio_ferro":     {"label": "Minério de Ferro", "tier": 0, "base_price": 8.0,   "min_price": 1.0},
    "petroleo":          {"label": "Petróleo",          "tier": 0, "base_price": 12.0,  "min_price": 1.0},
    "algodao":           {"label": "Algodão",           "tier": 0, "base_price": 6.0,   "min_price": 1.0},
    "aco":               {"label": "Aço",               "tier": 1, "base_price": 25.0,  "min_price": 3.0},
    "plastico":          {"label": "Plástico",          "tier": 1, "base_price": 30.0,  "min_price": 3.0},
    "tecido":            {"label": "Tecido",            "tier": 1, "base_price": 18.0,  "min_price": 2.0},
    "pecas_mecanicas":   {"label": "Peças Mecânicas",   "tier": 2, "base_price": 90.0,  "min_price": 10.0},
    "roupas":            {"label": "Roupas",            "tier": 2, "base_price": 70.0,  "min_price": 8.0},
    "eletrodomesticos":  {"label": "Eletrodomésticos",  "tier": 3, "base_price": 220.0, "min_price": 25.0},
}

# Each recipe is keyed by the good it produces (one recipe per good, one
# factory type per recipe). `inputs` maps input good -> units consumed per
# unit of output produced.
RECIPES = {
    "aco": {
        "label": "Siderúrgica (Aço)", "output_good": "aco", "tier": 1,
        "inputs": {"minerio_ferro": 2.0}, "output_rate_per_hour": 5.0, "build_cost": 15_000.0,
    },
    "plastico": {
        "label": "Petroquímica (Plástico)", "output_good": "plastico", "tier": 1,
        "inputs": {"petroleo": 2.0}, "output_rate_per_hour": 5.0, "build_cost": 15_000.0,
    },
    "tecido": {
        "label": "Têxtil (Tecido)", "output_good": "tecido", "tier": 1,
        "inputs": {"algodao": 2.0}, "output_rate_per_hour": 6.0, "build_cost": 12_000.0,
    },
    "pecas_mecanicas": {
        "label": "Metalúrgica (Peças Mecânicas)", "output_good": "pecas_mecanicas", "tier": 2,
        "inputs": {"aco": 2.0, "plastico": 1.0}, "output_rate_per_hour": 3.0, "build_cost": 35_000.0,
    },
    "roupas": {
        "label": "Confecção (Roupas)", "output_good": "roupas", "tier": 2,
        "inputs": {"tecido": 2.0, "plastico": 0.5}, "output_rate_per_hour": 4.0, "build_cost": 28_000.0,
    },
    "eletrodomesticos": {
        "label": "Fábrica de Eletrodomésticos", "output_good": "eletrodomesticos", "tier": 3,
        "inputs": {"pecas_mecanicas": 2.0, "plastico": 1.0}, "output_rate_per_hour": 2.0, "build_cost": 60_000.0,
    },
}

FACTORY_UPKEEP_PER_DAY = 50.0  # per factory, regardless of recipe

# --- Market / dynamic pricing ---
PRICE_ELASTICITY = 0.02  # how strongly price reacts to supply/demand imbalance per tick
PRICE_REVERSION_PER_HOUR = 0.05  # price drifts back toward base_price at this rate when imbalance eases

# Baseline supply/demand per hour, by tier: raw materials are mostly
# supplied externally (miners/farmers) and consumed only by factories;
# finished consumer goods are mostly demanded externally (shoppers) and
# barely supplied by anyone but the player/competitors.
TIER_BASELINE_SUPPLY_PER_HOUR = {0: 25.0, 1: 10.0, 2: 3.0, 3: 1.0}
TIER_BASELINE_DEMAND_PER_HOUR = {0: 0.0, 1: 5.0, 2: 6.0, 3: 10.0}
BASELINE_SUPPLY_PER_HOUR = {name: TIER_BASELINE_SUPPLY_PER_HOUR[g["tier"]] for name, g in GOODS.items()}
BASELINE_DEMAND_PER_HOUR = {name: TIER_BASELINE_DEMAND_PER_HOUR[g["tier"]] for name, g in GOODS.items()}

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
    {"name": "Industrias Aurora", "recipe_id": "aco", "production_rate_per_hour": 6.0},
    {"name": "Grupo Vantage", "recipe_id": "roupas", "production_rate_per_hour": 5.0},
    {"name": "Cooperativa Sertao", "recipe_id": "eletrodomesticos", "production_rate_per_hour": 2.0},
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

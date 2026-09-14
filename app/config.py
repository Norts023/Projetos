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

    # Agropecuária / Alimentos
    "vacas":              {"label": "Vacas",              "tier": 0, "base_price": 20.0, "min_price": 3.0},
    "cereal":             {"label": "Cereal",              "tier": 0, "base_price": 5.0,  "min_price": 1.0},
    "leite":              {"label": "Leite",               "tier": 1, "base_price": 12.0, "min_price": 2.0},
    "bife":               {"label": "Bife",                "tier": 1, "base_price": 30.0, "min_price": 4.0},
    "farinha":            {"label": "Farinha",             "tier": 1, "base_price": 10.0, "min_price": 1.0},
    "queijo":             {"label": "Queijo",              "tier": 2, "base_price": 40.0, "min_price": 5.0},
    "pao":                {"label": "Pão",                 "tier": 2, "base_price": 15.0, "min_price": 2.0},
    "hamburguer":         {"label": "Hamburguer",          "tier": 3, "base_price": 60.0, "min_price": 8.0},

    # Construção
    "argila":             {"label": "Argila",              "tier": 0, "base_price": 4.0,  "min_price": 1.0},
    "calcario":           {"label": "Calcário",            "tier": 0, "base_price": 5.0,  "min_price": 1.0},
    "madeira":            {"label": "Madeira",             "tier": 0, "base_price": 6.0,  "min_price": 1.0},
    "tijolos":            {"label": "Tijolos",             "tier": 1, "base_price": 12.0, "min_price": 2.0},
    "cimento":            {"label": "Cimento",             "tier": 1, "base_price": 14.0, "min_price": 2.0},
    "tabuas":             {"label": "Tábuas",              "tier": 1, "base_price": 10.0, "min_price": 1.0},
    "vigas_aco":          {"label": "Vigas de Aço",        "tier": 1, "base_price": 55.0, "min_price": 6.0},
    "concreto_armado":    {"label": "Concreto Armado",     "tier": 2, "base_price": 120.0, "min_price": 15.0},

    # Eletrônicos
    "silicio":            {"label": "Silício",             "tier": 0, "base_price": 15.0, "min_price": 2.0},
    "minerais":           {"label": "Minerais",            "tier": 0, "base_price": 10.0, "min_price": 1.0},
    "processadores":      {"label": "Processadores",       "tier": 1, "base_price": 60.0, "min_price": 8.0},
    "baterias":           {"label": "Baterias",            "tier": 1, "base_price": 35.0, "min_price": 4.0},
    "telas":              {"label": "Telas",               "tier": 1, "base_price": 45.0, "min_price": 5.0},
    "componentes_eletronicos": {"label": "Componentes Eletrônicos", "tier": 1, "base_price": 40.0, "min_price": 5.0},
    "smartphones":        {"label": "Smartphones",         "tier": 2, "base_price": 350.0, "min_price": 40.0},
    "robos":              {"label": "Robôs",                "tier": 3, "base_price": 900.0, "min_price": 100.0},

    # Automóvel
    "carroceria":         {"label": "Carroceria",          "tier": 1, "base_price": 150.0, "min_price": 20.0},
    "motor_combustao":    {"label": "Motor de Combustão",  "tier": 1, "base_price": 180.0, "min_price": 25.0},
    "interior_basico":    {"label": "Interior Básico",     "tier": 1, "base_price": 90.0,  "min_price": 12.0},
    "motor_eletrico":     {"label": "Motor Elétrico",      "tier": 2, "base_price": 260.0, "min_price": 35.0},
    "computador_bordo":   {"label": "Computador de Bordo", "tier": 2, "base_price": 300.0, "min_price": 40.0},
    "carro_economico":    {"label": "Carro Econômico",     "tier": 3, "base_price": 1_200.0, "min_price": 150.0},
    "carro_eletrico_economico": {"label": "Carro Elétrico Econômico", "tier": 3, "base_price": 1_600.0, "min_price": 200.0},
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

    # Agropecuária / Alimentos
    "leite": {
        "label": "Fazenda Leiteira (Leite)", "output_good": "leite", "tier": 1,
        "inputs": {"vacas": 0.2}, "output_rate_per_hour": 8.0, "build_cost": 14_000.0,
    },
    "bife": {
        "label": "Frigorífico (Bife)", "output_good": "bife", "tier": 1,
        "inputs": {"vacas": 0.3}, "output_rate_per_hour": 4.0, "build_cost": 16_000.0,
    },
    "farinha": {
        "label": "Moinho (Farinha)", "output_good": "farinha", "tier": 1,
        "inputs": {"cereal": 1.5}, "output_rate_per_hour": 7.0, "build_cost": 11_000.0,
    },
    "queijo": {
        "label": "Laticínios (Queijo)", "output_good": "queijo", "tier": 2,
        "inputs": {"leite": 3.0}, "output_rate_per_hour": 3.0, "build_cost": 22_000.0,
    },
    "pao": {
        "label": "Padaria (Pão)", "output_good": "pao", "tier": 2,
        "inputs": {"farinha": 1.5}, "output_rate_per_hour": 5.0, "build_cost": 13_000.0,
    },
    "hamburguer": {
        "label": "Lanchonete (Hamburguer)", "output_good": "hamburguer", "tier": 3,
        "inputs": {"pao": 1.0, "queijo": 1.0, "bife": 1.0}, "output_rate_per_hour": 3.0, "build_cost": 25_000.0,
    },

    # Construção
    "tijolos": {
        "label": "Olaria (Tijolos)", "output_good": "tijolos", "tier": 1,
        "inputs": {"argila": 2.0}, "output_rate_per_hour": 6.0, "build_cost": 11_000.0,
    },
    "cimento": {
        "label": "Fábrica de Cimento", "output_good": "cimento", "tier": 1,
        "inputs": {"calcario": 2.0}, "output_rate_per_hour": 6.0, "build_cost": 13_000.0,
    },
    "tabuas": {
        "label": "Serraria (Tábuas)", "output_good": "tabuas", "tier": 1,
        "inputs": {"madeira": 1.5}, "output_rate_per_hour": 6.0, "build_cost": 10_000.0,
    },
    "vigas_aco": {
        "label": "Metalúrgica (Vigas de Aço)", "output_good": "vigas_aco", "tier": 1,
        "inputs": {"aco": 1.5}, "output_rate_per_hour": 4.0, "build_cost": 20_000.0,
    },
    "concreto_armado": {
        "label": "Fábrica de Concreto Armado", "output_good": "concreto_armado", "tier": 2,
        "inputs": {"cimento": 3.0, "vigas_aco": 1.0}, "output_rate_per_hour": 3.0, "build_cost": 32_000.0,
    },

    # Eletrônicos
    "processadores": {
        "label": "Fábrica de Processadores", "output_good": "processadores", "tier": 1,
        "inputs": {"silicio": 2.0}, "output_rate_per_hour": 4.0, "build_cost": 25_000.0,
    },
    "baterias": {
        "label": "Fábrica de Baterias", "output_good": "baterias", "tier": 1,
        "inputs": {"minerais": 2.0}, "output_rate_per_hour": 5.0, "build_cost": 18_000.0,
    },
    "telas": {
        "label": "Fábrica de Telas", "output_good": "telas", "tier": 1,
        "inputs": {"silicio": 1.0, "plastico": 1.0}, "output_rate_per_hour": 4.0, "build_cost": 22_000.0,
    },
    "componentes_eletronicos": {
        "label": "Fábrica de Componentes Eletrônicos", "output_good": "componentes_eletronicos", "tier": 1,
        "inputs": {"plastico": 1.0, "minerais": 1.0}, "output_rate_per_hour": 5.0, "build_cost": 20_000.0,
    },
    "smartphones": {
        "label": "Fábrica de Smartphones", "output_good": "smartphones", "tier": 2,
        "inputs": {"processadores": 1.0, "baterias": 1.0, "telas": 1.0},
        "output_rate_per_hour": 2.0, "build_cost": 50_000.0,
    },
    "robos": {
        "label": "Fábrica de Robôs", "output_good": "robos", "tier": 3,
        "inputs": {"processadores": 2.0, "componentes_eletronicos": 2.0, "baterias": 2.0},
        "output_rate_per_hour": 1.0, "build_cost": 80_000.0,
    },

    # Automóvel
    "carroceria": {
        "label": "Estamparia (Carroceria)", "output_good": "carroceria", "tier": 1,
        "inputs": {"aco": 3.0, "plastico": 1.0}, "output_rate_per_hour": 2.0, "build_cost": 30_000.0,
    },
    "motor_combustao": {
        "label": "Fábrica de Motores a Combustão", "output_good": "motor_combustao", "tier": 1,
        "inputs": {"aco": 2.0, "petroleo": 2.0}, "output_rate_per_hour": 2.0, "build_cost": 32_000.0,
    },
    "interior_basico": {
        "label": "Fábrica de Interiores", "output_good": "interior_basico", "tier": 1,
        "inputs": {"tecido": 2.0, "plastico": 1.0}, "output_rate_per_hour": 3.0, "build_cost": 18_000.0,
    },
    "motor_eletrico": {
        "label": "Fábrica de Motores Elétricos", "output_good": "motor_eletrico", "tier": 2,
        "inputs": {"baterias": 2.0, "aco": 1.0}, "output_rate_per_hour": 2.0, "build_cost": 38_000.0,
    },
    "computador_bordo": {
        "label": "Fábrica de Computadores de Bordo", "output_good": "computador_bordo", "tier": 2,
        "inputs": {"processadores": 1.0, "componentes_eletronicos": 1.0},
        "output_rate_per_hour": 2.0, "build_cost": 36_000.0,
    },
    "carro_economico": {
        "label": "Montadora (Carro Econômico)", "output_good": "carro_economico", "tier": 3,
        "inputs": {"carroceria": 1.0, "motor_combustao": 1.0, "interior_basico": 1.0},
        "output_rate_per_hour": 1.0, "build_cost": 90_000.0,
    },
    "carro_eletrico_economico": {
        "label": "Montadora (Carro Elétrico)", "output_good": "carro_eletrico_economico", "tier": 3,
        "inputs": {"carroceria": 1.0, "motor_eletrico": 1.0, "interior_basico": 1.0, "computador_bordo": 1.0},
        "output_rate_per_hour": 1.0, "build_cost": 110_000.0,
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
    {"name": "Fazenda Sol Nascente", "recipe_id": "hamburguer", "production_rate_per_hour": 4.0},
    {"name": "AutoMotors Brasil", "recipe_id": "carro_economico", "production_rate_per_hour": 1.0},
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

# --- Goals (optional progress milestones, Sim Companies "missions"-style) ---
# The actual pass/fail check for each id lives in app.simulation.goals.CHECKS.
GOALS = [
    {"id": "primeira_fabrica", "label": "Primeira Fábrica",
     "description": "Construa sua primeira fábrica.", "reward_cash": 2_000.0},
    {"id": "expansao_territorial", "label": "Expansão Territorial",
     "description": "Possua 3 terrenos.", "reward_cash": 5_000.0},
    {"id": "diversificacao_industrial", "label": "Diversificação Industrial",
     "description": "Tenha fábricas de pelo menos 3 receitas diferentes.", "reward_cash": 8_000.0},
    {"id": "producao_em_massa", "label": "Produção em Massa",
     "description": "Produza 1.000 unidades acumuladas (qualquer bem).", "reward_cash": 5_000.0},
    {"id": "vendedor_nato", "label": "Vendedor Nato",
     "description": "Acumule R$ 100.000 em receita de vendas.", "reward_cash": 4_000.0},
    {"id": "sem_dividas", "label": "Sem Dívidas",
     "description": "Contraia um empréstimo e quite-o totalmente.", "reward_cash": 3_000.0},
    {"id": "credito_impecavel", "label": "Crédito Impecável",
     "description": "Alcance um score de crédito de 800.", "reward_cash": 3_000.0},
    {"id": "lider_de_mercado", "label": "Líder de Mercado",
     "description": "Ultrapasse todos os concorrentes em valorização.", "reward_cash": 10_000.0},
    {"id": "primeiro_milhao", "label": "Primeiro Milhão",
     "description": "Atinja R$ 1.000.000 de patrimônio líquido.", "reward_cash": 20_000.0},
    {"id": "magnata", "label": "Magnata",
     "description": "Atinja R$ 5.000.000 de patrimônio líquido.", "reward_cash": 50_000.0},
]

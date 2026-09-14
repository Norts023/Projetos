from sqlalchemy.orm import Session

from app import config
from app.models import AchievedGoal, Company, Competitor, Loan
from app.simulation import categories, finance
from app.simulation.ledger import record


def _check_primeira_fabrica(session: Session, company: Company) -> bool:
    return len(company.factories) >= 1


def _check_expansao_territorial(session: Session, company: Company) -> bool:
    return len(company.land_plots) >= 3


def _check_diversificacao_industrial(session: Session, company: Company) -> bool:
    return len({f.recipe_id for f in company.factories}) >= 3


def _check_producao_em_massa(session: Session, company: Company) -> bool:
    return company.total_produced >= 1_000


def _check_vendedor_nato(session: Session, company: Company) -> bool:
    return finance.sum_category(session, company.id, categories.SALES) >= 100_000


def _check_sem_dividas(session: Session, company: Company) -> bool:
    has_taken_loan = session.query(Loan).filter_by(company_id=company.id).count() > 0
    no_active_loans = not any(loan.status == "ACTIVE" for loan in company.loans)
    return has_taken_loan and no_active_loans


def _check_credito_impecavel(session: Session, company: Company) -> bool:
    return company.credit_score >= 800


def _check_lider_de_mercado(session: Session, company: Company) -> bool:
    if not company.factories:
        return False
    equity = finance.balance_sheet(session, company)["equity"]
    competitor_cashes = [c.cash for c in session.query(Competitor).all()]
    return bool(competitor_cashes) and equity > max(competitor_cashes)


def _check_primeiro_milhao(session: Session, company: Company) -> bool:
    return finance.balance_sheet(session, company)["equity"] >= 1_000_000


def _check_magnata(session: Session, company: Company) -> bool:
    return finance.balance_sheet(session, company)["equity"] >= 5_000_000


CHECKS = {
    "primeira_fabrica": _check_primeira_fabrica,
    "expansao_territorial": _check_expansao_territorial,
    "diversificacao_industrial": _check_diversificacao_industrial,
    "producao_em_massa": _check_producao_em_massa,
    "vendedor_nato": _check_vendedor_nato,
    "sem_dividas": _check_sem_dividas,
    "credito_impecavel": _check_credito_impecavel,
    "lider_de_mercado": _check_lider_de_mercado,
    "primeiro_milhao": _check_primeiro_milhao,
    "magnata": _check_magnata,
}


def check_goals(session: Session, company: Company, current_day: int, game_minutes: int) -> list[dict]:
    """Checks every not-yet-achieved goal, rewards and records the ones that
    just became true. Returns the list of newly achieved goal configs."""
    achieved_ids = {row.goal_id for row in session.query(AchievedGoal).all()}
    newly_achieved = []

    for goal in config.GOALS:
        if goal["id"] in achieved_ids:
            continue
        if not CHECKS[goal["id"]](session, company):
            continue

        session.add(AchievedGoal(goal_id=goal["id"], achieved_day=current_day))
        company.cash += goal["reward_cash"]
        record(session, company.id, game_minutes, categories.INCOME, categories.GOAL_REWARD,
               goal["reward_cash"], f"Meta concluída: {goal['label']}")
        newly_achieved.append(goal)

    return newly_achieved

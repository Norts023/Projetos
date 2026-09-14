from dataclasses import dataclass

from sqlalchemy.orm import Session

from app import config
from app.models import Bank, Company, Loan
from app.simulation import categories
from app.simulation.ledger import record


@dataclass
class LoanOffer:
    bank_id: int
    bank_name: str
    eligible: bool
    annual_rate: float
    max_annual_rate: float
    max_principal: float
    max_term_months: int
    reason: str = ""


def score_rate_adjustment(credit_score: int) -> float:
    """Worse-than-average credit adds a spread on top of the bank's base rate."""
    if credit_score >= 700:
        return 0.0
    return (700 - credit_score) * 0.0003


def amount_rate_adjustment(principal: float, cash: float) -> float:
    """Borrowing more than your own cash on hand is riskier for the bank, so it
    adds a rate premium proportional to how many multiples of your cash you're
    asking for, capped so it never becomes absurd."""
    ratio = principal / max(cash, 1.0)
    if ratio <= 1.0:
        return 0.0
    premium = (ratio - 1.0) * config.LOAN_AMOUNT_RISK_PREMIUM_PER_UNIT
    return min(premium, config.LOAN_AMOUNT_RISK_PREMIUM_CAP)


def get_offers(session: Session, company: Company) -> list[LoanOffer]:
    offers = []
    for bank in session.query(Bank).all():
        if company.credit_score < bank.min_credit_score:
            offers.append(LoanOffer(
                bank_id=bank.id, bank_name=bank.name, eligible=False,
                annual_rate=0.0, max_annual_rate=0.0, max_principal=0.0, max_term_months=0,
                reason=f"Score de crédito abaixo do mínimo exigido ({bank.min_credit_score})",
            ))
            continue
        base_rate = bank.base_annual_rate + score_rate_adjustment(company.credit_score)
        max_principal = max(company.cash, 1.0) * bank.max_loan_to_cash_ratio
        rate_at_max = base_rate + amount_rate_adjustment(max_principal, company.cash)
        offers.append(LoanOffer(
            bank_id=bank.id, bank_name=bank.name, eligible=True,
            annual_rate=round(base_rate, 4), max_annual_rate=round(rate_at_max, 4),
            max_principal=round(max_principal, 2), max_term_months=bank.max_term_months,
        ))
    return offers


def _price_installment(principal: float, monthly_rate: float, term_months: int) -> float:
    if monthly_rate == 0:
        return principal / term_months
    factor = (1 + monthly_rate) ** term_months
    return principal * monthly_rate * factor / (factor - 1)


def request_loan(session: Session, company: Company, bank_id: int, principal: float,
                  term_months: int, payment_type: str, game_minutes: int) -> Loan:
    current_day = game_minutes // config.MINUTES_PER_DAY
    bank = session.get(Bank, bank_id)
    if bank is None:
        raise ValueError("Banco não encontrado")
    if payment_type not in ("PRICE", "SAC"):
        raise ValueError("Forma de pagamento inválida (use PRICE ou SAC)")

    offers = {o.bank_id: o for o in get_offers(session, company)}
    offer = offers[bank_id]
    if not offer.eligible:
        raise ValueError(offer.reason)
    if principal <= 0 or principal > offer.max_principal:
        raise ValueError(f"Principal deve estar entre 0 e {offer.max_principal:.2f}")
    if term_months <= 0 or term_months > offer.max_term_months:
        raise ValueError(f"Prazo deve estar entre 1 e {offer.max_term_months} meses")

    actual_rate = offer.annual_rate + amount_rate_adjustment(principal, company.cash)
    monthly_rate = actual_rate / 12
    monthly_payment = (
        _price_installment(principal, monthly_rate, term_months) if payment_type == "PRICE" else 0.0
    )

    loan = Loan(
        company_id=company.id, bank_id=bank_id, principal=principal, annual_rate=actual_rate,
        term_months=term_months, payment_type=payment_type, monthly_payment=monthly_payment,
        remaining_balance=principal, installments_paid=0, start_day=current_day,
        next_payment_day=current_day + 30, status="ACTIVE",
    )
    session.add(loan)
    company.cash += principal
    record(session, company.id, game_minutes, categories.INCOME,
           categories.LOAN_PROCEEDS, principal, f"Empréstimo do {bank.name}")
    return loan


def process_due_loans(session: Session, company: Company, current_day: int, game_minutes: int) -> None:
    for loan in company.loans:
        while loan.status == "ACTIVE" and loan.next_payment_day <= current_day:
            monthly_rate = loan.annual_rate / 12
            interest_portion = loan.remaining_balance * monthly_rate

            if loan.payment_type == "PRICE":
                payment = loan.monthly_payment
                principal_portion = payment - interest_portion
            else:  # SAC: fixed principal amortization, declining total payment
                principal_portion = loan.principal / loan.term_months
                payment = principal_portion + interest_portion

            principal_portion = min(principal_portion, loan.remaining_balance)
            payment = principal_portion + interest_portion

            if company.cash >= payment:
                company.cash -= payment
                loan.remaining_balance -= principal_portion
                loan.installments_paid += 1
                company.credit_score = min(
                    config.CREDIT_SCORE_MAX, company.credit_score + config.CREDIT_SCORE_ON_TIME_PAYMENT
                )
                company.company_level = min(
                    config.COMPANY_LEVEL_MAX, company.company_level + config.COMPANY_LEVEL_PER_LOAN_PAYMENT
                )
                record(session, company.id, game_minutes, categories.EXPENSE,
                       categories.LOAN_PRINCIPAL, principal_portion, f"Amortização empréstimo #{loan.id}")
                record(session, company.id, game_minutes, categories.EXPENSE,
                       categories.LOAN_INTEREST, interest_portion, f"Juros empréstimo #{loan.id}")
                loan.next_payment_day += 30
                if loan.installments_paid >= loan.term_months or loan.remaining_balance <= 0.01:
                    loan.status = "PAID"
            else:
                company.credit_score = max(
                    config.CREDIT_SCORE_MIN,
                    company.credit_score - config.CREDIT_SCORE_MISSED_PAYMENT_PENALTY,
                )
                company.company_level = max(
                    config.COMPANY_LEVEL_MIN,
                    company.company_level - config.COMPANY_LEVEL_PENALTY_MISSED_LOAN,
                )
                record(session, company.id, game_minutes, categories.EXPENSE,
                       categories.LOAN_MISSED, 0.0, f"Pagamento perdido empréstimo #{loan.id}")
                loan.next_payment_day = current_day + config.LOAN_MISSED_RETRY_DAYS


def payoff_loan(session: Session, company: Company, loan: Loan, game_minutes: int) -> float:
    if loan.status != "ACTIVE":
        raise ValueError("Empréstimo não está ativo")
    payoff_amount = loan.remaining_balance * (1 - config.EARLY_PAYOFF_DISCOUNT)
    if company.cash < payoff_amount:
        raise ValueError(f"Caixa insuficiente para quitar (necessário {payoff_amount:.2f})")

    company.cash -= payoff_amount
    loan.status = "PAID"
    loan.remaining_balance = 0.0
    record(session, company.id, game_minutes, categories.EXPENSE,
           categories.LOAN_PAYOFF, payoff_amount, f"Quitação antecipada empréstimo #{loan.id}")
    return payoff_amount

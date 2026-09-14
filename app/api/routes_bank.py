from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.api.deps import get_clock, get_company
from app.database import get_session
from app.models import Company, GameClock, Loan
from app.schemas import LoanRequest
from app.simulation import bank as bank_sim

router = APIRouter(prefix="/api", tags=["bank"])


@router.get("/banks/offers")
def get_offers(company: Company = Depends(get_company), session: Session = Depends(get_session)):
    offers = bank_sim.get_offers(session, company)
    return [
        {
            "bank_id": o.bank_id, "bank_name": o.bank_name, "eligible": o.eligible,
            "annual_rate": o.annual_rate, "max_principal": o.max_principal,
            "max_term_months": o.max_term_months, "reason": o.reason,
        }
        for o in offers
    ]


@router.post("/loans")
def request_loan(body: LoanRequest, company: Company = Depends(get_company),
                  clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    try:
        loan = bank_sim.request_loan(session, company, body.bank_id, body.principal,
                                      body.term_months, body.payment_type, clock.game_minutes)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    session.commit()
    return {"loan_id": loan.id, "monthly_payment": round(loan.monthly_payment, 2),
            "cash": round(company.cash, 2)}


@router.get("/loans")
def list_loans(company: Company = Depends(get_company)):
    return [
        {
            "id": loan.id, "bank": loan.bank.name, "principal": loan.principal,
            "annual_rate": loan.annual_rate, "term_months": loan.term_months,
            "payment_type": loan.payment_type, "remaining_balance": round(loan.remaining_balance, 2),
            "installments_paid": loan.installments_paid, "next_payment_day": loan.next_payment_day,
            "status": loan.status,
        }
        for loan in company.loans
    ]


@router.post("/loans/{loan_id}/payoff")
def payoff_loan(loan_id: int, company: Company = Depends(get_company),
                 clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    loan = session.get(Loan, loan_id)
    if loan is None or loan.company_id != company.id:
        raise HTTPException(status_code=404, detail="Empréstimo não encontrado")
    try:
        amount = bank_sim.payoff_loan(session, company, loan, clock.game_minutes)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    session.commit()
    return {"paid_amount": round(amount, 2), "cash": round(company.cash, 2)}

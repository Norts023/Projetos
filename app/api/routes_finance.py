from fastapi import APIRouter, Depends
from fastapi.responses import PlainTextResponse
from sqlalchemy.orm import Session

from app.api.deps import get_clock, get_company
from app.database import get_session
from app.models import Company, GameClock
from app.simulation import finance

router = APIRouter(prefix="/api/finance", tags=["finance"])


@router.get("/dre")
def get_dre(days: int = 30, company: Company = Depends(get_company),
            clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    current_day = clock.game_minutes // 1440
    from_day = max(0, current_day - days + 1)
    return finance.income_statement(session, company, from_day, current_day)


@router.get("/balance")
def get_balance(company: Company = Depends(get_company), session: Session = Depends(get_session)):
    return finance.balance_sheet(session, company)


@router.get("/cashflow")
def get_cashflow(days: int = 30, company: Company = Depends(get_company),
                  clock: GameClock = Depends(get_clock), session: Session = Depends(get_session)):
    current_day = clock.game_minutes // 1440
    from_day = max(0, current_day - days + 1)
    return finance.cash_flow_statement(session, company, from_day, current_day)


@router.get("/ledger")
def get_ledger(q: str = "", limit: int = 50, company: Company = Depends(get_company),
               session: Session = Depends(get_session)):
    return finance.recent_ledger(session, company.id, q, limit)


@router.get("/export.csv")
def export_csv(company: Company = Depends(get_company), session: Session = Depends(get_session)):
    csv_data = finance.export_ledger_csv(session, company)
    return PlainTextResponse(
        content=csv_data, media_type="text/csv",
        headers={"Content-Disposition": "attachment; filename=historico_financeiro.csv"},
    )

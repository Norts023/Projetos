from fastapi import Depends, HTTPException
from sqlalchemy.orm import Session

from app.database import get_session
from app.models import Company, GameClock


def get_company(session: Session = Depends(get_session)) -> Company:
    company = session.get(Company, 1)
    if company is None:
        raise HTTPException(status_code=500, detail="Empresa não inicializada")
    return company


def get_clock(session: Session = Depends(get_session)) -> GameClock:
    clock = session.get(GameClock, 1)
    if clock is None:
        raise HTTPException(status_code=500, detail="Relógio de jogo não inicializado")
    return clock

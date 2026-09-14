import asyncio
from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles

from app.api import routes_bank, routes_company, routes_finance, routes_game, routes_land, routes_market, routes_production
from app.database import Base, SessionLocal, engine
from app.seed import seed_if_empty
from app.simulation.clock import run_background_clock

STATIC_DIR = Path(__file__).resolve().parent.parent / "static"


@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)
    session = SessionLocal()
    try:
        seed_if_empty(session)
    finally:
        session.close()

    clock_task = asyncio.create_task(run_background_clock())
    yield
    clock_task.cancel()


app = FastAPI(title="Business Sim", lifespan=lifespan)

app.include_router(routes_company.router)
app.include_router(routes_market.router)
app.include_router(routes_production.router)
app.include_router(routes_bank.router)
app.include_router(routes_land.router)
app.include_router(routes_finance.router)
app.include_router(routes_game.router)

app.mount("/", StaticFiles(directory=STATIC_DIR, html=True), name="static")

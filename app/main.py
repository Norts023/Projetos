import asyncio
from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles

from app.api import (
    routes_admin,
    routes_bank,
    routes_company,
    routes_competitors,
    routes_finance,
    routes_game,
    routes_goals,
    routes_land,
    routes_market,
    routes_production,
    routes_retail,
)
from app.database import STATE_LOCK, Base, SessionLocal, engine
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


@app.middleware("http")
async def serialize_api_requests(request, call_next):
    """Every /api request and the background clock tick share the same game
    state, so they're serialized through STATE_LOCK to avoid lost updates."""
    if request.url.path.startswith("/api/"):
        async with STATE_LOCK:
            return await call_next(request)
    return await call_next(request)


app.include_router(routes_company.router)
app.include_router(routes_market.router)
app.include_router(routes_production.router)
app.include_router(routes_bank.router)
app.include_router(routes_land.router)
app.include_router(routes_finance.router)
app.include_router(routes_game.router)
app.include_router(routes_competitors.router)
app.include_router(routes_admin.router)
app.include_router(routes_goals.router)
app.include_router(routes_retail.router)

app.mount("/", StaticFiles(directory=STATIC_DIR, html=True), name="static")

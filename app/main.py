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
from app.database import STATE_LOCK, Base, SessionLocal, engine, ensure_schema
from app.seed import seed_if_empty
from app.simulation.clock import run_background_clock

STATIC_DIR = Path(__file__).resolve().parent.parent / "static"


@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)
    ensure_schema()
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


@app.middleware("http")
async def disable_static_cache(request, call_next):
    """Mobile browsers (Termux/Android in particular) cache static files
    aggressively, so a `git pull` that updates app.js/style.css can silently
    keep serving the old version until a hard-refresh. Force revalidation on
    every load instead — this is a local single-player game, not something
    that needs CDN-style caching."""
    response = await call_next(request)
    if not request.url.path.startswith("/api/"):
        response.headers["Cache-Control"] = "no-cache, no-store, must-revalidate"
    return response


app.mount("/", StaticFiles(directory=STATIC_DIR, html=True), name="static")

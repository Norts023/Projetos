import asyncio

from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker

from app.config import DATA_DIR, DATABASE_URL

DATA_DIR.mkdir(parents=True, exist_ok=True)

engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(bind=engine, autoflush=False, expire_on_commit=False)

# Serializes every request against the background real-time clock tick.
# Without this, the tick (a separate asyncio task with its own session) and
# an HTTP request mutating the same rows could each load stale in-memory
# state and clobber each other's writes on commit (lost updates on cash,
# inventory, or the clock itself).
STATE_LOCK = asyncio.Lock()


class Base(DeclarativeBase):
    pass


def get_session():
    session = SessionLocal()
    try:
        yield session
    finally:
        session.close()

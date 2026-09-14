import asyncio

from sqlalchemy import create_engine, inspect, text
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


def ensure_schema() -> None:
    """`Base.metadata.create_all()` only creates tables that don't exist yet — it
    never adds columns to a table that's already there. Since this is a solo local
    save file with no real migration system, a `git pull` that adds a model field
    (like Factory.status) would otherwise leave old saves with a column missing
    and every query touching it crashing. Patch that gap here: for every table
    that already exists, add whatever columns the models declare but the table
    doesn't have yet, defaulting existing rows to the column's Python default
    when it's a plain scalar (e.g. status -> 'ACTIVE')."""
    inspector = inspect(engine)
    existing_tables = set(inspector.get_table_names())
    with engine.begin() as conn:
        for table in Base.metadata.sorted_tables:
            if table.name not in existing_tables:
                continue
            existing_columns = {col["name"] for col in inspector.get_columns(table.name)}
            for column in table.columns:
                if column.name in existing_columns:
                    continue
                col_type = column.type.compile(dialect=engine.dialect)
                default_clause = ""
                if column.default is not None and getattr(column.default, "is_scalar", False):
                    default_value = column.default.arg
                    if isinstance(default_value, bool):
                        default_clause = f" DEFAULT {1 if default_value else 0}"
                    elif isinstance(default_value, (int, float)):
                        default_clause = f" DEFAULT {default_value}"
                    elif isinstance(default_value, str):
                        default_clause = f" DEFAULT '{default_value}'"
                conn.execute(text(
                    f'ALTER TABLE "{table.name}" ADD COLUMN "{column.name}" {col_type}{default_clause}'
                ))

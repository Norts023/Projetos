from pydantic import BaseModel, Field

from app import config


class QuantityRequest(BaseModel):
    quantity: float = Field(gt=0)


class GoodQuantityRequest(BaseModel):
    good_name: str
    quantity: float = Field(gt=0)


class BuildFactoryRequest(BaseModel):
    recipe_id: str


class RetailOrderRequest(BaseModel):
    good_name: str
    quantity: float = Field(gt=0)
    price_per_unit: float = Field(gt=0)


class LoanRequest(BaseModel):
    bank_id: int
    principal: float = Field(gt=0)
    term_months: int = Field(gt=0)
    payment_type: str = Field(pattern="^(PRICE|SAC)$")


class AdvanceRequest(BaseModel):
    minutes: int = Field(gt=0, le=43200)  # cap a single jump at 30 game-days


class SpeedRequest(BaseModel):
    multiplier: float = Field(gt=0, le=config.MAX_SPEED_MULTIPLIER)
    running: bool = True


class AdminCompanyUpdate(BaseModel):
    cash: float | None = Field(default=None, ge=0)
    credit_score: int | None = Field(default=None, ge=config.CREDIT_SCORE_MIN, le=config.CREDIT_SCORE_MAX)


class AdminMarketUpdate(BaseModel):
    good_name: str
    price: float = Field(gt=0)


class AdminCompetitorUpdate(BaseModel):
    cash: float | None = None
    production_rate_per_hour: float | None = Field(default=None, ge=0)


class AdminGameUpdate(BaseModel):
    game_minutes: int | None = Field(default=None, ge=0)

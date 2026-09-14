from pydantic import BaseModel, Field


class QuantityRequest(BaseModel):
    quantity: float = Field(gt=0)


class LoanRequest(BaseModel):
    bank_id: int
    principal: float = Field(gt=0)
    term_months: int = Field(gt=0)
    payment_type: str = Field(pattern="^(PRICE|SAC)$")


class AdvanceRequest(BaseModel):
    minutes: int = Field(gt=0, le=43200)  # cap a single jump at 30 game-days


class SpeedRequest(BaseModel):
    multiplier: float = Field(gt=0, le=50)
    running: bool = True

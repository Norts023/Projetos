from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app import config
from app.database import get_session
from app.models import MarketGoodState

router = APIRouter(prefix="/api/market", tags=["market"])


@router.get("")
def get_market(session: Session = Depends(get_session)):
    prices = {g.name: g.current_price for g in session.query(MarketGoodState).all()}
    return [
        {
            "name": name, "label": data["label"], "tier": data["tier"],
            "current_price": round(prices[name], 2), "base_price": data["base_price"],
            "has_recipe": name in config.RECIPES,
        }
        for name, data in sorted(config.GOODS.items(), key=lambda kv: (kv[1]["tier"], kv[1]["label"]))
    ]


@router.get("/recipes")
def get_recipes(session: Session = Depends(get_session)):
    prices = {g.name: g.current_price for g in session.query(MarketGoodState).all()}
    result = []
    for recipe_id, recipe in config.RECIPES.items():
        output_price = prices[recipe["output_good"]]
        input_cost_per_unit = sum(ratio * prices[good] for good, ratio in recipe["inputs"].items())
        profit_per_unit = output_price - input_cost_per_unit
        profit_per_hour = profit_per_unit * recipe["output_rate_per_hour"]
        result.append({
            "id": recipe_id, "label": recipe["label"], "output_good": recipe["output_good"],
            "output_label": config.GOODS[recipe["output_good"]]["label"], "tier": recipe["tier"],
            "sector": recipe["sector"], "sector_icon": recipe["sector_icon"],
            "output_rate_per_hour": recipe["output_rate_per_hour"], "build_cost": recipe["build_cost"],
            "inputs": [
                {"good": good, "label": config.GOODS[good]["label"], "ratio": ratio}
                for good, ratio in recipe["inputs"].items()
            ],
            "input_cost_per_unit": round(input_cost_per_unit, 2),
            "output_price": round(output_price, 2),
            "profit_per_unit": round(profit_per_unit, 2),
            "profit_per_hour": round(profit_per_hour, 2),
            "payback_hours": round(recipe["build_cost"] / profit_per_hour, 1) if profit_per_hour > 0 else None,
        })
    return result

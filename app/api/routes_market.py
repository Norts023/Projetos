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
def get_recipes():
    return [
        {
            "id": recipe_id, "label": recipe["label"], "output_good": recipe["output_good"],
            "output_label": config.GOODS[recipe["output_good"]]["label"], "tier": recipe["tier"],
            "output_rate_per_hour": recipe["output_rate_per_hour"], "build_cost": recipe["build_cost"],
            "inputs": [
                {"good": good, "label": config.GOODS[good]["label"], "ratio": ratio}
                for good, ratio in recipe["inputs"].items()
            ],
        }
        for recipe_id, recipe in config.RECIPES.items()
    ]

"""车机只读接口(JSON)。依赖:flask, db, services.catalog_service。
只做收参/调业务/序列化;业务不在此写。"""
from flask import Blueprint, current_app, g, jsonify

from server import db
from server.services import catalog_service as svc

bp = Blueprint("api", __name__, url_prefix="/api")


def _conn():
    if "conn" not in g:
        g.conn = db.connect(current_app.config["DB_PATH"])
    return g.conn


def _yuan(cents: int) -> str:
    return f"{cents / 100:.2f}"


def _product_json(p) -> dict:
    return {
        "id": p.id,
        "name": p.name,
        "price": _yuan(p.price_cents),
        "price_cents": p.price_cents,
        "description": p.description,
        "category_id": p.category_id,
    }


def _banner_json(b) -> dict:
    return {
        "product_id": b.product_id,
        "title": b.title,
        "description": b.description,
        "price": _yuan(b.price_cents),
        "price_cents": b.price_cents,
        "image": b.image_url,
    }


@bp.get("/home")
def home():
    data = svc.home_data(_conn())
    return jsonify(
        {
            "banners": [_banner_json(b) for b in data.banners],
            "recommended": [_product_json(p) for p in data.recommended],
        }
    )


@bp.get("/categories")
def categories():
    cats = svc.nav_categories(_conn())
    return jsonify([{"id": c.id, "name": c.name} for c in cats])


@bp.get("/categories/<int:category_id>/products")
def category_products(category_id: int):
    items = svc.category_products(_conn(), category_id)
    return jsonify([_product_json(p) for p in items])


@bp.get("/products/<int:product_id>")
def product_detail(product_id: int):
    try:
        detail = svc.product_detail(_conn(), product_id)
    except svc.ProductNotFound:
        return jsonify({"error": "商品已下架"}), 404
    payload = _product_json(detail.product)
    payload["images"] = [img.url for img in detail.images]
    return jsonify(payload)

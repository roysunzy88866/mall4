"""车机只读 + 下单接口(JSON)。依赖:flask, db, services。
只做收参 / 校验 / 调业务 / 序列化;业务不在此写;domain 异常在此翻成 HTTP。"""
from datetime import datetime

from flask import Blueprint, current_app, g, jsonify, request

from server import db
from server.models.entities import OrderLineInput
from server.rules import lifecycle as lc
from server.rules import money
from server.services import catalog_service as svc
from server.services import lifecycle_service as life_svc
from server.services import order_service as order_svc

bp = Blueprint("api", __name__, url_prefix="/api")


def _conn():
    if "conn" not in g:
        g.conn = db.connect(current_app.config["DB_PATH"])
    return g.conn


def _step() -> int:
    return current_app.config.get("STATUS_STEP_SECONDS", 30)


def _window() -> int:
    return current_app.config.get("RETURN_WINDOW_SECONDS", 7 * 86400)


def _yuan(cents: int) -> str:
    return money.cents_to_yuan(cents)


def _now() -> str:
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


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


def _order_json(ow, logistics=None, can_cancel=False, can_return=False) -> dict:
    o = ow.order
    payload = {
        "id": o.id,
        "device_id": o.device_id,
        "status": o.status,
        "status_label": lc.status_label(o.status),
        "created_at": o.created_at,
        "total": _yuan(o.total_cents),
        "total_cents": o.total_cents,
        "logistics": logistics or [],
        "can_cancel": can_cancel,
        "can_return": can_return,
        "return_reason": o.return_reason,
        "return_note": o.return_note,
        "items": [
            {
                "product_id": it.product_id,
                "name": it.product_name,
                "image": it.product_image,
                "price": _yuan(it.price_cents),
                "price_cents": it.price_cents,
                "qty": it.qty,
            }
            for it in ow.items
        ],
    }
    return payload


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


@bp.post("/orders")
def create_order():
    device_id = request.headers.get("X-Device-Id")
    data = request.get_json(silent=True)
    if not isinstance(data, dict):
        data = {}
    raw = data.get("items")
    raw = raw if isinstance(raw, list) else []
    items = [
        OrderLineInput(
            product_id=r.get("product_id"),
            name=r.get("name") or "",
            image=r.get("image"),
            price_cents=r.get("price_cents"),
            qty=r.get("qty"),
        )
        for r in raw
        if isinstance(r, dict)
    ]
    try:
        created = order_svc.place_order(_conn(), device_id, items, _now())
    except order_svc.InvalidOrder as e:
        return jsonify({"error": "; ".join(e.errors)}), 400
    return jsonify(_order_json(created)), 201


@bp.get("/orders")
def list_orders():
    device_id = request.headers.get("X-Device-Id")
    if not device_id:
        return jsonify([])
    now = _now()
    rows = life_svc.list_orders(_conn(), device_id, now, _step())
    return jsonify([
        _order_json(ow, log, life_svc.can_cancel(ow.order), life_svc.can_return(ow.order, now, _window()))
        for (ow, log) in rows
    ])


@bp.get("/orders/<int:order_id>")
def order_detail(order_id: int):
    now = _now()
    result = life_svc.order_detail(_conn(), order_id, now, _step())
    if result is None:
        return jsonify({"error": "订单不存在"}), 404
    ow, log = result
    return jsonify(_order_json(
        ow, log, life_svc.can_cancel(ow.order), life_svc.can_return(ow.order, now, _window())
    ))


_ERR_HTTP = {"not_found": 404, "forbidden": 403, "conflict": 409, "bad_request": 400}


@bp.post("/orders/<int:order_id>/cancel")
def cancel_order(order_id: int):
    device_id = request.headers.get("X-Device-Id")
    try:
        life_svc.cancel_order(_conn(), order_id, device_id, _now(), _step())
    except life_svc.OrderActionError as e:
        return jsonify({"error": e.msg}), _ERR_HTTP.get(e.code, 400)
    return order_detail(order_id)


@bp.post("/orders/<int:order_id>/return")
def return_order(order_id: int):
    device_id = request.headers.get("X-Device-Id")
    data = request.get_json(silent=True) or {}
    try:
        life_svc.request_return(
            _conn(), order_id, device_id, data.get("reason"), data.get("note"),
            _now(), _step(), _window(),
        )
    except life_svc.OrderActionError as e:
        return jsonify({"error": e.msg}), _ERR_HTTP.get(e.code, 400)
    return order_detail(order_id)

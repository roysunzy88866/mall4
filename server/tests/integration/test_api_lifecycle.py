"""订单状态机 + 物流 集成测试(对应 specs/order-lifecycle)。"""
from datetime import datetime, timedelta

DEV = {"X-Device-Id": "device-life"}
_FMT = "%Y-%m-%d %H:%M:%S"


def _place(client):
    body = {"items": [{"product_id": 1, "name": "车载充电头", "image": "/u/x.png", "price_cents": 3900, "qty": 1}]}
    r = client.post("/api/orders", json=body, headers=DEV)
    assert r.status_code == 201
    return r.get_json()["id"]


def _set_clock_base(conn, order_id, seconds_ago):
    ts = (datetime.now() - timedelta(seconds=seconds_ago)).strftime(_FMT)
    conn.execute("UPDATE orders SET clock_base_at=? WHERE id=?", (ts, order_id))
    conn.commit()


def test_order_starts_paid_with_logistics(client):
    _place(client)
    o = client.get("/api/orders", headers=DEV).get_json()[0]
    assert o["status"] == "paid"
    assert len(o["logistics"]) == 4
    assert o["logistics"][0]["reached"] is True
    assert o["logistics"][3]["reached"] is False


def test_detail_returns_logistics(client):
    oid = _place(client)
    o = client.get(f"/api/orders/{oid}", headers=DEV).get_json()
    assert o["id"] == oid
    assert o["status"] == "paid"
    assert [n["label"] for n in o["logistics"]] == ["已下单", "已发货", "派送中", "已签收"]


def test_detail_404(client):
    assert client.get("/api/orders/99999", headers=DEV).status_code == 404


def test_auto_advance_shipping(client, conn):
    oid = _place(client)
    _set_clock_base(conn, oid, 35)  # 默认步长 30 → 阶段 1
    o = client.get(f"/api/orders/{oid}", headers=DEV).get_json()
    assert o["status"] == "shipping"


def test_auto_advance_delivered(client, conn):
    oid = _place(client)
    _set_clock_base(conn, oid, 100)  # 100/30 = 3 → 已签收
    o = client.get(f"/api/orders/{oid}", headers=DEV).get_json()
    assert o["status"] == "delivered"
    # delivered_at 已落库
    row = conn.execute("SELECT delivered_at FROM orders WHERE id=?", (oid,)).fetchone()
    assert row["delivered_at"] is not None


def test_manual_override_stops_auto(client, conn):
    oid = _place(client)
    conn.execute("UPDATE orders SET status='delivering', manual=1 WHERE id=?", (oid,))
    conn.commit()
    _set_clock_base(conn, oid, 100)  # 即便时间够久
    o = client.get(f"/api/orders/{oid}", headers=DEV).get_json()
    assert o["status"] == "delivering"  # 被接管,不自动推进

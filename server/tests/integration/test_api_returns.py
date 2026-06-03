"""取消 + 退货 集成测试(对应 specs/returns)。"""
from datetime import datetime, timedelta

DEV = {"X-Device-Id": "device-ret"}
OTHER = {"X-Device-Id": "device-other"}
_FMT = "%Y-%m-%d %H:%M:%S"


def _place(client, price=3900, qty=1):
    body = {"items": [{"product_id": 1, "name": "x", "image": None, "price_cents": price, "qty": qty}]}
    return client.post("/api/orders", json=body, headers=DEV).get_json()["id"]


def _make_delivered(conn, oid, delivered_secs_ago=0):
    ts = (datetime.now() - timedelta(seconds=delivered_secs_ago)).strftime(_FMT)
    conn.execute("UPDATE orders SET status='delivered', manual=1, delivered_at=? WHERE id=?", (ts, oid))
    conn.commit()


def test_cancel_in_transit(client):
    oid = _place(client)
    r = client.post(f"/api/orders/{oid}/cancel", headers=DEV)
    assert r.status_code == 200
    assert r.get_json()["status"] == "cancelled"


def test_cancel_delivered_rejected(client, conn):
    oid = _place(client)
    _make_delivered(conn, oid)
    r = client.post(f"/api/orders/{oid}/cancel", headers=DEV)
    assert r.status_code == 409


def test_cancel_wrong_device_forbidden(client):
    oid = _place(client)
    assert client.post(f"/api/orders/{oid}/cancel", headers=OTHER).status_code == 403


def test_return_within_window(client, conn):
    oid = _place(client)
    _make_delivered(conn, oid, delivered_secs_ago=10)
    r = client.post(f"/api/orders/{oid}/return", json={"reason": "7天无理由"}, headers=DEV)
    assert r.status_code == 200
    assert r.get_json()["status"] == "return_review"


def test_return_out_of_window_rejected(client, conn):
    oid = _place(client)
    _make_delivered(conn, oid, delivered_secs_ago=8 * 86400)  # 超 7 天
    r = client.post(f"/api/orders/{oid}/return", json={"reason": "x"}, headers=DEV)
    assert r.status_code == 409


def test_return_requires_reason(client, conn):
    oid = _place(client)
    _make_delivered(conn, oid)
    assert client.post(f"/api/orders/{oid}/return", json={"reason": ""}, headers=DEV).status_code == 400


def test_admin_approve_return(client, admin_client, conn):
    oid = _place(client)
    _make_delivered(conn, oid)
    client.post(f"/api/orders/{oid}/return", json={"reason": "不想要了"}, headers=DEV)
    admin_client.post(f"/admin/returns/{oid}/approve")
    assert conn.execute("SELECT status FROM orders WHERE id=?", (oid,)).fetchone()["status"] == "refunded"


def test_admin_reject_return(client, admin_client, conn):
    oid = _place(client)
    _make_delivered(conn, oid)
    client.post(f"/api/orders/{oid}/return", json={"reason": "x"}, headers=DEV)
    admin_client.post(f"/admin/returns/{oid}/reject")
    assert conn.execute("SELECT status FROM orders WHERE id=?", (oid,)).fetchone()["status"] == "return_rejected"


def test_revenue_excludes_cancelled_and_refunded(client, admin_client, conn):
    before = conn.execute(
        "SELECT COALESCE(SUM(total_cents),0) s FROM orders WHERE status NOT IN ('cancelled','refunded')"
    ).fetchone()["s"]
    oid = _place(client, price=10000)
    client.post(f"/api/orders/{oid}/cancel", headers=DEV)  # 取消 → 不计营业额
    after = conn.execute(
        "SELECT COALESCE(SUM(total_cents),0) s FROM orders WHERE status NOT IN ('cancelled','refunded')"
    ).fetchone()["s"]
    assert after == before  # 取消单不增加营业额


def test_returns_page_loads(admin_client, client, conn):
    oid = _place(client)
    _make_delivered(conn, oid)
    client.post(f"/api/orders/{oid}/return", json={"reason": "质量问题"}, headers=DEV)
    html = admin_client.get("/admin/returns").get_data(as_text=True)
    assert "退货审核" in html and "质量问题" in html

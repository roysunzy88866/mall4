"""后台订单查看 集成测试(admin-console 批3)。"""


def test_orders_page_shows_seed_orders(admin_client):
    html = admin_client.get("/admin/orders").get_data(as_text=True)
    assert "订单" in html
    assert "demo-seed-device" in html      # seed 预置 3 笔挂在演示设备
    assert "车载充电头" in html            # seed 订单含 p1


def test_orders_page_shows_newly_placed_order(admin_client):
    admin_client.post(
        "/api/orders",
        json={"items": [{"product_id": 4, "name": "行车记录仪", "image": None, "price_cents": 29900, "qty": 1}]},
        headers={"X-Device-Id": "car-7"},
    )
    html = admin_client.get("/admin/orders").get_data(as_text=True)
    assert "car-7" in html and "行车记录仪" in html


# ---- order-lifecycle:状态机操作 ----
def _first_order_id(conn):
    return conn.execute("SELECT id FROM orders ORDER BY id LIMIT 1").fetchone()["id"]


def test_orders_filter_by_status(admin_client):
    assert admin_client.get("/admin/orders?status=delivered").status_code == 200


def test_order_detail_has_logistics(admin_client, conn):
    oid = _first_order_id(conn)
    html = admin_client.get(f"/admin/orders/{oid}").get_data(as_text=True)
    assert "物流轨迹" in html and "管理操作" in html


def test_admin_set_status_takes_over(admin_client, conn):
    oid = _first_order_id(conn)
    admin_client.post(f"/admin/orders/{oid}/status", data={"status": "delivered"})
    row = conn.execute("SELECT status, manual FROM orders WHERE id=?", (oid,)).fetchone()
    assert row["status"] == "delivered" and row["manual"] == 1


def test_admin_advance_one(admin_client, conn):
    # 下一张新单(manual=0,可自动),快进一步后读取应前进到 shipping
    admin_client.post(
        "/api/orders",
        json={"items": [{"product_id": 1, "name": "x", "image": None, "price_cents": 100, "qty": 1}]},
        headers={"X-Device-Id": "adv-dev"},
    )
    oid = conn.execute("SELECT id FROM orders WHERE device_id='adv-dev'").fetchone()["id"]
    admin_client.post(f"/admin/orders/{oid}/advance")
    row = conn.execute("SELECT status FROM orders WHERE id=?", (oid,)).fetchone()
    # advance 后惰性推进需一次读取触发
    admin_client.get(f"/admin/orders/{oid}")
    row = conn.execute("SELECT status FROM orders WHERE id=?", (oid,)).fetchone()
    assert row["status"] == "shipping"

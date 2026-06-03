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

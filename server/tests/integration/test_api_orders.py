"""下单 + 本机订单 集成测试(对应 specs/ordering)。"""

DEV_A = {"X-Device-Id": "device-A"}
DEV_B = {"X-Device-Id": "device-B"}


def _body(name="车载充电头", price_cents=3900, qty=1, product_id=1):
    return {
        "items": [
            {
                "product_id": product_id,
                "name": name,
                "image": "/uploads/placeholder.png",
                "price_cents": price_cents,
                "qty": qty,
            }
        ]
    }


def test_place_order_success(client):
    r = client.post("/api/orders", json=_body(price_cents=3900, qty=2), headers=DEV_A)
    assert r.status_code == 201
    data = r.get_json()
    assert data["status"] == "paid"
    assert data["total_cents"] == 7800
    assert data["items"][0]["name"] == "车载充电头"
    assert data["items"][0]["qty"] == 2


def test_place_order_missing_device(client):
    r = client.post("/api/orders", json=_body())
    assert r.status_code == 400
    assert r.get_json()["error"]


def test_place_order_empty_items(client):
    assert client.post("/api/orders", json={"items": []}, headers=DEV_A).status_code == 400


def test_orders_by_device_newest_first(client):
    client.post("/api/orders", json=_body(name="第一单"), headers=DEV_A)
    client.post("/api/orders", json=_body(name="第二单"), headers=DEV_A)
    orders = client.get("/api/orders", headers=DEV_A).get_json()
    assert len(orders) == 2
    assert orders[0]["items"][0]["name"] == "第二单"  # 倒序:后下的在前


def test_orders_only_own_device(client):
    client.post("/api/orders", json=_body(), headers=DEV_A)
    assert client.get("/api/orders", headers=DEV_B).get_json() == []


def test_orders_empty_state(client):
    assert client.get("/api/orders", headers=DEV_A).get_json() == []


def test_snapshot_unaffected_by_product_change(client, conn):
    client.post("/api/orders", json=_body(price_cents=3900), headers=DEV_A)
    conn.execute("UPDATE products SET price_cents=99999, name='改名了' WHERE id=1")
    conn.commit()
    item = client.get("/api/orders", headers=DEV_A).get_json()[0]["items"][0]
    assert item["price_cents"] == 3900  # 快照不受商品改价影响
    assert item["name"] == "车载充电头"


def test_e2e_browse_then_order_appears(client):
    detail = client.get("/api/products/1").get_json()
    body = {
        "items": [
            {
                "product_id": detail["id"],
                "name": detail["name"],
                "image": detail["images"][0],
                "price_cents": detail["price_cents"],
                "qty": 1,
            }
        ]
    }
    assert client.post("/api/orders", json=body, headers=DEV_A).status_code == 201
    orders = client.get("/api/orders", headers=DEV_A).get_json()
    assert any(o["items"][0]["product_id"] == detail["id"] for o in orders)


def test_order_rollback_no_half_order(client, conn, monkeypatch):
    """第 2 个行项插入失败 → 整单回滚,不留半单。"""
    from server.repositories import orders_repo

    real = orders_repo.insert_order_item
    state = {"n": 0}

    def boom(c, oid, line):
        state["n"] += 1
        if state["n"] == 2:
            raise RuntimeError("boom")
        return real(c, oid, line)

    monkeypatch.setattr(orders_repo, "insert_order_item", boom)
    body = {
        "items": [
            {"product_id": 1, "name": "A", "image": None, "price_cents": 100, "qty": 1},
            {"product_id": 2, "name": "B", "image": None, "price_cents": 200, "qty": 1},
        ]
    }
    before = conn.execute("SELECT COUNT(*) c FROM orders WHERE device_id='device-A'").fetchone()["c"]
    r = client.post("/api/orders", json=body, headers=DEV_A)
    after = conn.execute("SELECT COUNT(*) c FROM orders WHERE device_id='device-A'").fetchone()["c"]
    assert r.status_code != 201
    assert after == before  # 没留半单

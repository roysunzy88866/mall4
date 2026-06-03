"""只读接口集成测试(Flask 测试客户端 + 临时已 seed 库)。
对应 specs/catalog-browsing 的场景。"""


def test_home_returns_banners_and_recommended(client):
    data = client.get("/api/home").get_json()
    assert 3 <= len(data["banners"]) <= 5
    assert len(data["recommended"]) > 0
    banner = data["banners"][0]
    assert banner["title"] and banner["price"]


def test_home_excludes_inactive_category(client, conn):
    conn.execute("UPDATE categories SET is_active=0 WHERE id=1")
    conn.commit()
    data = client.get("/api/home").get_json()
    hidden = [r["id"] for r in conn.execute("SELECT id FROM products WHERE category_id=1").fetchall()]
    rec_ids = [p["id"] for p in data["recommended"]]
    assert hidden  # 该分类确有商品
    assert all(pid not in rec_ids for pid in hidden)


def test_home_empty_state(unseeded_client):
    data = unseeded_client.get("/api/home").get_json()
    assert data["recommended"] == []
    assert data["banners"] == []


def test_categories_returns_active_sorted(client, conn):
    conn.execute("UPDATE categories SET is_active=0 WHERE id=2")
    conn.commit()
    ids = [c["id"] for c in client.get("/api/categories").get_json()]
    assert 2 not in ids
    assert ids == sorted(ids)


def test_category_products(client):
    items = client.get("/api/categories/1/products").get_json()
    assert items
    assert all(p["category_id"] == 1 for p in items)


def test_category_products_empty(client, conn):
    conn.execute(
        "INSERT INTO categories (name, sort_order, is_active, created_at) "
        "VALUES ('空类', 99, 1, '2026-05-01 09:09:00')"
    )
    conn.commit()
    cid = conn.execute("SELECT id FROM categories WHERE name='空类'").fetchone()["id"]
    assert client.get(f"/api/categories/{cid}/products").get_json() == []


def test_product_detail(client):
    data = client.get("/api/products/1").get_json()
    assert data["id"] == 1
    assert data["name"] and data["price"] and data["description"]
    assert isinstance(data["images"], list) and data["images"]


def test_product_detail_not_found(client):
    resp = client.get("/api/products/999999")
    assert resp.status_code == 404
    assert resp.get_json()["error"]

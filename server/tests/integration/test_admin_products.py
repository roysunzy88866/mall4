"""后台商品管理 + 传图 集成测试(admin-console 批2)。"""
import io
import os


def _create(admin_client, name="新商品", category_id=1, price="19.95", stock="5", description="d"):
    return admin_client.post(
        "/admin/products/create",
        data={"name": name, "category_id": str(category_id), "price": price, "stock": stock, "description": description},
    )


def test_create_product_price_to_cents(admin_client, conn):
    _create(admin_client, name="价格测试", price="19.95")
    assert conn.execute("SELECT price_cents FROM products WHERE name='价格测试'").fetchone()["price_cents"] == 1995


def test_create_product_with_image(admin_client, conn, cfg):
    admin_client.post(
        "/admin/products/create",
        data={
            "name": "带图商品", "category_id": "1", "price": "5.00", "stock": "1", "description": "d",
            "image": (io.BytesIO(b"fakepngbytes"), "pic.png"),
        },
        content_type="multipart/form-data",
    )
    pid = conn.execute("SELECT id FROM products WHERE name='带图商品'").fetchone()["id"]
    img = conn.execute("SELECT url FROM product_images WHERE product_id=?", (pid,)).fetchone()
    assert img is not None and img["url"].startswith("/uploads/")
    assert os.path.exists(os.path.join(cfg.uploads_dir, img["url"].split("/")[-1]))


def test_create_product_bad_image_rejected(admin_client, conn):
    before = conn.execute("SELECT COUNT(*) AS c FROM products").fetchone()["c"]
    admin_client.post(
        "/admin/products/create",
        data={"name": "坏图", "category_id": "1", "price": "5.00", "image": (io.BytesIO(b"x"), "evil.txt")},
        content_type="multipart/form-data",
    )
    assert conn.execute("SELECT COUNT(*) AS c FROM products").fetchone()["c"] == before


def test_create_product_invalid_price_rejected(admin_client, conn):
    before = conn.execute("SELECT COUNT(*) AS c FROM products").fetchone()["c"]
    _create(admin_client, name="坏价", price="abc")
    assert conn.execute("SELECT COUNT(*) AS c FROM products").fetchone()["c"] == before


def test_update_product_price(admin_client, conn):
    admin_client.post(
        "/admin/products/1/update",
        data={"name": "车载充电头", "category_id": "1", "price": "9.99", "stock": "3"},
    )
    assert conn.execute("SELECT price_cents FROM products WHERE id=1").fetchone()["price_cents"] == 999


def test_update_product_replaces_image(admin_client, conn):
    # 商品 1 原有占位图;编辑上传新图 → 主图被替换(仍只有一张,且非占位)
    admin_client.post(
        "/admin/products/1/update",
        data={
            "name": "车载充电头", "category_id": "1", "price": "9.99", "stock": "3",
            "image": (io.BytesIO(b"newpngbytes"), "new.png"),
        },
        content_type="multipart/form-data",
    )
    imgs = conn.execute("SELECT url FROM product_images WHERE product_id=1").fetchall()
    assert len(imgs) == 1
    assert imgs[0]["url"].startswith("/uploads/") and imgs[0]["url"] != "/uploads/placeholder.png"


def test_update_product_without_image_keeps_original(admin_client, conn):
    before = [r["url"] for r in conn.execute("SELECT url FROM product_images WHERE product_id=1").fetchall()]
    admin_client.post(
        "/admin/products/1/update",
        data={"name": "车载充电头", "category_id": "1", "price": "9.99", "stock": "3"},
    )
    after = [r["url"] for r in conn.execute("SELECT url FROM product_images WHERE product_id=1").fetchall()]
    assert after == before  # 未传图 → 原主图不变


def test_delete_product(admin_client, conn):
    _create(admin_client, name="待删商品")
    pid = conn.execute("SELECT id FROM products WHERE name='待删商品'").fetchone()["id"]
    admin_client.post(f"/admin/products/{pid}/delete")
    assert conn.execute("SELECT * FROM products WHERE id=?", (pid,)).fetchone() is None


def test_delete_ordered_product_keeps_order_snapshot(admin_client, conn):
    # D8:删被订单买过的商品 → 允许,订单整单快照不受影响
    admin_client.post(
        "/api/orders",
        json={"items": [{"product_id": 1, "name": "车载充电头", "image": "/uploads/placeholder.png", "price_cents": 3900, "qty": 1}]},
        headers={"X-Device-Id": "dev-x"},
    )
    admin_client.post("/admin/products/1/delete")
    assert admin_client.get("/api/products/1").status_code == 404  # 商品已下架
    item = admin_client.get("/api/orders", headers={"X-Device-Id": "dev-x"}).get_json()[0]["items"][0]
    assert item["name"] == "车载充电头" and item["price_cents"] == 3900  # 快照仍在

"""后台分类管理 集成测试(admin-console 批2)。"""


def test_create_category(admin_client, conn):
    admin_client.post("/admin/categories/create", data={"name": "测试新类"})
    row = conn.execute("SELECT * FROM categories WHERE name='测试新类'").fetchone()
    assert row is not None and row["is_active"] == 1


def test_rename_category(admin_client, conn):
    admin_client.post("/admin/categories/1/rename", data={"name": "改名了"})
    assert conn.execute("SELECT name FROM categories WHERE id=1").fetchone()["name"] == "改名了"


def test_toggle_category(admin_client, conn):
    admin_client.post("/admin/categories/1/toggle", data={"active": "0"})
    assert conn.execute("SELECT is_active FROM categories WHERE id=1").fetchone()["is_active"] == 0


def test_delete_empty_category(admin_client, conn):
    admin_client.post("/admin/categories/create", data={"name": "空类待删"})
    cid = conn.execute("SELECT id FROM categories WHERE name='空类待删'").fetchone()["id"]
    admin_client.post(f"/admin/categories/{cid}/delete")
    assert conn.execute("SELECT * FROM categories WHERE id=?", (cid,)).fetchone() is None


def test_delete_category_with_products_rejected(admin_client, conn):
    # 分类 1 有商品 → 删除被拒,分类仍在
    admin_client.post("/admin/categories/1/delete")
    assert conn.execute("SELECT * FROM categories WHERE id=1").fetchone() is not None


def test_move_category_down_swaps_sort(admin_client, conn):
    admin_client.post("/admin/categories/1/move", data={"direction": "down"})
    s1 = conn.execute("SELECT sort_order FROM categories WHERE id=1").fetchone()["sort_order"]
    s2 = conn.execute("SELECT sort_order FROM categories WHERE id=2").fetchone()["sort_order"]
    assert s1 == 2 and s2 == 1

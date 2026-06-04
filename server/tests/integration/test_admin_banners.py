"""后台推荐位管理 集成测试(admin-console 批3)。seed 预置 4 个推荐位。"""
import json
import re


def _count(conn):
    return conn.execute("SELECT COUNT(*) AS c FROM banners").fetchone()["c"]


def _banner_ids(conn):
    return [r["id"] for r in conn.execute("SELECT id FROM banners ORDER BY sort_order, id").fetchall()]


def test_seed_has_four_banners(conn):
    assert _count(conn) == 4


def test_add_banner(admin_client, conn):
    admin_client.post("/admin/banners/add", data={"product_id": "2"})  # 商品2 原不在推荐位
    assert _count(conn) == 5
    assert conn.execute("SELECT 1 FROM banners WHERE product_id=2").fetchone() is not None


def test_add_banner_over_5_rejected(admin_client, conn):
    admin_client.post("/admin/banners/add", data={"product_id": "2"})  # → 5
    admin_client.post("/admin/banners/add", data={"product_id": "3"})  # → 6,被拒
    assert _count(conn) == 5


def test_add_duplicate_rejected(admin_client, conn):
    before = _count(conn)
    admin_client.post("/admin/banners/add", data={"product_id": "1"})  # 商品1(p1)已是推荐位
    assert _count(conn) == before


def test_remove_banner_below_3_rejected(admin_client, conn):
    ids = _banner_ids(conn)
    admin_client.post(f"/admin/banners/{ids[0]}/remove")  # 4 → 3,可
    assert _count(conn) == 3
    admin_client.post(f"/admin/banners/{ids[1]}/remove")  # 3 → 2,被拒
    assert _count(conn) == 3


def test_move_banner_down_swaps_sort(admin_client, conn):
    bs = conn.execute("SELECT id, sort_order FROM banners ORDER BY sort_order, id").fetchall()
    first_id, second_sort = bs[0]["id"], bs[1]["sort_order"]
    admin_client.post(f"/admin/banners/{first_id}/move", data={"direction": "down"})
    new_sort = conn.execute("SELECT sort_order FROM banners WHERE id=?", (first_id,)).fetchone()["sort_order"]
    assert new_sort == second_sort


def test_banner_select_is_grouped_by_category(admin_client, conn):
    # 推荐位选择改为「先选分类→再选商品」:两级下拉 + 内嵌按分类分组的数据
    html = admin_client.get("/admin/banners").get_data(as_text=True)
    assert 'id="banner-cat"' in html and 'id="banner-prod"' in html
    m = re.search(r"BANNER_PRODUCTS = (\{.*\});", html)
    assert m, "应内嵌按分类分组的可选商品 JSON"
    grouped = json.loads(m.group(1))
    # 取一个非推荐位商品,确认它被归到其所属分类分组下
    row = conn.execute(
        "SELECT id, category_id FROM products WHERE id NOT IN (SELECT product_id FROM banners) LIMIT 1"
    ).fetchone()
    assert row["id"] in [p["id"] for p in grouped[str(row["category_id"])]]

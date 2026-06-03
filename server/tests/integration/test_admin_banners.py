"""后台推荐位管理 集成测试(admin-console 批3)。seed 预置 4 个推荐位。"""


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

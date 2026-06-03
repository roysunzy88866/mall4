"""后台登录 + 限速 + 仪表盘 集成测试(对应 admin-console 批1)。"""


def test_login_success_redirects_to_dashboard(client):
    r = client.post("/admin/login", data={"username": "admin", "password": "test-pass"})
    assert r.status_code in (302, 303)
    assert "/admin" in r.headers["Location"]


def test_login_wrong_password_rejected(client):
    r = client.post("/admin/login", data={"username": "admin", "password": "nope"})
    assert r.status_code == 401
    # 未登录 → 仪表盘重定向
    assert client.get("/admin/").status_code in (302, 303)


def test_login_throttled_after_5_fails(client):
    for _ in range(5):
        client.post("/admin/login", data={"username": "admin", "password": "nope"})
    # 第 6 次即使口令正确也被限速
    r = client.post("/admin/login", data={"username": "admin", "password": "test-pass"})
    assert r.status_code == 429
    assert client.get("/admin/").status_code in (302, 303)  # 仍未登录


def test_unauthed_dashboard_redirects_to_login(client):
    r = client.get("/admin/")
    assert r.status_code in (302, 303)
    assert "/admin/login" in r.headers["Location"]


def test_dashboard_shows_stats(admin_client):
    r = admin_client.get("/admin/")
    assert r.status_code == 200
    html = r.get_data(as_text=True)
    assert "仪表盘" in html
    assert "商品数" in html and "营业额" in html
    # seed 预置了 3 笔挂在 demo-seed-device 的订单 → 最近订单表里能看到
    assert "demo-seed-device" in html

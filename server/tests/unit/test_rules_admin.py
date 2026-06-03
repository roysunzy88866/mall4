"""rules.admin 纯函数单测 —— 必须 100% 覆盖。"""
from server.rules import admin


def test_is_throttled_under_limit():
    assert admin.is_throttled([100.0, 101.0], now=102.0) is False


def test_is_throttled_at_limit():
    assert admin.is_throttled([1, 2, 3, 4, 5], now=5.0) is True


def test_is_throttled_old_attempts_excluded():
    # 5 次但都在 5 分钟窗口外 → 不限速
    assert admin.is_throttled([1, 2, 3, 4, 5], now=1000.0) is False


def test_banner_count_ok():
    assert admin.banner_count_ok(3) is True
    assert admin.banner_count_ok(5) is True
    assert admin.banner_count_ok(2) is False
    assert admin.banner_count_ok(6) is False


def test_allowed_image_ext():
    assert admin.allowed_image_ext("pic.png") is True
    assert admin.allowed_image_ext("a.JPG") is True
    assert admin.allowed_image_ext("noext") is False
    assert admin.allowed_image_ext("evil.txt") is False

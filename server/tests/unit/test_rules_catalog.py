"""rules.catalog 纯函数单测 —— 必须 100% 覆盖(机器闸门)。"""
from server.models.entities import Product
from server.rules import catalog as rules


def _p(pid, created_at, category_id=1, name="商品", desc="描述"):
    return Product(
        id=pid, name=name, price_cents=100, description=desc, stock=0,
        category_id=category_id, sort_order=0, created_at=created_at,
    )


def test_sort_recommended_newest_first():
    out = rules.sort_recommended([
        _p(1, "2026-05-01 10:00:00"),
        _p(2, "2026-05-03 10:00:00"),
        _p(3, "2026-05-02 10:00:00"),
    ])
    assert [p.id for p in out] == [2, 3, 1]


def test_sort_recommended_tiebreak_by_id_desc():
    out = rules.sort_recommended([
        _p(1, "2026-05-01 10:00:00"),
        _p(2, "2026-05-01 10:00:00"),
    ])
    assert [p.id for p in out] == [2, 1]


def test_filter_active_category_products():
    out = rules.filter_active_category_products(
        [_p(1, "t", category_id=1), _p(2, "t", category_id=2)], [1]
    )
    assert [p.id for p in out] == [1]


def test_description_excerpt_short_unchanged():
    assert rules.description_excerpt("短描述") == "短描述"


def test_description_excerpt_none_is_empty():
    assert rules.description_excerpt(None) == ""


def test_description_excerpt_long_truncated_with_ellipsis():
    out = rules.description_excerpt("字" * 50, max_len=10)
    assert out.endswith("…")
    assert len(out) == 11


def test_banner_view_auto_fills_from_product():
    product = _p(7, "t", name="车载充电头", desc="超长描述" * 20)
    bv = rules.banner_view(product, "/uploads/x.png")
    assert bv.title == "车载充电头"
    assert bv.price_cents == 100
    assert bv.product_id == 7
    assert bv.image_url == "/uploads/x.png"
    assert bv.description.endswith("…")

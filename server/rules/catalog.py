"""目录浏览的纯业务规则。无 IO;时间/数据全由参数注入。100% 单测覆盖。
暴露:sort_recommended, filter_active_category_products, description_excerpt, banner_view。"""
from server.models.entities import BannerView, Product

EXCERPT_MAX = 40


def sort_recommended(products: list[Product]) -> list[Product]:
    """「为你推荐」:按上架时间倒序(新品在前);以 (created_at, id) 保证确定性、不跳屏。"""
    return sorted(products, key=lambda p: (p.created_at, p.id), reverse=True)


def filter_active_category_products(
    products: list[Product], active_category_ids: list[int]
) -> list[Product]:
    """剔除停用分类下的商品。"""
    active = set(active_category_ids)
    return [p for p in products if p.category_id in active]


def description_excerpt(text: str | None, max_len: int = EXCERPT_MAX) -> str:
    """描述节选(供 Banner 文案用)。"""
    text = (text or "").strip()
    if len(text) <= max_len:
        return text
    return text[:max_len].rstrip() + "…"


def banner_view(product: Product, image_url: str | None) -> BannerView:
    """Banner 文案自动套商品:标题=名称、价格=价格、描述=描述节选。"""
    return BannerView(
        product_id=product.id,
        title=product.name,
        description=description_excerpt(product.description),
        price_cents=product.price_cents,
        image_url=image_url,
    )

"""数据实体(dataclass)。禁止 dict 渗入 services / rules。"""
from dataclasses import dataclass


@dataclass(frozen=True)
class Category:
    id: int
    name: str
    sort_order: int
    is_active: bool
    created_at: str


@dataclass(frozen=True)
class Product:
    id: int
    name: str
    price_cents: int
    description: str
    stock: int
    category_id: int
    sort_order: int
    created_at: str


@dataclass(frozen=True)
class ProductImage:
    id: int
    product_id: int
    url: str
    sort_order: int


@dataclass(frozen=True)
class Banner:
    id: int
    product_id: int
    sort_order: int


@dataclass(frozen=True)
class BannerView:
    """Banner 展示视图:文案自动套自所引用商品。"""
    product_id: int
    title: str
    description: str
    price_cents: int
    image_url: str | None

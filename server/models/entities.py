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


@dataclass(frozen=True)
class OrderLineInput:
    """车机上送的下单行项(整单快照:名 / 图 / 价 / 数量)。"""
    product_id: int | None
    name: str
    image: str | None
    price_cents: int
    qty: int


@dataclass(frozen=True)
class Order:
    id: int
    device_id: str
    status: str
    total_cents: int
    created_at: str
    clock_base_at: str | None = None
    manual: int = 0
    delivered_at: str | None = None
    return_reason: str | None = None
    return_note: str | None = None
    cancelled_at: str | None = None


@dataclass(frozen=True)
class OrderItem:
    id: int
    order_id: int
    product_id: int | None
    product_name: str
    product_image: str | None
    price_cents: int
    qty: int


@dataclass(frozen=True)
class OrderWithItems:
    order: Order
    items: list[OrderItem]

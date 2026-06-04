"""目录浏览业务编排。依赖:repositories.catalog_repo, rules.catalog。
暴露:home_data, nav_categories, category_products, product_detail, ProductNotFound。"""
from dataclasses import dataclass

from server.models.entities import BannerView, Category, Product, ProductImage
from server.repositories import catalog_repo as repo
from server.rules import catalog as rules


class ProductNotFound(Exception):
    """请求的商品不存在或已删除(接口层翻成 404「商品已下架」)。"""

    def __init__(self, product_id: int):
        super().__init__(f"product {product_id} not found")
        self.product_id = product_id


@dataclass(frozen=True)
class HomeData:
    banners: list[BannerView]
    recommended: list[Product]


@dataclass(frozen=True)
class ProductDetail:
    product: Product
    images: list[ProductImage]


def home_data(conn) -> HomeData:
    active_ids = repo.active_category_ids(conn)
    active_set = set(active_ids)
    in_category = rules.filter_active_category_products(repo.all_products(conn), active_ids)
    visible = rules.filter_listed(in_category)  # 下架商品不进推荐
    recommended = rules.sort_recommended(visible)
    banner_views: list[BannerView] = []
    for b in repo.banners(conn):
        product = repo.product_by_id(conn, b.product_id)
        # 停用分类下(DEBT-001)或已下架的商品都不进 banner
        if product is None or product.category_id not in active_set or not product.is_active:
            continue
        images = repo.images_for_product(conn, product.id)
        main_url = images[0].url if images else None
        banner_views.append(rules.banner_view(product, main_url))
    return HomeData(banners=banner_views, recommended=recommended)


def nav_categories(conn) -> list[Category]:
    return repo.active_categories(conn)


def category_products(conn, category_id: int) -> list[Product]:
    return rules.filter_listed(repo.products_by_category(conn, category_id))  # 下架商品不进分类列表


def main_image_urls(conn, products) -> dict[int, str | None]:
    """每个商品的主图(图集首张)URL,无图为 None。供列表接口给商品卡配图。"""
    result: dict[int, str | None] = {}
    for p in products:
        imgs = repo.images_for_product(conn, p.id)
        result[p.id] = imgs[0].url if imgs else None
    return result


def product_detail(conn, product_id: int) -> ProductDetail:
    product = repo.product_by_id(conn, product_id)
    if product is None or not product.is_active:  # 下架商品对顾客等同查不到
        raise ProductNotFound(product_id)
    return ProductDetail(product=product, images=repo.images_for_product(conn, product_id))

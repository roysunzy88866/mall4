"""后台分类 / 商品写业务。事务边界在此(一次业务 = 一个事务)。抛 domain 异常。"""
from decimal import InvalidOperation

from server.repositories import catalog_write_repo as repo
from server.rules import admin as admin_rules
from server.rules import money


class CategoryNotEmpty(Exception):
    """删除一个仍含商品的分类(接口层翻成提示)。"""


class InvalidPrice(Exception):
    """价格非法(接口层翻成提示)。"""


class ProductDeleteBlocked(Exception):
    """商品已被订单引用,不许永久删除(接口层翻成提示『请改为下架』)。"""


def _to_cents(price_yuan) -> int:
    try:
        return money.yuan_to_cents(price_yuan)
    except (InvalidOperation, ValueError, TypeError):
        raise InvalidPrice()


# ---- 分类 ----
def create_category(conn, name: str, now: str) -> None:
    with conn:
        repo.insert_category(conn, name, repo.max_category_sort(conn) + 1, now)


def rename_category(conn, category_id: int, name: str) -> None:
    with conn:
        repo.update_category_name(conn, category_id, name)


def toggle_category(conn, category_id: int, active: bool) -> None:
    with conn:
        repo.set_category_active(conn, category_id, active)


def move_category(conn, category_id: int, direction: str) -> None:
    cats = repo.all_categories(conn)
    ids = [c.id for c in cats]
    if category_id not in ids:
        return
    i = ids.index(category_id)
    j = i - 1 if direction == "up" else i + 1
    if 0 <= j < len(cats):
        a, b = cats[i], cats[j]
        with conn:
            repo.set_category_sort(conn, a.id, b.sort_order)
            repo.set_category_sort(conn, b.id, a.sort_order)


def delete_category(conn, category_id: int) -> None:
    if repo.count_products_in_category(conn, category_id) > 0:
        raise CategoryNotEmpty()
    with conn:
        repo.delete_category(conn, category_id)


# ---- 商品 ----
def create_product(conn, name, price_yuan, description, stock, category_id, now, image_url=None) -> int:
    price_cents = _to_cents(price_yuan)
    with conn:
        pid = repo.insert_product(
            conn, name, price_cents, description, stock, category_id,
            repo.max_product_sort(conn) + 1, now,
        )
        if image_url:
            repo.insert_product_image(conn, pid, image_url, 0)
    return pid


def update_product(conn, product_id, name, price_yuan, description, stock, category_id, image_url=None) -> None:
    price_cents = _to_cents(price_yuan)
    with conn:
        repo.update_product(conn, product_id, name, price_cents, description, stock, category_id)
        if image_url:  # 传了新图 = 替换主图(覆盖旧图);未传则保持原图
            repo.delete_product_images(conn, product_id)
            repo.insert_product_image(conn, product_id, image_url, 0)


def toggle_product_active(conn, product_id: int, active: bool) -> None:
    with conn:
        repo.set_product_active(conn, product_id, active)


def delete_product(conn, product_id: int) -> None:
    # 受限永久删除:被订单引用过的商品不许硬删,改走下架(保护历史快照与可逆性)
    if repo.product_has_order_refs(conn, product_id):
        raise ProductDeleteBlocked()
    with conn:
        repo.delete_product(conn, product_id)


# ---- 推荐位 ----
class BannerLimit(Exception):
    """推荐位数量 / 重复约束(接口层翻成提示)。"""

    def __init__(self, msg: str):
        super().__init__(msg)
        self.msg = msg


def add_banner(conn, product_id: int) -> None:
    if repo.is_product_banner(conn, product_id):
        raise BannerLimit("该商品已在推荐位")
    if not admin_rules.banner_count_ok(repo.banner_count(conn) + 1):
        raise BannerLimit("推荐位最多 5 个")
    with conn:
        repo.insert_banner(conn, product_id, repo.max_banner_sort(conn) + 1)


def remove_banner(conn, banner_id: int) -> None:
    if not admin_rules.banner_count_ok(repo.banner_count(conn) - 1):
        raise BannerLimit("推荐位最少 3 个")
    with conn:
        repo.delete_banner(conn, banner_id)


def move_banner(conn, banner_id: int, direction: str) -> None:
    bs = repo.all_banners(conn)
    ids = [b.id for b in bs]
    if banner_id not in ids:
        return
    i = ids.index(banner_id)
    j = i - 1 if direction == "up" else i + 1
    if 0 <= j < len(bs):
        a, b = bs[i], bs[j]
        with conn:
            repo.set_banner_sort(conn, a.id, b.sort_order)
            repo.set_banner_sort(conn, b.id, a.sort_order)

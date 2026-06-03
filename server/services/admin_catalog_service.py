"""后台分类 / 商品写业务。事务边界在此(一次业务 = 一个事务)。抛 domain 异常。"""
from decimal import InvalidOperation

from server.repositories import catalog_write_repo as repo
from server.rules import money


class CategoryNotEmpty(Exception):
    """删除一个仍含商品的分类(接口层翻成提示)。"""


class InvalidPrice(Exception):
    """价格非法(接口层翻成提示)。"""


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


def update_product(conn, product_id, name, price_yuan, description, stock, category_id) -> None:
    price_cents = _to_cents(price_yuan)
    with conn:
        repo.update_product(conn, product_id, name, price_cents, description, stock, category_id)


def delete_product(conn, product_id: int) -> None:
    with conn:
        repo.delete_product(conn, product_id)

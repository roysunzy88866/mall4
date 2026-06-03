"""分类 / 商品写操作 + 后台列表查询。事务由 service 管,不自行 commit/rollback。"""
from server.models.entities import Banner, Category, Product


def _category(row) -> Category:
    return Category(
        id=row["id"], name=row["name"], sort_order=row["sort_order"],
        is_active=bool(row["is_active"]), created_at=row["created_at"],
    )


def _product(row) -> Product:
    return Product(
        id=row["id"], name=row["name"], price_cents=row["price_cents"],
        description=row["description"], stock=row["stock"],
        category_id=row["category_id"], sort_order=row["sort_order"],
        created_at=row["created_at"],
    )


# ---- 分类 ----
def all_categories(conn) -> list[Category]:
    rows = conn.execute("SELECT * FROM categories ORDER BY sort_order, id").fetchall()
    return [_category(r) for r in rows]


def count_products_in_category(conn, category_id: int) -> int:
    return conn.execute(
        "SELECT COUNT(*) AS c FROM products WHERE category_id=?", (category_id,)
    ).fetchone()["c"]


def max_category_sort(conn) -> int:
    return conn.execute("SELECT COALESCE(MAX(sort_order), 0) AS m FROM categories").fetchone()["m"]


def insert_category(conn, name: str, sort_order: int, created_at: str) -> int:
    cur = conn.execute(
        "INSERT INTO categories (name, sort_order, is_active, created_at) VALUES (?,?,1,?)",
        (name, sort_order, created_at),
    )
    return cur.lastrowid


def update_category_name(conn, category_id: int, name: str) -> None:
    conn.execute("UPDATE categories SET name=? WHERE id=?", (name, category_id))


def set_category_active(conn, category_id: int, active: bool) -> None:
    conn.execute("UPDATE categories SET is_active=? WHERE id=?", (1 if active else 0, category_id))


def set_category_sort(conn, category_id: int, sort_order: int) -> None:
    conn.execute("UPDATE categories SET sort_order=? WHERE id=?", (sort_order, category_id))


def delete_category(conn, category_id: int) -> None:
    conn.execute("DELETE FROM categories WHERE id=?", (category_id,))


# ---- 商品 ----
def all_products_admin(conn) -> list[Product]:
    rows = conn.execute("SELECT * FROM products ORDER BY category_id, sort_order, id").fetchall()
    return [_product(r) for r in rows]


def max_product_sort(conn) -> int:
    return conn.execute("SELECT COALESCE(MAX(sort_order), 0) AS m FROM products").fetchone()["m"]


def insert_product(conn, name, price_cents, description, stock, category_id, sort_order, created_at) -> int:
    cur = conn.execute(
        "INSERT INTO products (name, price_cents, description, stock, category_id, sort_order, created_at) "
        "VALUES (?,?,?,?,?,?,?)",
        (name, price_cents, description, stock, category_id, sort_order, created_at),
    )
    return cur.lastrowid


def update_product(conn, product_id, name, price_cents, description, stock, category_id) -> None:
    conn.execute(
        "UPDATE products SET name=?, price_cents=?, description=?, stock=?, category_id=? WHERE id=?",
        (name, price_cents, description, stock, category_id, product_id),
    )


def insert_product_image(conn, product_id: int, url: str, sort_order: int) -> None:
    conn.execute(
        "INSERT INTO product_images (product_id, url, sort_order) VALUES (?,?,?)",
        (product_id, url, sort_order),
    )


def delete_product(conn, product_id: int) -> None:
    # D8:订单整单快照独立,删商品时把订单行的引用置空(快照名/图/价保留),并清图与推荐位
    conn.execute("UPDATE order_items SET product_id=NULL WHERE product_id=?", (product_id,))
    conn.execute("DELETE FROM banners WHERE product_id=?", (product_id,))
    conn.execute("DELETE FROM product_images WHERE product_id=?", (product_id,))
    conn.execute("DELETE FROM products WHERE id=?", (product_id,))


# ---- 推荐位 ----
def all_banners(conn) -> list[Banner]:
    rows = conn.execute("SELECT * FROM banners ORDER BY sort_order, id").fetchall()
    return [Banner(id=r["id"], product_id=r["product_id"], sort_order=r["sort_order"]) for r in rows]


def banner_count(conn) -> int:
    return conn.execute("SELECT COUNT(*) AS c FROM banners").fetchone()["c"]


def is_product_banner(conn, product_id: int) -> bool:
    return conn.execute("SELECT 1 FROM banners WHERE product_id=?", (product_id,)).fetchone() is not None


def max_banner_sort(conn) -> int:
    return conn.execute("SELECT COALESCE(MAX(sort_order), 0) AS m FROM banners").fetchone()["m"]


def insert_banner(conn, product_id: int, sort_order: int) -> int:
    cur = conn.execute("INSERT INTO banners (product_id, sort_order) VALUES (?,?)", (product_id, sort_order))
    return cur.lastrowid


def delete_banner(conn, banner_id: int) -> None:
    conn.execute("DELETE FROM banners WHERE id=?", (banner_id,))


def set_banner_sort(conn, banner_id: int, sort_order: int) -> None:
    conn.execute("UPDATE banners SET sort_order=? WHERE id=?", (sort_order, banner_id))

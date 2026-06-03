"""目录数据读取。依赖:models.entities。暴露:分类/商品/图/推荐位的只读查询。"""
from server.models.entities import Banner, Category, Product, ProductImage


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


def _image(row) -> ProductImage:
    return ProductImage(id=row["id"], product_id=row["product_id"], url=row["url"], sort_order=row["sort_order"])


def _banner(row) -> Banner:
    return Banner(id=row["id"], product_id=row["product_id"], sort_order=row["sort_order"])


def active_categories(conn) -> list[Category]:
    rows = conn.execute("SELECT * FROM categories WHERE is_active=1 ORDER BY sort_order, id").fetchall()
    return [_category(r) for r in rows]


def active_category_ids(conn) -> list[int]:
    rows = conn.execute("SELECT id FROM categories WHERE is_active=1").fetchall()
    return [r["id"] for r in rows]


def all_products(conn) -> list[Product]:
    rows = conn.execute("SELECT * FROM products ORDER BY id").fetchall()
    return [_product(r) for r in rows]


def products_by_category(conn, category_id: int) -> list[Product]:
    rows = conn.execute(
        "SELECT * FROM products WHERE category_id=? ORDER BY sort_order, id", (category_id,)
    ).fetchall()
    return [_product(r) for r in rows]


def product_by_id(conn, product_id: int) -> Product | None:
    row = conn.execute("SELECT * FROM products WHERE id=?", (product_id,)).fetchone()
    return _product(row) if row else None


def images_for_product(conn, product_id: int) -> list[ProductImage]:
    rows = conn.execute(
        "SELECT * FROM product_images WHERE product_id=? ORDER BY sort_order, id", (product_id,)
    ).fetchall()
    return [_image(r) for r in rows]


def banners(conn) -> list[Banner]:
    rows = conn.execute("SELECT * FROM banners ORDER BY sort_order, id").fetchall()
    return [_banner(r) for r in rows]

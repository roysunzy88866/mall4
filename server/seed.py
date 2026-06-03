"""首次启动 seed(库空时预置,幂等)。依赖:data.seed_data。
预置 5 分类 + 19 商品 + 占位图 + 4 推荐位 + 3 笔演示订单(挂演示设备号)。"""
import base64
import os

from server.data.seed_data import BANNER_PIDS, CATEGORIES, PRODUCTS, SEED_ORDERS
from server.rules.money import yuan_to_cents

# 1x1 透明 PNG 占位图(真实商品图后续替换;此处仅让图片 URL 能解析到一个文件)
_PLACEHOLDER_PNG = base64.b64decode(
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAC0lEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
)


def _ensure_placeholder(uploads_dir: str) -> str:
    os.makedirs(uploads_dir, exist_ok=True)
    path = os.path.join(uploads_dir, "placeholder.png")
    if not os.path.exists(path):
        with open(path, "wb") as f:
            f.write(_PLACEHOLDER_PNG)
    return "/uploads/placeholder.png"


def _last_id(conn) -> int:
    return conn.execute("SELECT last_insert_rowid() AS id").fetchone()["id"]


def seed_if_empty(conn, uploads_dir: str) -> None:
    if conn.execute("SELECT COUNT(*) AS c FROM products").fetchone()["c"] > 0:
        return  # 幂等:已有数据则不重复 seed

    placeholder = _ensure_placeholder(uploads_dir)

    cat_id: dict[str, int] = {}
    for i, (key, name) in enumerate(CATEGORIES, start=1):
        conn.execute(
            "INSERT INTO categories (name, sort_order, is_active, created_at) VALUES (?,?,1,?)",
            (name, i, f"2026-05-01 09:{i:02d}:00"),
        )
        cat_id[key] = _last_id(conn)

    pid: dict[str, int] = {}
    for idx, (pkey, name, price_yuan, catkey, desc) in enumerate(PRODUCTS):
        created = f"2026-05-{10 + idx // 6:02d} {10 + idx % 6:02d}:00:00"
        conn.execute(
            "INSERT INTO products (name, price_cents, description, stock, category_id, sort_order, created_at) "
            "VALUES (?,?,?,?,?,?,?)",
            (name, yuan_to_cents(price_yuan), desc, 99, cat_id[catkey], idx, created),
        )
        pid[pkey] = _last_id(conn)
        conn.execute(
            "INSERT INTO product_images (product_id, url, sort_order) VALUES (?,?,0)",
            (pid[pkey], placeholder),
        )

    for i, pkey in enumerate(BANNER_PIDS, start=1):
        conn.execute("INSERT INTO banners (product_id, sort_order) VALUES (?,?)", (pid[pkey], i))

    # 预置订单铺开各阶段(manual=1 定格,让后台一开就有层次,不被时间推到全已签收)
    _seed_statuses = ["paid", "shipping", "delivering", "delivered", "delivered"]
    for idx, (pkey, qty, when) in enumerate(SEED_ORDERS):
        row = conn.execute("SELECT name, price_cents FROM products WHERE id=?", (pid[pkey],)).fetchone()
        st = _seed_statuses[idx % len(_seed_statuses)]
        delivered_at = when if st == "delivered" else None
        conn.execute(
            "INSERT INTO orders (device_id, status, total_cents, created_at, clock_base_at, manual, delivered_at) "
            "VALUES (?,?,?,?,?,1,?)",
            ("demo-seed-device", st, row["price_cents"] * qty, when, when, delivered_at),
        )
        oid = _last_id(conn)
        conn.execute(
            "INSERT INTO order_items (order_id, product_id, product_name, product_image, price_cents, qty) "
            "VALUES (?,?,?,?,?,?)",
            (oid, pid[pkey], row["name"], placeholder, row["price_cents"], qty),
        )

    conn.commit()

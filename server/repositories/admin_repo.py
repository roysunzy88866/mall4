"""后台只读统计与全量订单查询。映射成实体;不含业务判断。"""
from server.models.entities import Order


def _order(row) -> Order:
    return Order(
        id=row["id"], device_id=row["device_id"], status=row["status"],
        total_cents=row["total_cents"], created_at=row["created_at"],
    )


def product_count(conn) -> int:
    return conn.execute("SELECT COUNT(*) AS c FROM products").fetchone()["c"]


def order_count(conn) -> int:
    return conn.execute("SELECT COUNT(*) AS c FROM orders").fetchone()["c"]


def revenue_cents(conn) -> int:
    # 营业额 = 已支付订单合计,扣除已退款/已取消(共识 A13)。
    return conn.execute(
        "SELECT COALESCE(SUM(total_cents), 0) AS s FROM orders "
        "WHERE status NOT IN ('cancelled', 'refunded')"
    ).fetchone()["s"]


def recent_orders(conn, limit: int = 5) -> list[Order]:
    rows = conn.execute(
        "SELECT * FROM orders ORDER BY created_at DESC, id DESC LIMIT ?", (limit,)
    ).fetchall()
    return [_order(r) for r in rows]


def all_orders(conn) -> list[Order]:
    rows = conn.execute("SELECT * FROM orders ORDER BY created_at DESC, id DESC").fetchall()
    return [_order(r) for r in rows]

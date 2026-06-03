"""订单数据读写。映射成实体;事务由业务层管,不自行 commit/rollback。"""
from server.models.entities import Order, OrderItem, OrderLineInput


def _order(row) -> Order:
    keys = row.keys()
    return Order(
        id=row["id"], device_id=row["device_id"], status=row["status"],
        total_cents=row["total_cents"], created_at=row["created_at"],
        clock_base_at=row["clock_base_at"] if "clock_base_at" in keys else None,
        manual=row["manual"] if "manual" in keys else 0,
        delivered_at=row["delivered_at"] if "delivered_at" in keys else None,
    )


def _item(row) -> OrderItem:
    return OrderItem(
        id=row["id"], order_id=row["order_id"], product_id=row["product_id"],
        product_name=row["product_name"], product_image=row["product_image"],
        price_cents=row["price_cents"], qty=row["qty"],
    )


def insert_order(conn, device_id: str, total_cents: int, created_at: str) -> int:
    cur = conn.execute(
        "INSERT INTO orders (device_id, status, total_cents, created_at, clock_base_at, manual) "
        "VALUES (?, 'paid', ?, ?, ?, 0)",
        (device_id, total_cents, created_at, created_at),
    )
    return cur.lastrowid


def insert_order_item(conn, order_id: int, line: OrderLineInput) -> None:
    conn.execute(
        "INSERT INTO order_items (order_id, product_id, product_name, product_image, price_cents, qty) "
        "VALUES (?,?,?,?,?,?)",
        (order_id, line.product_id, line.name, line.image, line.price_cents, line.qty),
    )


def order_by_id(conn, order_id: int) -> Order | None:
    row = conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()
    return _order(row) if row else None


def orders_by_device(conn, device_id: str) -> list[Order]:
    rows = conn.execute(
        "SELECT * FROM orders WHERE device_id=? ORDER BY created_at DESC, id DESC", (device_id,)
    ).fetchall()
    return [_order(r) for r in rows]


def items_for_order(conn, order_id: int) -> list[OrderItem]:
    rows = conn.execute("SELECT * FROM order_items WHERE order_id=? ORDER BY id", (order_id,)).fetchall()
    return [_item(r) for r in rows]


def all_orders(conn) -> list[Order]:
    """后台:全部订单倒序。"""
    rows = conn.execute("SELECT * FROM orders ORDER BY created_at DESC, id DESC").fetchall()
    return [_order(r) for r in rows]


def update_status(conn, order_id: int, status: str, manual: int | None = None,
                  delivered_at: str | None = None) -> None:
    sets, params = ["status=?"], [status]
    if manual is not None:
        sets.append("manual=?"); params.append(manual)
    if delivered_at is not None:
        sets.append("delivered_at=?"); params.append(delivered_at)
    params.append(order_id)
    conn.execute(f"UPDATE orders SET {', '.join(sets)} WHERE id=?", params)


def set_clock_base(conn, order_id: int, clock_base_at: str) -> None:
    conn.execute("UPDATE orders SET clock_base_at=? WHERE id=?", (clock_base_at, order_id))

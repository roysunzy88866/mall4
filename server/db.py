"""SQLite 连接与建表。依赖:sqlite3, models.schema。暴露:connect, init_schema。
不含业务判断。"""
import sqlite3

from server.models.schema import SCHEMA_SQL


def connect(db_path: str) -> sqlite3.Connection:
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON")
    return conn


# 订单生命周期(order-lifecycle)新增列;老库平滑补列。
_ORDER_MIGRATIONS = {
    "clock_base_at": "ALTER TABLE orders ADD COLUMN clock_base_at TEXT",
    "manual": "ALTER TABLE orders ADD COLUMN manual INTEGER NOT NULL DEFAULT 0",
    "delivered_at": "ALTER TABLE orders ADD COLUMN delivered_at TEXT",
    "return_reason": "ALTER TABLE orders ADD COLUMN return_reason TEXT",
    "return_note": "ALTER TABLE orders ADD COLUMN return_note TEXT",
    "cancelled_at": "ALTER TABLE orders ADD COLUMN cancelled_at TEXT",
}


def _migrate(conn: sqlite3.Connection) -> None:
    cols = {r["name"] for r in conn.execute("PRAGMA table_info(orders)").fetchall()}
    for col, ddl in _ORDER_MIGRATIONS.items():
        if col not in cols:
            conn.execute(ddl)
    # clock_base_at 默认回填为 created_at(老订单)
    conn.execute(
        "UPDATE orders SET clock_base_at = created_at WHERE clock_base_at IS NULL"
    )


def init_schema(conn: sqlite3.Connection) -> None:
    conn.executescript(SCHEMA_SQL)
    _migrate(conn)
    conn.commit()

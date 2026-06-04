"""迁移:老库(products 无 is_active)平滑补列,默认在架=1,且幂等可重入。"""
import sqlite3

from server import db


def _make_old_db(path: str) -> None:
    """造一个『没有 is_active 列』的老 products 库 + 一个存量商品。"""
    conn = sqlite3.connect(path)
    conn.executescript(
        """
        CREATE TABLE categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL,
          sort_order INTEGER NOT NULL DEFAULT 0, is_active INTEGER NOT NULL DEFAULT 1, created_at TEXT NOT NULL);
        CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL,
          price_cents INTEGER NOT NULL, description TEXT NOT NULL DEFAULT '', stock INTEGER NOT NULL DEFAULT 0,
          category_id INTEGER NOT NULL, sort_order INTEGER NOT NULL DEFAULT 0, created_at TEXT NOT NULL);
        INSERT INTO categories (name, created_at) VALUES ('c', '2026-01-01');
        INSERT INTO products (name, price_cents, category_id, created_at) VALUES ('老商品', 100, 1, '2026-01-01');
        """
    )
    conn.commit()
    conn.close()


def test_migrate_adds_is_active_defaulting_listed(tmp_path):
    path = str(tmp_path / "old.db")
    _make_old_db(path)

    conn = db.connect(path)
    db.init_schema(conn)  # CREATE IF NOT EXISTS 不动既有表 + _migrate 补列

    cols = {r["name"] for r in conn.execute("PRAGMA table_info(products)").fetchall()}
    assert "is_active" in cols
    row = conn.execute("SELECT is_active FROM products WHERE name='老商品'").fetchone()
    assert row["is_active"] == 1  # 存量商品默认在架,不受影响

    db.init_schema(conn)  # 幂等:再跑一次不报错、不重复加列
    conn.close()

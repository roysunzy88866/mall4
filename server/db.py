"""SQLite 连接与建表。依赖:sqlite3, models.schema。暴露:connect, init_schema。
不含业务判断。"""
import sqlite3

from server.models.schema import SCHEMA_SQL


def connect(db_path: str) -> sqlite3.Connection:
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON")
    return conn


def init_schema(conn: sqlite3.Connection) -> None:
    conn.executescript(SCHEMA_SQL)
    conn.commit()

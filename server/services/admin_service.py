"""后台业务:仪表盘统计。依赖:repositories.admin_repo。"""
from dataclasses import dataclass

from server.models.entities import Order
from server.repositories import admin_repo as repo


@dataclass(frozen=True)
class DashboardStats:
    product_count: int
    order_count: int
    revenue_cents: int
    recent_orders: list[Order]


def dashboard_stats(conn) -> DashboardStats:
    return DashboardStats(
        product_count=repo.product_count(conn),
        order_count=repo.order_count(conn),
        revenue_cents=repo.revenue_cents(conn),
        recent_orders=repo.recent_orders(conn, 5),
    )

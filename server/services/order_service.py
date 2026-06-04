"""下单业务编排。事务边界在此(一次业务 = 一个事务)。抛 domain 异常。
暴露:place_order, list_orders, InvalidOrder, ProductUnavailable。"""
from server.models.entities import OrderLineInput, OrderWithItems
from server.repositories import catalog_repo, orders_repo as repo
from server.rules import ordering as rules


class InvalidOrder(Exception):
    """下单入参非法(接口层翻成 400)。"""

    def __init__(self, errors: list[str]):
        super().__init__("; ".join(errors))
        self.errors = errors


class ProductUnavailable(Exception):
    """整单含不存在/已下架的商品(接口层翻成 409「商品已下架」)。"""

    def __init__(self, items: list[OrderLineInput]):
        super().__init__("商品已下架")
        self.items = items


def place_order(conn, device_id: str | None, items: list[OrderLineInput], now: str) -> OrderWithItems:
    errors = rules.order_input_errors(device_id, items)
    if errors:
        raise InvalidOrder(errors)
    # 落库前校验:每件商品存在且在架(只查存在/状态,不重取现价)
    available = catalog_repo.listed_product_ids(conn, [it.product_id for it in items])
    unavailable = rules.unavailable_items(items, available)
    if unavailable:
        raise ProductUnavailable(unavailable)
    total = rules.order_total(items)
    with conn:  # 单事务:成功提交,异常回滚(不留半单)
        order_id = repo.insert_order(conn, device_id, total, now)
        for line in items:
            repo.insert_order_item(conn, order_id, line)
    order = repo.order_by_id(conn, order_id)
    return OrderWithItems(order=order, items=repo.items_for_order(conn, order_id))


def list_orders(conn, device_id: str) -> list[OrderWithItems]:
    return [
        OrderWithItems(order=o, items=repo.items_for_order(conn, o.id))
        for o in repo.orders_by_device(conn, device_id)
    ]

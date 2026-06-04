"""下单的纯业务规则。无 IO。100% 单测覆盖。
暴露:order_total, order_input_errors。"""
from server.models.entities import OrderLineInput


def order_total(items: list[OrderLineInput]) -> int:
    """订单合计(分)= Σ(快照价 × 数量)。"""
    return sum(it.price_cents * it.qty for it in items)


def unavailable_items(
    items: list[OrderLineInput], available_ids: set[int]
) -> list[OrderLineInput]:
    """挑出不可用的行项:商品不存在或已下架(其 product_id 不在『存在且在架』集合里)。"""
    return [it for it in items if it.product_id not in available_ids]


def order_input_errors(device_id: str | None, items: list[OrderLineInput]) -> list[str]:
    """下单入参校验(纯函数);返回问题列表,空 = 合法。"""
    errors: list[str] = []
    if not device_id:
        errors.append("缺少设备号")
    if not items:
        errors.append("订单为空")
    for it in items:
        if not it.name:
            errors.append("商品缺名称")
        if not isinstance(it.price_cents, int) or it.price_cents < 0:
            errors.append("商品价格非法")
        if not isinstance(it.qty, int) or it.qty < 1:
            errors.append("数量非法")
    return errors

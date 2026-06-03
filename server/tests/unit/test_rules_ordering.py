"""rules.ordering 纯函数单测 —— 必须 100% 覆盖(机器闸门)。"""
from server.models.entities import OrderLineInput
from server.rules import ordering as rules


def _line(name="商品", price_cents=3900, qty=1, product_id=1, image="/u/x.png"):
    return OrderLineInput(product_id=product_id, name=name, image=image, price_cents=price_cents, qty=qty)


def test_order_total():
    assert rules.order_total([_line(price_cents=3900, qty=2), _line(price_cents=100, qty=3)]) == 8100


def test_order_input_errors_valid_is_empty():
    assert rules.order_input_errors("dev-1", [_line()]) == []


def test_order_input_errors_missing_device():
    assert "缺少设备号" in rules.order_input_errors(None, [_line()])


def test_order_input_errors_empty_items():
    assert "订单为空" in rules.order_input_errors("dev-1", [])


def test_order_input_errors_missing_name():
    assert "商品缺名称" in rules.order_input_errors("dev-1", [_line(name="")])


def test_order_input_errors_bad_price():
    assert "商品价格非法" in rules.order_input_errors("dev-1", [_line(price_cents=None)])
    assert "商品价格非法" in rules.order_input_errors("dev-1", [_line(price_cents=-1)])


def test_order_input_errors_bad_qty():
    assert "数量非法" in rules.order_input_errors("dev-1", [_line(qty=0)])
    assert "数量非法" in rules.order_input_errors("dev-1", [_line(qty=None)])

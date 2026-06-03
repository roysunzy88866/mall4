"""rules.money 纯函数单测 —— 必须 100% 覆盖(闭合 DEBT-003)。"""
from server.rules import money


def test_yuan_to_cents_basic():
    assert money.yuan_to_cents("39.00") == 3900
    assert money.yuan_to_cents(19.95) == 1995


def test_yuan_to_cents_half_up_no_float_error():
    # 老的 round(元*100) 在这两个会偏;Decimal + HALF_UP 不偏
    assert money.yuan_to_cents("2.675") == 268
    assert money.yuan_to_cents("0.005") == 1


def test_cents_to_yuan():
    assert money.cents_to_yuan(1995) == "19.95"
    assert money.cents_to_yuan(0) == "0.00"
    assert money.cents_to_yuan(45900) == "459.00"

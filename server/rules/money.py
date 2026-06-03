"""价格元↔分换算(纯函数,用 Decimal 防浮点误差)。100% 单测。闭合 DEBT-003。"""
from decimal import ROUND_HALF_UP, Decimal


def yuan_to_cents(yuan) -> int:
    """元(数字或字符串)→ 分,四舍五入到分,无浮点误差。如 '19.95' → 1995。"""
    return int((Decimal(str(yuan)) * 100).quantize(Decimal("1"), rounding=ROUND_HALF_UP))


def cents_to_yuan(cents: int) -> str:
    """分 → 元的两位小数字符串。如 1995 → '19.95'。"""
    return f"{Decimal(cents) / 100:.2f}"

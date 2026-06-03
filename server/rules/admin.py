"""后台纯业务规则(无 IO)。100% 单测。"""


def is_throttled(fail_times: list[float], now: float, window_sec: int = 300, max_fails: int = 5) -> bool:
    """同一来源在时间窗内失败次数达上限 → 限速。fail_times/now 为 epoch 秒。"""
    recent = [t for t in fail_times if now - t < window_sec]
    return len(recent) >= max_fails


def banner_count_ok(count: int) -> bool:
    """推荐位数量必须 3~5(F4a)。"""
    return 3 <= count <= 5

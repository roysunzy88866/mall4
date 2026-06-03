"""订单状态机纯规则。无 IO,100% 单测。
阶段 paid(待发货)→ shipping(运输中)→ delivering(派送中)→ delivered(已签收)。
datetime 解析/落库不在此(交 service);此处只算阶段与物流节点的「到达与否」。"""

STAGES = ["paid", "shipping", "delivering", "delivered"]
STAGE_LABELS = {
    "paid": "待发货",
    "shipping": "运输中",
    "delivering": "派送中",
    "delivered": "已签收",
}
# 物流节点(序号 i = 到达阶段 i 时点亮)
LOGISTICS_LABELS = ["已下单", "已发货", "派送中", "已签收"]


def stage_index(status: str) -> int:
    """主阶段序(0~3);非主阶段(取消/退货等分支)返回 -1。"""
    return STAGES.index(status) if status in STAGES else -1


def stage_for_elapsed(elapsed_seconds: int, step_seconds: int) -> int:
    """按已过秒数算应处阶段序,夹在 0~3。step<=0 视为不推进(返 0)。"""
    if step_seconds <= 0:
        return 0
    idx = int(elapsed_seconds) // int(step_seconds)
    return max(0, min(3, idx))


def next_stage(status: str) -> str:
    """下一主阶段;已签收/非主阶段原样返回。"""
    i = stage_index(status)
    if i < 0 or i >= 3:
        return status
    return STAGES[i + 1]


def is_auto(status: str) -> bool:
    """是否仍可自动前进的主阶段(paid/shipping/delivering)。"""
    i = stage_index(status)
    return 0 <= i < 3


def logistics_nodes(status: str) -> list[dict]:
    """物流节点的标签 + 是否已到达(已到达 = 当前阶段序 ≥ 节点序)。
    时间由 service 按推进基准 + 序×步长补上。分支状态(idx<0)按未发货处理。"""
    idx = stage_index(status)
    if idx < 0:
        idx = 0
    return [{"label": LOGISTICS_LABELS[i], "reached": idx >= i} for i in range(4)]

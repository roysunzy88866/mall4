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

# 分支态(取消 / 退货,第四刀)
BRANCH_LABELS = {
    "cancelled": "已取消",
    "return_review": "退货审核中",
    "returning": "退货中",
    "refunded": "已退款",
    "return_rejected": "退货被拒",
}


def status_label(status: str) -> str:
    """任意状态 → 中文(主阶段 + 分支态)。"""
    return STAGE_LABELS.get(status) or BRANCH_LABELS.get(status) or status


def can_cancel(status: str) -> bool:
    """签收前(主阶段 0~2)可取消;已签收及分支态不可。"""
    i = stage_index(status)
    return 0 <= i < 3


def can_request_return(status: str, delivered_elapsed_seconds: int, window_seconds: int) -> bool:
    """已签收且距签收在退货窗口内可申请退货(含端点)。"""
    if status != "delivered":
        return False
    return 0 <= int(delivered_elapsed_seconds) <= int(window_seconds)


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
    时间由 service 按推进基准 + 序×步长补上。
    分支态:退货类曾已签收 → 物流全程到达;取消类途中 → 仅「已下单」。"""
    idx = stage_index(status)
    if status in ("return_review", "returning", "refunded", "return_rejected"):
        idx = 3  # 退货发生在签收后,物流早已走完
    elif idx < 0:
        idx = 0
    return [{"label": LOGISTICS_LABELS[i], "reached": idx >= i} for i in range(4)]

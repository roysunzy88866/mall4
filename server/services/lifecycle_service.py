"""订单生命周期编排:惰性自动推进 + 管理员干预 + 物流时间线。
时间解析/落库在此(rules.lifecycle 是纯函数)。事务:推进/操作后 commit。"""
from datetime import datetime, timedelta

from server.models.entities import OrderWithItems
from server.repositories import orders_repo as repo
from server.rules import lifecycle as lc

_FMT = "%Y-%m-%d %H:%M:%S"


def _parse(ts: str) -> datetime:
    return datetime.strptime(ts, _FMT)


def _fmt(dt: datetime) -> str:
    return dt.strftime(_FMT)


def _advance(conn, order, now: str, step: int):
    """惰性推进:未接管且仍可自动的订单,按 now-clock_base 推到对应阶段并落库。返回(可能更新后的)order。"""
    if order.manual or not lc.is_auto(order.status):
        return order
    base = _parse(order.clock_base_at or order.created_at)
    elapsed = int((_parse(now) - base).total_seconds())
    target = lc.stage_for_elapsed(elapsed, step)
    cur = lc.stage_index(order.status)
    if target <= cur:
        return order
    new_status = lc.STAGES[target]
    delivered_at = now if new_status == "delivered" else None
    repo.update_status(conn, order.id, new_status, delivered_at=delivered_at)
    return repo.order_by_id(conn, order.id)


def _logistics(order, step: int) -> list[dict]:
    """物流时间线:节点标签 + 是否到达 + 推导时间(clock_base + 序×step)。"""
    base = _parse(order.clock_base_at or order.created_at)
    nodes = lc.logistics_nodes(order.status)
    out = []
    for i, n in enumerate(nodes):
        out.append({
            "label": n["label"],
            "reached": n["reached"],
            "at": _fmt(base + timedelta(seconds=i * step)) if n["reached"] else None,
        })
    return out


def _with_items(conn, order) -> OrderWithItems:
    return OrderWithItems(order=order, items=repo.items_for_order(conn, order.id))


# ---- 车机 ----
def list_orders(conn, device_id: str, now: str, step: int) -> list[tuple[OrderWithItems, list[dict]]]:
    out = []
    for o in repo.orders_by_device(conn, device_id):
        o = _advance(conn, o, now, step)
        out.append((_with_items(conn, o), _logistics(o, step)))
    conn.commit()
    return out


def order_detail(conn, order_id: int, now: str, step: int):
    o = repo.order_by_id(conn, order_id)
    if o is None:
        return None
    o = _advance(conn, o, now, step)
    conn.commit()
    return _with_items(conn, o), _logistics(o, step)


# ---- 后台 ----
def admin_list(conn, now: str, step: int, status_filter: str | None = None):
    out = []
    for o in repo.all_orders(conn):
        o = _advance(conn, o, now, step)
        if status_filter and o.status != status_filter:
            continue
        out.append((_with_items(conn, o), _logistics(o, step)))
    conn.commit()
    return out


def admin_advance_one(conn, order_id: int, step: int) -> None:
    """快进一步:推进基准回拨一个步长(下次读取自动前进一阶,仍自动)。"""
    o = repo.order_by_id(conn, order_id)
    if o is None or o.manual or not lc.is_auto(o.status):
        return
    base = _parse(o.clock_base_at or o.created_at)
    repo.set_clock_base(conn, order_id, _fmt(base - timedelta(seconds=step)))
    conn.commit()


def admin_advance_delivered(conn, order_id: int, now: str, step: int) -> None:
    """快进到已签收:直接置 delivered(接管),对任意主阶段都生效;分支态(取消/退货)不动。"""
    o = repo.order_by_id(conn, order_id)
    if o is None or lc.stage_index(o.status) < 0:
        return
    repo.update_status(conn, order_id, "delivered", manual=1, delivered_at=now)
    conn.commit()


def admin_set_status(conn, order_id: int, status: str, now: str) -> None:
    """直接改状态 = 接管(manual=1,停自动);到已签收记 delivered_at。"""
    delivered_at = now if status == "delivered" else None
    repo.update_status(conn, order_id, status, manual=1, delivered_at=delivered_at)
    conn.commit()


# ---- 取消 / 退货(第四刀) ----
class OrderActionError(Exception):
    """取消/退货动作非法。code: 'not_found'/'forbidden'/'conflict'/'bad_request'。"""

    def __init__(self, code: str, msg: str):
        super().__init__(msg)
        self.code = code
        self.msg = msg


def _return_elapsed(order, now: str) -> int:
    if not order.delivered_at:
        return -1
    return int((_parse(now) - _parse(order.delivered_at)).total_seconds())


def can_cancel(order) -> bool:
    return lc.can_cancel(order.status)


def can_return(order, now: str, window: int) -> bool:
    return lc.can_request_return(order.status, _return_elapsed(order, now), window)


def _fresh(conn, order_id, now, step):
    o = repo.order_by_id(conn, order_id)
    if o is None:
        raise OrderActionError("not_found", "订单不存在")
    return _advance(conn, o, now, step)


def cancel_order(conn, order_id: int, device_id: str | None, now: str, step: int) -> None:
    o = _fresh(conn, order_id, now, step)
    if o.device_id != device_id:
        raise OrderActionError("forbidden", "无权操作该订单")
    if not lc.can_cancel(o.status):
        raise OrderActionError("conflict", "该订单已签收或不可取消")
    repo.set_cancelled(conn, order_id, now)
    conn.commit()


def request_return(conn, order_id, device_id, reason, note, now, step, window) -> None:
    o = _fresh(conn, order_id, now, step)
    if o.device_id != device_id:
        raise OrderActionError("forbidden", "无权操作该订单")
    if not (reason or "").strip():
        raise OrderActionError("bad_request", "请填写退货原因")
    if not lc.can_request_return(o.status, _return_elapsed(o, now), window):
        raise OrderActionError("conflict", "该订单不可退货(未签收或超 7 天窗口)")
    repo.set_return(conn, order_id, reason.strip(), (note or "").strip() or None)
    conn.commit()


def approve_return(conn, order_id: int, now: str) -> None:
    o = repo.order_by_id(conn, order_id)
    if o and o.status == "return_review":
        repo.update_status(conn, order_id, "refunded", manual=1)
        conn.commit()


def reject_return(conn, order_id: int) -> None:
    o = repo.order_by_id(conn, order_id)
    if o and o.status == "return_review":
        repo.update_status(conn, order_id, "return_rejected", manual=1)
        conn.commit()


def fast_forward_return_window(conn, order_id: int, now: str, window: int) -> None:
    """演示:把签收时刻拨到窗口外,令退货过期。"""
    o = repo.order_by_id(conn, order_id)
    if o and o.delivered_at:
        from datetime import timedelta
        repo.set_delivered_at(conn, order_id, _fmt(_parse(now) - timedelta(seconds=window + 60)))
        conn.commit()


def returns_list(conn) -> list[OrderWithItems]:
    return [_with_items(conn, o) for o in repo.returns_orders(conn)]

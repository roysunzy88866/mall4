"""rules.lifecycle 纯函数单测 —— 必须 100% 覆盖(机器闸门)。"""
from server.rules import lifecycle as lc


def test_stage_index():
    assert lc.stage_index("paid") == 0
    assert lc.stage_index("shipping") == 1
    assert lc.stage_index("delivering") == 2
    assert lc.stage_index("delivered") == 3
    assert lc.stage_index("cancelled") == -1


def test_stage_for_elapsed():
    assert lc.stage_for_elapsed(0, 30) == 0
    assert lc.stage_for_elapsed(29, 30) == 0
    assert lc.stage_for_elapsed(30, 30) == 1
    assert lc.stage_for_elapsed(61, 30) == 2
    assert lc.stage_for_elapsed(90, 30) == 3
    assert lc.stage_for_elapsed(99999, 30) == 3  # 夹在 3
    assert lc.stage_for_elapsed(50, 0) == 0  # step<=0 不推进


def test_next_stage():
    assert lc.next_stage("paid") == "shipping"
    assert lc.next_stage("shipping") == "delivering"
    assert lc.next_stage("delivering") == "delivered"
    assert lc.next_stage("delivered") == "delivered"  # 终态
    assert lc.next_stage("cancelled") == "cancelled"  # 非主阶段原样


def test_is_auto():
    assert lc.is_auto("paid") is True
    assert lc.is_auto("shipping") is True
    assert lc.is_auto("delivering") is True
    assert lc.is_auto("delivered") is False
    assert lc.is_auto("cancelled") is False


def test_logistics_nodes():
    paid = lc.logistics_nodes("paid")
    assert [n["reached"] for n in paid] == [True, False, False, False]
    delivering = lc.logistics_nodes("delivering")
    assert [n["reached"] for n in delivering] == [True, True, True, False]
    delivered = lc.logistics_nodes("delivered")
    assert all(n["reached"] for n in delivered)
    # 分支状态按未发货(只点亮已下单)
    cancelled = lc.logistics_nodes("cancelled")
    assert [n["reached"] for n in cancelled] == [True, False, False, False]
    assert paid[0]["label"] == "已下单"


def test_stage_labels_cover_all():
    assert set(lc.STAGE_LABELS) == set(lc.STAGES)


def test_status_label_main_and_branch():
    assert lc.status_label("paid") == "待发货"
    assert lc.status_label("cancelled") == "已取消"
    assert lc.status_label("refunded") == "已退款"
    assert lc.status_label("未知") == "未知"


def test_can_cancel():
    assert lc.can_cancel("paid") is True
    assert lc.can_cancel("shipping") is True
    assert lc.can_cancel("delivering") is True
    assert lc.can_cancel("delivered") is False  # 已签收改走退货
    assert lc.can_cancel("cancelled") is False


def test_can_request_return():
    w = 7 * 86400
    assert lc.can_request_return("delivered", 0, w) is True
    assert lc.can_request_return("delivered", w, w) is True  # 含端点
    assert lc.can_request_return("delivered", w + 1, w) is False  # 超窗口
    assert lc.can_request_return("shipping", 0, w) is False  # 未签收不可退
    assert lc.can_request_return("refunded", 0, w) is False

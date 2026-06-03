# 手测清单结果 · cancel-and-return

> 后端 `127.0.0.1:8000`(`RIDEMALL_STATUS_STEP_SECONDS=3` 加速测退货),模拟器 `ridemall-car`,后台静态快照 Edge 截图。日期 2026-06-04。截图 `18~20`、`admin-returns`。

| # | 手测项(对应 spec 场景) | 结果 | 证据 |
|---|---|---|---|
| 1 | 取消订单(签收前自动) | ✅ | 集成 test_cancel_in_transit;车机 18 订单详情有「取消订单」入口 |
| 2 | 已签收不可取消 | ✅ | 集成 test_cancel_delivered_rejected(409) |
| 3 | 他人订单不可取消 | ✅ | 集成 test_cancel_wrong_device_forbidden(403) |
| 4 | 7 天内可申请退货 | ✅ | 车机 18→19→20:已签收单「申请退货」→ 原因表单 → 提交 → 退货审核中(reason 7天无理由) |
| 5 | 超窗口不可退货 | ✅ | 集成 test_return_out_of_window_rejected(409) |
| 6 | 退货须填原因 | ✅ | 集成 test_return_requires_reason(400) |
| 7 | 后台退货审核页 | ✅ | admin-returns:#4 7天无理由 退货审核中 + 通过/拒绝 |
| 8 | 后台通过 → 已退款 | ✅ | 集成 test_admin_approve_return(refunded) |
| 9 | 后台拒绝 → 退货被拒 | ✅ | 集成 test_admin_reject_return(return_rejected) |
| 10 | 营业额扣取消/退款 | ✅ | 集成 test_revenue_excludes_cancelled_and_refunded |
| 11 | 退货窗口快进过期(演示) | ✅ | 后台订单详情 delivered 单有「快进退货窗口到过期」按钮(fast_forward_return_window) |
| 12 | 全程无崩溃 | ✅ | pytest 95 绿;车机编译+26 单测绿 |

**已知小瑕疵(非阻塞)**:车机订单进入分支态(退货审核中/已取消)后,顶部状态步条与物流时间线按主阶段序重置为灰(分支态 stage_index=-1)。分支态信息由徽章 + 退货原因卡清晰展示,功能不受影响。记 [tech-debt](../../../docs/tech-debt.md) DEBT-2026-06-009。

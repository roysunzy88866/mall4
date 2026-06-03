# 手测清单结果 · order-lifecycle

> 后端 `127.0.0.1:8000`(`RIDEMALL_STATUS_STEP_SECONDS=30`),模拟器 `ridemall-car`,后台静态快照经 Edge 截图。日期 2026-06-04。截图 `/tmp/ridemall-shots/admin-*`、`17-car-orderdetail`。

| # | 手测项(对应 spec 场景) | 结果 | 证据 |
|---|---|---|---|
| 1 | 后端状态机:下单仍 paid | ✅ | 单测 test_api_orders status==paid 绿 |
| 2 | 按时间自动推进(惰性) | ✅ | 车机订单 #4 下单 01:32:31 → 查看时已「运输中」(已发货 01:33:01) |
| 3 | 物流轨迹跟状态点亮 | ✅ | 车机详情 + 后台详情:已下单/已发货 亮、派送中/已签收 灰 |
| 4 | 后台浅色重做 + 侧栏 | ✅ | admin-dashboard/orders/order-detail:白底现代风、侧栏订单高亮 |
| 5 | 仪表盘:统计 + 状态分布 + 最近单 | ✅ | admin-dashboard:商品19/订单3/营业额/退货待审0、分布待发货1/运输中1/派送中1 |
| 6 | 订单列表:状态列 + 筛选 | ✅ | admin-orders:状态徽章 + 状态筛选 chips + 详情入口 |
| 7 | 订单详情:4 节点步条 + 物流 + 操作 | ✅ | admin-order-detail #3 派送中:步条、时间线、快进一步/到签收/改状态、演示控制 30 秒 |
| 8 | 管理员改状态接管(停自动) | ✅ | 集成测试 test_admin_set_status_takes_over(manual=1) |
| 9 | 管理员快进一步 | ✅ | 集成测试 test_admin_advance_one(paid→shipping) |
| 10 | 营业额扣退款/取消(A13) | ✅ | revenue_cents 改 NOT IN(cancelled,refunded) |
| 11 | 车机订单详情:步条 + 物流时间线 | ✅ | 17 截图:订单 #4 运输中、步条、物流轨迹 |
| 12 | 全程无崩溃 | ✅ | logcat 无 FATAL;pytest 78 绿 |

**机器闸门**:pytest 78 通过、rules 100%(含 lifecycle.py)、`openspec validate --strict` 过、车机 26 单测绿。

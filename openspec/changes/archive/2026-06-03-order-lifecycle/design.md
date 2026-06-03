## Context

orders 表现有 `id/device_id/status/total_cents/created_at`。下单服务设 `status='paid'`。本刀加状态机底座,尽量不破现有接口/测试(下单仍返 `paid`)。

## Goals / Non-Goals

**Goals:** 状态机(paid→shipping→delivering→delivered)、时间惰性自动推进 + 管理员干预、mock 物流时间线、后台订单管理(浅色重做)、车机订单详情看物流。

**Non-Goals:** 取消/退货(第四刀);真实物流;后台其它页(分类/商品/推荐位)大改(仅随 base 浅色统一)。

## Decisions

### D1 · 阶段值复用 `paid` 作阶段 0
`STAGES = ["paid","shipping","delivering","delivered"]`,标签 待发货/运输中/派送中/已签收。下单初始仍 `paid` → 现有 `test_api_orders` 不破。车机 `orderStatusLabel("paid")` 调整为「待发货」。

### D2 · 惰性时间推进(无后台调度)
不引后台线程/定时器。orders 加 `clock_base_at`(推进基准,默认=created_at)、`manual`(0/1)、`delivered_at`。读订单(列表/详情)时:若 `manual=0` 且当前阶段 < `min(3, (now−clock_base)/step)`,推进并落库(reaching delivered 记 `delivered_at`)。step=`RIDEMALL_STATUS_STEP_SECONDS`(默认 30)。
**为何:** Flask 请求驱动,惰性推进零额外进程;演示时一看就在动 + 后台可快进。

### D3 · 管理员干预语义
- **快进一步**:`clock_base_at -= step`(下次读取自动前进一阶,仍 `manual=0` 可继续自动)。
- **快进到已签收**:`clock_base_at = now − 3·step`。
- **改状态 X**:`status=X, manual=1`(接管,停自动);X=delivered 记 `delivered_at`。
**为何:** 快进=加速自动(不接管),改状态=完全接管;贴「自动 mock + 后台可干预」。

### D4 · 纯函数(rules/lifecycle.py,100% 测)
`stage_index(status)`、`stage_for_elapsed(elapsed_s, step_s)`、`next_stage(status)`、`is_auto(status)`(在前三阶)、`logistics_nodes(status, base_dt, step_s)`(节点标签+时间+reached)。datetime 解析/落库在 service。

### D5 · schema 迁移(幂等)
`init_schema` 执行建表后,对 orders 缺列做 `ALTER TABLE ... ADD COLUMN`(查 `PRAGMA table_info` 决定),老库平滑补列;新库由 schema SQL 直接含列。seed 预置订单铺开几个阶段(paid/shipping/delivering/delivered)让后台一开就有层次。

### D6 · 后台浅色重做
`base.html` 换浅色现代风(白底 + 左侧栏 + 顶栏 + 青色点缀),Bootstrap 5 基础上加自定义 CSS。重做 dashboard/orders + 新增 order_detail.html(4 节点步条 + 物流时间线 + 状态操作 + 演示控制:步长展示、快进按钮)。其它页随 base 统一,不逐一重排。

### D7 · 车机订单详情
车机订单历史每单可点 → `OrderDetailScreen`:顶部状态步条(4 节点)+ 商品 + 物流时间线(`GET /api/orders/{id}`)。沿用深色青色语言。

### D8 · 测试
- rules/lifecycle 纯函数 100% 单测。
- 集成:下单仍 `paid`;快进/改状态后 GET 反映;`/api/orders/{id}` 返物流;按时间推进(注入早 `created_at`/`clock_base` 模拟 elapsed)。
- 后台/车机 UI happy path 手测(后台浏览器、车机模拟器)。

## Risks / Trade-offs

- **R1 惰性推进依赖被读取**:车机不轮询,状态在「重新打开订单/详情」时前进;演示主要靠后台快进 + 重新拉。可接受(非缺陷)。
- **R2 物流节点时间为推导值**:被管理员改状态后,推导时间与 `manual` 不完全自洽——demo 可接受,时间线仍按阶段点亮。
- **R3 schema 迁移**:`ALTER ADD COLUMN` 幂等;dev 库可直接删重建 seed。

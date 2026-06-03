# order-lifecycle Specification

## Purpose
TBD - created by archiving change order-lifecycle. Update Purpose after archive.
## Requirements
### Requirement: 订单状态机

订单状态 SHALL 走有序阶段 `paid(待发货) → shipping(运输中) → delivering(派送中) → delivered(已签收)`。下单初始状态 MUST 为 `paid`。状态 MUST NOT 自动回退到更早阶段。`delivered` 为自动推进的终态(此后不再自动前进)。

#### Scenario: 下单初始为待发货
- **WHEN** 车机成功下单
- **THEN** 该订单状态为 `paid`(待发货,阶段 0)

#### Scenario: 阶段有序且不回退
- **WHEN** 订单处于 `shipping`
- **THEN** 自动推进只会走向 `delivering`/`delivered`,不会回到 `paid`

### Requirement: 按时间自动推进

未被管理员接管(`manual=0`)的订单,系统 SHALL 在读取时按 `elapsed = now − 推进基准`、`阶段 = min(3, elapsed / 步长)` 惰性推进到对应阶段并落库。步长 SHALL 取配置 `RIDEMALL_STATUS_STEP_SECONDS`(默认 30 秒)。

#### Scenario: 一步长后进运输中
- **WHEN** 一笔 `paid` 订单经过 ≥1 个步长且未被接管
- **THEN** 再次读取时其状态为 `shipping`

#### Scenario: 三步长后已签收
- **WHEN** 经过 ≥3 个步长
- **THEN** 状态为 `delivered`,且不再继续自动前进

#### Scenario: 接管后停止自动
- **WHEN** 管理员对某订单直接改状态(置 `manual=1`)
- **THEN** 之后时间流逝不再自动改变其状态

### Requirement: 管理员状态干预

后台 SHALL 提供:**快进一步**(推进基准回拨一个步长,使下次读取前进一阶)、**快进到已签收**、**直接改状态**(接管,`manual=1`)。改状态到 `delivered` MUST 记录 `delivered_at`。

#### Scenario: 快进一步
- **WHEN** 管理员对 `paid` 订单点「快进一步」
- **THEN** 该订单读取时前进到 `shipping`

#### Scenario: 直接改状态接管
- **WHEN** 管理员把订单状态直接设为 `delivering`
- **THEN** 状态变为 `delivering` 且 `manual=1`,记录(若 delivered)签收时刻

### Requirement: mock 物流轨迹

系统 SHALL 为订单生成跟状态走的物流时间线:节点 `已下单 → 已发货 → 派送中 → 已签收`,每个节点带推导时间(推进基准 + 阶段序 × 步长),并标记「已到达 / 未到达」(已到达 = 当前阶段序 ≥ 节点序)。`GET /api/orders/{id}` SHALL 返回该订单的 `status` 与 `logistics` 时间线。

#### Scenario: 物流随阶段点亮
- **WHEN** 订单处于 `delivering`
- **THEN** 物流时间线中「已下单/已发货/派送中」标记为已到达,「已签收」未到达

#### Scenario: 单单详情含物流
- **WHEN** 车机请求 `GET /api/orders/{id}`
- **THEN** 返回该单 status + items + logistics 时间线

### Requirement: 后台订单管理

后台订单列表 SHALL 显示每单状态并可按状态筛选;订单详情页 SHALL 展示 4 节点状态步条、多商品清单、物流时间线与状态操作(快进/改状态)。后台 MUST 在浅色现代风格下呈现。

#### Scenario: 列表带状态与筛选
- **WHEN** 管理员打开订单列表并选某状态筛选
- **THEN** 仅显示该状态的订单,每行展示状态

#### Scenario: 详情可改状态
- **WHEN** 管理员在订单详情点状态操作
- **THEN** 该单状态相应变化,页面刷新后保持


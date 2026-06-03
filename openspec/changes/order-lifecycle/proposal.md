## Why

下单闭环已通,但订单状态恒 `paid`、无物流、后台订单页只读。本刀加**订单状态机 + mock 物流**——这是物流展示(本刀)与取消/退货(第四刀)的公共底座。对应 ADR-0012(F12/F13)、共识 §1 + B5、后台浅色重做(仪表盘/订单列表/订单详情)。

## What Changes

- **订单状态机**(后端):`paid(待发货) → shipping(运输中) → delivering(派送中) → delivered(已签收)`,顺序不回退。`paid` 仍是下单初始值(不破现有接口)。
- **按时间自动推进**(后端,lazy-on-read):未被管理员接管的订单,按 `(now − 推进基准) / 步长` 推到对应阶段(步长走 `RIDEMALL_STATUS_STEP_SECONDS`,默认 30 秒)。读订单时惰性推进并落库。
- **管理员干预**(后台):一键快进一步 / 快进到已签收 / 直接改状态(改状态=接管,停自动)。
- **mock 物流轨迹**:跟状态走的节点时间线(已下单→已发货→派送中→已签收),节点时间由推进基准 + 步长推导。
- **接口**:`GET /api/orders` 每单加 `status`(中文不变映射在车机)+ `logistics` 时间线;新增 `GET /api/orders/{id}`(单单详情含物流)。后台新增订单状态操作端点。
- **后台浅色重做**:`base.html` 浅色现代风;**仪表盘**(营业额 + 订单状态分布 + 最近单带状态)、**订单列表**(状态列 + 按状态筛选 + 每单进详情/快进)、**订单详情**(4 节点步条 + 多商品 + 物流时间线 + 状态操作 + 演示控制)。
- **车机**:订单历史每单显示阶段中文 + 点单进**车机订单详情**(状态步条 + 物流时间线)。

## Capabilities

### New Capabilities
- `order-lifecycle`: 订单状态机 + 按时间自动推进 + 管理员干预 + mock 物流轨迹 + 后台订单管理(列表/详情)+ 车机订单详情看物流。是取消/退货(第四刀)的底座。

### Modified Capabilities
<!-- 以新能力承载;不重写 ordering/admin-console/car-checkout 的既有 spec,本刀为其加订单生命周期这层新行为。 -->

## Impact

- **orders 表 +3 列**:`clock_base_at`(推进基准)、`manual`(是否被接管)、`delivered_at`(签收时刻,第四刀 7 天窗口用)。`db.init_schema` 加幂等迁移(老库 ALTER 补列)。
- **新增**:`server/rules/lifecycle.py`(纯函数,100% 测)、订单生命周期 service + admin 端点;`server/admin/templates` 浅色重做 + 订单详情模板;车机 `OrderDetail` 屏。
- **不破**:下单仍返 `status=paid`(现有测试不动);`rules` 覆盖率闸门含新 lifecycle 纯函数。
- **配置**:`RIDEMALL_STATUS_STEP_SECONDS`(默认 30)。

## 1. 状态机纯函数(红绿)

- [x] 1.1 先写失败单测 `rules/lifecycle`:stage_index、stage_for_elapsed(elapsed/step 夹到 0~3)、next_stage、is_auto、logistics_nodes(标签+reached)
- [x] 1.2 实现 `server/rules/lifecycle.py`(纯函数),跑绿(rules 覆盖率仍 100%)

## 2. schema 迁移 + 仓库/服务

- [x] 2.1 schema SQL orders 加 `clock_base_at/manual/delivered_at`;`init_schema` 幂等 ALTER 补列(查 PRAGMA)
- [x] 2.2 下单设 `clock_base_at=created_at, manual=0`;orders_repo 读写新列;`advance_auto(order, now, step)` 落库
- [x] 2.3 service:list/detail 读取时惰性推进;admin 干预 `advance_one / advance_to_delivered / set_status`
- [x] 2.4 seed 预置订单铺开 paid/shipping/delivering/delivered 各若干

## 3. 接口

- [x] 3.1 `GET /api/orders` 每单含 status + logistics;新增 `GET /api/orders/{id}`(单单 + 物流)
- [x] 3.2 后台订单端点:`POST /admin/orders/{id}/advance|deliver|status`(改状态/快进)
- [x] 3.3 集成测试:下单仍 paid;时间推进(注入早 clock_base)→ shipping/delivered;快进/改状态生效;/api/orders/{id} 返物流

## 4. 后台浅色重做

- [x] 4.1 `base.html` 浅色现代风(白底+左栏+顶栏+青点缀,Bootstrap5+自定义 CSS)
- [x] 4.2 仪表盘:统计卡 + 订单状态分布 + 最近单(带状态徽章)
- [x] 4.3 订单列表:状态列 + 按状态筛选 + 行内「详情/快进」
- [x] 4.4 订单详情 `order_detail.html`:4 节点步条 + 多商品 + 物流时间线 + 状态操作(快进一步/到签收/改状态)+ 演示控制(步长展示)

## 5. 车机订单详情

- [x] 5.1 车机订单历史每单可点 → `OrderDetailScreen`(状态步条 + 商品 + 物流时间线,GET /api/orders/{id})
- [x] 5.2 订单历史状态用阶段中文(待发货/运输中/派送中/已签收)

## 6. 验证收尾

- [x] 6.1 pytest 全绿 + rules 100%;车机 testDebugUnitTest 绿;`openspec validate order-lifecycle --strict`
- [x] 6.2 后台浏览器手测(列表筛选/详情快进/物流)+ 车机模拟器手测(订单详情看物流)截图
- [x] 6.3 README/docs 同步;后台浅色快照 deploy 到 mall4-admin.hearagain.space

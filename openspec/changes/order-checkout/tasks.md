## 1. 业务规则(rules · 纯函数 · 先写失败测试再实现)

- [ ] 1.1 `order_total(items)` 合计 = Σ(price_cents × qty) + 单测
- [ ] 1.2 下单入参校验规则(设备号非空、items 非空、每项含名/价/数量、qty≥1)+ 单测

## 2. 数据层

- [ ] 2.1 `orders_repo`:事务内插入 order + order_items;按 `device_id` 查订单(含行项、倒序)

## 3. 业务层

- [ ] 3.1 `place_order(conn, device_id, items)`:单事务落库,返回订单;失败回滚不留半单
- [ ] 3.2 `list_orders(conn, device_id)`:本机订单倒序 + 行项

## 4. 接口层(每个接口立刻集成测试)

- [ ] 4.1 `POST /api/orders`(收参校验失败→400;成功→201)+ 集成测试
- [ ] 4.2 `GET /api/orders`(按 `X-Device-Id`、倒序、空态、仅本机可见)+ 集成测试
- [ ] 4.3 e2e 闭环:浏览→下单→拉回本机订单出现该单;改商品价不影响老单 + 测试

## 5. 校验

- [ ] 5.1 全部单元 + 集成测试绿,`rules` 100% 覆盖
- [ ] 5.2 `openspec validate order-checkout --strict` 通过

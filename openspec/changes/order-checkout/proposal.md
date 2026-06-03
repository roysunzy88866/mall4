## Why

`backend-foundation` 让车机能浏览;现在打通**下单闭环**的后端:车机下单 → 后端落库 → 能按本机设备号拉回订单。这是车机购买流程(详情→下单→去支付→点屏成功→进订单页)能成立的服务器一侧。

## What Changes

- 新接口 `POST /api/orders`:车机带**整单快照**(商品名 / 图 / 价 / 数量)+ 设备号下单;后端在**单个事务**内创建订单与行项,成功返 201(D4「先落库再成功」由车机据 201 决定是否显示成功)。
- 新接口 `GET /api/orders`:按本机设备号(`X-Device-Id`)拉订单历史,**倒序**,含整单快照行项(D3「订单只存服务器」)。
- 业务规则:订单合计 = Σ(快照价 × 数量);订单状态恒「已支付」(demo 无真支付);下单价用车机上送的锁定快照价,**不重取现价**(A8);整单快照,改/删商品不影响老单(D2)。

不在本变更:后台网页 / 支付页 UI / 收货地址录入(车机端展示固定假地址,后端不存)。无 **BREAKING**。

## Capabilities

### New Capabilities
- `ordering`: 车机下单(创建订单 + 整单快照)与拉取本机订单历史。

### Modified Capabilities
（无——`catalog-browsing` 不变。）

## Impact

- `orders` / `order_items` 表已在 `backend-foundation` 建好,本变更填写入与查询逻辑。
- 用上 `X-Device-Id` 请求头(`backend-foundation` 已约定);车机无登录(A9)。

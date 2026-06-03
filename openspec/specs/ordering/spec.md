# ordering Specification

## Purpose
TBD - created by archiving change order-checkout. Update Purpose after archive.
## Requirements
### Requirement: 创建订单
系统 SHALL 通过 `POST /api/orders` 接收设备号与整单快照(商品名 / 图 / 价 / 数量),在**单个事务**内创建订单及其行项,成功返回 HTTP 201 与订单。订单状态 MUST 恒为「已支付」。下单价 MUST 采用车机上送的快照价,系统 MUST NOT 在下单时重取商品现价。

#### Scenario: 成功下单
- **WHEN** 车机 `POST /api/orders` 带设备号与一个商品快照(名 / 图 / 价 / 数量)
- **THEN** 返回 HTTP 201 与新订单,状态为「已支付」,合计 = 快照价 × 数量

#### Scenario: 缺少设备号或商品
- **WHEN** 请求缺少设备号或商品项
- **THEN** 返回 HTTP 400 与 `{error}`,不创建任何订单

#### Scenario: 落库失败不留半单
- **WHEN** 事务中写入失败
- **THEN** 订单与其行项要么全部存在、要么全部不存在(不留半张订单)

### Requirement: 本机订单历史
系统 SHALL 通过 `GET /api/orders`(按 `X-Device-Id`)返回该设备的订单,按下单时间倒序,每单含整单快照行项;MUST NOT 返回其他设备的订单。

#### Scenario: 拉本机订单
- **WHEN** 设备 X 请求 `GET /api/orders`
- **THEN** 返回设备 X 的订单(倒序),每单含商品名 / 图 / 价 / 数量

#### Scenario: 仅本机可见
- **WHEN** 设备 X 查询订单
- **THEN** 结果不包含设备 Y 的订单

#### Scenario: 无订单空态
- **WHEN** 该设备从未下过单
- **THEN** 返回空列表

### Requirement: 整单快照不受后续改动影响
订单行项 SHALL 存下单时的商品名、图、价快照;此后商品被改价 / 改名 / 删除 MUST NOT 影响该订单的展示。

#### Scenario: 改商品价不影响老单
- **WHEN** 下单后管理员把该商品改了价
- **THEN** 该订单历史仍显示下单时的快照价


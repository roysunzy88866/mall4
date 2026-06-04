## ADDED Requirements

### Requirement: 下单校验商品在架
系统 SHALL 在 `POST /api/orders` 落库前校验每件商品**存在且在架**(`is_active=1`)。任一商品不存在或已下架时,系统 MUST 拒绝整单、不创建任何订单,并返回 `409` 与 `{"error":"商品已下架","unavailable":[{id,name}...]}`(列出不可用商品)。该校验 MUST NOT 改变价格快照规则——下单价仍采用车机上送的快照价,系统 MUST NOT 在下单时重取商品现价。

#### Scenario: 含已下架商品被拒
- **WHEN** 车机 `POST /api/orders` 的某商品已被后台下架
- **THEN** 返回 `409` 与 `{"error":"商品已下架","unavailable":[该商品]}`,不创建任何订单

#### Scenario: 含已删除商品被拒
- **WHEN** 车机 `POST /api/orders` 的某商品已被后台永久删除(查不到)
- **THEN** 返回 `409` 与不可用列表,不创建任何订单

#### Scenario: 全部在架正常下单
- **WHEN** 整单商品均存在且在架
- **THEN** 正常返回 `201` 与新订单(合计仍 = 快照价 × 数量,不重取现价)

#### Scenario: 拒绝时不留半单
- **WHEN** 整单中混有可用与不可用商品而被拒
- **THEN** 不创建订单及任何行项(不留半张订单)

#### Scenario: 入参缺失仍按 400
- **WHEN** 请求缺少设备号或商品项(而非"商品不可用")
- **THEN** 仍返回 `400`(与新增的 `409` 不可用语义区分开)

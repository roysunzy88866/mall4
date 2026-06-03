## Context

`orders` / `order_items` 表已在 `backend-foundation` 建好但未写入。本变更加「下单写入 + 本机订单查询」,打通车机购买闭环的后端。决策依据见 [后端需求.md](../../../后端需求.md)(D2/D3/D4/A8/A9)。

## Goals / Non-Goals

**Goals:** 原子下单(单事务)、按设备号查本机订单(倒序)、整单快照不可变。
**Non-Goals:** 真支付、后台网页、收货地址录入、车机支付页 UI。

## Decisions

- **事务边界**:`place_order` 在业务层用单个事务包住 `orders` + `order_items` 写入;数据层不自行 commit/rollback(一次业务 = 一个事务)。
- **信任车机上送的快照价**(A8):后端原样存,不在下单时重取现价。demo 无真钱,可接受;记录在案。
- **整单快照**(D2):`order_items` 存商品名 / 图 / 价 / 数量,独立于 `products`,改删不影响。
- **设备号**经 `X-Device-Id` 头读取(A9 无登录);`GET /api/orders` 按该头过滤。
- **合计**用纯函数 `order_total` 计算并 100% 单测;入参校验也走规则函数。

## Risks / Trade-offs

- [信任 client 价] → demo 可接受(无真支付);若将来要真支付需改为服务端定价。
- [无鉴权下单] → demo 性质;公网风险靠「演示完下线」兜底(共识 §3)。

## Open Questions

- 无。

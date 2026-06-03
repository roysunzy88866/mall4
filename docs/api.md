# 后端接口清单(API · 骨架)

> 详细行为见 [后端需求.md](../后端需求.md)。本表是接口速查;字段/状态码/校验细则在实现接口时逐个补实。
> `¥` 两位小数;货币/快照规则见 [需求共识.md §3](../需求共识.md) + 后端需求.md。

## 车机端(JSON · 均带请求头 `X-Device-Id`)

| 方法 | 路径 | 作用 | 返回 |
|---|---|---|---|
| GET | `/api/home` | 推荐页 | `banners`[3~5] + 为你推荐(全部上架商品,排除停用分类) |
| GET | `/api/categories` | 车机导航分类(启用,按排序) | `[{id,name}]` |
| GET | `/api/categories/{id}/products` | 某分类商品网格 | `[product]` |
| GET | `/api/products/{id}` | 商品详情(含图集) | `product` + `images`[1~4] |
| POST | `/api/orders` | 下单(带整单快照) | 成功 → 201 + `order`;失败 → 错误码(车机不显示成功) |
| GET | `/api/orders` | 本机订单历史(按 `X-Device-Id`,倒序) | `[order]` |

> 错误统一 JSON `{error}`;车机遇任何失败 → 全屏「网络异常」,不缓存。
> 下单价 = 车机进确认页锁定并随请求上送的快照价,后端原样存(不在支付时重取现价)。

## 后台(网页 · 登录后)

| 方法 | 路径 | 作用 |
|---|---|---|
| GET / POST | `/admin/login` | 登录(失败 5 次/5 分/IP 限速) |
| GET | `/admin` | 仪表盘(商品数 / 订单数 / 营业额 / 最近 5 单) |
| GET / POST … | `/admin/categories` | 分类:增 / 删 / 改名 / 排序 / 停用 |
| GET / POST … | `/admin/products` | 商品:增 / 删 / 改 / 传图 / 选分类 |
| GET / POST … | `/admin/banners` | 推荐位:选商品 / 上移下移(保存校验 3~5) |
| GET | `/admin/orders` | 订单倒序(手动 F5,不自动刷新) |

> 删有商品的分类 → 拒绝并提示先清空;推荐位数量 <3 或 >5 → 拒绝 + 提示。

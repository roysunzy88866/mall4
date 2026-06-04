# 后端接口清单(API)

> 详细行为见 [后端需求.md](../后端需求.md);正式需求/场景见 `openspec/specs/`。
> 本表与代码同步:车机端 = [server/api/routes.py](../server/api/routes.py);后台 = [server/admin/views.py](../server/admin/views.py)。
> `¥` 两位小数(`price` 字段如 `"459.00"`,另给整数分 `price_cents`);货币/快照规则见 [需求共识.md §3](../需求共识.md)。

## 订单状态枚举(见 [server/rules/lifecycle.py](../server/rules/lifecycle.py))

| 状态码 | 中文 | 说明 |
|---|---|---|
| `paid` | 待发货 | 主阶段 0(下单初始) |
| `shipping` | 运输中 | 主阶段 1 |
| `delivering` | 派送中 | 主阶段 2 |
| `delivered` | 已签收 | 主阶段 3 |
| `cancelled` | 已取消 | 分支态(签收前取消) |
| `return_review` | 退货审核中 | 分支态(申请退货,待后台审) |
| `refunded` | 已退款 | 分支态(退货通过) |
| `return_rejected` | 退货被拒 | 分支态 |
| `returning` | 退货中 | 分支态(预留) |

主阶段按 `RIDEMALL_STATUS_STEP_SECONDS`(默认 30 秒)惰性自动推进,后台接管(改状态/快进)后停自动。

---

## 一、车机端(JSON · 均带请求头 `X-Device-Id`)

| 方法 | 路径 | 作用 | 成功返回 |
|---|---|---|---|
| GET | `/api/home` | 推荐页 | `{banners[], recommended[]}` |
| GET | `/api/categories` | 导航分类(启用,按排序) | `[{id, name}]` |
| GET | `/api/categories/{id}/products` | 某分类商品网格 | `[product]` |
| GET | `/api/products/{id}` | 商品详情(含图集) | `product` + `images[]` |
| POST | `/api/orders` | 下单(带整单快照) | `201` + `order` |
| GET | `/api/orders` | 本机订单历史(按设备号,倒序) | `[order]` |
| GET | `/api/orders/{id}` | 单个订单详情(含物流) | `order` |
| POST | `/api/orders/{id}/cancel` | 取消订单(签收前) | 更新后的 `order` |
| POST | `/api/orders/{id}/return` | 申请退货(签收后 7 天内) | 更新后的 `order` |

**请求体**
- `POST /api/orders`:`{"items": [{"product_id", "name", "image", "price_cents", "qty"}, ...]}`(整单快照,后端原样存,不重取现价)。
- `POST /api/orders/{id}/return`:`{"reason": "...", "note": "...(选填)"}`。

**响应对象结构**
- `product` = `{id, name, price, price_cents, description, category_id}`;`/products/{id}` 额外带 `images: [url, ...]`(1~4 张,按 sort_order)。
- `banner` = `{product_id, title, description, price, price_cents, image}`(文案自动套引用商品)。
- `order` = `{id, device_id, status, status_label, created_at, total, total_cents, logistics[], can_cancel, can_return, return_reason, return_note, items[]}`;
  - `items[]` = `{product_id, name, image, price, price_cents, qty}`(下单时快照,商品改价不影响);
  - `logistics[]` = `[{label, reached, at?}]`(已下单/已发货/派送中/已签收,随阶段点亮);
  - `can_cancel` / `can_return` = 当前是否允许取消 / 退货(车机据此显隐按钮)。

**错误**(统一 JSON `{error}`)
- `/products/{id}` 商品不存在**或已下架** → `404 {"error":"商品已下架"}`。
- `POST /orders` 缺设备号 / 无商品 / 字段非法 → `400 {"error": "..."}`。
- `POST /orders` 含**不存在 / 已下架**的商品 → `409 {"error":"商品已下架","unavailable":[{id,name}...]}`,整单不创建。车机据此提示「已下架」并移出购物车(区别于网络异常)。
- `/orders/{id}` 不存在 → `404`。
- 取消 / 退货失败按业务码映射:`not_found→404`、`forbidden→403`(非本设备)、`conflict→409`(状态不允许,如已签收不能取消、超 7 天不能退)、`bad_request→400`。
- 车机遇下单 `409`「商品已下架」→ 专门提示 + 移出购物车 + 退回购物车;其余失败 → 全屏「网络异常」(可重试),不缓存。

---

## 二、后台(网页 · 服务端渲染 · 登录后;写操作走表单 / multipart)

未登录访问任意 `/admin/*` → 重定向登录页。

| 方法 | 路径 | 作用 |
|---|---|---|
| GET / POST | `/admin/login` | 登录(失败 5 次/5 分/IP 限速 → 429) |
| GET | `/admin/logout` | 登出 |
| GET | `/admin/` | 仪表盘(商品数/订单数/营业额/最近 5 单 + 状态分布) |
| **分类** | | |
| GET | `/admin/categories` | 分类列表(含各类商品数) |
| POST | `/admin/categories/create` | 新建(`name`) |
| POST | `/admin/categories/{id}/rename` | 改名(`name`) |
| POST | `/admin/categories/{id}/toggle` | 停用/启用(`active=1/0`) |
| POST | `/admin/categories/{id}/move` | 排序上移/下移(`direction=up/down`) |
| POST | `/admin/categories/{id}/delete` | 删除(有商品 → 拒绝提示先清空) |
| **商品** | | |
| GET | `/admin/products` | 商品列表 |
| POST | `/admin/products/create` | 新建(`name/price/description/stock/category_id` + 选传 `image`;价格非法/缺名缺分类 → 拒绝) |
| POST | `/admin/products/{id}/update` | 修改(同上字段) |
| POST | `/admin/products/{id}/toggle` | 下架/上架(`active=1/0`,经查询参数;下架=对顾客隐藏) |
| POST | `/admin/products/{id}/delete` | **受限永久删除**:仅无订单引用时可删;被订单引用 → 拒绝并提示改为下架 |
| **推荐位** | | |
| GET | `/admin/banners` | 推荐位列表 + 可选商品 |
| POST | `/admin/banners/add` | 加入(`product_id`;超 5 个 → 拒绝) |
| POST | `/admin/banners/{id}/remove` | 移出(少于 3 个 → 拒绝) |
| POST | `/admin/banners/{id}/move` | 上移/下移(`direction=up/down`) |
| **订单(状态机 + 物流)** | | |
| GET | `/admin/orders` | 订单倒序列表(可 `?status=` 过滤;手动 F5 不自动刷新) |
| GET | `/admin/orders/{id}` | 订单详情(物流时间线 + 可选状态 + 就地操作) |
| POST | `/admin/orders/{id}/advance` | 快进一个阶段(演示用) |
| POST | `/admin/orders/{id}/deliver` | 一键直达「已签收」 |
| POST | `/admin/orders/{id}/status` | 直接设状态(`status=` 须是主阶段值)并接管 |
| POST | `/admin/orders/{id}/expire-return` | 快进退货窗口(演示用,把签收时间往前拨过 7 天) |
| **退货审核** | | |
| GET | `/admin/returns` | 待审退货列表 |
| POST | `/admin/returns/{id}/approve` | 通过 → 已退款(营业额扣除) |
| POST | `/admin/returns/{id}/reject` | 拒绝 → 退货被拒 |

> 营业额统计排除 `cancelled` / `refunded` 订单。
> 后台写操作多为表单提交后重定向回列表页(失败经 `?error=` 回显提示),非 JSON。

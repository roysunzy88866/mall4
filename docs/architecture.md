# 架构 · RIDEMALL 后端 + 车机端

> 务实分层(2026-06-03 用户拍板,见 [ADR-0001](adr/0001-backend-architecture.md))。建立在 [后端需求.md](../后端需求.md) + [需求共识.md §3](../需求共识.md) 技术栈之上。本「总览」段力求 ≤80 行;接口明细见 [api.md](api.md)。

## 总览(一张图)

```
车机 Android(Kotlin+Compose) ──HTTPS / JSON──┐
                                              ├──> 后端 Flask ──> SQLite
管理员浏览器 ──HTTP / 网页表单──────────────────┘         └─ 商品图存 server/uploads/,以 URL 提供
                                                  后台用 Jinja2 服务端渲染
部署:Mac mini + Cloudflared tunnel @ mall4.hearagain.space(复用 panqian-tunnel)
```

## 后端分层(务实 4 层 + 业务规则)

| 层 | 职责 | 不许做 |
|---|---|---|
| 接口层 `views`(api/ + admin/) | HTTP 入口:收参、校验、调业务、返响应 | 不写业务(超 3 行就抽走) |
| 业务层 `services` | 编排一次操作 = 一个事务;调规则 + 数据层 | 不抛 HTTP 异常 |
| 数据层 `repositories` | 读写 SQLite | 不含业务判断、不自行 commit/rollback |
| 数据表 `models` | SQLite 表结构 | — |
| 业务规则 `rules`(纯函数) | 真正要重点测的规则 | 无 IO / print;时间随机配置靠注入 |

依赖方向:`views → services → repositories → models`;`rules` 谁都能调、它不调任何层。
`rules` 100% 单测,覆盖:推荐位 3~5、整单/价格快照、Banner 文案自动套、停用分类过滤、为你推荐排序、**订单状态机/物流节点/可否取消退货、元分换算**。

## 后端目录草图

```
server/
  app.py          入口         config.py  配置(账号口令/路径/端口/步长/退货窗口走这,不硬编码)
  api/            车机 JSON 接口(home / category / product / 下单 / 订单详情 / 取消 / 退货)
  admin/          后台网页(login / dashboard / categories / products / banners / orders / returns)
  services/       业务编排(事务边界):catalog · order · lifecycle(状态机推进/取消/退货审核)
                  · admin(仪表盘) · admin_catalog(分类/商品/推荐位写)
  repositories/   数据读写(catalog / catalog_write / orders / admin)
  models/         SQLite 表(分类/商品/商品图/推荐位/订单/订单行)
  rules/          纯业务规则(catalog / ordering / admin / lifecycle / money,重点 100% 测)
  seed.py         首次启动预置数据      uploads/  商品图(运行时生成)
```

## 车机端分层(工程 `android/`,详见各 change 的 design.md)

`ui`(Compose)→ `viewmodel` → `repository` → `api`(Retrofit);本地仅存一个虚拟设备号。
DTO 与 UI Model 经 repository 显式映射(`Mappers` 通用 + `CheckoutMappers` 下单专用)。

- 栈:Kotlin + Compose + Retrofit/Moshi + OkHttp + Coil;手动 `ServiceLocator`(不 Hilt);minSdk 26 / compileSdk 34;JDK 17 + Gradle 8.7 + AGP 8.5.2 + Kotlin 2.0.20。
- **状态驱动单 Activity 路由**:`StoreViewModel` 持 `route` + `content` + `overlay` 状态。
  - `Route`:Home / Category / **Detail** / **Cart** / **Confirm** / Orders / **OrderDetail**;
  - `Overlay`:None / **Pay**(支付倒计时 179s)/ **Success**;网络异常为最上层覆盖层。
- 屏:首页 · 分类 · 详情 · 购物车 · 确认 · 订单列表 · 订单详情(物流时间线);`util/`:购物车(`Cart`)、轮播、金额/订单格式化。
- **1920×1080 等比缩放容器**:覆盖 `LocalDensity` 使「1 设计 px==dp」,整体 letterbox 居中(`ui/ScalingContainer.kt`)。
- **设备号注入**:OkHttp `Interceptor` 给每请求统一加 `X-Device-Id`;设备号存 SharedPreferences(只此一项)。
- **模拟器**:1920×1080 横屏 AVD `ridemall-car`(克隆现有 AVD + 改 `config.ini` 分辨率/`skin.name`/`showDeviceFrame=no`,不入库)。

## 关键数据流

**下单闭环**
```
详情「加购/下单」→ 购物车选品 → 确认页锁定整单快照(名/图/价)→「去支付」弹码 → 点屏
  → POST /api/orders(带快照)→ 业务层一个事务里落库 → 回 201
  → 车机收到成功才显「支付成功」→ 进订单页 GET /api/orders?device → 拉本机单
  → 后台订单页 F5 → 看到同一单(闭环)
```
**生命周期**:订单按步长惰性自动推进 `paid→shipping→delivering→delivered`(后台可快进/接管);
签收前 `POST /orders/{id}/cancel` 取消;签收后 7 天内 `POST /orders/{id}/return` 申请退货 → 后台 `/admin/returns` 通过(已退款)/拒绝。物流时间线随状态点亮。

> 失败语义:任何接口失败/断网 → 车机全屏「网络异常」,不显示成功、不本地缓存。

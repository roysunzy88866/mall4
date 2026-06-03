# 架构 · RIDEMALL 后端 + 车机端

> 务实分层(2026-06-03 用户拍板,见 [ADR-0001](adr/0001-backend-architecture.md))。建立在 [后端需求.md](../后端需求.md) + [需求共识.md §3](../需求共识.md) 技术栈之上。本「总览」段力求 ≤80 行。

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
| 业务规则 `rules`(纯函数) | 真正要重点测的几条规则 | 无 IO / print;时间随机配置靠注入 |

依赖方向:`views → services → repositories → models`;`rules` 谁都能调、它不调任何层。
`rules` 100% 单测,覆盖:推荐位必须 3~5、整单/价格快照、Banner 文案自动套、停用分类过滤、为你推荐排序。

## 后端目录草图

```
server/
  app.py          入口
  config.py       配置(账号口令/路径/端口走这,不硬编码)
  api/            车机 JSON 接口(home / category / product / order)
  admin/          后台网页(login / dashboard / categories / products / banners / orders)
  services/       业务编排(事务边界)
  repositories/   数据读写
  models/         SQLite 表
  rules/          纯业务规则(重点 100% 测)
  seed.py         首次启动预置数据
  uploads/        商品图(运行时生成)
```

## 车机端分层

`ui`(Compose)→ `viewmodel` → `repository` → `api`(Retrofit);本地仅存一个虚拟设备号。
接口 DTO 与 UI Model 经 repository 显式映射(DTO ≠ Model)。

**落地(car-storefront-browse,工程 `android/`,详见该 change 的 design.md):**
- 栈:Kotlin + Compose + Retrofit/Moshi + OkHttp + Coil;手动 `ServiceLocator`(不 Hilt);minSdk 26 / compileSdk 34;JDK 17 + Gradle 8.7 + AGP 8.5.2 + Kotlin 2.0.20。
- **状态驱动单 Activity 路由**:`StoreViewModel` 持 `route(Home/Category/Orders)` + `content` 状态,NavRail + 内容区按状态渲染;网络异常为最上层覆盖层。
- **1920×1080 等比缩放容器**:覆盖 `LocalDensity` 使「1 设计 px==dp」,整体 letterbox 居中(`ui/ScalingContainer.kt`)。
- **设备号注入**:OkHttp `Interceptor` 给每请求统一加 `X-Device-Id`;设备号存 SharedPreferences(只此一项)。
- **模拟器**:1920×1080 横屏 AVD `ridemall-car`(本机无 cmdline-tools 时由克隆现有 AVD + 改 `config.ini` 分辨率/`skin.name=1920x1080`/`showDeviceFrame=no` 而来,不入库)。

## 关键数据流 · 下单闭环

```
详情「下单」→ 确认页锁定整单快照(名/图/价)→「去支付」弹码 → 点屏
  → 车机 POST /api/orders(带快照)→ 业务层一个事务里落库 → 回 201
  → 车机收到成功才显示「支付成功」→ 进订单页 GET /api/orders?device → 拉本机单
  → 后台订单页 F5 → 看到同一单(订单闭环)
```

> 失败语义:POST 失败 → 车机不显示成功、弹「网络异常」;任何接口失败/断网 → 全屏「网络异常」,不本地缓存。

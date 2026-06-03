## Context

`car-storefront-browse` 已建车机浏览主干(StoreViewModel 状态机 + NavRail + Home/Category + 网络层 + 设备号 + 网络异常覆盖层)。本刀在其上加「下单闭环」6 屏,沿用同一状态机与设计语言(深色青色 token)。后端 `GET /api/products/{id}`、`POST /api/orders`、`GET /api/orders` 实测就绪(多商品、status=`paid`、total 自动算)。

## Goals / Non-Goals

**Goals:** 详情/购物车/确认/支付/成功/订单历史 6 屏跑通;购物车本地态(20×99、勾选、角标);确认页锁价快照;179s 倒计时;先落库再成功;订单倒序多商品展示。复用 browse 的网络/设备号/主题。

**Non-Goals:** 不改后端;不做订单状态机/物流/退货(第三、四刀);不做真实支付/退款。

## Decisions

### D1 · 路由扩展(沿用状态驱动单 Activity)
`Route` 扩展:`Detail(id)`、`Cart`、`Confirm(order draft)`、`Pay`、`Success`、`Orders`(订单历史本刀落地)。`ContentState` 增对应态。详情/确认/支付/成功在内容区渲染;扫码/成功为**全屏覆盖层**(盖住导航,沿用网络异常覆盖层模型)。导航高亮:进详情/确认保持来源(activeNav 解耦)。

### D2 · 购物车 = ViewModel 内存态 + 纯函数逻辑
`cart: List<CartItem>`(CartItem = product 快照 + qty + selected),存在 ViewModel(关 app 即清,符合 F11「本地临时」)。核心逻辑抽**纯函数**便于单测:`addToCart`(同商品合并、20 种上限)、`clampQty`(1~99)、`selectedTotalCents`、`badgeCount`、`toOrderItems`(选中项 → 下单 payload)。

### D3 · 价格快照在确认页锁定
进确认页那刻,把「勾中项 / 立即购买项」**复制成不可变的下单草稿**(name/image/price_cents/qty 快照),后续即便购物车或商品变动不影响该草稿(A8)。`POST /api/orders` 上送此草稿。

### D4 · 先落库再成功
点屏 → 先 `POST /api/orders`,成功(201)才切支付成功态;失败 → 网络异常覆盖层(不显示成功)。倒计时用 `LaunchedEffect` 每秒减,归零仅置「超时提示」标志、不改可点性。

### D5 · 详情图集与标签
详情主图 + 缩略图来自 `images`(1~4,首张主图);3 个固定标签前端写死。商品卡列表接口不返图(沿用占位),详情接口有 `images`。

### D6 · 测试
纯逻辑 JVM 单测:购物车 add/merge/clamp/上限、selectedTotal、badgeCount、toOrderItems、倒计时格式化(秒→`mm:ss`)、订单 DTO→Model 映射、status→中文映射。UI happy path 在 `ridemall-car` 手测(详情→加购物车→购物车→结算→确认→支付→成功→订单)。后端不计入。

## Risks / Trade-offs

- **R1 倒计时与下单时机**:点屏即下单,网络慢时短暂等待;落库成功才显示成功(D4 保证「显示成功=后台必有」)。可接受。
- **R2 status 文案**:后端 `paid` 第三刀会扩成状态机;本刀映射表只认已知值、未知值原样显示,避免第三刀改动时车机崩。
- **R3 购物车关 app 清空**:ViewModel 随 Activity 重建而清——符合 F11「本地临时」,非缺陷。

## Context

后端三块能力(`catalog-browsing` / `ordering` / `admin-console`)已归档,只读接口 `GET /api/home`、`/api/categories`、`/api/categories/{id}/products` 实测可用(返回结构已读 `server/api/routes.py` 核实)。车机端尚无任何工程。本刀新建 `android/` 工程并实现「浏览主干」,作为第二刀(下单闭环)的载体。视觉真相源是 `design_handoff_ridemall_carstore/`(高保真,token 齐全);范围真相源是 `需求共识.md`(锚点 F2/F3/F4a/F4b/F4c/F9)+ `后端需求.md`;架构遵循 [ADR-0001](../../../docs/adr/0001-backend-architecture.md) 的车机端分层 `ui → viewmodel → repository → api`。

**已核实的接口事实**(写代码按此,不臆造):
- `GET /api/home` → `{ banners: [{product_id,title,description,price,price_cents,image}], recommended: [{id,name,price,price_cents,description,category_id}] }`,实测 banners=4、recommended=19。
- `GET /api/categories` → `[{id,name}]`,实测 5 项。
- `GET /api/categories/{id}/products` → `[{id,name,price,price_cents,description,category_id}]`。
- `price` 为不含符号的两位小数字符串(如 `"459.00"`),`price_cents` 为整数分;`image` 为相对路径(如 `/uploads/placeholder.png`)。

## Goals / Non-Goals

**Goals:**
- 一个能在 1920×1080 横屏沉浸式跑起来的 Android 工程,默认进推荐页,左导航 + 推荐页(轮播+为你推荐)+ 分类页全部对接真实后端。
- 落设计稿主题 token(青色深色主题、Oxanium+Noto Sans SC、字号 ×1.24);断网优雅失败;设备号本地持久化、不缓存业务数据。
- 工程结构与依赖为第二刀(详情/下单/订单)留好扩展位,不返工。

**Non-Goals:**
- 不做商品详情页、确认订单页、扫码支付、支付成功、订单历史页(第二刀)。
- 不引入复杂 DI 框架(Hilt)、不做多模块拆分、不做 Compose UI 自动化测试(demo 过重)。
- 不改任何后端代码/接口;不动后端 pytest + `rules` 100% 覆盖闸门。

## Decisions

### D1 · 单模块 `app` + 手动依赖装配
单一 `:app` 模块,不拆多模块、不上 Hilt。依赖在 `Application` 里手动构造一个轻量 `ServiceLocator`(Retrofit/Repository/DataStore 单例),ViewModel 经工厂取用。
**为何**:demo 规模(两刀共 8 屏),多模块与 Hilt 的样板成本 > 收益;手动装配清晰可控。**备选**:Hilt(否决:对此规模过重)。

### D2 · 状态驱动单 Activity 路由(非 Navigation-Compose)
单 Activity、单 Compose 树。一个 `StoreViewModel` 持有 `screen`(`Home | Category`)、`activeNav`、`currentCategoryId` 等状态,NavRail + 右侧内容区按状态渲染;覆盖层(本刀只有「网络异常」)盖在最上层。
**为何**:与设计稿的 React 状态机一一对应(README「路由是单 Composable 树内的状态切换」),第二刀的扫码/成功覆盖层同样套此模型。**备选**:Navigation-Compose 多目的地(否决:全屏覆盖层 + letterbox 容器下,状态机更贴合且更简单)。

### D3 · 1920×1080 等比缩放容器(letterbox)
根部一个缩放容器:量取实际窗口尺寸,`scale = min(w/1920, h/1080)`,把固定 1920×1080 的内容用 `graphicsLayer`/`Layout` 缩放并居中、补黑边。所有内部尺寸按 1920×1080 设计像素写死(dp 直接用设计 px 值,在缩放容器内等价)。
**为何**:设计稿明确「1920×1080 等比缩放 letterbox」,一处缩放、内部全部按设计像素,免去每个组件做响应式。**风险**见 R1。

### D4 · 网络层:Retrofit + OkHttp + Moshi + Coil
Retrofit(Moshi kotlin-reflect 转换,免 KSP)+ OkHttp;`X-Device-Id` 由一个 OkHttp `Interceptor` 统一注入(每请求自动带,不在每个调用点手写)。图片用 Coil `AsyncImage`。base URL 走 `BuildConfig.API_BASE_URL`(debug=`http://10.0.2.2:<port>`,release=`https://mall4.hearagain.space`),不硬编码。
**为何**:Retrofit+OkHttp 是 Android 事实标准;Interceptor 注入设备号符合「每请求带头」且不散落;Moshi-reflect 免 codegen 降低构建摩擦。**备选**:Gson(否决:Kotlin 可空性差);kotlinx.serialization(可选,需额外插件,本刀不必)。

### D5 · DTO ≠ Model,repository 显式映射
`api` 层 DTO(贴后端 JSON 字段)与 UI `Model`(`Product`、`Banner`、`NavCategory`)分开;repository 做映射,并在映射处完成两件「呈现规则」:① 价格 `"459.00"` → 展示用 `¥459.00`(实际保留原串,呈现层加 `¥`);② 相对图片路径 → base URL 补全为绝对 URL。这两条抽成**纯函数**便于单测。
**为何**:符合架构「DTO 与 Model 经 repository 显式映射」;呈现规则集中、可测。

### D6 · 设备号:SharedPreferences + UUID
首启生成 `UUID.randomUUID()` 存 SharedPreferences;之后读同一值。本地**只**存这一项,不存任何商品/分类数据。
**为何**:共识 §1 + 后端需求 D3「车机只本地存设备号」。原计划用 DataStore,但其 1.1.1 的传递依赖 `kotlin-parcelize-runtime` 不在本机 Gradle 缓存、沙箱网络又取不到(TLS 握手被中断);改用 framework 自带的 SharedPreferences 零新增依赖、全部依赖命中缓存即可离线构建。对「只存一个字符串」的场景两者无实质差异。

### D7 · 轮播节奏引用锚点,不重抄数值
自动切换间隔、手动后静默时长在代码里以**具名常量 + 注释引用 F4b/F4c** 实现(`AUTO_ADVANCE`/`MANUAL_SILENCE`),数值来源指向 `需求共识.md §3`。轮播索引推进逻辑(`(i+1) % n`、手动重置静默计时)抽纯函数单测。
**为何**:防漂移铁律「带锚点硬事实别处只写见 Fn 不重抄」;计时副作用用 `LaunchedEffect`,纯推进逻辑单测覆盖。

### D8 · 车机端测试策略(本刀确立,随 change 归档)
车机端**不进**后端 pytest 体系、不计入 `rules` 覆盖率闸门。质量保障分两层:
- **JVM 单元测试(JUnit)**:纯逻辑——价格呈现、图片 URL 补全、DTO→Model 映射、轮播索引推进、设备号「无则生成有则复用」。红绿循环。
- **手测清单(模拟器 `ridemall-car` 人工走)**:沉浸式横屏、导航构成与选中态、轮播自动/手动静默、推荐/分类网格与空态、断网异常页与重试。机器卡不死的项进里程碑/演示前手测清单。
**为何**:demo 性质下 Compose UI 自动化成本高、收益低;把可纯测的逻辑测死、UI happy path 人工走,符合 `测试方案.md` 三档强度的务实落法。

### D9 · 工程入库边界
`android/` 提交源码 + Gradle wrapper;`build/`、`.gradle/`、`local.properties`、`*.apk` 进 `android/.gitignore`。新建的 `ridemall-car` AVD 是本机开发物,不入库(其创建步骤写进 tasks 与 README)。

### D10 · 字体:本刀先用系统字体回退,留单点切换位
设计稿要求 Oxanium(数字/价格)+ Noto Sans SC(中文),但本机无这两个字体文件、沙箱网络又取不到。本刀先用系统回退:中文/正文走系统 sans-serif(设计 README 明示「车机本地可用系统黑体替代」),数字/价格走 `FontFamily.Monospace` 近似 Oxanium 的等宽数字观感。Typography 把字体族集中在一处(`Type.kt` 的 `BodyFamily`/`NumberFamily`),日后把真字体放进 `assets/fonts` 只改这一处即可切换。
**为何**:不阻塞本刀;CJK 回退设计已许可,数字字体属次要视觉项;集中单点便于后续无痛替换。已记 [tech-debt](../../../docs/tech-debt.md)。

## Risks / Trade-offs

- **R1 · 等比缩放下的图片清晰度/触控精度** → letterbox 缩放在 1920×1080 专用 AVD 上 scale≈1,无损;在其它屏可能缩放。缓解:demo 指定用 `ridemall-car`(1920×1080),scale=1 路径为主;缩放路径仅作兜底。
- **R2 · 模拟器访问本机后端的明文 HTTP** → debug 连 `http://10.0.2.2`,Android 9+ 默认禁明文。缓解:加 `network_security_config` 仅 debug 放行 `10.0.2.2`(及局域网调试地址);release 走 HTTPS 公网,不放行明文。
- **R3 · 后端未启动时的体验** → 正是「网络异常」覆盖层要覆盖的场景,已作为需求(非缺陷);手测清单含此项。
- **R4 · pre-commit pytest 闸门** → 提交 `android/` 不应触发后端测试失败(互不依赖);但需确认钩子作用域不被 android 文件干扰。缓解:提交前本地跑一次 `pytest` 确认仍绿。
- **R5 · 文档断链 `docs/user-stories.md`** → 被 `prototype-wireframes.md` 引用但不存在。不在本刀范围内修(避免范围蔓延),单独记 tech-debt;本刀行为真相源用设计稿 + specs。

## Open Questions

- 商品图当前后端全是 `placeholder.png` 占位(后端需求 §5),车机正常加载占位图即可;真实图素材用户以后给、换 seed 即可,不阻塞本刀。
- 公网 release 构建与签名本刀不做(demo 主要在模拟器演示);如需装真车机,留作后续。

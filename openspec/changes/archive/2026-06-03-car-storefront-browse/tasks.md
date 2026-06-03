## 1. 工程骨架与构建配置

- [x] 1.1 新建 `android/` Gradle 工程(Kotlin DSL + 版本目录):`:app` 单模块,minSdk 26 / compileSdk 34 / targetSdk 34,Compose 开启;补 `android/.gitignore`(忽略 `build/`、`.gradle/`、`local.properties`、`*.apk`)
- [x] 1.2 加依赖:Compose BOM、Activity-Compose、Retrofit + OkHttp + Moshi(kotlin-reflect)、Coil-Compose、Kotlin Coroutines、JUnit(测试);设备号用 framework SharedPreferences(无新增依赖);`API_BASE_URL` 经 `BuildConfig`(debug=`http://10.0.2.2:8000`,release=`https://mall4.hearagain.space`)
- [x] 1.3 `AndroidManifest`:单 Activity 锁横屏(`screenOrientation=landscape`)、`network_security_config` 仅 debug 放行明文 `10.0.2.2`;`MainActivity` 设全屏沉浸(`WindowInsetsControllerCompat` + `IMMERSIVE_STICKY`)+ 保持常亮(`FLAG_KEEP_SCREEN_ON`)
- [x] 1.4 `Application` 里建轻量 `ServiceLocator`(Retrofit/Repository/DataStore 单例);确认空工程能编译、能在模拟器横屏沉浸启动(对应「沉浸式横屏骨架」)

## 2. 主题层(设计稿 token)

- [x] 2.1 打包字体 Oxanium + Noto Sans SC 到 `assets/fonts`,定义 `FontFamily`(中文 Noto、数字/价格/品牌 Oxanium)
- [x] 2.2 落颜色 token(`bg/panel/card/line/text-1..3/accent #2FD6C4/accent-soft/line/glow/warn-*`)成 Compose 颜色;落圆角与间距常量
- [x] 2.3 落字号刻度为 Typography,基准值 ×`fsScale`(默认 1.24);全屏覆盖页字号固定不随密度
- [x] 2.4 组装 `RidemallTheme`(深色),包裹全 APP

## 3. 网络层与设备号(纯逻辑红绿)

- [x] 3.1 先写失败单测:DTO→Model 映射(`HomeDto`/`ProductDto`/`CategoryDto` → `Product`/`Banner`/`NavCategory`)、价格呈现(`"459.00"`→`¥459.00`)、相对图 URL 补全(`/uploads/x.png`+base→绝对)、设备号「无则生成、有则复用」、轮播索引推进(`(i+1)%n`)
- [x] 3.2 实现 DTO(贴已核实的真实 JSON 字段)+ Retrofit `ApiService`(`/api/home`、`/api/categories`、`/api/categories/{id}/products`)
- [x] 3.3 实现 OkHttp `Interceptor` 统一注入 `X-Device-Id`;`DeviceIdStore`(SharedPreferences + UUID,首启生成持久化)
- [x] 3.4 实现 `StoreRepository`:调接口 + DTO↔Model 映射 + 呈现规则(价格/图 URL 纯函数),返回 `Result`/密封类区分成功/失败;跑绿 3.1 全部单测(对应「设备号与本地存储边界」「价格与图片呈现」)

## 4. 缩放容器 + NavRail 骨架

- [x] 4.1 实现 1920×1080 等比缩放 letterbox 容器(`scale=min(w/1920,h/1080)` 居中补黑边),内部按设计像素布局
- [x] 4.2 实现左侧 `NavRail`:品牌区(RIDEMALL,MALL 青色)+ 「推荐」固定首 + 动态分类(来自 `/api/categories`,按序)+ 「订单」固定末;选中态(accent-soft 底 + 4×30 青竖条 + 加粗 + 图标变青)
- [x] 4.3 `StoreViewModel`:`screen(Home|Category)`/`activeNav`/`currentCategoryId` 状态 + StateFlow;点导航切换状态;「订单」项点击为占位不报错(对应「左侧常驻导航」三场景)

## 5. 推荐页(轮播 + 为你推荐)

- [x] 5.1 推荐页拉 `/api/home`;Banner 轮播组件:按 F4b 间隔自动切(`LaunchedEffect`)、圆点指示可点跳、0.7s 淡入淡出、手动后按 F4c 静默再恢复(索引推进用 3.x 纯函数)
- [x] 5.2 「为你推荐」两列网格 + 商品卡(图/名/价,hover/按压态);可纵向滚动;`recommended` 为空时空态(对应「推荐页轮播」「为你推荐网格」)
- [x] 5.3 点 Banner 本刀降级不跳详情、不报错

## 6. 分类页 + 空态

- [x] 6.1 点分类 → 拉 `/api/categories/{id}/products`;顶部标题 = 分类名 +「N 件商品」;两列网格复用商品卡
- [x] 6.2 该类无商品 → 「该分类暂无商品」空态(对应「分类页」两场景)

## 7. 网络异常覆盖层

- [x] 7.1 任一接口失败/断网 → 全屏「网络异常,点击重试」覆盖层(盖住导航,amber 警告环 + 文案 + 重试按钮);不本地缓存
- [x] 7.2 「点击重试」重发当前页请求,成功回正常页、失败留异常页(对应「网络异常优雅失败」两场景)

## 8. 模拟器与手测清单

- [x] 8.1 用 `avdmanager` 新建 1920×1080 横屏 AVD `ridemall-car`(复用 android-34 镜像);记录创建命令到 README
- [x] 8.2 本机起后端(debug base URL 指向 `10.0.2.2`),在 `ridemall-car` 上人工走手测清单:沉浸式横屏满屏、导航构成+选中态、轮播自动/手动静默、推荐网格+空态、分类网格+空态、断网异常页+重试恢复
- [x] 8.3 记录手测结果(✅/❌ 逐项)作为本刀验收证据

## 9. 文档与收尾

- [x] 9.1 `README.md` 补车机端:工程位置、如何起后端、如何建 `ridemall-car` AVD、如何 run/装 APP、base URL 配置说明
- [x] 9.2 `docs/architecture.md` 车机端段落如需补落地细节(状态机/缩放容器/Interceptor 注入设备号)
- [x] 9.3 `docs/tech-debt.md` 记一条:`docs/user-stories.md` 断链(被 `prototype-wireframes.md` 引用但不存在),本刀未修
- [x] 9.4 提交前本地跑 `pytest` 确认后端闸门仍绿(车机改动不影响);`openspec validate car-storefront-browse --strict` 通过

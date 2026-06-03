## Why

后端三块能力(浏览接口 / 下单接口 / 网页后台)已全部落地归档,但**车机端 Android APP 一行未写**——而车机商城正是这个 demo 给老板/同事看的「脸」。本刀搭起车机端工程骨架与「浏览主干」(推荐页 + 分类页),对接已就绪的后端只读接口,让 demo 第一次在 1920×1080 横屏上真正「跑起来、能浏览」。详情→下单→订单闭环留作第二刀,本刀是其载体。

## What Changes

- **新建 Android 工程**(Kotlin + Jetpack Compose,minSdk 26 / compileSdk 34),单仓新增 `android/` 目录。
- **沉浸式车机骨架**:单 Activity、锁横屏、全屏沉浸(隐状态栏/导航栏)、保持常亮、1920×1080 根容器等比缩放 letterbox 居中(任意屏适配)。
- **设计稿主题层**:落 `design_handoff_ridemall_carstore` 的 Design Tokens——深色青色色板(accent `#2FD6C4`)、Oxanium + Noto Sans SC 字体、字号 ×1.24、圆角/间距,做成 Compose 主题。
- **左侧常驻导航 NavRail**:品牌区 + 「推荐(固定首)+ 后端启用分类(动态、按排序)+ 订单(固定末)」,带选中态;分类来自 `GET /api/categories`;「订单」项本刀仅占位(第二刀填)。
- **推荐页**(默认首页):Banner 轮播(3 秒自动切、圆点可点跳、手动后 8 秒静默)+「为你推荐」两列商品网格,数据来自 `GET /api/home`;无商品空态。
- **分类页**:点导航某分类 → 右侧该类商品网格 + 「N 件商品」,来自 `GET /api/categories/{id}/products`;空态「该分类暂无商品」。
- **网络与设备号**:Retrofit/OkHttp + Repository(DTO↔Model 显式映射)+ ViewModel/StateFlow;设备号首次启动随机生成并持久化,每请求带 `X-Device-Id`;本地只存设备号、不缓存业务数据。base URL 走 BuildConfig(不硬编码)。
- **网络异常覆盖层**:任何接口失败/断网 → 全屏「网络异常,点击重试」(盖住导航),点重试重拉当前页;不做本地缓存。

非范围(留第二刀):商品详情页、确认订单页、扫码支付、支付成功、订单历史页。

## Capabilities

### New Capabilities
- `car-browsing`: 车机端用户在 1920×1080 横屏沉浸式 APP 内浏览商城的能力——启动与骨架、左侧动态导航、推荐页(轮播+为你推荐)、分类页(网格+空态)、设备号与网络层、断网优雅失败。与后端只读能力 `catalog-browsing`(数据侧)互补:本能力是**车机 UI 侧的浏览行为**。

### Modified Capabilities
<!-- 无:本刀不改动任何已建后端能力的 spec 级行为,仅作为其消费方对接已就绪接口。 -->

## Impact

- **新增**:`android/` 整个工程(Gradle 配置、Compose UI、network、theme、assets 字体)。
- **新建模拟器**:1920×1080 横屏 AVD `ridemall-car`(开发/演示用,不入库)。
- **不改**:`server/` 后端(只消费现有 `/api/home`、`/api/categories`、`/api/categories/{id}/products`,不动接口);后端 pytest + `rules` 100% 覆盖闸门不受影响(车机代码不计入)。
- **依赖**:Retrofit、OkHttp、Compose、Kotlin Coroutines、DataStore;字体 Oxanium / Noto Sans SC(OFL,打包 assets)。
- **文档**:完工后补 `docs/architecture.md` 车机端落地细节(如需)、`README.md` 车机端运行说明;`docs/user-stories.md` 断链问题单独记录,不在本刀修。

## Context

`StoreViewModel`(`viewmodel/StoreViewModel.kt`)每个浏览动作都 `viewModelScope.launch { runCatching { repo.xxx() }.onSuccess { 更新 content } }`,彼此独立、无时序控制。两次快速切换 → 两个协程并发在飞,`onSuccess` 谁后执行谁覆盖 `content`,与"最后选中哪个 tab"无关。已有 `browseRoute` 记录当前浏览路由,但 `onSuccess` 不校验它。

## Goals / Non-Goals

**Goals:**
- 快速连续切换时,右侧最终内容**一定**对应最后一次选中的页面。
- 修复可被单测复现/守住(不只靠手测)。

**Non-Goals:**
- 不改后端/接口/DTO;不改视觉。
- 不引入本地缓存(共识 §1/§2 仍不缓存)。
- 不重构 `StoreRepository` 为接口(`ApiService` 已是接口,足够注入 fake)。

## Decisions

**D1 · 取消在飞 + 序号守卫(双保险)**:`StoreViewModel` 持有 `loadSeq: Long` 与 `loadJob: Job?`。每次浏览加载:`loadSeq++` 取本次 `seq`,`loadJob?.cancel()` 取消上一个在飞加载,`loadJob = viewModelScope.launch { ... }`。`onSuccess`/`onFailure` 内**仅当 `seq == loadSeq` 时**才写 `content`(过期响应丢弃)。取消是主手段(协程取消后 `onSuccess` 不跑),序号守卫兜住"已 resume 但更晚加载已发起"的边角。
- **备选**:Flow + `flatMapLatest` → 否决(改动面更大,当前是命令式状态机)。仅取消不加序号 → 多数情况够,但 resume 与 launch 之间的极窄窗口仍可能漏,故加序号。

**D2 · 哪些加载纳入**:`openHome` / `openCategory` / `openDetail` / `openOrders` / `openOrderDetail` 这些"加载右侧内容"的动作统一走同一 `loadSeq`/`loadJob`(它们互斥地占据右侧)。下单 `onPayTap`、购物车本地操作**不**走这个机制(不是右侧浏览加载)。

**D3 · 可测**:`ApiService` 是 Retrofit 接口 → 测试实现一个 fake `ApiService`,用 `CompletableDeferred`/可控挂起让"先发的请求晚返回";`StoreRepository(fakeApi, baseUrl)` 注入 `StoreViewModel`;`kotlinx-coroutines-test`(`runTest` + `Dispatchers.setMain(StandardTestDispatcher)`)驱动调度,断言:先切 A 再切 B、A 晚于 B 返回 → 最终 `content` 是 B。

## Risks / Trade-offs

- **取消正在进行的请求** → 仅取消浏览加载协程,不影响下单/购物车;取消是协程协作式取消,OkHttp 调用会被中断,无副作用(只读请求)。
- **新增 test 依赖 `kotlinx-coroutines-test`** → test-only,不进 release;若版本与现有 coroutines 不匹配需对齐 BOM。
- **`Dispatchers.setMain`** → 测试需 `@Before/@After` 设置/还原主调度器,标准做法。

## Migration Plan

无数据迁移。随车机 release 包重出(可与刀1 的 ops 合并一次)。回滚=代码回退。

## Open Questions

(无)

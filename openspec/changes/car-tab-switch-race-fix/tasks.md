## 1. 车机端 · 加载时序控制

- [ ] 1.1 `StoreViewModel` 加 `loadSeq: Long` + `loadJob: Job?`;抽一个私有 `launchLoad { ... }` 帮助器:`loadSeq++` 取 seq、`loadJob?.cancel()`、`loadJob = launch`,响应写 `content` 前校验 `seq == loadSeq`
- [ ] 1.2 `openHome` / `openCategory` / `openDetail` / `openOrders` / `openOrderDetail` 改走 `launchLoad`(下单/购物车不动)
- [ ] 1.3 保持既有成功/失败/404→DetailUnavailable 等分支语义不变(仅包一层时序守卫)

## 2. 车机端 · 并发测试

- [ ] 2.1 加 test-only 依赖 `kotlinx-coroutines-test`(与现有 coroutines 版本对齐)
- [ ] 2.2 fake `ApiService`(可控延迟:用 `CompletableDeferred` 让先发的请求晚返回)+ `StoreRepository(fakeApi, baseUrl)`
- [ ] 2.3 `StoreViewModelRaceTest`(红):先 `openCategory(A)` 再 `openCategory(B)`,放行 B 再放行 A → 断言最终 `content` 是 B;最后选中决定展示
- [ ] 2.4 转绿 + `./gradlew :app:testDebugUnitTest` 全绿(既有用例不回归)

## 3. 收口

- [ ] 3.1 车机单测全绿;`openspec validate car-tab-switch-race-fix --strict` 过
- [ ] 3.2 中文人话交付报告
- [ ] 3.3 (ops,代码外)随车机 release 包重出 + 模拟器实测快速切 tab 不串台(可与刀1 ops 合并)

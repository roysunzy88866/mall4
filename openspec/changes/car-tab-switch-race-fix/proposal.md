## Why

快速连续切换分类/页面时,车机右侧会"串台"——点了 Tab1 却显示 Tab2 的内容。根因(已核实代码):`StoreViewModel` 的 `openCategory` / `openHome` / `openDetail` 每次切换都起一个协程拉数据,但**既不取消上一个在飞请求、也不校验响应回来时是否还在该页面**;于是哪个网络响应**后到**就覆盖右侧,与用户最后点的 tab 无关。

## What Changes

- **加载时序控制**:浏览类加载(`openHome` / `openCategory` / `openDetail` / `openOrders` / `openOrderDetail`)引入"取消旧加载 + 过期响应丢弃"——发起新加载时取消上一个在飞加载,并给每次加载打单调序号,响应回来时只有"仍是最新一次加载"才应用到右侧。保证右侧最终内容**一定对应最后一次选中**的页面。
- **可测性**:利用 `ApiService` 本就是 Retrofit 接口,测试用 fake 实现注入可控延迟,配合 `kotlinx-coroutines-test` 复现"先发晚到"竞态并断言不串台。

无后端 / 接口 / 数据改动;无视觉改动(纯并发正确性修复)。

## Capabilities

### New Capabilities

(无)

### Modified Capabilities

- `car-browsing`: 新增"切换页面的并发一致性"关注点——右侧展示 MUST 对应最后选中页面,晚到的过期响应 MUST NOT 覆盖。

## Impact

- **车机端**:`viewmodel/StoreViewModel.kt`(加载序号 + 取消在飞加载 + 应用前校验最新)。`StoreRepository` / `ApiService` / UI 不变。
- **测试**:新增 test-only 依赖 `kotlinx-coroutines-test`;新增 `StoreViewModel` 并发测试(fake `ApiService` 可控延迟)。
- **数据/部署**:无迁移、无接口变化;随车机 release 包一并重出即可(与刀1 ops 可合并一次)。

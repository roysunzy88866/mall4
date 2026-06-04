# Project Context

> OpenSpec 的项目上下文。给生成 proposal/specs/design/tasks 时定调。**详细需求别在这重抄**,指向下方权威文档。

## Purpose
RIDEMALL 车载商店 **demo**:车机 Android 横屏商城(1920×1080)+ 网页后台 + Flask/SQLite 后端。内部演示给老板/同事,**不接真实支付、不发货**。

## Tech Stack
- 后端:Python + Flask + SQLite;商品图存 `server/uploads/` 以 URL 提供
- 车机端:Kotlin + Jetpack Compose(minSdk 26)
- 后台:Bootstrap 5 + Jinja2(服务端渲染)
- 部署:Mac mini + Cloudflared tunnel @ `mall4-admin.hearagain.space`(复用 panqian-tunnel)

## Architecture(务实分层 · ADR-0001)
后端:接口层(views)→ 业务层(services,事务边界)→ 数据层(repositories)→ 数据表(models)+ 业务规则(rules,纯函数,100% 单测)。
车机端:ui(Compose)→ viewmodel → repository → api(Retrofit),本地仅存设备号。
详见 [../docs/architecture.md](../docs/architecture.md)。

## Source-of-truth 文档(分层,不重抄)
- **意图 / 范围合同 / 为什么** → [../需求共识.md](../需求共识.md)、[../后端需求.md](../后端需求.md)、[../docs/adr/](../docs/adr/)
- **正式 WHAT(需求+场景)** → `openspec/specs/`(随每个 change 增量长出)
- **怎么做** → [../docs/architecture.md](../docs/architecture.md)、各 change 的 design.md
- **测试** → [../测试方案.md](../测试方案.md)(其场景与 specs 的 `#### Scenario` 同构)
- **接口速查** → [../docs/api.md](../docs/api.md) · **视觉/交互** → `../design_handoff_ridemall_carstore/`

## Conventions(摘自 ../CLAUDE.md,该文件是工作纪律宪法)
- 用户**不读代码**:所有反馈用中文人话 + 例子,不贴代码。
- **重纪律**:TDD 红绿循环;`rules` 纯函数 100% 覆盖;一次业务=一个事务;不硬编码(账号/路径/URL 走配置)。
- 锁定结论不再议;改共识走「用户明文 + 改文档 + 记 ADR」。
- specs/proposal 用中文可以(需求关键词 SHALL/MUST 保留英文以过校验)。

## Hard Constraints / NOT
- demo:不做购物车、真实支付、收货地址录入、物流、车机登录、多管理员、商品搜索、多语言、离线兜底(详见 [../需求共识.md §2](../需求共识.md))。
- 安全:后台弱口令 + 公网 → **演示完必须 `launchctl unload` 下线**。
- 数据:5 大分类;一个商品只属一个分类;订单整单快照;推荐位强制 3~5。

## Testing(测试落地 · 见 [../测试方案.md](../测试方案.md))
[测试方案.md](../测试方案.md) 是测试的**约束合同**,每个 change 都按它落地,三档强度:
- **规格层**:测试方案「要测什么」→ specs 的 `#### Scenario`(每个场景 = 一个测试用例)。
- **实现层**:每个 change 的 `tasks.md` 必须含三层测试任务——`rules` 纯函数单元(100% 覆盖)、每个接口一条集成、闭环 API 级 e2e;红绿循环(先写失败测试)。
- **机器闸门**:`pytest` + 覆盖率门槛(`rules` 100%)必须绿,change 才算 done / 才能 archive。
- **手测清单**:机器卡不死的(真机 happy path、断网、下线 404)在里程碑/演示前人工走(测试方案 §3②)。

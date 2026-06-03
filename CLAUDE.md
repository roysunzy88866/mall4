# CLAUDE.md · 车载商店 02 工作纪律

> 每次进入本项目自动加载,是**我必须遵守的铁律**;详细规则下沉到专题文档,本文件硬上限 200 行。
> 品牌 **RIDEMALL** · 本轮工作目录 `商店4` · 公网 `mall4.hearagain.space` · 后台口令见 [需求共识.md §3](需求共识.md)。

---

## 🚦 当前状态(2026-06-03 · 第 4 轮起点)

本目录现在有**需求 / 设计 / 后端需求 / 架构 / 接口文档**,**尚无代码、git、测试方案、建造流程文档**。纪律里凡引用 🚧 文件的,**等它真正建好才生效**——别去 `git log` / `cat` 不存在的东西,也别假装它们在。

| | 内容 |
|---|---|
| ✅ 已就位 | `需求共识.md`、`后端需求.md`、`测试方案.md`、`design_handoff_ridemall_carstore/`、本 `CLAUDE.md`、`docs/architecture.md`、`docs/api.md`、`docs/adr/`、`README.md` |
| 🚧 动工前 / 时建 | `git` 仓库 + `.pre-commit-config.yaml` + `.claude/settings.json`、`docs/`(story-map·user-stories·delivery-process·drift-log·.path-whitelist)、`server/`·`android/`·`deploy/` 代码骨架 |

---

## 📜 不变铁律(立即生效,不依赖任何待建物)

**未落文档不算结论** — 讨论 / 对齐 / 决策必须落成本目录里的文件;下一轮会话不认"上次说过",**只认文件**。口诀:**3 天后还要再讲一次的,就写下来**。

**锁定结论**:[需求共识.md §3](需求共识.md) 已定型,**不要再问、不要再议**;要改走「共识修改流程」(用户明文 + 改共识 + 🚧 建 git 后同提交记 ADR)。

**真相优先级 + 防腐**:谁赢 —— 范围→`需求共识.md`、为什么→`docs/adr/`(🚧)、行为→测试、带锚点硬事实→`需求共识.md §3` 锚点表那行(别处只写「见 Fn/An」**不重抄数值**);改了任一锚点,完工前必做跨文档一致性核对(grep 锚点值 + 通读找矛盾)。

---

## 🔄 新会话自启动 SOP 🚧(待 git + docs 建好后启用)

git / docs 就位前,开会话先**人工对齐**:读 `需求共识.md` + 本文件「当前状态」,确认上次到哪、本次做什么。建好后恢复自动流程:
1. `git log --oneline -20` 看上次到哪 · 2. `cat docs/wip.md` 看半成品遗留 · 3. 读 `docs/story-map.md` 校验切片
4. 两锚一致 → 提议「上次完成 C_n,下一片 C_{n+1}(US-XXX-NN),开始?」;不一致 → 停问用户,登记 `drift-log.md`
5. 用户拍板后:先在 `docs/user-stories.md` 补本片 Gherkin → 用户二次确认 → 才进红绿循环(禁止跳过直接写代码)

---

## ⚖️ 两锚校验 🚧(待 git + story-map)

两个独立事实之锚:`git log` commit 状态(开工/wip/done)+ `docs/story-map.md` 故事状态(⏳/🟡/✅)。**两锚必须一致**;不一致 → 停 + 问用户 + 落 `drift-log.md`。

## 💾 commit message 强制格式 🚧(待 git)

`[C_n <状态>] <描述> (<故事 ID>)`,状态 ∈ {`开工`,`wip`,`done`};里程碑 `[M_n <状态>]`;切片过大可 `C10a`/`C10b` 各自独立走完。例:`[C3 done] categories CRUD (US-ADMIN-03)`。`.pre-commit-config.yaml`(🚧)自动校验,不合规物理无法落地。

## ✋ 切片完整性铁律

- **禁止半成品过夜**:切片未达 DoD(见 `docs/delivery-process.md` 🚧)不允许标 `done`
- 会话末要么 done 要么 wip 要么(待 git)`git stash`;不留未提交改动跨会话遗留

## 🪝 自动化检查 🚧(待 git,建仓时一并装)

两层机械补强:① `.pre-commit-config.yaml` — 基础卫生 + 硬编码扫描 + commit 格式 + C_n 必带故事 ID + 代码⟹文档闸门 + 两锚同步闸门(架构定稿后再补:路径白名单 / architecture bump),失败 **block commit**;② `.claude/settings.json` — 会话末 `git status && git log`(打印,非阻断)。**待 C1 起补 ruff / mypy / pytest 钩子**。

---

## 📋 文档地图

| 我要做什么 | 该读哪个 | 状态 |
|---|---|---|
| 做什么 / 不做什么 | [需求共识.md](需求共识.md) | ✅ |
| 后端存啥 / 给啥接口 | [后端需求.md](后端需求.md) | ✅ |
| 视觉 / 交互怎么还原 | `design_handoff_ridemall_carstore/README.md` | ✅ |
| 系统现在长什么样 | `docs/architecture.md` | ✅ |
| 后端接口速查 | `docs/api.md` | ✅ |
| 为什么这样设计 | `docs/adr/` | ✅ 起步 |
| 怎么运行 / 测试 / 部署 | `README.md` | ✅ 骨架 |
| 测试怎么写、覆盖到哪 | [测试方案.md](测试方案.md) | ✅ |
| 还有哪些故事 + 优先级 | `docs/story-map.md` | 🚧 动工前 |
| 测试要覆盖的 Gherkin | `docs/user-stories.md` | 🚧 动工前 |
| 什么算"做完" / 怎么反馈 | `docs/delivery-process.md` | 🚧 动工前 |

---

## 🏗 架构一句话(务实分层 · 2026-06-03 定)

> 后端务实分层:**接口层(views)→ 业务层(services)→ 数据层(repositories)→ 数据表(models)+ 业务规则(rules,纯函数,100% 单测)**。车机端:`ui`(Compose)→ `viewmodel` → `repository` → `api`(Retrofit),本地仅存设备号。详见 [docs/architecture.md](docs/architecture.md),决策见 [ADR-0001](docs/adr/0001-backend-architecture.md)。技术栈与部署 @ `mall4.hearagain.space` 见 [需求共识.md §3](需求共识.md),不重抄。

---

## 🚧 共识防漂移(3 条铁律 · 立即生效)

1. 用户主动让我越界 → 先停 + 回放「这超出共识 §X」+ 三选(更新共识 / 临时补丁 / 放弃)→ 用户拍板再动
2. 我无意越界 → 自停 + 落 `docs/drift-log.md`(🚧,惰性创建)+ 等用户确认
3. 共识灰色地带(共识 §7)→ 一律停,列 2-3 做法 + 后果 + 推荐 → 用户拍板 → 记一份 ADR

---

## 💻 代码硬规矩(写代码起生效)

**架构与边界(务实分层 · 2026-06-03 补回,详见 [ADR-0001](docs/adr/0001-backend-architecture.md))**
- 后端依赖单向:**接口层 → 业务层 → 数据层 → 数据表**;`rules` 纯函数谁都能调、它不调任何层
- 接口层只「收参 / 校验 / 调业务 / 返响应」,**写业务超 3 行就抽到业务层**;异常→HTTP 的翻译只在接口层,业务层不抛 HTTP
- **一次业务操作 = 一个事务**,在业务层显式包裹;数据层不自行 commit / rollback
- 外部输入(HTTP 参数 / 表单 / 上传)在接口层校验,失败统一返错;业务层 / 规则不接未校验数据
- `rules` 纯函数:无 IO / print,时间 / 随机 / 配置靠参数注入,**100% 单测**(推荐位 3~5、整单 / 价格快照、Banner 文案自动套、停用分类过滤、为你推荐排序)
- 车机端:Composable 不调网络只调 ViewModel;接口 DTO 与 UI Model 经 repository 显式映射

**通用(与架构无关)**
1. **修改范围最小化**:非本任务文件不顺手改 / 重命名;发现更大问题先报告 + 落 drift-log(🚧),不借机大改;每次最小可验证改动
2. **不假设运行环境**:任何 hardcode 路径 / 端口 / URL / 账号 / 密码 → 必须走配置(env / config / BuildConfig),代码里不许出现 `localhost:18767` / `/Users/Admin/...` / `‹REDACTED›` 具体值
3. **加新依赖前先记决策**:加 pip / gradle 新包前先开 ADR(🚧)写清原因 / 替代 / 影响
4. **模块自声明**:每个模块顶部 docstring 声明「依赖什么 / 暴露什么」
5. **命名**:布尔 `is_*` / `has_*` / `can_*`;基础风格(snake_case / PascalCase / camelCase)由 ruff / ktlint 强制

---

## 🧪 测试纪律(3 条 · 详见 [测试方案.md](测试方案.md))

1. 红绿循环(TDD):先写失败测试 → 写代码转绿 → 重构;禁止"一口气写完再测"
2. 每写一个函数 / 模块 → 立刻单测;每写一个接口 → 立刻集成测试
3. 切片测试全绿才算切片完成,否则不允许交付

---

## ✅ 交付铁律

切片"做完" = 单元 + 集成 + E2E 全绿 + 文档同步 + drift-log 无未闭合 + 中文交付报告 + 共识对账。详见 `docs/delivery-process.md`(🚧)。

## 💬 给用户反馈(2 条核心)

1. 用户**不读代码**:所有反馈用中文人话,**不贴代码**,改完用「人话 + 1~2 个例子」讲前后差异
2. 测试报告格式:`✅ 23/23 通过` 或 `❌ 22/23(失败:Banner 超 5 应报错但没报)`,失败用人话讲业务影响

---

## 🛑 越界即停 / 防债务

- 共识漂移 / 灰色地带 → 先停问用户
- 写"临时方案"前先登记 `docs/tech-debt.md`(🚧,无登记不许写);代码 TODO / FIXME 必带债务编号(`# DEBT-2026-MM-NNN`),裸 TODO 禁止
- 安全类债务 🔴 不许 accepted:后台弱口令 + 公网,**演示完必须 `launchctl unload` 下线**闭合
- **删 / 重命名任何文档前**:必须 grep 所有引用并修复,裸删禁止

---

## 📅 实施节奏 / 防膨胀

- 业务能力纵切 C1…C12,演示节奏 M1…M6;详见 `docs/delivery-process.md`(🚧)
- 防膨胀,任一触发强制拆:① 本文件 >200 行 / architecture 总览 >80 行 / 分册 >150 行 ② 新人 onboard >15 分钟没看懂 ③ 解释一个问题要横向引用 3+ ADR

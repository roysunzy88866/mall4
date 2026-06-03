# CLAUDE.md · RIDEMALL 车载商店 工作纪律

> 每次进入本项目自动加载,是**我必须遵守的铁律**;本文件硬上限 200 行。
> 品牌 **RIDEMALL** · 工作目录 `商店4` · 公网 `mall4.hearagain.space` · 后台口令见 [需求共识.md §3](需求共识.md)。
> **本项目用 OpenSpec 管理变更**(见下「OpenSpec 工作流」)。

---

## 🚦 当前状态(2026-06-03)

已建成:**git 仓库(有提交)、OpenSpec 工作流、后端代码 `server/`、pytest + 覆盖率 pre-commit 闸门**。
- ✅ 已交付并归档:`backend-foundation`(车机浏览侧只读后端 = 能力 `catalog-browsing`)
- 🚧 进行中(已 propose,待 apply):`order-checkout`(下单 + 本机订单)
- ✅ 文档:需求共识 / 后端需求 / 测试方案 / `docs/`(architecture·api·adr·tech-debt) / `openspec/`(project·specs·changes) / 设计稿
- 🚧 未建:`server/` 后台与登录、车机 `android/`、`deploy/`(后续 change)

---

## 🧭 OpenSpec 工作流(变更的唯一通道)

切片单位 = 一个 **OpenSpec change**。三段生命周期:
1. **propose**(`/opsx:propose`)→ 生成 `openspec/changes/<名>/`:`proposal.md`(为什么/改什么/能力)→ `specs/<能力>/spec.md`(正式需求 + `#### Scenario` WHEN/THEN)→ `design.md`(怎么做 + 决策)→ `tasks.md`(实现清单)。
2. **apply**(`/opsx:apply`)→ 照 tasks 红绿循环写代码,逐条勾 `[x]`。
3. **archive**(`/opsx:archive`)→ 校验后把 spec 增量并进 **`openspec/specs/`(长期真相源)**,change 入 `archive/`。
- `openspec/project.md` = 项目上下文;每个 change `openspec validate <名> --strict` 必须过(机器闸门)。
- **禁止跳过 propose 直接写代码**:先有 change 的 specs/tasks,再 apply。

---

## 📜 不变铁律

**未落文档不算结论** — 讨论/对齐/决策必须落成文件;下一轮只认文件,不认"上次说过"。口诀:**3 天后还要再讲一次的,就写下来**。
**锁定结论**:[需求共识.md §3](需求共识.md) 已定型,不要再议;要改走「用户明文 + 改共识 + 记 ADR + 同提交」。
**真相优先级**:范围→`需求共识.md` · 正式需求/场景→`openspec/specs/` · 行为→测试 · 为什么(跨切片/架构/范围裁决)→`docs/adr/` · 单个 change 的技术决策→该 change 的 `design.md` · 带锚点硬事实→`需求共识.md §3` 锚点表(别处只写「见 Fn/An」**不重抄数值**)。改了锚点,完工前 grep 锚点值 + 通读找矛盾。

---

## 🗂 ADR vs OpenSpec(分两层,别重复)

- **单个 change 内的技术决策** → 写在该 change 的 `design.md`(随 change 归档)。
- **跨切片 / 架构 / 共识范围裁决** → `docs/adr/000N-*.md`(编号,被共识正文引用;索引见 [docs/adr/README.md](docs/adr/README.md))。
- 判据:只影响这一刀 → design.md;约束很多刀 / 是产品范围 → ADR。**两者不冲突、不重复**。

---

## 🔄 新会话 SOP

1. 读 `需求共识.md` + `openspec/project.md` + 本文件「当前状态」。
2. `git log --oneline -10` + `openspec list`(看 changes)+ `openspec list --specs`(看已建能力)。
3. 有进行中的 change → 接着 apply;否则提议下一刀(propose 新 change)。

---

## 🪝 自动化检查(机器闸门 · 据实)

**当前闸门**:`.pre-commit-config.yaml` 的 `pytest-gate` —— 每次 `git commit` 跑 `pytest` + **`rules` 100% 覆盖率**(`--cov-fail-under=100`),不绿则**提交被拦**。已 `pre-commit install`、实测生效。
**计划补**:ruff / mypy 钩子(尚无)。
> ⚠️ 目前**没有** commit-message 格式校验、没有路径白名单 / 两锚钩子——别声称有。

## 💾 commit 约定

- **Conventional Commits**:`feat:` / `fix:` / `chore(openspec):` / `docs:` / `test:`。一个 change 实现完一起提交;归档单独提交。
- 提交信息末尾带 `Co-Authored-By: ...`。**只在用户要求时提交**(本项目用户已授权按 change 提交)。

---

## 📋 文档地图

| 我要做什么 | 该读哪个 |
|---|---|
| 做什么 / 不做什么 | [需求共识.md](需求共识.md) |
| 后端存啥 / 给啥接口 | [后端需求.md](后端需求.md) · [docs/api.md](docs/api.md) |
| 正式需求 + 场景(真相源) | `openspec/specs/` |
| 变更提案 / 实现任务 | `openspec/changes/` |
| 系统架构 | [docs/architecture.md](docs/architecture.md) |
| 为什么这样设计(跨切片/范围) | [docs/adr/](docs/adr/) |
| 测试怎么写、覆盖到哪 | [测试方案.md](测试方案.md) |
| 视觉 / 交互还原 | `design_handoff_ridemall_carstore/README.md` |
| 怎么运行 / 测试 / 部署 | [README.md](README.md) |
| 技术债 | [docs/tech-debt.md](docs/tech-debt.md) |

---

## 🏗 架构一句话(务实分层 · [ADR-0001](docs/adr/0001-backend-architecture.md))

后端:**接口层(`api/`)→ 业务层(`services/`)→ 数据层(`repositories/`)→ 数据表(`models/`)+ 业务规则(`rules/`,纯函数,100% 单测)**。车机端:`ui`(Compose)→ `viewmodel` → `repository` → `api`(Retrofit),本地仅存设备号。详见 [docs/architecture.md](docs/architecture.md)。

---

## 💻 代码硬规矩

**架构与边界(务实分层)**
- 依赖单向:接口层 → 业务层 → 数据层 → 数据表;`rules` 谁都能调、它不调任何层。
- 接口层只「收参/校验/调业务/返响应」,业务超 3 行抽到业务层;**异常→HTTP 翻译只在接口层**。
- **一次业务 = 一个事务**,在业务层显式包裹;数据层不自行 commit/rollback。
- 外部输入在接口层校验;`rules` 纯函数无 IO/print,时间/随机/配置靠注入,**100% 单测**。
- 车机端:Composable 不调网络只调 ViewModel;DTO 与 Model 经 repository 显式映射。

**通用**
1. 修改范围最小化:非本任务文件不顺手改;发现更大问题先报告 + 记 [docs/tech-debt.md](docs/tech-debt.md),不借机大改。
2. 不假设环境:任何 hardcode 路径/端口/URL/账号/密码 → 走配置(env/config/BuildConfig);代码里不许出现 `localhost:18767` / `/Users/Admin/...` / `‹REDACTED›`。
3. 加新依赖前记决策(跨切片影响则开 ADR)。
4. 模块顶部 docstring 声明依赖/暴露。
5. 命名:布尔 `is_*`/`has_*`/`can_*`;风格由 ruff/ktlint 强制。

---

## 🚧 共识防漂移

1. 用户主动让我越界 → 停 + 回放「超出共识 §X」+ 三选(更新共识/临时补丁/放弃)→ 拍板再动。
2. 我无意越界 → 自停 + 记 [docs/drift-log.md](docs/drift-log.md) + 等确认。
3. 共识灰色地带(共识 §7)→ 停,列 2-3 做法 + 后果 + 推荐 → 拍板 → 记 ADR。

---

## 🧪 测试纪律(详见 [测试方案.md](测试方案.md))

1. 红绿循环:先写失败测试 → 转绿 → 重构。
2. 每写一个函数/模块立刻单测;每写一个接口立刻集成测试。
3. **change 的测试全绿 + `rules` 100% 覆盖** 才算这刀完成。

## ✅ 交付铁律

change「做完」= 单元 + 集成(+可行的 E2E)全绿 + `rules` 100% 覆盖闸门过 + `openspec validate --strict` 过 + 文档/specs 同步 + 中文人话交付报告。

## 💬 给用户反馈

1. 用户**不读代码**:反馈用中文人话 + 1~2 个例子,**不贴代码**。
2. 测试报告:`✅ 15/15 通过` 或 `❌ …(失败:停用分类商品仍出现在 banner)`,失败讲业务影响。

---

## 🛑 越界即停 / 防债务

- 共识漂移 / 灰色地带 → 先停问。
- 写"临时方案"前先登记 [docs/tech-debt.md](docs/tech-debt.md);代码 TODO/FIXME 必带债务编号(`# DEBT-2026-MM-NNN`)。
- 安全债 🔴 不许 accepted:后台弱口令 + 公网,**演示完必须 `launchctl unload` 下线**。
- **删/重命名文档前**:grep 所有引用并修复,裸删禁止。

## 📐 防膨胀(任一触发强制拆)

① 本文件 >200 行 / architecture 总览 >80 行 ② 新人 onboard >15 分钟没懂 ③ 解释一个问题要横引 3+ ADR。

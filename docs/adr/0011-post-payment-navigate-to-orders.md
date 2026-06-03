# ADR-0011 · 支付成功后跳转:回推荐页 → 进订单页

- **Status**: Accepted
- **Date**: 2026-05-31
- **Tags**: car-app, ux, consensus-change

## 背景 Context

C11 公网上线后试用,用户提出:**点屏支付成功后,应进入「订单」页**,让用户立刻看到刚下的这笔单,而不是回到推荐页。

原行为是**共识锁定项**:
- [需求共识.md](../../需求共识.md) §1 车机端原文「点屏幕任意位置 = 支付成功 → 显示『支付成功』1.5 秒 → **返回推荐页**」。
- [ADR-0010](0010-car-order-write-api.md) 主链路描述、[user-stories.md](../user-stories.md) US-CAR-05 场景「点屏任意处支付成功并回推荐页」均写「回推荐页」。
- 代码 [`RidemallNavHost.kt`](../../android/app/src/main/java/space/hearagain/ridemall/ui/RidemallNavHost.kt) `onPaid` 原为 `popBackStack(HOME)`。

按 [CLAUDE.md](../../CLAUDE.md) §共识防漂移,锁定项不得擅改,须走「共识修改流程」。

## 决策 Decision

2026-05-31 经用户明文授权(AskUserQuestion 选「改成进订单页(改共识+记 ADR)」):

- 支付成功 → 显示「支付成功」1.5 秒 → **跳转到「订单」页(订单历史)**,而非返回推荐页。
- 跳转用 `navigate(ORDERS) { popUpTo(HOME) { inclusive=false }; launchSingleTop=true }`:清掉详情/确认页回退栈,栈变为 `HOME → ORDERS`,从订单页按返回/点「推荐」自然回到推荐页。
- 订单页 ViewModel 路由作用域、`init` 即 `load()`,新进入会重新拉取本机订单 → 刚下的单立即出现在列表顶部(倒序)。
- 据此**修订 [需求共识.md](../../需求共识.md) §1**(「返回推荐页」→「进入订单页」)+ **US-CAR-05 Gherkin**(对应场景与写入失败场景措辞)。

## 理由 Rationale

- **闭环更自然**:付完即见订单,给用户「单已生成」的确定反馈,胜过回到推荐页后还要自己点「订单」去找。
- **零新增成本**:订单页已存在(US-CAR-07),进入即自动加载,无需新页面或新接口。
- **失败路径不变**:写入失败 / 断网仍是「网络异常,不算成功、不跳转、不落单」(ADR-0010 第六条),只改成功路径的落点。

## 替代方案 Alternatives Considered

- **维持回推荐页**:符合原共识,但用户需手动找订单,体验更绕。→ 用户否。
- **停在二维码页显示成功不跳转**:无后续引导,用户不知下一步。→ 否。

## 后果 Consequences

- ✅ 支付成功落到订单页并高亮显示新单;返回栈干净。
- ⚠️ 本次为**导航行为变更**,属 Compose UI 层,JVM 单测不覆盖(挂 [DEBT-2026-05-007](../tech-debt.md)),以**模拟器实测**验收。
- 📎 取代:[需求共识.md](../../需求共识.md) §1「返回推荐页」一行 + US-CAR-05 场景;ADR-0010 主链路尾段「回推荐页」措辞以本 ADR 为准(历史 ADR 不回改,此处声明取代)。

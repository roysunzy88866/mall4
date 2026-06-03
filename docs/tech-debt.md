# 技术债台账(tech-debt)

> 已知问题 / 妥协的登记处。每条带编号 `DEBT-YYYY-MM-NNN`。修复后标 ✅ 闭合。
> 来源:2026-06-03 三个独立子 Agent 对 backend-foundation 的审查。

## DEBT-2026-06-001 · banner 未过滤停用分类(真 bug)🟠 MAJOR
- **位置**:`server/services/catalog_service.py` 的 `home_data` banner 循环。
- **问题**:停用某分类后,其商品已从「为你推荐」剔除,但引用该商品的 banner 仍出现在 `/api/home`(子 Agent 实测复现)。违反 `openspec/specs/catalog-browsing` 的「停用分类商品不出现」+ 后端需求 D5。
- **修复**:banner 循环里用已算好的 `active_ids` 判断 `product.category_id`,不在则跳过;补一条集成测试。
- **状态**:✅ 闭合(2026-06-03,随 order-checkout 修复:banner 循环加 `active_set` 过滤 + 回归测试 `test_home_banner_excludes_inactive_category`)。

## DEBT-2026-06-002 · 分类排序测试「假绿」🟠 MAJOR
- **位置**:`server/tests/integration/test_api_catalog.py::test_categories_returns_active_sorted`。
- **问题**:seed 里 `sort_order` 恰好＝id 顺序,SQL 即使错写成按 id 排也照样绿,没真正验证 spec 要求的「按 sort_order」。
- **修复**:测试里把某分类 `sort_order` 设成与 id 相反,断言返回顺序随 `sort_order` 变。
- **状态**:✅ 闭合(2026-06-03,测试改为真咬 sort_order:`test_categories_returns_active_sorted_by_sort_order`)。

## DEBT-2026-06-003 · 价格元→分换算的浮点 / 舍入隐患 🟡 MINOR
- **位置**:`server/seed.py` 的 `round(price_yuan * 100)`(将来「后台商品 CRUD」会复用此换算)。
- **问题**:`round()` 经浮点 + 银行家舍入,对 `X.X5` 结尾价可能偏 1 分。当前 seed 全整数价,未触发;后台录入任意价时会成真 bug。
- **修复**:把元→分换算抽进 `rules` 纯函数,用 `Decimal`,补 `X.X5` 价单测。
- **状态**:✅ 闭合(2026-06-03,admin-console 批1:换算抽进 `rules/money`(Decimal + `X.X5` 单测),seed 与车机接口改用它)。

## DEBT-2026-06-004 · 详情主图 / 图集语义未被测试钉住 🟡 MINOR
- **位置**:`server/api/routes.py` 详情图集、`catalog-browsing` spec「1~4 张、首张为主图」。
- **问题**:读侧未断言「首张=主图(sort_order 最小)」、未卡「最多 4 张」(写侧职责)。
- **修复**:补详情图序断言;数量上限留给「传图」写侧。状态:⏳ 未闭合。

## DEBT-2026-06-005 · 闭环 E2E 延后(已知,非缺陷)🟢 计划内
- 测试方案的「API 级闭环 E2E(浏览→下单→订单可见)」依赖 `POST /api/orders`,属 `order-checkout` change。`catalog-browsing` 的「完成」= 只读侧全绿;E2E 随 `order-checkout` 落地。状态:⏳ 计划内。

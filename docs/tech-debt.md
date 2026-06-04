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
- **修复**:补详情图序断言;数量上限留给「传图」写侧。**状态:✅ 闭合(2026-06-04,`test_api_detail_images.py` 断言首张=主图、按 `sort_order` 排;读侧查询本就 `ORDER BY sort_order, id`,现已被测试钉住)。**

## DEBT-2026-06-005 · 闭环 E2E 延后(已知,非缺陷)🟢 计划内
- 测试方案的「API 级闭环 E2E(浏览→下单→订单可见)」依赖 `POST /api/orders`,属 `order-checkout` change。`catalog-browsing` 的「完成」= 只读侧全绿;E2E 随 `order-checkout` 落地。**状态:✅ 闭合(2026-06-04,`order-checkout` 已落 `POST/GET /api/orders`;`cart-checkout` 车机端浏览→加购物车→下单→订单可见的完整闭环已在模拟器手测全过)。**

## DEBT-2026-06-006 · 文档断链 `docs/user-stories.md` 🟡 MINOR
- **位置**:`docs/prototype-wireframes.md` 顶部声明「行为/验收以 `user-stories.md` 为准」,但该文件不存在。
- **问题**:断链;线框文件指向一个不存在的真相源。当前车机端行为真相源实际是设计稿 + `openspec/specs/` + 需求共识,线框声明已与现实脱节。
- **修复**:二选一——① 把 `prototype-wireframes.md` 标「🗄 已归档·被设计稿取代」(其失效条件已写明:设计稿采用即失效),并删掉对 user-stories 的引用;② 或补建 `user-stories.md`。建议走 ①。
- **状态**:✅ 闭合(2026-06-04,`prototype-wireframes.md` 顶部标「🗄 已归档·被设计稿取代」+ 两处 `user-stories.md` 断链改指 `openspec/specs/`)。

## DEBT-2026-06-007 · 车机商品卡无真实图(列表接口不返图)🟡 MINOR
- **位置**:`server/api/routes.py` 的 `_product_json`(不含 image 字段);车机 `ui/components/ProductCard.kt` 用占位图块。
- **问题**:`/api/home` 的 recommended 与 `/api/categories/{id}/products` 不返回商品图 URL,故车机商品卡只能显示占位图标。当前 seed 商品图本就是 `placeholder.png`,视觉无差;但若日后要卡片显示真实商品图,需后端 list 接口补 `image`(取商品主图)字段。
- **修复**:后端 `_product_json` 增 `image`(商品主图 URL);车机 `ProductDto`/`Product` 加 imageUrl 并在卡片用 Coil 加载。属后端改动,留后续 change。
- **状态**:✅ 闭合(2026-06-04,change `product-images-and-catalog-ux`:后端 `_product_json` 增 `image` 主图字段、列表两接口带图;车机 `Product.imageUrl` + `ProductCard` 用 Coil `AsyncImage` 加载、无图回退占位;集成测试 `test_home_recommended_includes_main_image` / `test_category_products_include_main_image` / `test_product_without_image_returns_null_image` + 车机 `MappersTest` 钉住。真实图经 ops 步骤挂到公网现有商品)。

## DEBT-2026-06-008 · 车机字体用系统回退(非 Oxanium/Noto)🟢 MINOR
- **位置**:`android/app/.../ui/theme/Type.kt` 的 `BodyFamily`/`NumberFamily`。
- **问题**:设计稿要 Oxanium(数字)+ Noto Sans SC(中文),但本机无字体文件、沙箱网络取不到,暂用系统 sans-serif + Monospace 回退(CJK 回退设计 README 已许可;数字字体为次要视觉)。
- **修复**:把 Oxanium / Noto Sans SC 的 `.ttf` 放进 `android/app/src/main/assets/fonts/`,在 `Type.kt` 把 `BodyFamily`/`NumberFamily` 指过去(单点切换,已留好)。
- **状态**:⏳ 未闭合(见 car-storefront-browse design.md D10)。**阻塞:需 Oxanium / Noto Sans SC 的 `.ttf` 文件**(此前沙箱网络取不到)。把文件放进 `android/app/src/main/assets/fonts/`,`Type.kt` 的 `BodyFamily`/`NumberFamily` 指过去即可(单点切换已留好)。用户可提供或允许联网下载。

## DEBT-2026-06-011 · 后台编辑商品会清空描述 🟠 MAJOR
- **位置**:`server/admin/templates/products.html` 编辑表单;`server/admin/views.py:product_update`。
- **问题**:商品编辑表单**没有描述输入框**,但 `product_update` 读 `request.form.get("description", "")` 并写库 → 每次「保存」都把该商品描述清成空串。新建表单有描述、编辑没有,属遗漏。
- **修复**:编辑表单补 `description` 输入(预填 `p.description`);或 service 在 description 缺省时不覆盖。
- **状态**:✅ 闭合(2026-06-04,`products.html` 编辑表单补 `description` 输入框预填原描述;集成测试 `test_edit_form_prefills_description` + `test_update_product_keeps_description` 钉住)。

## DEBT-2026-06-010 · 品牌域名 mall4 未启用,现用 mall4-admin 🟢 MINOR · 计划内
- **位置**:公网部署 / `android/app/build.gradle.kts` release / 共识 §3 F6。
- **问题**:共识 2026-06-03 曾计划公网用品牌子域 `mall4.hearagain.space`,但该子域 DNS/隧道从未配置(2026-06-04 实测 `dig` 无记录、`curl` SSL 失败),实际部署在 `mall4-admin.hearagain.space`(Cloudflare,返 200)。文档/配置/共识已统一校正为 `mall4-admin`(见 [ADR-0013](adr/0013-public-domain-correction.md))。
- **修复(若要迁回品牌域 mall4)**:配 `mall4` 子域 DNS + Cloudflared 路由 → 改 release `API_BASE_URL` + 文档 → 重出 release APK。
- **状态**:⏳ 未闭合(低优;`mall4-admin` 当前可用,迁移非演示必须)。

## DEBT-2026-06-009 · 车机分支态步条/物流重置为灰 🟢 MINOR
- **位置**:`android/.../ui/orders/OrderDetailScreen.kt` 状态步条 + 物流时间线;`rules/lifecycle.py` logistics_nodes/stage_index 对分支态返回 -1/0。
- **问题**:订单进入分支态(退货审核中/已取消/已退款/退货被拒)后,stage_index=-1,步条与物流按主阶段重置为灰;实际该单可能曾走到已签收。分支态信息已由徽章+退货原因卡清晰展示,功能不受影响。
- **修复**:分支态时,步条/物流保留「曾达到的最高主阶段」(可存 max_stage 或据 delivered_at/cancelled_at 推断);或分支态单独画一条「售后时间线」。
- **状态**:✅ 闭合(2026-06-04,后端 `logistics_nodes`:退货类按曾已签收→物流全程到达、取消类→仅已下单;车机 `displayStageOf`:退货类显「已签收」全亮步条、取消类不画主步条只显徽章,不再「重置成灰」)。

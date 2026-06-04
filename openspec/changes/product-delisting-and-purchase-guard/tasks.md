## 1. 后端 · 商品上架状态 + 幂等迁移(数据层)

- [x] 1.1 `products` 建表 SQL 加 `is_active INTEGER NOT NULL DEFAULT 1`(新库直接带列)
- [x] 1.2 启动幂等迁移:`PRAGMA table_info(products)` 无 `is_active` 则 `ALTER TABLE products ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1`(存量库自动加列、默认在架);在 app 启动初始化处调用,幂等可重入
- [x] 1.3 集成测试:对"老结构(无该列)"的库跑迁移后,列存在且既有商品 `is_active=1`;重复跑不报错

## 2. 后端 · 下架商品对顾客隐藏(catalog-browsing)

- [x] 2.1 集成测试(红):下架某商品后 `/api/home` recommended 不含它、其 banner 不出现;`/api/categories/{id}/products` 不含它;`/api/products/{id}` 返回 404「商品已下架」;重新上架后恢复
- [x] 2.2 数据/业务层:推荐、分类商品、banner 候选查询加 `is_active=1` 过滤(复用「排除停用分类」同款位置)
- [x] 2.3 详情 service 对 `is_active=0` 抛 `ProductNotFound`(接口层已翻 404「商品已下架」,复用)
- [x] 2.4 转绿 + 既有 catalog 集成测试不回归

## 3. 后端 · 下单校验商品在架(ordering)

- [x] 3.1 `rules/ordering.py` 新增纯函数 `unavailable_items(items, available_ids)` → 返回不可用项;单测覆盖(全可用/部分不可用/全不可用),维持 `rules` 100%
- [x] 3.2 集成测试(红):整单含已下架/已删商品 → `POST /api/orders` 返回 409 + `{"error":"商品已下架","unavailable":[...]}` 且不建单;全在架 → 201;缺设备号/空单仍 400
- [x] 3.3 `order_service.place_order` 落库前:repo 批量取这批 id 的「存在且在架」集合 → 调 `unavailable_items` → 非空抛 `ProductUnavailable(items)`;价格仍用快照(不重取现价)
- [x] 3.4 `api/routes.py:create_order` 捕 `ProductUnavailable` → 409 + body;`InvalidOrder` 仍 400(不混淆)
- [x] 3.5 转绿 + 既有下单/快照集成测试不回归(「整单快照不受删除影响」仍成立)

## 4. 后端 · 后台下架/上架 + 受限删除(admin-console)

- [x] 4.1 集成测试(红):toggle 下架→上架 往返;受限删除——无订单引用→删成功,有订单引用→拒绝且不删 + 提示文案
- [x] 4.2 repo:`set_product_active(product_id, active)`、`product_has_order_refs(product_id)`
- [x] 4.3 `admin_catalog_service`:`toggle_product_active`;`delete_product` 改为先查引用,有引用抛 `ProductDeleteBlocked`,无则删
- [x] 4.4 `admin/views.py`:新增 `products/<id>/toggle` 路由;`product_delete` 捕 `ProductDeleteBlocked` 翻成提示;`products_page` 传 `is_active`
- [x] 4.5 `templates/products.html`:每行显示上架状态 + 「下架/上架」按钮(主操作),「删除」保留;被拒删除显示提示
- [x] 4.6 转绿 + 既有商品 CRUD / 推荐位集成测试不回归

## 5. 车机端 · 下单 409 友好处理 + 不可用详情可返回(car-checkout / car-browsing)

- [x] 5.1 纯解析 `parseUnavailableIds`(util)+ `OrderErrorTest`(红):从 409 体解析不可用 id;null/空/无匹配→空。(ViewModel 用具体 repo 难直测,按项目「纯逻辑可测」抽函数 + 既有 `removeItem` 已测)
- [x] 5.2 `StoreViewModel.onPayTap` 的 onFailure 按 `e is HttpException && code()==409` 分流:解析 unavailable → 关支付层、移出购物车对应项、退回购物车、置 `delistNotice`;普通失败仍 `payError`。新增 `StoreUiState.delistNotice` + `dismissDelistNotice()`
- [x] 5.3 `StoreScreen`:`delistNotice` → 品牌风 `DelistNoticeOverlay`(已为你移出);`DetailUnavailable` → `UnavailableView` 补「返回」按钮(回 `backFromDetail`)
- [x] 5.4 `./gradlew :app:testDebugUnitTest` 全绿(编译通过 + 含既有 Mappers/Format/Cart 不回归;OrderErrorTest 2/2)

## 6. 文档 · 共识 / ADR / API

- [x] 6.1 更新 `需求共识.md`:§1 后台商品管理加「下架/上架 + 受限删除」、§3 产品决策新增「商品上架状态」行(引 ADR-0014;扩范围明文授权已在本轮对话)
- [x] 6.2 新增 `docs/adr/0014-product-listing-state.md`(含取代旧 D8 说明)+ `docs/adr/README.md` 索引
- [x] 6.3 `docs/api.md`:补 `/admin/products/{id}/toggle`、下单 409「商品已下架」、详情对下架商品 404、受限删除、车机 409 处理
- [x] 6.4 无新增临时取舍 → 不登记技术债

## 7. 收口

- [x] 7.1 `pytest` 全绿(122 passed)+ `rules` 100% 覆盖闸门过;车机单测全绿(OrderErrorTest 2/2 + 不回归)
- [x] 7.2 `openspec validate product-delisting-and-purchase-guard --strict` 过
- [x] 7.3 中文人话交付报告(逐条对验收场景)
- [x] 7.4 (ops)已部署 mall4-admin(rsync+kickstart)+ 迁移自动加 is_active(prod 19/19 在架)+ 重出 release APK 装 ridemall-car;**公网实测**(真实 admin toggle):下架→详情 404 + 下单 409 + 上架还原(净零);**模拟器实测**:下架→品质生活 3→2 件商品消失、点旧卡→「商品已下架」+「返回」可退出(死页已修)。〔409 下单提示 overlay 由后端 prod 409 + 单测 + 编译保障,未单独走完整购买流手测〕

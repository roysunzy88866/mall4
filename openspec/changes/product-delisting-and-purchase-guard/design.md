## Context

商品无上架状态:`products` 仅有分类的 `is_active`,商品本身没有。后台只能硬删商品;下单 `place_order`(`server/services/order_service.py:16`)只跑纯输入校验(`rules.ordering.order_input_errors`),**不查商品是否存在/在架**,直接用车机快照落单。已删商品因此能"静默下单成功";详情 `GET /api/products/{id}` 对查不到的商品已返回 `404「商品已下架」`(`routes.py:124`),车机 `openDetail` 已把 404 映射为 `DetailUnavailable` 态——但该态在 `StoreScreen` 只渲染一行「商品已下架」、**无返回按钮**(死页观感)。
现有可复用资产:分类「停用→其商品不进推荐/banner」的过滤先例(DEBT-001,`catalog_service.py`);`ProductNotFound`→404 路径;后台分类 `toggle` 模式(`admin/views.py:categories/<id>/toggle`);订单行项已是**快照**(`order_items.product_name/image`,无 FK 到 products)→ 历史订单不受删除影响(ordering 现有「整单快照不受后续改动影响」已锁此约束)。

## Goals / Non-Goals

**Goals:**
- 商品有上架/下架可逆状态;下架商品对顾客**彻底隐藏**(推荐/分类/banner/详情)。
- 下单逐件校验商品存在且在架,不可用则**拒绝**并返回明确错误;车机据此给提示并移出购物车,不静默成功、不进死页。
- 后台「删除」降级为「下架」为主;永久删除仅限无订单引用的商品。
- 所有不可用/错误页有返回出口。

**Non-Goals:**
- 不做库存扣减 / 售罄(`stock` 仍仅展示)。
- 不做缺货登记 / 到货提醒。
- 不动下单**价格快照**规则(仍用车机上送快照价,不重取现价)。
- 车机切 tab 竞态(问题③)= 独立第二刀,不在本 change。

## Decisions

**D1 · 商品状态字段 + 幂等迁移**:`products` 加 `is_active INTEGER NOT NULL DEFAULT 1`。库已存在(prod 在飞)→ 启动时幂等迁移:`PRAGMA table_info(products)` 无 `is_active` 则 `ALTER TABLE products ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1`。默认 1 = 存量全部在架,零人工干预、安全。**备选**:独立状态表 —— 否决(单字段足够,避免 join 膨胀)。

**D2 · 下架 = 查询层过滤**:复用「排除停用分类」同款做法,在数据层/业务层把 `is_active=1` 作为顾客可见过滤条件:`/api/home` recommended、`/api/categories/{id}/products`、banner 候选均排除下架商品;详情 service 对 `is_active=0` 抛现有 `ProductNotFound` → 接口层仍 `404「商品已下架」`(复用既有路径,车机 `DetailUnavailable` 直接生效)。

**D3 · 下单可用性校验(service 查 + rule 判)**:`place_order` 在事务内、落库前 → `repo` 批量取这批 `product_id` 的「存在且在架」映射;抽纯规则 `ordering.unavailable_items(items, available_ids)` 返回不可用项(便于 100% 单测);非空 → 抛 `ProductUnavailable(names)`。接口层翻成 `409 {"error":"商品已下架","unavailable":[{id,name}...]}`。**不取现价**(只查存在/状态),不违反快照规则。**备选状态码**:404 —— 否决,选 **409**(语义=与当前商品状态冲突,车机据此与「网络异常」分流)。

**D4 · 删除降级为受限**:`admin_catalog_service.delete_product` 先查 `order_items` 是否引用该 `product_id`:有 → 抛 `ProductDeleteBlocked`(接口层提示「该商品已有订单,请改为下架」);无 → 真删。新增 `toggle_product_active(product_id)` + repo `set_product_active`。后台商品列表显示状态 + 「下架/上架」按钮(主操作),「删除」保留但受限。

**D5 · 车机 409 友好处理**:`StoreRepository.createOrder` 透出 `HttpException`;`StoreViewModel.onPayTap` 的 `onFailure` 分流:`e is HttpException && e.code()==409` → 解析 `unavailable` → 置新提示态(品牌风 overlay,如「部分商品已下架,已为你移出」)+ 从 `cart` 移除这些 `productId` + 关支付层退回购物车;其余失败仍走 `payError`。`StoreScreen` 的 `DetailUnavailable` 补一个返回按钮(回浏览)。

## Risks / Trade-offs

- **prod 迁移**:加列在公网库自动跑 → 默认 1、幂等、旧代码忽略该列;回滚无需删列。低风险,但碰 prod 前按部署纪律再确认授权。
- **与未归档 change 的冲突**:`product-images-and-catalog-ux` 也改了 catalog/admin/car-browsing。本刀 spec 增量**全用 `ADDED Requirements`(新增关注点,不 MODIFY 同名需求)→ 两刀无共享需求块、归档顺序无关**,规避 MODIFIED 合并丢内容的坑。
- **409 与现有 400 区分**:输入缺失仍 400(现有),商品不可用 409(新增);车机按 code 分流,避免都落进「网络异常」。
- **rules 覆盖率闸门**:可用性判定抽成纯规则 `unavailable_items`,有单测 → 维持 `rules` 100%;DB 查询在 repo/service,靠集成测试。

## Migration Plan

1. 加 `is_active` 列(幂等迁移,随后端启动/部署自动执行,默认在架)。
2. 后端逻辑 + 后台 UI + 车机改动(本 change 代码)。
3. ops(本 change 范围外):部署后端到 mall4-admin(迁移自动跑)、重出车机 release 包、模拟器实测。碰 prod 前确认授权。
- **回滚**:代码回退即可;`is_active` 列保留无害(旧代码不读)。

## Open Questions

(无 —— 下架语义/删除降级/切片已与用户拍板:下架=隐藏、删除改下架、本刀只做①②。)

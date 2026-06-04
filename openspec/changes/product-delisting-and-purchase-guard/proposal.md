## Why

商品没有「上架/下架」状态:后台只能硬删除商品,而下单接口对「商品是否存在/在架」零校验——删掉的商品能被购物车快照"静默下单成功",打开已删商品详情还会进一个无返回按钮的「商品已下架」死页。需要真正的下架能力 + 下单时对不可用商品的拦截与友好提示,堵住"买到不存在的东西"和"卡死"两个坑。

## What Changes

- **商品上架状态**:products 新增 `is_active`(默认在架);后台商品管理新增「下架/上架」可逆切换,商品列表显示状态。
- **下架=对顾客隐藏**:车机只读接口(`/api/home` recommended、`/api/categories/{id}/products`、首页 banner)排除下架商品;`/api/products/{id}` 对下架商品按 `404「商品已下架」`返回(等同查不到)。
- **下单拦截不可用商品**:`POST /api/orders` 逐件校验商品存在且在架,任一不满足 → 拒绝下单,返回 `409 {"error":"商品已下架","unavailable":[...]}`;不再静默落单。
- **BREAKING(后台行为)**:商品「硬删除」降级——仅当该商品**从未被任何订单引用**时才允许永久删除,否则拒绝并提示改用「下架」。日常下线走「下架」。
- **车机端容错**:下单收到 409「商品已下架」→ 弹品牌风提示 + 把对应商品移出购物车 + 退回购物车(不静默成功、不进死页);「商品已下架」详情页补返回按钮。

非破坏(数据):`is_active` 默认 1,存量商品全部保持在架;不动表结构其它部分、不改 seed 既有商品。

## Capabilities

### New Capabilities

(无)

### Modified Capabilities

- `catalog-browsing`: 推荐页 / 分类商品 / banner 排除下架商品;商品详情对下架商品视为不可见(404)。
- `ordering`: 下单 MUST 校验每件商品存在且在架,不可用则拒绝并返回可用性错误。
- `admin-console`: 商品管理新增「下架/上架」切换;「删除」降级为「仅无订单引用时可永久删除,否则拒绝」。
- `car-browsing`: 「商品已下架」不可用详情页 MUST 提供返回出口。
- `car-checkout`: 下单遇「商品已下架」MUST 给明确提示并将该商品移出购物车,不静默成功。

## Impact

- **后端**:`server/models/`(products 加 `is_active` + 启动幂等迁移 `ALTER TABLE ADD COLUMN`)、`server/repositories/catalog_repo.py`+`catalog_write_repo.py`(过滤在架 / toggle / 受限删除查引用)、`server/services/catalog_service.py`(列表/详情过滤)、`server/services/order_service.py`(下单查可用性,抛 `ProductUnavailable`)、`server/rules/ordering.py`(纯规则:给定可用性集合判可否下单,维持 100% 覆盖)、`server/api/routes.py`(详情/下单错误→HTTP 翻译,新增 409)、`server/admin/views.py`+`admin_catalog_service.py`+模板 `products.html`(下架/上架按钮 + 受限删除提示)。
- **车机端**:`StoreViewModel`(下单 409 分支:提示 overlay + 移出购物车 + 退回)、新增「商品已下架」提示 UI 态、`StoreScreen` 的 `DetailUnavailable` 补返回按钮。
- **数据/部署(本 change 代码范围外,后续 ops)**:迁移随后端部署自动跑(加列,默认在架,无人工干预);需重出车机 release 包。碰 prod 前再确认授权。
- **共识/ADR**:扩了 `需求共识.md`(新增商品上架状态生命周期)→ 本 change 一并更新共识相关条目 + 新增 `docs/adr/` 一条(为什么引入 `is_active`、下架=隐藏、删除降级为受限删除)。

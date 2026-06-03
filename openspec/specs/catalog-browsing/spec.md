# catalog-browsing Specification

## Purpose
TBD - created by archiving change backend-foundation. Update Purpose after archive.
## Requirements
### Requirement: 推荐页数据
系统 SHALL 通过 `GET /api/home` 返回推荐位 banner 列表与「为你推荐」商品列表。「为你推荐」MUST 为全部上架商品、按上架时间倒序、并排除停用分类下的商品。

#### Scenario: 正常返回推荐页
- **WHEN** 车机请求 `GET /api/home`
- **THEN** 返回 banner 列表(每个含商品名、描述节选、价格、主图 URL)与「为你推荐」商品列表(按上架时间倒序)

#### Scenario: 停用分类的商品不出现
- **WHEN** 某分类被停用,其下含若干商品
- **THEN** 这些商品不出现在「为你推荐」中

#### Scenario: 无商品空态
- **WHEN** 系统中没有任何上架商品
- **THEN** 「为你推荐」返回空列表(不报错)

### Requirement: 导航分类
系统 SHALL 通过 `GET /api/categories` 返回启用的分类,按排序字段升序;停用的分类 MUST 不返回。

#### Scenario: 返回启用分类
- **WHEN** 车机请求 `GET /api/categories`
- **THEN** 仅返回启用分类,按 `sort_order` 顺序排列

### Requirement: 分类商品
系统 SHALL 通过 `GET /api/categories/{id}/products` 返回该分类下的商品。

#### Scenario: 返回某分类商品
- **WHEN** 车机请求一个启用分类的商品列表
- **THEN** 返回该分类下全部商品

#### Scenario: 空分类
- **WHEN** 该分类下没有商品
- **THEN** 返回空列表(车机据此显示「该分类暂无商品」)

### Requirement: 商品详情
系统 SHALL 通过 `GET /api/products/{id}` 返回商品基本信息与图集(1~4 张,第一张为主图)。请求不存在或已删除的商品时,系统 MUST 返回「商品已下架」语义(HTTP 404)。

#### Scenario: 返回商品详情
- **WHEN** 车机请求一个存在商品的详情
- **THEN** 返回名称、价格、描述与图集(主图 + 最多 3 张细节图)

#### Scenario: 商品不存在或已删除
- **WHEN** 车机请求一个不存在或已删除的商品
- **THEN** 返回 HTTP 404 与「商品已下架」语义

### Requirement: Banner 文案自动套用商品信息
推荐位 banner 的展示文案 SHALL 自动取自其引用的商品:标题取商品名称、价格取商品价格、描述取商品描述节选。后台 MUST 不为 banner 单独录入文案。

#### Scenario: Banner 文案来自所引用商品
- **WHEN** 某 banner 引用商品 X
- **THEN** 该 banner 的标题等于 X 的名称、价格等于 X 的价格、描述为 X 描述的节选


## MODIFIED Requirements

### Requirement: 推荐页数据
系统 SHALL 通过 `GET /api/home` 返回推荐位 banner 列表与「为你推荐」商品列表。「为你推荐」MUST 为全部上架商品、按上架时间倒序、并排除停用分类下的商品。每个「为你推荐」商品 MUST 附带主图 URL 字段(`image`,取该商品图集首张;无图时为 `null`)。

#### Scenario: 正常返回推荐页
- **WHEN** 车机请求 `GET /api/home`
- **THEN** 返回 banner 列表(每个含商品名、描述节选、价格、主图 URL)与「为你推荐」商品列表(按上架时间倒序,每个含主图 URL 字段 `image`,可为 null)

#### Scenario: 停用分类的商品不出现
- **WHEN** 某分类被停用,其下含若干商品
- **THEN** 这些商品不出现在「为你推荐」中

#### Scenario: 无商品空态
- **WHEN** 系统中没有任何上架商品
- **THEN** 「为你推荐」返回空列表(不报错)

### Requirement: 分类商品
系统 SHALL 通过 `GET /api/categories/{id}/products` 返回该分类下的商品。每个商品 MUST 附带主图 URL 字段(`image`,取该商品图集首张;无图时为 `null`)。

#### Scenario: 返回某分类商品
- **WHEN** 车机请求一个启用分类的商品列表
- **THEN** 返回该分类下全部商品,每个含主图 URL 字段 `image`(可为 null)

#### Scenario: 空分类
- **WHEN** 该分类下没有商品
- **THEN** 返回空列表(车机据此显示「该分类暂无商品」)

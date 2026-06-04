## ADDED Requirements

### Requirement: 下架商品对顾客不可见
系统 SHALL 对车机顾客隐藏下架(`is_active=0`)的商品:`GET /api/home` 的「为你推荐」与 Banner 列表、`GET /api/categories/{id}/products` MUST NOT 包含下架商品;`GET /api/products/{id}` 对下架商品 MUST 返回 `404` 与 `{"error":"商品已下架"}`(等同查不到)。重新上架后 MUST 恢复可见。

#### Scenario: 下架商品不进推荐
- **WHEN** 某商品被下架,车机请求 `GET /api/home`
- **THEN** 「为你推荐」列表不含该商品

#### Scenario: 下架商品不进分类列表
- **WHEN** 某商品被下架,车机请求其所属分类的 `GET /api/categories/{id}/products`
- **THEN** 返回列表不含该商品

#### Scenario: 指向下架商品的 Banner 不出现
- **WHEN** 某推荐位 Banner 指向的商品被下架
- **THEN** `GET /api/home` 的 Banner 列表不含该 Banner(与「停用分类商品不进 Banner」同理)

#### Scenario: 下架商品详情视为不可见
- **WHEN** 车机请求一个已下架商品的 `GET /api/products/{id}`
- **THEN** 返回 `404` 与 `{"error":"商品已下架"}`

#### Scenario: 重新上架后恢复可见
- **WHEN** 某下架商品被重新上架
- **THEN** 它重新出现在推荐/分类列表中,详情接口正常返回该商品

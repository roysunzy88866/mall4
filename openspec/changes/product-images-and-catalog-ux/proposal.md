## Why

车机端商品卡只显示占位图标(列表接口不返商品图,DEBT-2026-06-007),商品视觉缺位;后台推荐位选择把全部商品平铺,选起来不合理;后台改了分类名/顺序后车机左侧导航不刷新。三项都直接影响演示观感与后台可用性。

## What Changes

- **商品图端到端**:列表接口 `/api/home`(recommended)与 `/api/categories/{id}/products` 返回商品主图;车机端商品卡有图显图、无图回退占位。闭合 DEBT-2026-06-007。
- **后台编辑商品可换图**:目前只有「新建商品」能传图,补上「编辑商品」也能上传/替换主图(替换语义:换图即覆盖旧主图)。
- **后台推荐位先选分类再选商品**:把「平铺全部商品」改为两级选择(先选分类 → 再选该类商品)。
- **车机切分类刷新分类列表**:切换到某分类 tab 时重新拉取分类,使后台改的分类名/顺序反映到左侧导航。

非破坏性:商品图字段为可选(`image` 可空),旧客户端忽略即可;不改数据库表结构、不改 seed。

## Capabilities

### New Capabilities

(无)

### Modified Capabilities

- `catalog-browsing`: 商品列表数据(推荐页 recommended、分类商品)新增返回商品主图 URL。
- `admin-console`: 商品管理「编辑」支持上传/替换主图;推荐位管理改为「先选分类再选该类商品」。
- `car-browsing`: 商品卡渲染真实商品图(无图回退占位);切换分类 tab 时刷新分类列表使导航反映后台改动。

## Impact

- **后端**:`server/api/routes.py`(`_product_json` 增 image)、`server/admin/views.py`(`product_update` 处理传图;`banners_page` 传分类)、`server/services/admin_catalog_service.py`(`update_product` 换图)、`server/repositories/catalog_write_repo.py`(新增 `delete_product_images`)、模板 `products.html` / `banners.html`。
- **车机端**:`data/api/Dto.kt`(`ProductDto.image`)、`model/Models.kt`(`Product.imageUrl`)、`data/repository/Mappers.kt`(`toModel(baseUrl)`)、`data/repository/StoreRepository.kt`(传 baseUrl)、`ui/components/ProductCard.kt`(Coil `AsyncImage`,Coil 已是依赖)、`viewmodel/StoreViewModel.kt`(`openCategory` 重拉分类)。
- **测试**:集成测试覆盖「列表返图」「编辑换图」;车机 `MappersTest` 覆盖映射加 baseUrl。`rules` 无新增纯逻辑,100% 覆盖闸门不受影响。
- **数据/部署(本 change 代码范围外,后续 ops)**:图片用现成绝对 URL(`ridemall.hearagain.space`),挂到公网现有商品(不改 seed、不重置数据);需部署后端到 mall4-admin + 一次性脚本按商品名设 image + 重出车机 release 包。碰 Mac mini 前再确认。

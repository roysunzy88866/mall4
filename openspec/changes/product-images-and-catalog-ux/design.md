## Context

车机端商品卡只显示占位图标(列表接口不返商品图,DEBT-2026-06-007);后台推荐位选择把全部商品平铺;后台改分类名/顺序后车机左导航不刷新。三项均为已就位架构上的增量,不动表结构、不动 seed。
现有可复用资产:`product_images` 表 + `catalog_repo.images_for_product`(取图集);后台单图上传辅助 `admin/views.py:_save_image`;车机 `StoreRepository.categories()` 已存在;Coil 已是依赖(`coil-compose:2.7.0`);图 URL 解析 `util/Format.kt`(绝对 URL 原样返,`FormatTest` 已覆盖)。

## Goals / Non-Goals

**Goals:**
- 列表接口 `/api/home`(recommended)、`/api/categories/{id}/products` 返回商品主图;车机卡片有图显图、无图占位。
- 后台编辑商品支持上传/替换主图(替换语义)。
- 后台推荐位「先选分类→再选该类商品」。
- 车机切分类 tab 时重拉分类,左导航反映后台名称/顺序。

**Non-Goals:**
- 不改数据库表结构、不改 seed_data。
- 不做多图编辑(只管主图);细节图集仍仅由新建/现有数据提供。
- 数据回填(给公网现有商品挂 URL)与部署不在本 change 代码范围,作为后续 ops 步骤。

## Decisions

**D1 · 主图字段**:`_product_json`(`server/api/routes.py:40`) 增 `image` 字段 = `images_for_product` 首张 URL,无图为 `null`。列表两接口因复用 `_product_json` 自动带图;详情接口 `images[]` 不变。
**D2 · 编辑换图 = 覆盖**:`update_product`(`admin_catalog_service.py:74`)加 `image_url=None` 参数;非空时先 `delete_product_images` 再 `insert_product_image`(新增 repo 方法 `catalog_write_repo.delete_product_images`),空则不动原图。接口层 `product_update`(`admin/views.py:192`)复用 `_save_image`,模板 `products.html` 编辑表单加 `enctype=multipart` + `<input type=file name=image>`。
**D3 · 推荐位两级选择**:`banners_page`(`admin/views.py:218`)额外传 `categories` 与按分类分组的可选商品;`banners.html` 改为「分类 `<select>` → 商品 `<select>`」,用内嵌 JSON(`data-*`)+ 小段原生 JS 联动第二个下拉(无新依赖)。后端 `banner_add` 入参不变(仍收 `product_id`)。
**D4 · 车机映射带 baseUrl**:`ProductDto` 加 `image`、`Product` 加 `imageUrl`;`Mappers.toModel` 改签名 `toModel(baseUrl)`,`imageUrl = resolveImageUrl(image, baseUrl)`(绝对 URL 原样、相对补全);`StoreRepository.home()/categoryProducts()` 传入 baseUrl。`ProductCard` 有 `imageUrl` 用 Coil `AsyncImage`,否则现有占位 Icon。
**D5 · 切分类刷新**:`StoreViewModel.openCategory`(:115)改为同时 `repo.categories()` + `repo.categoryProducts(id)`,成功后一并更新 `categories` 状态(左导航)与内容;失败走既有 NetworkError。

## Risks / Trade-offs

- **跨域图**:图用现成绝对 URL(`ridemall.hearagain.space`),该域名挂了则无图(已与用户确认接受;非自包含)。
- **`rules` 覆盖率闸门**:本刀不新增 `rules` 纯逻辑(主图取首张是 repo 查询,非规则),100% 闸门不受影响;新增行为靠集成测试(列表返图、编辑换图)+ 车机 `MappersTest`。
- **推荐位联动 JS**:服务端渲染 + 少量原生 JS,无前端框架;keep simple,避免引依赖。
- **编辑换图覆盖语义**:替换会删该商品所有 `product_images` 行(当前主图模型即单图);若未来要保留多图细节图,需改为「仅替换 sort_order=0 主图」——本刀按单主图处理,记为已知约束。

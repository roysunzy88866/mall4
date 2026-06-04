## 1. 后端 · 商品列表返回主图(catalog-browsing)

- [x] 1.1 集成测试(红):`/api/home` 的 recommended 与 `/api/categories/{id}/products` 每个商品含 `image` 字段;有图商品返 URL、无图商品返 `null`
- [x] 1.2 `server/api/routes.py:_product_json` 增 `image` = `catalog_repo.images_for_product` 首张 URL(无图 None);确认两列表接口复用后带图(经业务层 `main_image_urls`)
- [x] 1.3 转绿 + 既有 catalog 集成测试不回归

## 2. 后端 · 后台编辑商品换图(admin-console)

- [x] 2.1 `catalog_write_repo` 新增 `delete_product_images(conn, product_id)`
- [x] 2.2 集成测试(红):编辑商品上传新图 → 该商品主图被替换为新图;编辑不传图 → 原主图不变
- [x] 2.3 `admin_catalog_service.update_product` 加 `image_url=None` 参数:非空则先 `delete_product_images` 再 `insert_product_image`(sort_order=0),空则不动
- [x] 2.4 `admin/views.py:product_update` 复用 `_save_image` 取图 + 校验 + 传入 service;`templates/products.html` 编辑表单加 `enctype=multipart/form-data` + `<input type=file name=image>`
- [x] 2.5 转绿 + 既有商品 CRUD 集成测试不回归
- [x] 2.6 后台商品列表显示当前主图缩略图(`products_page` 传 images + `products.html` 渲染 `<img>`);测试 `test_products_page_shows_image_preview`

## 3. 后端 · 推荐位先选分类再选商品(admin-console)

- [x] 3.1 集成测试(红):推荐位页含两级下拉 + 内嵌按分类分组 JSON;某非推荐位商品被归到其分类分组下
- [x] 3.2 `admin/views.py:banners_page` 额外传 `categories` + 可选商品按分类分组
- [x] 3.3 `templates/banners.html` 改为两级选择(分类 `<select>` → 商品 `<select>`),内嵌分组 JSON + 小段原生 JS 联动(createElement,无 XSS);`banner_add` 入参不变
- [x] 3.4 转绿 + 既有推荐位 3~5 / 去重 / 排序集成测试不回归

## 4. 车机端 · 商品卡显示真实图(car-browsing)

- [x] 4.1 `MappersTest`(红):`ProductDto.toModel(baseUrl)` 映射 `image`(绝对 URL 原样 / 相对补全 / null→null)
- [x] 4.2 `data/api/Dto.kt`:`ProductDto` 加 `image: String? = null`;`model/Models.kt`:`Product` 加 `imageUrl: String? = null`
- [x] 4.3 `data/repository/Mappers.kt`:`toModel` 改签名带 `baseUrl`;`StoreRepository.categoryProducts()` 传 baseUrl;修复所有调用点
- [x] 4.4 `ui/components/ProductCard.kt`:`imageUrl` 非空用 Coil `AsyncImage`、空回退占位 Icon
- [x] 4.5 `./gradlew :app:testDebugUnitTest` 全绿(含既有 FormatTest/MappersTest 不回归)

## 5. 车机端 · 切分类刷新分类(car-browsing)

- [x] 5.1 `viewmodel/StoreViewModel.openCategory`:成功分支同时 `repo.categories()` + `repo.categoryProducts(id)`,一并更新 `categories`(左导航)与内容;失败走既有 NetworkError
- [ ] 5.2 模拟器手测:后台改某分类名/排序 → 车机点该分类 tab → 左导航名称/顺序更新(随 6.5 部署后一并验)

## 6. 收口

- [x] 6.1 `pytest` 全绿(107 passed)+ `rules` 100% 覆盖闸门过;车机单测全绿
- [x] 6.2 `openspec validate product-images-and-catalog-ux --strict` 过
- [x] 6.3 闭合 DEBT-2026-06-007(`docs/tech-debt.md` 标 ✅);另记 DEBT-011(编辑清空描述,题外发现)
- [x] 6.4 中文人话交付报告
- [ ] 6.5 (ops · 单独确认后)部署后端到 mall4-admin + 脚本按商品名挂图 URL + 重出车机 release 包 + 模拟器实测

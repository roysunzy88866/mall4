package space.hearagain.ridemall.model

/**
 * UI 层模型(与后端 DTO 分离,经 repository 显式映射)。价格已是可直接显示的 "¥459.00"。
 *
 * 注:`/api/home` 的 recommended 与 `/api/categories/{id}/products` 后端返回商品主图字段 `image`,
 * 映射为 [Product.imageUrl](已补全为绝对地址);为空时卡片回退占位图块。
 */
data class Product(
    val id: Int,
    val name: String,
    val priceLabel: String,
    val priceCents: Int,
    val description: String,
    val categoryId: Int,
    val imageUrl: String? = null,
)

data class Banner(
    val productId: Int,
    val title: String,
    val description: String,
    val priceLabel: String,
    val priceCents: Int,
    val imageUrl: String?,
)

data class NavCategory(
    val id: Int,
    val name: String,
)

data class HomeData(
    val banners: List<Banner>,
    val recommended: List<Product>,
)

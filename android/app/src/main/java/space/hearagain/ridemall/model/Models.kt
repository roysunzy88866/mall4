package space.hearagain.ridemall.model

/**
 * UI 层模型(与后端 DTO 分离,经 repository 显式映射)。价格已是可直接显示的 "¥459.00"。
 *
 * 注:`/api/home` 的 recommended 与 `/api/categories/{id}/products` 后端均不返商品图字段,
 * 故 [Product] 无 imageUrl,卡片用占位图块;只有 [Banner] 带真实图 URL。
 */
data class Product(
    val id: Int,
    val name: String,
    val priceLabel: String,
    val priceCents: Int,
    val description: String,
    val categoryId: Int,
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

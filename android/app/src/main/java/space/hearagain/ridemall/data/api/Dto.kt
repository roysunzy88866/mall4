package space.hearagain.ridemall.data.api

import com.squareup.moshi.Json

/**
 * 后端 JSON DTO,字段贴 server/api/routes.py 实测返回结构(已核实,不臆造)。
 * 用 Moshi KotlinJsonAdapterFactory(反射),无需 codegen。
 */
data class ProductDto(
    val id: Int,
    val name: String,
    val price: String,
    @Json(name = "price_cents") val priceCents: Int,
    val description: String?,
    @Json(name = "category_id") val categoryId: Int,
)

data class BannerDto(
    @Json(name = "product_id") val productId: Int,
    val title: String,
    val description: String?,
    val price: String,
    @Json(name = "price_cents") val priceCents: Int,
    val image: String?,
)

data class HomeDto(
    val banners: List<BannerDto>,
    val recommended: List<ProductDto>,
)

data class CategoryDto(
    val id: Int,
    val name: String,
)

package space.hearagain.ridemall.data.api

import com.squareup.moshi.Json

/** 详情:product 字段 + images 列表(贴 server/api/routes.py 实测)。 */
data class ProductDetailDto(
    val id: Int,
    val name: String,
    val price: String,
    @Json(name = "price_cents") val priceCents: Int,
    val description: String?,
    @Json(name = "category_id") val categoryId: Int,
    val images: List<String> = emptyList(),
)

/** 下单请求体。 */
data class CreateOrderBody(val items: List<CreateOrderItemDto>)

data class CreateOrderItemDto(
    @Json(name = "product_id") val productId: Int,
    val name: String,
    val image: String?,
    @Json(name = "price_cents") val priceCents: Int,
    val qty: Int,
)

/** 订单返回(POST 201 / GET 列表)。 */
data class OrderDto(
    val id: Int,
    @Json(name = "device_id") val deviceId: String?,
    val status: String,
    @Json(name = "created_at") val createdAt: String,
    val total: String,
    @Json(name = "total_cents") val totalCents: Int,
    val items: List<OrderItemDto> = emptyList(),
)

data class OrderItemDto(
    @Json(name = "product_id") val productId: Int,
    val name: String,
    val image: String?,
    val price: String,
    @Json(name = "price_cents") val priceCents: Int,
    val qty: Int,
)

package space.hearagain.ridemall.data.repository

import space.hearagain.ridemall.data.api.CreateOrderBody
import space.hearagain.ridemall.data.api.CreateOrderItemDto
import space.hearagain.ridemall.data.api.OrderDto
import space.hearagain.ridemall.data.api.OrderItemDto
import space.hearagain.ridemall.data.api.ProductDetailDto
import space.hearagain.ridemall.model.Order
import space.hearagain.ridemall.model.OrderItem
import space.hearagain.ridemall.model.ProductDetail
import space.hearagain.ridemall.util.orderStatusLabel
import space.hearagain.ridemall.util.priceLabel
import space.hearagain.ridemall.util.resolveImageUrl

fun ProductDetailDto.toModel(baseUrl: String): ProductDetail = ProductDetail(
    id = id,
    name = name,
    priceLabel = priceLabel(price),
    priceCents = priceCents,
    description = description ?: "",
    categoryId = categoryId,
    images = images.mapNotNull { resolveImageUrl(it, baseUrl) },
)

fun OrderItemDto.toModel(baseUrl: String): OrderItem = OrderItem(
    productId = productId ?: 0,  // 已删商品快照 product_id=null;展示不用此字段,占位 0
    name = name,
    imageUrl = resolveImageUrl(image, baseUrl),
    priceLabel = priceLabel(price),
    priceCents = priceCents,
    qty = qty,
)

fun OrderDto.toModel(baseUrl: String): Order = Order(
    id = id,
    status = status,
    statusLabel = orderStatusLabel(status),
    createdAt = createdAt,
    totalLabel = priceLabel(total),
    items = items.map { it.toModel(baseUrl) },
    logistics = logistics.map {
        space.hearagain.ridemall.model.LogisticsNode(it.label, it.reached, it.at)
    },
    canCancel = canCancel,
    canReturn = canReturn,
    returnReason = returnReason,
)

/** 下单草稿行 → 请求体(image 原样上送,后端存什么车机就显示什么)。 */
fun List<OrderItem>.toCreateBody(): CreateOrderBody = CreateOrderBody(
    items = map {
        CreateOrderItemDto(
            productId = it.productId,
            name = it.name,
            image = it.imageUrl,
            priceCents = it.priceCents,
            qty = it.qty,
        )
    },
)

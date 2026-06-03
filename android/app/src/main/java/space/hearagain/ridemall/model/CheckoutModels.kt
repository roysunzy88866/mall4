package space.hearagain.ridemall.model

/** 商品详情(含图集,1~4 张,首张主图)。 */
data class ProductDetail(
    val id: Int,
    val name: String,
    val priceLabel: String,
    val priceCents: Int,
    val description: String,
    val categoryId: Int,
    val images: List<String>,
)

/** 购物车一行(车机本地临时态,自带图快照)。 */
data class CartItem(
    val productId: Int,
    val name: String,
    val priceLabel: String,
    val priceCents: Int,
    val imageUrl: String?,
    val qty: Int,
    val selected: Boolean = true,
)

/** 下单草稿一行(进确认页锁定的价格快照,A8)。 */
data class OrderItem(
    val productId: Int,
    val name: String,
    val imageUrl: String?,
    val priceLabel: String,
    val priceCents: Int,
    val qty: Int,
)

/** 确认页锁定的整单草稿。 */
data class OrderDraft(
    val items: List<OrderItem>,
) {
    val totalCents: Int get() = items.sumOf { it.priceCents * it.qty }
    val totalLabel: String get() = "¥%.2f".format(totalCents / 100.0)
}

/** 已下单订单(从后端拉)。 */
data class Order(
    val id: Int,
    val statusLabel: String,
    val createdAt: String,
    val totalLabel: String,
    val items: List<OrderItem>,
)

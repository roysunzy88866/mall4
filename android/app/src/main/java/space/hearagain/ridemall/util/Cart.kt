package space.hearagain.ridemall.util

import space.hearagain.ridemall.model.CartItem
import space.hearagain.ridemall.model.OrderItem

/** 购物车纯逻辑(无 IO,可单测)。上限见共识 F11。 */
const val CART_MAX_KINDS = 20
const val CART_MAX_QTY = 99
const val CART_MIN_QTY = 1

fun clampQty(qty: Int): Int = qty.coerceIn(CART_MIN_QTY, CART_MAX_QTY)

/** 加入购物车:同商品累加(夹 99);新种类受 20 上限;到顶则原样返回。 */
fun addToCart(cart: List<CartItem>, item: CartItem): List<CartItem> {
    val idx = cart.indexOfFirst { it.productId == item.productId }
    if (idx >= 0) {
        val cur = cart[idx]
        return cart.toMutableList().apply {
            this[idx] = cur.copy(qty = clampQty(cur.qty + item.qty))
        }
    }
    if (cart.size >= CART_MAX_KINDS) return cart
    return cart + item.copy(qty = clampQty(item.qty), selected = true)
}

fun setQty(cart: List<CartItem>, productId: Int, qty: Int): List<CartItem> =
    cart.map { if (it.productId == productId) it.copy(qty = clampQty(qty)) else it }

fun removeItem(cart: List<CartItem>, productId: Int): List<CartItem> =
    cart.filterNot { it.productId == productId }

fun toggleSelected(cart: List<CartItem>, productId: Int): List<CartItem> =
    cart.map { if (it.productId == productId) it.copy(selected = !it.selected) else it }

fun setAllSelected(cart: List<CartItem>, selected: Boolean): List<CartItem> =
    cart.map { it.copy(selected = selected) }

/** 勾选项合计(分)。 */
fun selectedTotalCents(cart: List<CartItem>): Int =
    cart.filter { it.selected }.sumOf { it.priceCents * it.qty }

/** 角标数 = 全部行数量之和。 */
fun cartBadgeCount(cart: List<CartItem>): Int = cart.sumOf { it.qty }

/** 勾选项 → 下单草稿行(价格快照)。 */
fun selectedToOrderItems(cart: List<CartItem>): List<OrderItem> =
    cart.filter { it.selected }.map {
        OrderItem(
            productId = it.productId,
            name = it.name,
            imageUrl = it.imageUrl,
            priceLabel = it.priceLabel,
            priceCents = it.priceCents,
            qty = it.qty,
        )
    }

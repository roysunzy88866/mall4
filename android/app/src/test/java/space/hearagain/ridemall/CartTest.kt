package space.hearagain.ridemall

import org.junit.Assert.assertEquals
import org.junit.Test
import space.hearagain.ridemall.model.CartItem
import space.hearagain.ridemall.util.addToCart
import space.hearagain.ridemall.util.cartBadgeCount
import space.hearagain.ridemall.util.clampQty
import space.hearagain.ridemall.util.selectedToOrderItems
import space.hearagain.ridemall.util.selectedTotalCents
import space.hearagain.ridemall.util.setQty
import space.hearagain.ridemall.util.toggleSelected

class CartTest {
    private fun item(id: Int, cents: Int, qty: Int = 1) =
        CartItem(id, "商品$id", "¥%.2f".format(cents / 100.0), cents, "/u/$id.png", qty)

    @Test fun add_merges_same_product() {
        var cart = addToCart(emptyList(), item(1, 3900, 1))
        cart = addToCart(cart, item(1, 3900, 1))
        assertEquals(1, cart.size)
        assertEquals(2, cart[0].qty)
    }

    @Test fun add_new_kinds_until_20_then_stop() {
        var cart = emptyList<CartItem>()
        for (i in 1..25) cart = addToCart(cart, item(i, 100, 1))
        assertEquals(20, cart.size) // 21~25 被上限挡住
    }

    @Test fun qty_clamped_1_to_99() {
        assertEquals(99, clampQty(150))
        assertEquals(1, clampQty(0))
        assertEquals(50, clampQty(50))
        var cart = addToCart(emptyList(), item(1, 100, 1))
        cart = setQty(cart, 1, 999)
        assertEquals(99, cart[0].qty)
    }

    @Test fun add_existing_caps_at_99() {
        var cart = addToCart(emptyList(), item(1, 100, 90))
        cart = addToCart(cart, item(1, 100, 50))
        assertEquals(99, cart[0].qty)
    }

    @Test fun selected_total_and_toggle() {
        var cart = listOf(item(1, 3900, 2), item(2, 3500, 1))
        assertEquals(3900 * 2 + 3500, selectedTotalCents(cart)) // 11300
        cart = toggleSelected(cart, 2) // 取消勾选 2
        assertEquals(3900 * 2, selectedTotalCents(cart)) // 7800
    }

    @Test fun badge_is_sum_of_qty() {
        val cart = listOf(item(1, 100, 2), item(2, 100, 3))
        assertEquals(5, cartBadgeCount(cart))
    }

    @Test fun selected_to_order_items_snapshots_only_selected() {
        val cart = listOf(item(1, 3900, 2), item(2, 3500, 1).copy(selected = false))
        val draft = selectedToOrderItems(cart)
        assertEquals(1, draft.size)
        assertEquals(1, draft[0].productId)
        assertEquals(2, draft[0].qty)
        assertEquals(3900, draft[0].priceCents)
    }
}

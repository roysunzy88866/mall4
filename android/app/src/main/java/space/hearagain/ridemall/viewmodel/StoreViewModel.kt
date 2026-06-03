package space.hearagain.ridemall.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import space.hearagain.ridemall.data.ServiceLocator
import space.hearagain.ridemall.data.repository.StoreRepository
import space.hearagain.ridemall.model.CartItem
import space.hearagain.ridemall.model.HomeData
import space.hearagain.ridemall.model.NavCategory
import space.hearagain.ridemall.model.Order
import space.hearagain.ridemall.model.OrderDraft
import space.hearagain.ridemall.model.Product
import space.hearagain.ridemall.model.ProductDetail
import space.hearagain.ridemall.util.addToCart
import space.hearagain.ridemall.util.removeItem
import space.hearagain.ridemall.util.selectedToOrderItems
import space.hearagain.ridemall.util.setAllSelected
import space.hearagain.ridemall.util.setQty
import space.hearagain.ridemall.util.toggleSelected

const val PAY_INITIAL_SECONDS = 179 // 共识 F5:初始 179 秒,显示 02:59

sealed interface Route {
    data object Home : Route
    data class Category(val id: Int, val name: String) : Route
    data class Detail(val productId: Int) : Route
    data object Cart : Route
    data object Confirm : Route
    data object Orders : Route
    data class OrderDetail(val orderId: Int) : Route
}

sealed interface Overlay {
    data object None : Overlay
    data object Pay : Overlay
    data object Success : Overlay
}

sealed interface ContentState {
    data object Loading : ContentState
    data class HomeLoaded(val data: HomeData) : ContentState
    data class CategoryLoaded(val products: List<Product>) : ContentState
    data class DetailLoaded(val detail: ProductDetail) : ContentState
    data object DetailUnavailable : ContentState
    data object CartView : ContentState
    data object ConfirmView : ContentState
    data class OrdersLoaded(val orders: List<Order>) : ContentState
    data class OrderDetailLoaded(val order: Order) : ContentState
    data object NetworkError : ContentState
}

data class StoreUiState(
    val categories: List<NavCategory> = emptyList(),
    val route: Route = Route.Home,
    val content: ContentState = ContentState.Loading,
    val cart: List<CartItem> = emptyList(),
    val draft: OrderDraft? = null,
    val overlay: Overlay = Overlay.None,
    val payRemainSec: Int = PAY_INITIAL_SECONDS,
    val payTimedOut: Boolean = false,
    val payError: Boolean = false,
    val placing: Boolean = false,
)

/**
 * 商城状态机:浏览(Home/Category/Detail)+ 购物车 + 下单闭环(Confirm/Pay/Success)+ 订单历史。
 */
class StoreViewModel(private val repo: StoreRepository) : ViewModel() {

    private val _state = MutableStateFlow(StoreUiState())
    val state: StateFlow<StoreUiState> = _state.asStateFlow()

    private var browseRoute: Route = Route.Home
    private var confirmBack: Route = Route.Cart
    private var countdownJob: Job? = null

    init {
        loadInitial()
    }

    // ---- 浏览 ----
    fun loadInitial() {
        _state.update { it.copy(route = Route.Home, content = ContentState.Loading) }
        browseRoute = Route.Home
        viewModelScope.launch {
            runCatching {
                val cats = repo.categories(); val home = repo.home(); cats to home
            }.onSuccess { (cats, home) ->
                _state.update { it.copy(categories = cats, route = Route.Home, content = ContentState.HomeLoaded(home)) }
            }.onFailure { _state.update { it.copy(content = ContentState.NetworkError) } }
        }
    }

    fun openHome() {
        _state.update { it.copy(route = Route.Home, content = ContentState.Loading) }
        browseRoute = Route.Home
        viewModelScope.launch {
            runCatching {
                val cats = _state.value.categories.ifEmpty { repo.categories() }; cats to repo.home()
            }.onSuccess { (cats, home) ->
                _state.update { it.copy(categories = cats, route = Route.Home, content = ContentState.HomeLoaded(home)) }
            }.onFailure { _state.update { it.copy(content = ContentState.NetworkError) } }
        }
    }

    fun openCategory(category: NavCategory) {
        val route = Route.Category(category.id, category.name)
        _state.update { it.copy(route = route, content = ContentState.Loading) }
        browseRoute = route
        viewModelScope.launch {
            runCatching { repo.categoryProducts(category.id) }
                .onSuccess { products -> _state.update { it.copy(content = ContentState.CategoryLoaded(products)) } }
                .onFailure { _state.update { it.copy(content = ContentState.NetworkError) } }
        }
    }

    // ---- 详情 ----
    fun openDetail(productId: Int) {
        _state.update { it.copy(route = Route.Detail(productId), content = ContentState.Loading) }
        viewModelScope.launch {
            runCatching { repo.productDetail(productId) }
                .onSuccess { d -> _state.update { it.copy(content = ContentState.DetailLoaded(d)) } }
                .onFailure { e ->
                    val unavailable = e is HttpException && e.code() == 404
                    _state.update { it.copy(content = if (unavailable) ContentState.DetailUnavailable else ContentState.NetworkError) }
                }
        }
    }

    fun backFromDetail() = backToBrowse()
    fun backFromCart() = backToBrowse()

    private fun backToBrowse() {
        when (val b = browseRoute) {
            is Route.Category -> openCategory(NavCategory(b.id, b.name))
            else -> openHome()
        }
    }

    // ---- 购物车 ----
    fun addDetailToCart(detail: ProductDetail) {
        val item = CartItem(
            productId = detail.id, name = detail.name, priceLabel = detail.priceLabel,
            priceCents = detail.priceCents, imageUrl = detail.images.firstOrNull(), qty = 1,
        )
        _state.update { it.copy(cart = addToCart(it.cart, item)) }
    }

    fun openCart() {
        _state.update { it.copy(route = Route.Cart, content = ContentState.CartView) }
    }

    fun setCartQty(productId: Int, qty: Int) =
        _state.update { it.copy(cart = setQty(it.cart, productId, qty)) }

    fun removeFromCart(productId: Int) =
        _state.update { it.copy(cart = removeItem(it.cart, productId)) }

    fun toggleCartSelected(productId: Int) =
        _state.update { it.copy(cart = toggleSelected(it.cart, productId)) }

    fun setAllCartSelected(selected: Boolean) =
        _state.update { it.copy(cart = setAllSelected(it.cart, selected)) }

    // ---- 确认 / 支付 / 成功 ----
    fun checkoutSelected() {
        val items = selectedToOrderItems(_state.value.cart)
        if (items.isEmpty()) return
        confirmBack = Route.Cart
        _state.update { it.copy(route = Route.Confirm, content = ContentState.ConfirmView, draft = OrderDraft(items)) }
    }

    fun buyNow(detail: ProductDetail) {
        val items = selectedToOrderItems(
            listOf(CartItem(detail.id, detail.name, detail.priceLabel, detail.priceCents, detail.images.firstOrNull(), 1)),
        )
        confirmBack = Route.Detail(detail.id)
        _state.update { it.copy(route = Route.Confirm, content = ContentState.ConfirmView, draft = OrderDraft(items)) }
    }

    fun backFromConfirm() {
        when (val b = confirmBack) {
            is Route.Detail -> openDetail(b.productId)
            else -> openCart()
        }
    }

    fun goPay() {
        _state.update { it.copy(overlay = Overlay.Pay, payRemainSec = PAY_INITIAL_SECONDS, payTimedOut = false, payError = false) }
        startCountdown()
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_state.value.payRemainSec > 0 && _state.value.overlay is Overlay.Pay) {
                delay(1000)
                _state.update { it.copy(payRemainSec = (it.payRemainSec - 1).coerceAtLeast(0)) }
            }
            if (_state.value.overlay is Overlay.Pay) _state.update { it.copy(payTimedOut = true) }
        }
    }

    /** 点屏 = 支付:先落库再成功(D4)。归零后点屏仍可成功(F5)。 */
    fun onPayTap() {
        if (_state.value.placing) return
        val draft = _state.value.draft ?: return
        _state.update { it.copy(placing = true, payError = false) }
        viewModelScope.launch {
            runCatching { repo.createOrder(draft.items) }
                .onSuccess { order ->
                    countdownJob?.cancel()
                    val orderedIds = draft.items.map { it.productId }.toSet()
                    _state.update {
                        it.copy(
                            placing = false,
                            overlay = Overlay.Success,
                            cart = it.cart.filterNot { c -> c.productId in orderedIds },
                        )
                    }
                }
                .onFailure { _state.update { it.copy(placing = false, payError = true) } }
        }
    }

    fun finishSuccess() {
        _state.update { it.copy(overlay = Overlay.None) }
        openOrders()
    }

    // ---- 订单历史 ----
    fun openOrders() {
        _state.update { it.copy(route = Route.Orders, content = ContentState.Loading) }
        viewModelScope.launch {
            runCatching { repo.orders() }
                .onSuccess { orders -> _state.update { it.copy(content = ContentState.OrdersLoaded(orders)) } }
                .onFailure { _state.update { it.copy(content = ContentState.NetworkError) } }
        }
    }

    fun openOrderDetail(orderId: Int) {
        _state.update { it.copy(route = Route.OrderDetail(orderId), content = ContentState.Loading) }
        viewModelScope.launch {
            runCatching { repo.orderDetail(orderId) }
                .onSuccess { o -> _state.update { it.copy(content = ContentState.OrderDetailLoaded(o)) } }
                .onFailure { _state.update { it.copy(content = ContentState.NetworkError) } }
        }
    }

    fun backFromOrderDetail() = openOrders()

    fun retry() {
        if (_state.value.categories.isEmpty()) { loadInitial(); return }
        when (val r = _state.value.route) {
            is Route.Home -> openHome()
            is Route.Category -> openCategory(NavCategory(r.id, r.name))
            is Route.Detail -> openDetail(r.productId)
            is Route.Orders -> openOrders()
            is Route.OrderDetail -> openOrderDetail(r.orderId)
            else -> openHome()
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StoreViewModel(ServiceLocator.repository) as T
    }
}

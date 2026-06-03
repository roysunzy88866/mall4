package space.hearagain.ridemall.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import space.hearagain.ridemall.ui.cart.CartScreen
import space.hearagain.ridemall.ui.category.CategoryScreen
import space.hearagain.ridemall.ui.checkout.ConfirmScreen
import space.hearagain.ridemall.ui.checkout.PayOverlay
import space.hearagain.ridemall.ui.checkout.SuccessOverlay
import space.hearagain.ridemall.ui.common.NetworkErrorOverlay
import space.hearagain.ridemall.ui.detail.DetailScreen
import space.hearagain.ridemall.ui.home.HomeScreen
import space.hearagain.ridemall.ui.orders.OrdersScreen
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmDimens
import space.hearagain.ridemall.ui.theme.RmType
import space.hearagain.ridemall.util.cartBadgeCount
import space.hearagain.ridemall.viewmodel.ContentState
import space.hearagain.ridemall.viewmodel.Overlay
import space.hearagain.ridemall.viewmodel.Route
import space.hearagain.ridemall.viewmodel.StoreViewModel

@Composable
fun StoreScreen(
    modifier: Modifier = Modifier,
    viewModel: StoreViewModel = viewModel(factory = StoreViewModel.Factory()),
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavRail(
                categories = state.categories,
                route = state.route,
                onHome = viewModel::openHome,
                onCategory = viewModel::openCategory,
                onOrders = viewModel::openOrders,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(RmColor.Bg)
                    .padding(
                        start = RmDimens.ContentPadH,
                        end = RmDimens.ContentPadH,
                        top = RmDimens.ContentPadTop,
                        bottom = RmDimens.ContentPadBottom,
                    ),
            ) {
                when (val content = state.content) {
                    is ContentState.Loading -> LoadingView()
                    is ContentState.HomeLoaded -> HomeScreen(
                        data = content.data,
                        onProductClick = { viewModel.openDetail(it.id) },
                        onBannerClick = { viewModel.openDetail(it) },
                    )
                    is ContentState.CategoryLoaded -> CategoryScreen(
                        categoryName = (state.route as? Route.Category)?.name ?: "",
                        products = content.products,
                        onProductClick = { viewModel.openDetail(it.id) },
                    )
                    is ContentState.DetailLoaded -> DetailScreen(
                        detail = content.detail,
                        onBack = viewModel::backFromDetail,
                        onAddToCart = { viewModel.addDetailToCart(content.detail) },
                        onBuyNow = { viewModel.buyNow(content.detail) },
                    )
                    is ContentState.DetailUnavailable -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("商品已下架", style = RmType.EmptyText, color = RmColor.Text2)
                    }
                    is ContentState.CartView -> CartScreen(
                        cart = state.cart,
                        onBack = viewModel::backFromCart,
                        onSetQty = viewModel::setCartQty,
                        onRemove = viewModel::removeFromCart,
                        onToggle = viewModel::toggleCartSelected,
                        onSelectAll = viewModel::setAllCartSelected,
                        onCheckout = viewModel::checkoutSelected,
                    )
                    is ContentState.ConfirmView -> state.draft?.let {
                        ConfirmScreen(draft = it, onBack = viewModel::backFromConfirm, onPay = viewModel::goPay)
                    }
                    is ContentState.OrdersLoaded -> OrdersScreen(content.orders)
                    is ContentState.NetworkError -> {}
                }

                // 右上角购物车入口 + 角标(覆盖层出现时隐藏)
                if (state.overlay is Overlay.None && state.content !is ContentState.CartView) {
                    CartFab(
                        count = cartBadgeCount(state.cart),
                        onClick = viewModel::openCart,
                        modifier = Modifier.align(Alignment.TopEnd),
                    )
                }
            }
        }

        when (state.overlay) {
            is Overlay.Pay -> state.draft?.let {
                PayOverlay(
                    totalLabel = it.totalLabel,
                    remainSec = state.payRemainSec,
                    timedOut = state.payTimedOut,
                    payError = state.payError,
                    onTap = viewModel::onPayTap,
                )
            }
            is Overlay.Success -> SuccessOverlay(onDone = viewModel::finishSuccess)
            is Overlay.None -> if (state.content is ContentState.NetworkError) {
                NetworkErrorOverlay(onRetry = viewModel::retry)
            }
        }
    }
}

@Composable
private fun CartFab(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(RmColor.Card)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.ShoppingCart, "购物车", tint = RmColor.Accent, modifier = Modifier.size(34.dp))
        }
        if (count > 0) {
            Box(
                Modifier.align(Alignment.TopEnd).size(30.dp).clip(CircleShape).background(RmColor.Accent),
                contentAlignment = Alignment.Center,
            ) {
                Text("$count", style = RmType.CardLink, color = RmColor.AccentInk)
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = RmColor.Accent, strokeWidth = 4.dp)
    }
}

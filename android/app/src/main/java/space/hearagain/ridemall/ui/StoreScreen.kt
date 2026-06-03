package space.hearagain.ridemall.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import space.hearagain.ridemall.ui.category.CategoryScreen
import space.hearagain.ridemall.ui.common.NetworkErrorOverlay
import space.hearagain.ridemall.ui.common.OrdersPlaceholder
import space.hearagain.ridemall.ui.home.HomeScreen
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmDimens
import space.hearagain.ridemall.viewmodel.ContentState
import space.hearagain.ridemall.viewmodel.Route
import space.hearagain.ridemall.viewmodel.StoreViewModel

/**
 * 商城根:左侧 NavRail + 右侧内容区;网络异常覆盖层盖在最上层(连导航一起盖住)。
 */
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
                        onProductClick = { /* 第二刀:进详情 */ },
                        onBannerClick = { /* 第二刀:进详情 */ },
                    )
                    is ContentState.CategoryLoaded -> {
                        val name = (state.route as? Route.Category)?.name ?: ""
                        CategoryScreen(
                            categoryName = name,
                            products = content.products,
                            onProductClick = { /* 第二刀:进详情 */ },
                        )
                    }
                    is ContentState.OrdersPlaceholder -> OrdersPlaceholder()
                    is ContentState.NetworkError -> { /* 由覆盖层处理 */ }
                }
            }
        }

        if (state.content is ContentState.NetworkError) {
            NetworkErrorOverlay(onRetry = viewModel::retry)
        }
    }
}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = RmColor.Accent, strokeWidth = 4.dp)
    }
}

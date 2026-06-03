package space.hearagain.ridemall.ui.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import space.hearagain.ridemall.ui.components.EmptyState

/** 订单页本刀占位(第二刀实现真订单历史)。点导航「订单」不报错,显示占位空态。 */
@Composable
fun OrdersPlaceholder(modifier: Modifier = Modifier) {
    EmptyState(
        text = "订单功能即将上线",
        icon = Icons.Filled.ReceiptLong,
        modifier = modifier.fillMaxSize(),
    )
}

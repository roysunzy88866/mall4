package space.hearagain.ridemall.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 导航项图标(线性图标等价替换,见设计稿 Assets)。按固定项名/分类名映射,未知分类用通用图标兜底。
 */
fun navIconFor(label: String): ImageVector = when (label) {
    "推荐" -> Icons.Filled.AutoAwesome
    "车载用品" -> Icons.Filled.DirectionsCar
    "加油充电" -> Icons.Filled.Bolt
    "户外旅行" -> Icons.Filled.Terrain
    "3C数码" -> Icons.Filled.Memory
    "品质生活" -> Icons.Filled.LocalCafe
    "购物车" -> Icons.Filled.ShoppingCart
    "订单" -> Icons.Filled.ReceiptLong
    else -> Icons.Filled.Category
}

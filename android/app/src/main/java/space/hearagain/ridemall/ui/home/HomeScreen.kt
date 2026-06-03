package space.hearagain.ridemall.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.hearagain.ridemall.model.HomeData
import space.hearagain.ridemall.model.Product
import space.hearagain.ridemall.ui.components.BannerCarousel
import space.hearagain.ridemall.ui.components.EmptyState
import space.hearagain.ridemall.ui.components.ProductGrid
import space.hearagain.ridemall.ui.components.SectionHeader
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmType

/** 推荐页:Banner 轮播 + 「为你推荐」两列网格。整体可纵向滚动。 */
@Composable
fun HomeScreen(
    data: HomeData,
    onProductClick: (Product) -> Unit,
    onBannerClick: (productId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        if (data.banners.isNotEmpty()) {
            BannerCarousel(
                banners = data.banners,
                onBannerClick = { onBannerClick(it.productId) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(44.dp))
        }
        SectionHeader("为你推荐")
        Spacer(Modifier.height(28.dp))
        if (data.recommended.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                EmptyState("暂无推荐商品", Icons.Filled.Inventory2)
            }
        } else {
            ProductGrid(products = data.recommended, onProductClick = onProductClick)
            Spacer(Modifier.height(40.dp))
        }
    }
}

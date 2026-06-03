package space.hearagain.ridemall.ui.category

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.hearagain.ridemall.model.Product
import space.hearagain.ridemall.ui.components.EmptyState
import space.hearagain.ridemall.ui.components.ProductGrid
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmType

/** 分类页:顶部分类名 + 「N 件商品」+ 两列网格;空态「该分类暂无商品」。 */
@Composable
fun CategoryScreen(
    categoryName: String,
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(categoryName, style = RmType.PageTitle, color = RmColor.Text1)
            Spacer(Modifier.width(20.dp))
            Text(
                "${products.size} 件商品",
                style = RmType.CountText,
                color = RmColor.Text3,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        Spacer(Modifier.height(32.dp))
        if (products.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                EmptyState("该分类暂无商品", Icons.Filled.Inventory2)
            }
        } else {
            ProductGrid(products = products, onProductClick = onProductClick)
            Spacer(Modifier.height(40.dp))
        }
    }
}

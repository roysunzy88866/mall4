package space.hearagain.ridemall.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import space.hearagain.ridemall.model.Product
import space.hearagain.ridemall.ui.theme.RmDimens

/**
 * 两列商品网格(非滚动,放进外层可滚动 Column)。奇数末行右侧留空,保持列宽一致。
 */
@Composable
fun ProductGrid(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        products.chunked(2).forEachIndexed { rowIndex, row ->
            if (rowIndex > 0) Spacer(Modifier.height(RmDimens.GridGap))
            Row(modifier = Modifier.fillMaxWidth()) {
                ProductCard(
                    product = row[0],
                    onClick = { onProductClick(row[0]) },
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(RmDimens.GridGap))
                if (row.size > 1) {
                    ProductCard(
                        product = row[1],
                        onClick = { onProductClick(row[1]) },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

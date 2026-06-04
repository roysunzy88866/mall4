package space.hearagain.ridemall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import space.hearagain.ridemall.model.Product
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmDimens
import space.hearagain.ridemall.ui.theme.RmType

/**
 * 商品卡:顶部商品图 + 名称(1~2 行)+ 一行(左价格、右「查看 ›」)。
 * 图区:有主图(`product.imageUrl`)加载真实图,无图回退占位块。
 */
@Composable
fun ProductCard(product: Product, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(RmDimens.RadCard))
            .background(RmColor.Card)
            .border(1.dp, RmColor.Line, RoundedCornerShape(RmDimens.RadCard))
            .clickable(onClick = onClick),
    ) {
        // 图区:有主图加载真实图,无图回退占位块
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(RmDimens.CardImageHeight)
                .background(RmColor.CardHi),
            contentAlignment = Alignment.Center,
        ) {
            if (product.imageUrl != null) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    Icons.Filled.Image,
                    contentDescription = null,
                    tint = RmColor.Text3,
                    modifier = Modifier.height(48.dp),
                )
            }
        }
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                product.name,
                style = RmType.CardName,
                color = RmColor.Text1,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(product.priceLabel, style = RmType.CardPrice, color = RmColor.Accent)
                Text("查看 ›", style = RmType.CardLink, color = RmColor.Text3)
            }
        }
    }
}

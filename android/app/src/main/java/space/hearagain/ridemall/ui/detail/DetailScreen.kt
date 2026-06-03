package space.hearagain.ridemall.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import space.hearagain.ridemall.model.ProductDetail
import space.hearagain.ridemall.ui.components.BackBar
import space.hearagain.ridemall.ui.components.CheckPill
import space.hearagain.ridemall.ui.components.GhostButton
import space.hearagain.ridemall.ui.components.PrimaryButton
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmDimens
import space.hearagain.ridemall.ui.theme.RmType

@Composable
fun DetailScreen(
    detail: ProductDetail,
    onBack: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var thumbIndex by remember(detail.id) { mutableIntStateOf(0) }
    Column(modifier = modifier.fillMaxSize()) {
        BackBar(onBack = onBack)
        Spacer(Modifier.height(28.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            // 左:主图 + 缩略图
            Column(modifier = Modifier.width(760.dp)) {
                ImageBox(
                    url = detail.images.getOrNull(thumbIndex),
                    modifier = Modifier.fillMaxWidth().height(540.dp),
                    iconSize = 90.dp,
                )
                if (detail.images.size > 1) {
                    Spacer(Modifier.height(18.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(detail.images.size) { i ->
                            val selected = i == thumbIndex
                            ImageBox(
                                url = detail.images[i],
                                modifier = Modifier
                                    .size(154.dp, 120.dp)
                                    .border(
                                        2.dp,
                                        if (selected) RmColor.Accent else RmColor.Line,
                                        RoundedCornerShape(RmDimens.RadThumb),
                                    )
                                    .clickable { thumbIndex = i },
                                iconSize = 28.dp,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.width(56.dp))
            // 右:信息
            Column(modifier = Modifier.weight(1f)) {
                Text(detail.name, style = RmType.BannerTitle, color = RmColor.Text1, maxLines = 2)
                Spacer(Modifier.height(20.dp))
                Text(detail.priceLabel, style = RmType.BannerPrice.copy(fontSize = RmType.BannerTitle.fontSize), color = RmColor.Accent)
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    CheckPill("7天无理由"); CheckPill("包邮"); CheckPill("自营")
                }
                Spacer(Modifier.height(28.dp))
                Text("商品描述", style = RmType.CardLink, color = RmColor.Text3)
                Spacer(Modifier.height(10.dp))
                Text(detail.description, style = RmType.BannerDesc, color = RmColor.Text2)
                Spacer(Modifier.height(40.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    GhostButton("加入购物车", onClick = onAddToCart)
                    PrimaryButton("立即购买", onClick = onBuyNow)
                }
            }
        }
    }
}

@Composable
private fun ImageBox(url: String?, modifier: Modifier, iconSize: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(RmDimens.RadPanel))
            .background(RmColor.CardHi),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            AsyncImage(url, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(Icons.Filled.Image, null, tint = RmColor.Text3, modifier = Modifier.size(iconSize))
        }
    }
}

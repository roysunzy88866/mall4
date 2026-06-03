package space.hearagain.ridemall.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import space.hearagain.ridemall.model.Banner
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmDimens
import space.hearagain.ridemall.ui.theme.RmType
import space.hearagain.ridemall.util.clampBannerIndex
import space.hearagain.ridemall.util.nextBannerIndex

// 节奏数值来源:需求共识 §3 锚点 F4b(自动切换)/ F4c(手动后静默),此处仅落地不重抄语义。
private const val AUTO_ADVANCE_MS = 3_000L     // F4b
private const val MANUAL_SILENCE_MS = 8_000L   // F4c

/**
 * 推荐页 Banner 轮播:自动切换(F4b)+ 圆点指示可点跳 + 手动后静默(F4c)+ 0.7s 淡入淡出。
 */
@Composable
fun BannerCarousel(
    banners: List<Banner>,
    onBannerClick: (Banner) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (banners.isEmpty()) return
    var index by remember(banners) { mutableIntStateOf(0) }
    var manualNonce by remember(banners) { mutableIntStateOf(0) }

    // 自动轮播:手动后先静默 MANUAL_SILENCE,再按 AUTO 间隔推进。manualNonce 变化即重启计时。
    androidx.compose.runtime.LaunchedEffect(banners, manualNonce) {
        if (banners.size <= 1) return@LaunchedEffect
        if (manualNonce > 0) delay(MANUAL_SILENCE_MS)
        while (true) {
            delay(AUTO_ADVANCE_MS)
            index = nextBannerIndex(index, banners.size)
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(RmDimens.BannerHeight)
                .clip(RoundedCornerShape(RmDimens.RadPanel)),
        ) {
            Crossfade(targetState = index, animationSpec = tween(700), label = "banner") { i ->
                val banner = banners[clampBannerIndex(i, banners.size)]
                BannerSlide(banner = banner, onClick = { onBannerClick(banner) })
            }
        }
        Spacer(Modifier.height(20.dp))
        // 圆点指示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            banners.indices.forEach { i ->
                val selected = i == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 7.dp)
                        .size(if (selected) 14.dp else 12.dp)
                        .clip(RoundedCornerShape(RmDimens.RadPill))
                        .background(if (selected) RmColor.Accent else RmColor.Line)
                        .clickable {
                            index = clampBannerIndex(i, banners.size)
                            manualNonce++
                        },
                )
            }
        }
    }
}

@Composable
private fun BannerSlide(banner: Banner, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RmColor.CardHi)
            .clickable(onClick = onClick),
    ) {
        if (banner.imageUrl != null) {
            AsyncImage(
                model = banner.imageUrl,
                contentDescription = banner.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        // 左侧黑→透明渐变,保证文字可读
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to RmColor.Bg.copy(alpha = 0.95f),
                        0.62f to RmColor.Bg.copy(alpha = 0.0f),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.62f)
                .padding(horizontal = 56.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "RIDEMALL 精选".uppercase(),
                style = RmType.BannerKicker,
                color = RmColor.Accent,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                banner.title,
                style = RmType.BannerTitle,
                color = RmColor.Text1,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
            )
            if (banner.description.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(banner.description, style = RmType.BannerDesc, color = RmColor.Text2, maxLines = 2)
            }
            Spacer(Modifier.height(18.dp))
            Text(banner.priceLabel, style = RmType.BannerPrice, color = RmColor.Accent)
        }
    }
}

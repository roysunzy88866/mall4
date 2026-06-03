package space.hearagain.ridemall.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import space.hearagain.ridemall.ui.theme.RmColor
import kotlin.math.min

const val DESIGN_W = 1920f
const val DESIGN_H = 1080f

/**
 * 1920×1080 设计空间等比缩放容器(letterbox 居中、补黑边)。
 * 做法:量实际像素 → 算 scale=min(w/1920,h/1080) → 覆盖 LocalDensity 使 1.dp==scale·px,
 * 于是固定 1920×1080 dp 的内容正好填满较短边、另一边补黑边。内部一切按设计 px 写为 dp 即可。
 * 在 1920×1080 专用模拟器(ridemall-car)上 scale≈1,无损。
 */
@Composable
fun ScalingContainer(content: @Composable () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(RmColor.Bg),
        contentAlignment = Alignment.Center,
    ) {
        val wPx = constraints.maxWidth
        val hPx = constraints.maxHeight
        val scale = min(wPx / DESIGN_W, hPx / DESIGN_H)
        if (scale > 0f) {
            CompositionLocalProvider(LocalDensity provides Density(density = scale, fontScale = 1f)) {
                Box(modifier = Modifier.size(DESIGN_W.dp, DESIGN_H.dp)) {
                    content()
                }
            }
        }
    }
}

package space.hearagain.ridemall.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmType
import space.hearagain.ridemall.util.secondsToClock
import kotlin.math.abs

/**
 * 全屏扫码支付覆盖层。点屏任意处 = 支付(由上层 onTap 触发下单)。
 * payError=true 时显示「网络异常,点击重试」(点屏即重试)。
 */
@Composable
fun PayOverlay(
    totalLabel: String,
    remainSec: Int,
    timedOut: Boolean,
    payError: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RmColor.Bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (payError) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("网络异常", style = RmType.OverlayTitle, color = RmColor.WarnAmber)
                Spacer(Modifier.height(16.dp))
                Text("点击屏幕重试支付", style = RmType.OverlayHint, color = RmColor.Text2)
            }
            return@Box
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                buildString { append("请扫码支付 ") },
                style = RmType.OverlayHint, color = RmColor.Text2,
            )
            Text(totalLabel, style = RmType.OverlayTitle, color = RmColor.Accent)
            Spacer(Modifier.height(28.dp))
            Box(
                Modifier.size(280.dp).clip(RoundedCornerShape(22.dp)).background(Color.White).padding(15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) { drawFakeQr() }
            }
            Spacer(Modifier.height(28.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("支付剩余时间 ", style = RmType.OverlayHint, color = RmColor.Text2)
                Text(
                    secondsToClock(remainSec),
                    style = RmType.OverlayTitle,
                    color = if (remainSec <= 30) RmColor.WarnRed else RmColor.Accent,
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                if (timedOut) "支付超时(点击屏幕仍可完成支付 · 演示用)" else "点击屏幕任意位置 = 支付成功(演示用)",
                style = RmType.OverlayHint,
                color = if (timedOut) RmColor.WarnRed else RmColor.Text3,
            )
        }
    }
}

/** 伪二维码:三个定位角 + 固定散点。 */
private fun DrawScope.drawFakeQr() {
    val n = 21
    val cell = size.minDimension / n
    val dark = Color(0xFF0A0C0F)
    fun module(c: Int, r: Int) = drawRect(dark, Offset(c * cell, r * cell), Size(cell, cell))
    fun finder(c0: Int, r0: Int) {
        for (r in 0..6) for (c in 0..6) {
            val edge = r == 0 || r == 6 || c == 0 || c == 6
            val inner = r in 2..4 && c in 2..4
            if (edge || inner) module(c0 + c, r0 + r)
        }
    }
    finder(0, 0); finder(n - 7, 0); finder(0, n - 7)
    // 固定伪散点(确定式,不依赖随机)
    for (r in 0 until n) for (c in 0 until n) {
        val inFinder = (r < 7 && c < 7) || (r < 7 && c >= n - 7) || (r >= n - 7 && c < 7)
        if (!inFinder && (abs(r * 7 + c * 13) % 5 == 0)) module(c, r)
    }
}

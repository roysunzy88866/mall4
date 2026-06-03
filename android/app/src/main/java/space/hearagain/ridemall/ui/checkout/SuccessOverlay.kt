package space.hearagain.ridemall.ui.checkout

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmType

/** 支付成功覆盖层:对勾弹性入场 + 1.5 秒后 onDone(进订单页)。 */
@Composable
fun SuccessOverlay(onDone: () -> Unit, modifier: Modifier = Modifier) {
    var shown by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pop",
    )
    LaunchedEffect(Unit) {
        shown = true
        delay(1500)
        onDone()
    }
    Box(modifier = modifier.fillMaxSize().background(RmColor.Bg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(168.dp).scale(scale).clip(CircleShape).background(RmColor.AccentSoft),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(118.dp).clip(CircleShape).background(RmColor.Accent), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Check, null, tint = RmColor.AccentInk, modifier = Modifier.size(72.dp))
                }
            }
            Spacer(Modifier.height(36.dp))
            Text("支付成功", style = RmType.OverlayTitle, color = RmColor.Text1)
            Spacer(Modifier.height(14.dp))
            Text("1.5 秒后自动进入订单页", style = RmType.OverlayHint, color = RmColor.Text2)
        }
    }
}

package space.hearagain.ridemall.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import space.hearagain.ridemall.ui.theme.RmColor

/** 空态:虚线圆角框(含图标)+ 文案,居中。设计稿空态样式。 */
@Composable
fun EmptyState(text: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .drawBehind {
                    drawRoundRect(
                        color = RmColor.Line,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(10.dp.toPx(), 8.dp.toPx()),
                            ),
                        ),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = RmColor.Text3, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text,
            color = RmColor.Text2,
            style = space.hearagain.ridemall.ui.theme.RmType.EmptyText,
        )
    }
}

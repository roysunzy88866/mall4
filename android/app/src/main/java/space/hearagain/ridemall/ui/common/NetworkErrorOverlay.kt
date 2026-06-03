package space.hearagain.ridemall.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmType

/**
 * 全屏网络异常覆盖层(盖住左导航)。amber 警告环 + 标题 + 提示 +「点击重试」。
 * 不做本地缓存(共识 §1/§2);重试由上层重发当前页请求。
 */
@Composable
fun NetworkErrorOverlay(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RmColor.Bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(RmDimens_RadPill))
                    .background(RmColor.WarnAmberSoft)
                    .border(2.dp, RmColor.WarnAmberLine, RoundedCornerShape(RmDimens_RadPill)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.WarningAmber,
                    contentDescription = null,
                    tint = RmColor.WarnAmber,
                    modifier = Modifier.size(66.dp),
                )
            }
            Spacer(Modifier.height(36.dp))
            Text("网络异常", style = RmType.OverlayTitle, color = RmColor.Text1)
            Spacer(Modifier.height(16.dp))
            Text("请检查网络后重试", style = RmType.OverlayHint, color = RmColor.Text2)
            Spacer(Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(RmDimens_RadButton))
                    .border(1.dp, RmColor.AccentLine, RoundedCornerShape(RmDimens_RadButton))
                    .background(RmColor.AccentSoft)
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 48.dp, vertical = 22.dp),
            ) {
                Text("点击重试", style = RmType.ButtonText, color = RmColor.Accent)
            }
        }
    }
}

private val RmDimens_RadPill = 999.dp
private val RmDimens_RadButton = 14.dp

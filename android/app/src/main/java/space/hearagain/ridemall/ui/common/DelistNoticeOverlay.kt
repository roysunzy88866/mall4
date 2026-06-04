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
 * 全屏「商品已下架」提示覆盖层(下单返回 409 时弹)。amber 警告环 + 标题 + 说明 +「我知道了」。
 * 点击确认 → 上层清除提示(已下架商品此前已被移出购物车、界面退回购物车)。
 */
@Composable
fun DelistNoticeOverlay(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
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
                    .clip(RoundedCornerShape(999.dp))
                    .background(RmColor.WarnAmberSoft)
                    .border(2.dp, RmColor.WarnAmberLine, RoundedCornerShape(999.dp)),
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
            Text("商品已下架", style = RmType.OverlayTitle, color = RmColor.Text1)
            Spacer(Modifier.height(16.dp))
            Text("部分商品已下架,已为你移出购物车", style = RmType.OverlayHint, color = RmColor.Text2)
            Spacer(Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, RmColor.AccentLine, RoundedCornerShape(14.dp))
                    .background(RmColor.AccentSoft)
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 48.dp, vertical = 22.dp),
            ) {
                Text("我知道了", style = RmType.ButtonText, color = RmColor.Accent)
            }
        }
    }
}

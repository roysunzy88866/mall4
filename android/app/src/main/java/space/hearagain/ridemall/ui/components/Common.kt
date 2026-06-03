package space.hearagain.ridemall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmDimens
import space.hearagain.ridemall.ui.theme.RmType

/** 「‹ 返回」胶囊按钮。 */
@Composable
fun BackBar(onBack: () -> Unit, title: String? = null, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(RmDimens.RadPill))
                .background(RmColor.Card)
                .border(1.dp, RmColor.Line, RoundedCornerShape(RmDimens.RadPill))
                .clickable(onClick = onBack)
                .padding(horizontal = 22.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = RmColor.Text2, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("返回", style = RmType.NavItem, color = RmColor.Text2)
        }
        if (title != null) {
            Spacer(Modifier.width(28.dp))
            Text(title, style = RmType.PageTitle, color = RmColor.Text1)
        }
    }
}

/** 主按钮(青色实心)。 */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(RmDimens.RadButton))
            .background(if (enabled) RmColor.Accent else RmColor.Line)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 56.dp, vertical = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = RmType.ButtonText, color = if (enabled) RmColor.AccentInk else RmColor.Text3)
    }
}

/** 次按钮(青色描边 ghost)。 */
@Composable
fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(RmDimens.RadButton))
            .background(RmColor.AccentSoft)
            .border(1.dp, RmColor.AccentLine, RoundedCornerShape(RmDimens.RadButton))
            .clickable(onClick = onClick)
            .padding(horizontal = 40.dp, vertical = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = RmType.ButtonText, color = RmColor.Accent)
    }
}

/** 固定标签胶囊(✓ + 文案,accent 风)。 */
@Composable
fun CheckPill(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(RmDimens.RadPill))
            .background(RmColor.AccentSoft)
            .border(1.dp, RmColor.AccentLine, RoundedCornerShape(RmDimens.RadPill))
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Check, null, tint = RmColor.Accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = RmColor.Accent, style = RmType.CardLink)
    }
}

/** 状态徽章(胶囊)。 */
@Composable
fun StatusBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(RmDimens.RadPill))
            .background(RmColor.AccentSoft)
            .border(1.dp, RmColor.AccentLine, RoundedCornerShape(RmDimens.RadPill))
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text("✓ $text", color = RmColor.Accent, style = RmType.CardLink)
    }
}

/** 固定假收货信息卡(只展示)。 */
@Composable
fun FixedAddressCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RmDimens.RadPanel))
            .background(RmColor.Card)
            .border(1.dp, RmColor.Line, RoundedCornerShape(RmDimens.RadPanel))
            .padding(28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("收货信息", style = RmType.CardName, color = RmColor.Text1)
            Spacer(Modifier.width(16.dp))
            Box(
                Modifier.clip(RoundedCornerShape(RmDimens.RadPill)).background(RmColor.CardHi)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) { Text("演示 · 固定地址", color = RmColor.Text3, style = RmType.CardLink) }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("张三", color = RmColor.Text1, fontWeight = FontWeight.Bold, style = RmType.BannerDesc)
            Spacer(Modifier.width(20.dp))
            Text("138****8888", color = RmColor.Text1, style = RmType.BannerDesc)
            Spacer(Modifier.width(20.dp))
            Text("上海市浦东新区 ×× 路 ×× 号", color = RmColor.Text2, style = RmType.BannerDesc)
        }
    }
}

/** 价格文字(¥ 青色 + 数字)。 */
@Composable
fun PriceText(label: String, style: androidx.compose.ui.text.TextStyle, color: Color = RmColor.Accent) {
    Text(label, style = style, color = color)
}

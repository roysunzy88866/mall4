package space.hearagain.ridemall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmDimens
import space.hearagain.ridemall.ui.theme.RmType

/** 区块标题:左侧 5×30 青色竖条 + 标题文字(设计稿「为你推荐」样式)。 */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Spacer(
            Modifier
                .size(RmDimens.SectionBarW, RmDimens.SectionBarH)
                .clip(RoundedCornerShape(RmDimens.RadPill))
                .background(RmColor.Accent),
        )
        Spacer(Modifier.width(16.dp))
        Text(title, style = RmType.SectionTitle, color = RmColor.Text1)
    }
}

package space.hearagain.ridemall.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import space.hearagain.ridemall.model.NavCategory
import space.hearagain.ridemall.ui.components.navIconFor
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmDimens
import space.hearagain.ridemall.ui.theme.RmType
import space.hearagain.ridemall.viewmodel.Route

/**
 * 左侧常驻导航:品牌区 + 「推荐」(固定首)+ 动态分类(按后端序)+ 「订单」(固定末)。
 */
@Composable
fun NavRail(
    categories: List<NavCategory>,
    route: Route,
    onHome: () -> Unit,
    onCategory: (NavCategory) -> Unit,
    onOrders: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(RmDimens.RailWidth)
            .fillMaxHeight()
            .background(RmColor.Panel)
            .padding(
                start = RmDimens.RailPadH,
                end = RmDimens.RailPadH,
                top = RmDimens.RailPadTop,
                bottom = RmDimens.RailPadBottom,
            ),
    ) {
        BrandHeader()
        Spacer(Modifier.height(28.dp))

        NavItem(
            label = "推荐",
            selected = route is Route.Home,
            onClick = onHome,
        )
        Spacer(Modifier.height(RmDimens.NavItemGap))

        // 动态分类(可滚动,占据中间弹性空间)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(categories, key = { it.id }) { cat ->
                NavItem(
                    label = cat.name,
                    selected = route is Route.Category && route.id == cat.id,
                    onClick = { onCategory(cat) },
                )
                Spacer(Modifier.height(RmDimens.NavItemGap))
            }
        }

        // 分隔线 + 订单(固定末)
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(RmColor.LineSoft),
        )
        Spacer(Modifier.height(RmDimens.NavItemGap))
        NavItem(
            label = "订单",
            selected = route is Route.Orders,
            onClick = onOrders,
        )
    }
}

@Composable
private fun BrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(RmDimens.RadSmall))
                .background(RmColor.Accent),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            buildAnnotatedString {
                withStyle(androidx.compose.ui.text.SpanStyle(color = RmColor.Text1)) { append("RIDE") }
                withStyle(androidx.compose.ui.text.SpanStyle(color = RmColor.Accent)) { append("MALL") }
            },
            style = RmType.Brand,
        )
    }
}

@Composable
private fun NavItem(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) RmColor.AccentSoft else Color.Transparent
    val border = if (selected) RmColor.AccentLine else Color.Transparent
    val contentColor = if (selected) RmColor.Accent else RmColor.Text2
    val textColor = if (selected) RmColor.Text1 else RmColor.Text2

    Box(contentAlignment = Alignment.CenterStart) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(RmDimens.NavItemHeight)
                .clip(RoundedCornerShape(RmDimens.RadThumb))
                .background(bg)
                .border(1.dp, border, RoundedCornerShape(RmDimens.RadThumb))
                .clickable(onClick = onClick)
                .padding(horizontal = RmDimens.NavItemPadH),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                navIconFor(label),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(RmDimens.NavIconSize),
            )
            Spacer(Modifier.width(RmDimens.NavIconTextGap))
            Text(
                label,
                style = RmType.NavItem,
                color = textColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
        }
        // 选中态左侧贴边竖条
        if (selected) {
            Spacer(
                Modifier
                    .size(RmDimens.NavSelBarW, RmDimens.NavSelBarH)
                    .clip(RoundedCornerShape(RmDimens.RadPill))
                    .background(RmColor.Accent),
            )
        }
    }
}

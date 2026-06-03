package space.hearagain.ridemall.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import space.hearagain.ridemall.model.Order
import space.hearagain.ridemall.ui.components.EmptyState
import space.hearagain.ridemall.ui.components.StatusBadge
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmDimens
import space.hearagain.ridemall.ui.theme.RmType

@Composable
fun OrdersScreen(orders: List<Order>, onOrderClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("我的订单", style = RmType.PageTitle, color = RmColor.Text1)
            Spacer(Modifier.width(20.dp))
            Text("${orders.size} 笔", style = RmType.CountText, color = RmColor.Text3, modifier = Modifier.padding(bottom = 8.dp))
        }
        Spacer(Modifier.height(28.dp))
        if (orders.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f)) { EmptyState("暂无订单", Icons.Filled.ReceiptLong) }
            return@Column
        }
        Column(modifier = Modifier.weight(1f).widthIn(max = 1140.dp).verticalScroll(rememberScrollState())) {
            orders.forEach { order ->
                OrderCard(order, onClick = { onOrderClick(order.id) })
                Spacer(Modifier.height(22.dp))
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(RmDimens.RadPanel)).background(RmColor.Card)
            .border(1.dp, RmColor.Line, RoundedCornerShape(RmDimens.RadPanel))
            .clickable(onClick = onClick).padding(24.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(order.createdAt, style = RmType.NavItem, color = RmColor.Text2)
            Spacer(Modifier.weight(1f))
            StatusBadge(order.statusLabel)
        }
        Spacer(Modifier.height(16.dp))
        order.items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(72.dp).clip(RoundedCornerShape(RmDimens.RadThumb)).background(RmColor.CardHi),
                    contentAlignment = Alignment.Center,
                ) {
                    if (item.imageUrl != null) AsyncImage(item.imageUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else Icon(Icons.Filled.Image, null, tint = RmColor.Text3, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(18.dp))
                Text(item.name, style = RmType.CardName, color = RmColor.Text1, modifier = Modifier.weight(1f))
                Text(item.priceLabel, style = RmType.CardPrice, color = RmColor.Text1)
                Spacer(Modifier.width(16.dp))
                Text("×${item.qty}", style = RmType.CardName, color = RmColor.Text2)
            }
        }
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(RmColor.LineSoft))
        Spacer(Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("收货 张三 138****8888 上海市浦东新区××路××号", style = RmType.CardLink, color = RmColor.Text3, modifier = Modifier.weight(1f))
            Text("合计 ", style = RmType.CardLink, color = RmColor.Text2)
            Text(order.totalLabel, style = RmType.CardPrice, color = RmColor.Accent)
        }
    }
}

package space.hearagain.ridemall.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import space.hearagain.ridemall.model.Order
import space.hearagain.ridemall.ui.components.BackBar
import space.hearagain.ridemall.ui.components.StatusBadge
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmDimens
import space.hearagain.ridemall.ui.theme.RmType
import space.hearagain.ridemall.util.STAGE_LABELS
import space.hearagain.ridemall.util.stageIndexOf

@Composable
fun OrderDetailScreen(order: Order, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val stage = stageIndexOf(order.status)
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackBar(onBack = onBack, title = "订单 #${order.id}")
            Spacer(Modifier.width(20.dp))
            StatusBadge(order.statusLabel)
        }
        Spacer(Modifier.height(28.dp))

        // 状态步条(4 节点)
        Card {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                STAGE_LABELS.forEachIndexed { i, label ->
                    val done = stage in 0..i || stage > i
                    val reached = stage >= i && stage >= 0
                    StepNode(index = i, label = label, done = reached)
                    if (i < STAGE_LABELS.lastIndex) {
                        Box(
                            Modifier.weight(1f).height(3.dp).padding(horizontal = 6.dp)
                                .background(if (stage > i) RmColor.Accent else RmColor.Line),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))

        // 物流时间线
        if (order.logistics.isNotEmpty()) {
            Card {
                Text("物流轨迹", style = RmType.CardName, color = RmColor.Text1)
                Spacer(Modifier.height(14.dp))
                order.logistics.forEach { node ->
                    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            Modifier.padding(top = 6.dp).size(12.dp).clip(CircleShape)
                                .background(if (node.reached) RmColor.Accent else RmColor.Line),
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(node.label, style = RmType.BannerDesc, color = if (node.reached) RmColor.Text1 else RmColor.Text3, fontWeight = FontWeight.Medium)
                            if (node.at != null) Text(node.at, style = RmType.CardLink, color = RmColor.Text3)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // 商品 + 合计
        Card {
            Text("商品清单(${order.items.size} 件)", style = RmType.CardName, color = RmColor.Text1)
            Spacer(Modifier.height(14.dp))
            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, style = RmType.CardName, color = RmColor.Text1, modifier = Modifier.weight(1f))
                    Text(item.priceLabel, style = RmType.CardPrice, color = RmColor.Text1)
                    Spacer(Modifier.width(16.dp))
                    Text("×${item.qty}", style = RmType.CardName, color = RmColor.Text2)
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(RmColor.LineSoft))
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Text("合计 ", style = RmType.CardName, color = RmColor.Text2)
                Text(order.totalLabel, style = RmType.SectionTitle, color = RmColor.Accent)
            }
            Spacer(Modifier.height(16.dp))
            Text("收货 张三 138****8888 上海市浦东新区××路××号", style = RmType.CardLink, color = RmColor.Text3)
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun Card(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(RmDimens.RadPanel)).background(RmColor.Card)
            .border(1.dp, RmColor.Line, RoundedCornerShape(RmDimens.RadPanel)).padding(28.dp),
        content = content,
    )
}

@Composable
private fun StepNode(index: Int, label: String, done: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(96.dp)) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(if (done) RmColor.Accent else RmColor.CardHi),
            contentAlignment = Alignment.Center,
        ) {
            if (done) Icon(Icons.Filled.Check, null, tint = RmColor.AccentInk, modifier = Modifier.size(24.dp))
            else Text("${index + 1}", style = RmType.CardName, color = RmColor.Text3)
        }
        Spacer(Modifier.height(10.dp))
        Text(label, style = RmType.CardLink, color = if (done) RmColor.Text1 else RmColor.Text3)
    }
}

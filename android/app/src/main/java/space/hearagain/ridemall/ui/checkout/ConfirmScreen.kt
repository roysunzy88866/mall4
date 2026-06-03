package space.hearagain.ridemall.ui.checkout

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import space.hearagain.ridemall.model.OrderDraft
import space.hearagain.ridemall.ui.components.BackBar
import space.hearagain.ridemall.ui.components.FixedAddressCard
import space.hearagain.ridemall.ui.components.PrimaryButton
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmDimens
import space.hearagain.ridemall.ui.theme.RmType

@Composable
fun ConfirmScreen(draft: OrderDraft, onBack: () -> Unit, onPay: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        BackBar(onBack = onBack, title = "确认订单")
        Spacer(Modifier.height(28.dp))
        Column(
            modifier = Modifier.weight(1f).widthIn(max = 1080.dp).verticalScroll(rememberScrollState()),
        ) {
            FixedAddressCard()
            Spacer(Modifier.height(24.dp))
            // 订单详情卡
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(RmDimens.RadPanel))
                    .background(RmColor.Card).border(1.dp, RmColor.Line, RoundedCornerShape(RmDimens.RadPanel)).padding(28.dp),
            ) {
                Text("订单详情", style = RmType.CardName, color = RmColor.Text1)
                Spacer(Modifier.height(20.dp))
                draft.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(96.dp).clip(RoundedCornerShape(RmDimens.RadThumb)).background(RmColor.CardHi),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (item.imageUrl != null) AsyncImage(item.imageUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            else Icon(Icons.Filled.Image, null, tint = RmColor.Text3, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(20.dp))
                        Text(item.name, style = RmType.CardName, color = RmColor.Text1, modifier = Modifier.weight(1f))
                        Text(item.priceLabel, style = RmType.CardPrice, color = RmColor.Text1)
                        Spacer(Modifier.width(20.dp))
                        Text("×${item.qty}", style = RmType.CardName, color = RmColor.Text2)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(RmColor.LineSoft))
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Text("合计 ", style = RmType.CardName, color = RmColor.Text2)
                    Text(draft.totalLabel, style = RmType.SectionTitle, color = RmColor.Accent)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            PrimaryButton("去支付 ${draft.totalLabel}", onClick = onPay)
        }
    }
}

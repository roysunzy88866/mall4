package space.hearagain.ridemall.ui.cart

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import space.hearagain.ridemall.model.CartItem
import space.hearagain.ridemall.ui.components.BackBar
import space.hearagain.ridemall.ui.components.EmptyState
import space.hearagain.ridemall.ui.components.PrimaryButton
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmDimens
import space.hearagain.ridemall.ui.theme.RmType
import space.hearagain.ridemall.util.selectedTotalCents

@Composable
fun CartScreen(
    cart: List<CartItem>,
    onBack: () -> Unit,
    onSetQty: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    onToggle: (Int) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BackBar(onBack = onBack, title = "购物车")
        Spacer(Modifier.height(28.dp))
        if (cart.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                EmptyState("购物车是空的", Icons.Filled.ShoppingCartCheckout)
            }
            return@Column
        }
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            cart.forEach { item ->
                CartRow(item, onSetQty, onRemove, onToggle)
                Spacer(Modifier.height(18.dp))
            }
        }
        // 底栏
        val allSelected = cart.all { it.selected }
        val totalCents = selectedTotalCents(cart)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(RmDimens.RadPanel))
                .background(RmColor.Panel)
                .padding(horizontal = 30.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CheckCircle(allSelected) { onSelectAll(!allSelected) }
            Spacer(Modifier.width(14.dp))
            Text("全选", style = RmType.NavItem, color = RmColor.Text2)
            Spacer(Modifier.weight(1f))
            Text("合计 ", style = RmType.NavItem, color = RmColor.Text2)
            Text("¥%.2f".format(totalCents / 100.0), style = RmType.SectionTitle, color = RmColor.Accent)
            Spacer(Modifier.width(28.dp))
            PrimaryButton("结算", onClick = { if (totalCents > 0) onCheckout() })
        }
    }
}

@Composable
private fun CartRow(item: CartItem, onSetQty: (Int, Int) -> Unit, onRemove: (Int) -> Unit, onToggle: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RmDimens.RadCard))
            .background(RmColor.Card)
            .border(1.dp, RmColor.Line, RoundedCornerShape(RmDimens.RadCard))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckCircle(item.selected) { onToggle(item.productId) }
        Spacer(Modifier.width(20.dp))
        Box(
            Modifier.size(96.dp).clip(RoundedCornerShape(RmDimens.RadThumb)).background(RmColor.CardHi),
            contentAlignment = Alignment.Center,
        ) {
            if (item.imageUrl != null) {
                AsyncImage(item.imageUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Filled.Image, null, tint = RmColor.Text3, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.width(20.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, style = RmType.CardName, color = RmColor.Text1, maxLines = 1)
            Spacer(Modifier.height(8.dp))
            Text(item.priceLabel, style = RmType.CardPrice, color = RmColor.Accent)
        }
        QtyStepper(item.qty) { onSetQty(item.productId, it) }
        Spacer(Modifier.width(20.dp))
        Icon(
            Icons.Filled.DeleteOutline, "删除", tint = RmColor.Text3,
            modifier = Modifier.size(30.dp).clickable { onRemove(item.productId) },
        )
    }
}

@Composable
private fun QtyStepper(qty: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepBtn("−") { onChange(qty - 1) }
        Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
            Text("$qty", style = RmType.CardName, color = RmColor.Text1)
        }
        StepBtn("+") { onChange(qty + 1) }
    }
}

@Composable
private fun StepBtn(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(48.dp).clip(RoundedCornerShape(RmDimens.RadThumb)).background(RmColor.CardHi)
            .border(1.dp, RmColor.Line, RoundedCornerShape(RmDimens.RadThumb)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(label, style = RmType.CardName, color = RmColor.Text1) }
}

@Composable
private fun CheckCircle(checked: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(34.dp).clip(CircleShape)
            .background(if (checked) RmColor.Accent else Color.Transparent)
            .border(2.dp, if (checked) RmColor.Accent else RmColor.Line, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Icon(Icons.Filled.Check, null, tint = RmColor.AccentInk, modifier = Modifier.size(22.dp))
    }
}

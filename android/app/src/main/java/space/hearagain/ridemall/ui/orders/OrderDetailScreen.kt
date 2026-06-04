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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import space.hearagain.ridemall.model.Order
import space.hearagain.ridemall.ui.components.BackBar
import space.hearagain.ridemall.ui.components.StatusBadge
import space.hearagain.ridemall.ui.theme.RmColor
import space.hearagain.ridemall.ui.theme.RmDimens
import space.hearagain.ridemall.ui.theme.RmType
import space.hearagain.ridemall.util.STAGE_LABELS
import space.hearagain.ridemall.util.displayStageOf

@Composable
fun OrderDetailScreen(
    order: Order,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onRequestReturn: (reason: String, note: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stage = displayStageOf(order.status)
    var showReturnForm by remember(order.id, order.status) { mutableStateOf(false) }
    // 待确认动作(弹框「是否要做」)
    var pending by remember(order.id, order.status) { mutableStateOf<ConfirmSpec?>(null) }

    pending?.let { spec ->
        ConfirmDialog(spec = spec, onDismiss = { pending = null })
    }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackBar(onBack = onBack, title = "订单 #${order.id}")
            Spacer(Modifier.width(20.dp))
            StatusBadge(order.statusLabel)
        }
        Spacer(Modifier.height(28.dp))

        // 取消 / 退货 操作
        if (order.canCancel || order.canReturn || order.returnReason != null) {
            Card {
                if (order.returnReason != null) {
                    Text("退货原因:${order.returnReason}", style = RmType.BannerDesc, color = RmColor.Text2)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (order.canCancel) {
                        space.hearagain.ridemall.ui.components.GhostButton(
                            "取消订单",
                            onClick = {
                                pending = ConfirmSpec(
                                    title = "取消订单",
                                    message = "取消后订单立即关闭并自动退款,此操作不可撤销。",
                                    confirmText = "确认取消",
                                    danger = true,
                                    action = onCancel,
                                )
                            },
                        )
                    }
                    if (order.canReturn) {
                        space.hearagain.ridemall.ui.components.GhostButton(
                            if (showReturnForm) "收起" else "申请退货",
                            onClick = { showReturnForm = !showReturnForm },
                        )
                    }
                }
                if (showReturnForm && order.canReturn) {
                    Spacer(Modifier.height(16.dp))
                    ReturnForm(onSubmit = { reason, note ->
                        pending = ConfirmSpec(
                            title = "提交退货申请",
                            message = "提交后进入后台人工审核,通过后按原路退款。",
                            highlightLabel = "退货原因",
                            highlightValue = reason,
                            confirmText = "确认提交",
                            action = { onRequestReturn(reason, note) },
                        )
                    })
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // 状态步条(4 节点);取消等分支态(stage<0)不画主步条,只靠顶部徽章显态
        if (stage >= 0) {
            Card {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    STAGE_LABELS.forEachIndexed { i, label ->
                        val reached = stage >= i
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
        }

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
private fun ReturnForm(onSubmit: (String, String?) -> Unit) {
    val reasons = listOf("7天无理由", "质量问题", "拍错了", "其它")
    var selected by remember { mutableStateOf(reasons.first()) }
    var note by remember { mutableStateOf("") }
    Column {
        Text("退货原因", style = RmType.CardLink, color = RmColor.Text3)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            reasons.forEach { r ->
                val on = r == selected
                Box(
                    Modifier.clip(RoundedCornerShape(RmDimens.RadPill))
                        .background(if (on) RmColor.AccentSoft else RmColor.CardHi)
                        .border(1.dp, if (on) RmColor.AccentLine else RmColor.Line, RoundedCornerShape(RmDimens.RadPill))
                        .clickable { selected = r }.padding(horizontal = 18.dp, vertical = 10.dp),
                ) { Text(r, color = if (on) RmColor.Accent else RmColor.Text2, style = RmType.CardLink) }
            }
        }
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = note, onValueChange = { note = it },
            placeholder = { Text("补充说明(选填)", color = RmColor.Text3) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        space.hearagain.ridemall.ui.components.PrimaryButton(
            "提交退货申请", onClick = { onSubmit(selected, note.ifBlank { null }) },
        )
    }
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

/** 确认弹框的内容规格。danger=true 时确认键转红(破坏性操作)。 */
private data class ConfirmSpec(
    val title: String,
    val message: String,
    val confirmText: String,
    val action: () -> Unit,
    val highlightLabel: String? = null,
    val highlightValue: String? = null,
    val danger: Boolean = false,
)

/** 品牌风确认弹框:深色面板 + 青色描边 + 强调竖条 + 主次按钮。 */
@Composable
private fun ConfirmDialog(spec: ConfirmSpec, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val barColor = if (spec.danger) RmColor.WarnRed else RmColor.Accent
        Column(
            Modifier
                .width(720.dp)
                .clip(RoundedCornerShape(RmDimens.RadPanel))
                .background(RmColor.Card)
                .border(1.dp, RmColor.AccentLine, RoundedCornerShape(RmDimens.RadPanel))
                .padding(44.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.width(RmDimens.SectionBarW).height(RmDimens.SectionBarH)
                        .clip(RoundedCornerShape(RmDimens.RadSmall)).background(barColor),
                )
                Spacer(Modifier.width(16.dp))
                Text(spec.title, style = RmType.SectionTitle, color = RmColor.Text1)
            }
            Spacer(Modifier.height(22.dp))
            Text(spec.message, style = RmType.BannerDesc, color = RmColor.Text2)
            if (spec.highlightValue != null) {
                Spacer(Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (spec.highlightLabel != null) {
                        Text(spec.highlightLabel, style = RmType.CardLink, color = RmColor.Text3)
                        Spacer(Modifier.width(14.dp))
                    }
                    Box(
                        Modifier.clip(RoundedCornerShape(RmDimens.RadPill)).background(RmColor.AccentSoft)
                            .border(1.dp, RmColor.AccentLine, RoundedCornerShape(RmDimens.RadPill))
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                    ) { Text(spec.highlightValue, color = RmColor.Accent, style = RmType.CardLink) }
                }
            }
            Spacer(Modifier.height(38.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DialogButton("再想想", filled = false, danger = false, modifier = Modifier.weight(1f), onClick = onDismiss)
                DialogButton(spec.confirmText, filled = true, danger = spec.danger, modifier = Modifier.weight(1f)) {
                    onDismiss(); spec.action()
                }
            }
        }
    }
}

@Composable
private fun DialogButton(
    text: String,
    filled: Boolean,
    danger: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val accent = if (danger) RmColor.WarnRed else RmColor.Accent
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(RmDimens.RadButton))
            .then(
                if (filled) Modifier.background(accent)
                else Modifier.background(RmColor.CardHi).border(1.dp, RmColor.Line, RoundedCornerShape(RmDimens.RadButton)),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = RmType.ButtonText,
            color = if (filled) (if (danger) Color.White else RmColor.AccentInk) else RmColor.Text2,
        )
    }
}

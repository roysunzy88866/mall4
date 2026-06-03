package space.hearagain.ridemall.util

/** 秒 → "mm:ss"(179 → "02:59"),倒计时显示用。 */
fun secondsToClock(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    return "%02d:%02d".format(s / 60, s % 60)
}

/**
 * 后端订单状态码 → 中文展示。未知值原样返回(第三刀扩状态机时不至于让车机崩)。
 * 第三刀会补 待发货/运输中/派送中/已签收/已取消/退货… 等。
 */
/** 主阶段步条:码 + 中文(与后端 STAGES 对齐)。 */
val STAGE_CODES = listOf("paid", "shipping", "delivering", "delivered")
val STAGE_LABELS = listOf("待发货", "运输中", "派送中", "已签收")

/** 当前状态在主阶段步条上的序号;分支状态(取消/退货)返回 -1。 */
fun stageIndexOf(status: String): Int = STAGE_CODES.indexOf(status)

fun orderStatusLabel(raw: String): String = when (raw) {
    "paid" -> "待发货"
    "pending_ship", "待发货" -> "待发货"
    "shipping", "运输中" -> "运输中"
    "delivering", "派送中" -> "派送中"
    "delivered", "已签收" -> "已签收"
    "cancelled", "已取消" -> "已取消"
    "return_review", "退货审核中" -> "退货审核中"
    "returning", "退货中" -> "退货中"
    "refunded", "已退款" -> "已退款"
    "return_rejected", "退货被拒" -> "退货被拒"
    else -> raw
}

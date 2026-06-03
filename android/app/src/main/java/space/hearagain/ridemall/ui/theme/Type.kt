package space.hearagain.ridemall.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 字体与字号刻度。字号采用设计稿「特大密度 ×1.24」列的数值(README「字号刻度」)。
 *
 * 字体族集中在此两处,便于日后无痛切换为真字体(D10):
 *  - [BodyFamily]   中文/正文 —— 暂用系统 sans-serif(设计 README 许可系统黑体替代 Noto Sans SC)。
 *  - [NumberFamily] 数字/价格/品牌 —— 暂用 Monospace 近似 Oxanium 的等宽数字观感。
 * 配合 ScalingContainer 的密度覆盖,1.sp ≈ 1 设计 px,故此处直接用设计 px 值。
 */
val BodyFamily: FontFamily = FontFamily.SansSerif
val NumberFamily: FontFamily = FontFamily.Monospace

object RmType {
    val NavItem = TextStyle(fontFamily = BodyFamily, fontSize = 27.sp, fontWeight = FontWeight.Medium)
    val PageTitle = TextStyle(fontFamily = BodyFamily, fontSize = 52.sp, fontWeight = FontWeight.Bold)
    val SectionTitle = TextStyle(fontFamily = BodyFamily, fontSize = 42.sp, fontWeight = FontWeight.Bold)
    val CountText = TextStyle(fontFamily = BodyFamily, fontSize = 27.sp, fontWeight = FontWeight.Normal)

    val CardName = TextStyle(fontFamily = BodyFamily, fontSize = 30.sp, fontWeight = FontWeight.Medium)
    val CardPrice = TextStyle(fontFamily = NumberFamily, fontSize = 35.sp, fontWeight = FontWeight.Bold)
    val CardLink = TextStyle(fontFamily = BodyFamily, fontSize = 25.sp, fontWeight = FontWeight.Normal)

    val BannerKicker = TextStyle(fontFamily = BodyFamily, fontSize = 25.sp, fontWeight = FontWeight.Bold)
    val BannerTitle = TextStyle(fontFamily = BodyFamily, fontSize = 64.sp, fontWeight = FontWeight.Bold)
    val BannerDesc = TextStyle(fontFamily = BodyFamily, fontSize = 29.sp, fontWeight = FontWeight.Normal)
    val BannerPrice = TextStyle(fontFamily = NumberFamily, fontSize = 50.sp, fontWeight = FontWeight.Bold)

    val Brand = TextStyle(fontFamily = NumberFamily, fontSize = 32.sp, fontWeight = FontWeight.Bold)

    // 全屏覆盖页(网络异常)字号在设计里固定、不随密度;此处给出固定值。
    val OverlayTitle = TextStyle(fontFamily = BodyFamily, fontSize = 50.sp, fontWeight = FontWeight.Bold)
    val OverlayHint = TextStyle(fontFamily = BodyFamily, fontSize = 27.sp, fontWeight = FontWeight.Normal)
    val EmptyText = TextStyle(fontFamily = BodyFamily, fontSize = 29.sp, fontWeight = FontWeight.Normal)
    val ButtonText = TextStyle(fontFamily = BodyFamily, fontSize = 28.sp, fontWeight = FontWeight.Bold)
}

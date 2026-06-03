package space.hearagain.ridemall.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 设计稿颜色 token(深色冷调主题)。数值来源:design_handoff_ridemall_carstore/README.md「Design Tokens · 颜色」。
 * 强调色已选定青色 #2FD6C4。
 */
object RmColor {
    val Bg = Color(0xFF0A0C0F)
    val BgGradA = Color(0xFF0C0F13)
    val BgGradB = Color(0xFF090A0D)
    val Panel = Color(0xFF101318)
    val Card = Color(0xFF161A20)
    val CardHi = Color(0xFF1B2027)
    val Line = Color(0xFF262C35)
    val LineSoft = Color(0xFF1C2128)
    val Text1 = Color(0xFFEEF1F5)
    val Text2 = Color(0xFFAAB2BD)
    val Text3 = Color(0xFF6B7480)

    val Accent = Color(0xFF2FD6C4)
    val AccentInk = Color(0xFF04201D)
    val AccentSoft = Color(0x242FD6C4) // 14% alpha
    val AccentLine = Color(0x6B2FD6C4) // 42% alpha
    val AccentGlow = Color(0x4D2FD6C4) // 30% alpha

    val WarnRed = Color(0xFFFF6B6B)
    val WarnAmber = Color(0xFFFFB74D)
    val WarnAmberSoft = Color(0x1AFFA84C) // ≈10% alpha,网络异常环底
    val WarnAmberLine = Color(0x66FFA84C) // ≈40% alpha,网络异常环描边
}

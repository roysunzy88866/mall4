package space.hearagain.ridemall.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 设计稿尺寸/间距/圆角 token,单位按 1920×1080 设计像素直接写为 dp。
 * 配合 ScalingContainer 覆盖 LocalDensity,使「1 设计 px == 1 dp」并整体等比缩放到实际屏幕。
 * 数值来源:design_handoff_ridemall_carstore/README.md「圆角 / 间距 / 尺寸」。
 */
object RmDimens {
    // 侧栏
    val RailWidth = 280.dp
    val RailPadTop = 40.dp
    val RailPadH = 22.dp
    val RailPadBottom = 30.dp
    val NavItemHeight = 64.dp
    val NavItemPadH = 18.dp
    val NavIconSize = 26.dp
    val NavIconTextGap = 16.dp
    val NavItemGap = 16.dp
    val NavSelBarW = 4.dp
    val NavSelBarH = 30.dp

    // 内容区
    val ContentPadTop = 46.dp
    val ContentPadH = 58.dp
    val ContentPadBottom = 60.dp

    // 商品网格
    val GridGap = 26.dp
    val CardImageHeight = 220.dp

    // Banner
    val BannerHeight = 384.dp

    // 圆角
    val RadCard = 10.dp
    val RadPanel = 14.dp
    val RadButton = 14.dp
    val RadSmall = 9.dp
    val RadThumb = 10.dp
    val RadPill = 999.dp

    // 标题区块竖条
    val SectionBarW = 5.dp
    val SectionBarH = 30.dp
}

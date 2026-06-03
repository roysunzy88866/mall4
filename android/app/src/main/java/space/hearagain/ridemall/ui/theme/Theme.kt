package space.hearagain.ridemall.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * 全局深色主题。Material3 仅作基线(默认文字色/涟漪等),具体视觉走 RmColor/RmType/RmDimens。
 */
private val RidemallColors = darkColorScheme(
    primary = RmColor.Accent,
    onPrimary = RmColor.AccentInk,
    background = RmColor.Bg,
    onBackground = RmColor.Text1,
    surface = RmColor.Panel,
    onSurface = RmColor.Text1,
)

@Composable
fun RidemallTheme(content: @Composable () -> Unit) {
    @Suppress("UNUSED_EXPRESSION")
    isSystemInDarkTheme() // 本应用恒深色,不随系统切换
    MaterialTheme(colorScheme = RidemallColors, content = content)
}

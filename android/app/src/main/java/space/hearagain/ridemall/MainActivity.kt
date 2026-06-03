package space.hearagain.ridemall

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import space.hearagain.ridemall.ui.ScalingContainer
import space.hearagain.ridemall.ui.StoreScreen
import space.hearagain.ridemall.ui.theme.RidemallTheme

/**
 * 单 Activity 入口。锁横屏(Manifest)+ 全屏沉浸 + 保持常亮。
 * 本片(Phase A)只验证骨架可编译可启动;界面在后续片补。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersive()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            RidemallTheme {
                ScalingContainer {
                    StoreScreen()
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // 失焦后(如系统弹窗)重新进入沉浸,避免系统栏残留。
        if (hasFocus) enableImmersive()
    }

    private fun enableImmersive() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

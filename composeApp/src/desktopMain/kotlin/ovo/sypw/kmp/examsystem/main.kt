package ovo.sypw.kmp.examsystem

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import java.awt.Dimension

/**
 * 桌面端应用主入口
 * 配置窗口最小尺寸为360x680，默认最大化显示
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KMP ExamSystem",
        state = WindowState(
//            placement = WindowPlacement.Maximized,  // 默认最大化
            size = DpSize(800.dp, 600.dp)  // 初始尺寸（最大化时不生效）
        ),
        resizable = true,  // 允许调整窗口大小
        undecorated = false  // 保留窗口装饰（标题栏等）
    ) {
        // 设置窗口最小尺寸
        window.minimumSize = Dimension(360, 680)
        App()
    }
}
package ovo.sypw.kmp.examsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ovo.sypw.kmp.examsystem.presentation.navigation.BottomNavigationBar
import ovo.sypw.kmp.examsystem.presentation.navigation.NavigationManager
import ovo.sypw.kmp.examsystem.presentation.navigation.NavigationScreen
import ovo.sypw.kmp.examsystem.presentation.navigation.rememberNavigationManager
import ovo.sypw.kmp.examsystem.utils.Logger
import ovo.sypw.kmp.examsystem.utils.ResponsiveLayoutConfig
import ovo.sypw.kmp.examsystem.utils.getResponsiveLayoutConfig

/**
 * 平台特定的 Koin 应用初始化
 */
@Composable
expect fun PlatformKoinApplication(content: @Composable () -> Unit)

/**
 * 应用主组件
 * 根据窗口宽度自适应显示导航界面
 */
@Composable
fun App() {
    Logger.i("APP START ON ${getPlatform().name}")
    PlatformKoinApplication {
        MaterialTheme {
            MainAppContent()
        }
    }

}


/**
 * 主应用内容（已登录状态）
 */
@Composable
private fun MainAppContent() {
    val navigationManager = rememberNavigationManager()
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val layoutConfig = getResponsiveLayoutConfig(maxWidth)
        BottomNavigationLayout(
            navigationManager = navigationManager,
            layoutConfig = layoutConfig
        )
    }
}

/**
 * 底部导航布局
 * @param navigationManager 导航管理器
 */
@Composable
private fun BottomNavigationLayout(
    navigationManager: NavigationManager,
    layoutConfig: ResponsiveLayoutConfig
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 主要内容区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            MainContent(
                navigationManager = navigationManager,
                modifier = Modifier.fillMaxSize(),
                layoutConfig = layoutConfig
            )
        }

        // 底部导航栏
        BottomNavigationBar(
            navigationManager = navigationManager
        )
    }
}

/**
 * 主要内容区域
 * 根据当前选中的页面显示对应内容
 * @param navigationManager 导航管理器
 * @param modifier 修饰符
 */
@Composable
private fun MainContent(
    navigationManager: NavigationManager,
    modifier: Modifier = Modifier,
    layoutConfig: ResponsiveLayoutConfig
) {
    val route = navigationManager.currentScreen.value
    NavigationScreen(route)
}


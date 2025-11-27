package ovo.sypw.kmp.examsystem.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * 导航管理器类
 * 负责管理当前选中的页面状态
 */
class NavigationManager {
    private val _currentScreen = mutableStateOf(AppScreen.HOME.route)
    val currentScreen: State<String> = _currentScreen


    /**
     * 导航到指定页面
     * @param route 目标页面路由
     */
    fun navigateTo(route: String) {
        _currentScreen.value = route
    }

    /**
     * 检查当前是否为指定页面
     * @param route 页面路由
     * @return 是否为当前页面
     */
    fun isCurrentScreen(route: String): Boolean {
        return _currentScreen.value == route
    }
}

/**
 * 创建导航管理器的Composable函数
 * @return 导航管理器实例
 */
@Composable
fun rememberNavigationManager(): NavigationManager {
    return remember { NavigationManager() }
}
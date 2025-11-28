package ovo.sypw.kmp.examsystem.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

/**
 * 底部导航栏组件 (移动端)
 * @param navigationManager 导航管理器
 * @param modifier 修饰符
 */
@Composable
fun BottomNavigationBar(
    navigationManager: NavigationManager,
    modifier: Modifier = Modifier
) {
    val currentScreen by navigationManager.currentScreen
    val navigationItems = getNavigationItems()

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = currentScreen == item.route,
                onClick = { navigationManager.navigateTo(item.route) }
            )
        }
    }
}

/**
 * 侧边导航栏组件 (桌面端)
 * @param navigationManager 导航管理器
 * @param modifier 修饰符
 */
@Composable
fun SideNavigationRail(
    navigationManager: NavigationManager,
    modifier: Modifier = Modifier
) {
    val currentScreen by navigationManager.currentScreen
    val navigationItems = getNavigationItems()

    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        navigationItems.forEach { item ->
            NavigationRailItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = currentScreen == item.route,
                onClick = { navigationManager.navigateTo(item.route) }
            )
        }
    }
}

/**
 * 响应式导航组件
 * 根据屏幕尺寸自动选择底部导航或侧边导航
 * @param navigationManager 导航管理器
 * @param screenSize 屏幕尺寸类型
 */
@Composable
fun ResponsiveNavigationBar(
    navigationManager: NavigationManager,
    screenSize: ResponsiveUtils.ScreenSize
) {
    when (screenSize) {
        ResponsiveUtils.ScreenSize.COMPACT, ResponsiveUtils.ScreenSize.MEDIUM -> {
            // 移动端和平板使用底部导航
            BottomNavigationBar(navigationManager)
        }
        ResponsiveUtils.ScreenSize.EXPANDED -> {
            // 桌面端使用侧边导航
            SideNavigationRail(navigationManager)
        }
    }
}


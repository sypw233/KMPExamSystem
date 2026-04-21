package ovo.sypw.kmp.examsystem.presentation.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.ui.unit.dp

/**
 * 底部导航栏（移动端）
 * 根据 NavigationManager.userRole 动态展示角色专属导航项
 */
@Composable
fun BottomNavigationBar(
    navigationManager: NavigationManager,
    modifier: Modifier = Modifier
) {
    val currentScreen by navigationManager.currentScreen
    val navigationItems = navigationManager.navigationItems()

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.title)
                },
                label = { Text(item.title) },
                selected = currentScreen == item.route,
                onClick = { navigationManager.navigateTo(item.route) }
            )
        }
    }
}

/**
 * 侧边导航栏（桌面端）
 * 根据 NavigationManager.userRole 动态展示角色专属导航项
 */
@Composable
fun SideNavigationRail(
    navigationManager: NavigationManager,
    modifier: Modifier = Modifier
) {
    val currentScreen by navigationManager.currentScreen
    val navigationItems = navigationManager.navigationItems()

    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        navigationItems.forEach { item ->
            NavigationRailItem(
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.title)
                },
                label = { Text(item.title) },
                selected = currentScreen == item.route,
                onClick = { navigationManager.navigateTo(item.route) }
            )
        }
    }
}

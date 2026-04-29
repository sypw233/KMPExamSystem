package ovo.sypw.kmp.examsystem.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.domain.AuthState

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
 * 侧边导航栏（桌面端增强版）
 *
 * 包含：
 * - 应用标题区
 * - 当前用户信息（头像、姓名、角色）
 * - 角色专属导航项
 * - 固定宽度 240dp，符合桌面端实用习惯
 */
@Composable
fun SideNavigationRail(
    navigationManager: NavigationManager,
    modifier: Modifier = Modifier
) {
    val currentScreen by navigationManager.currentScreen
    val navigationItems = navigationManager.navigationItems()
    val authRepository: AuthRepository = koinInject()
    val authState by authRepository.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user

    Surface(
        modifier = modifier.width(240.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── 应用标题 ──────────────────────────────────────────────────
            Text(
                text = "考试系统",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // ── 用户信息区 ─────────────────────────────────────────────────
            if (user != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = user.realName?.take(1) ?: user.username.take(1),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = user.realName ?: user.username,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when (UserRole.from(user.role)) {
                            UserRole.ADMIN -> "管理员"
                            UserRole.TEACHER -> "教师"
                            UserRole.STUDENT -> "学生"
                            UserRole.UNKNOWN -> "用户"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            // ── 导航项 ────────────────────────────────────────────────────
            navigationItems.forEach { item ->
                val selected = currentScreen == item.route
                NavigationRailItem(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    icon = {
                        Icon(imageVector = item.icon, contentDescription = item.title)
                    },
                    label = {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    selected = selected,
                    onClick = { navigationManager.navigateTo(item.route) },
                    alwaysShowLabel = true
                )
            }
        }
    }
}

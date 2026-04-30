package ovo.sypw.kmp.examsystem.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    navigationManager: NavigationManager,
    modifier: Modifier = Modifier
) {
    val currentScreen by navigationManager.currentScreen
    val role by navigationManager.userRole
    val navigationItems = getBottomNavigationItemsForRole(role)
    var overflowExpanded by remember { mutableStateOf(false) }

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp
    ) {
        navigationItems.primaryItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(item.title) },
                selected = currentScreen == item.route,
                onClick = { navigationManager.navigateTo(item.route) }
            )
        }

        if (navigationItems.overflowItems.isNotEmpty()) {
            val overflowSelected = navigationItems.overflowItems.any { it.route == currentScreen }
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "更多导航",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("更多") },
                selected = overflowSelected,
                onClick = { overflowExpanded = true }
            )
        }
    }

    if (overflowExpanded) {
        ModalBottomSheet(
            onDismissRequest = { overflowExpanded = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "更多功能",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
                navigationItems.overflowItems.forEach { item ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = item.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selected = currentScreen == item.route,
                        onClick = {
                            overflowExpanded = false
                            navigationManager.navigateTo(item.route)
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.heightIn(min = 56.dp)
                    )
                }
            }
        }
    }
}

/**
 * 侧边导航栏（桌面端）
 *
 * 包含：
 * - 应用标题区
 * - 当前用户信息（头像、姓名、角色）
 * - 角色专属导航项
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

    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                AppMark(compact = true)
                if (user != null) {
                    val displayName = user.realName ?: user.username
                    UserAvatar(displayName = displayName, size = 48)
                }
            }
        }
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        navigationItems.forEach { item ->
            NavigationRailItem(
                selected = currentScreen == item.route,
                onClick = { navigationManager.navigateTo(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
fun AppPermanentDrawerSheet(
    navigationManager: NavigationManager,
    modifier: Modifier = Modifier
) {
    val currentScreen by navigationManager.currentScreen
    val navigationItems = navigationManager.navigationItems()
    val authRepository: AuthRepository = koinInject()
    val authState by authRepository.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user

    PermanentDrawerSheet(
        modifier = modifier.width(288.dp).fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppMark(compact = false)

            if (user != null) {
                val displayName = user.realName ?: user.username
                UserProfileListItem(
                    displayName = displayName,
                    role = UserRole.from(user.role)
                )
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            navigationItems.forEach { item ->
                DrawerNavigationItem(
                    item = item,
                    selected = currentScreen == item.route,
                    onClick = { navigationManager.navigateTo(item.route) }
                )
            }
        }
    }
}

@Composable
private fun DrawerNavigationItem(
    item: NavigationItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = item.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        shape = MaterialTheme.shapes.extraLarge,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unselectedContainerColor = Color.Transparent
        )
    )
}

@Composable
private fun AppMark(compact: Boolean) {
    if (compact) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "考",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "考",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "考试系统",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "KMP Exam",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UserProfileListItem(
    displayName: String,
    role: UserRole
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = displayName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
            },
            supportingContent = {
                Text(text = role.displayName())
            },
            leadingContent = {
                UserAvatar(displayName = displayName, size = 44)
            },
            colors = androidx.compose.material3.ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun UserAvatar(
    displayName: String,
    size: Int
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(size.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = displayName.take(1).ifBlank { "?" },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private fun UserRole.displayName(): String = when (this) {
    UserRole.ADMIN -> "管理员"
    UserRole.TEACHER -> "教师"
    UserRole.STUDENT -> "学生"
    UserRole.UNKNOWN -> "用户"
}

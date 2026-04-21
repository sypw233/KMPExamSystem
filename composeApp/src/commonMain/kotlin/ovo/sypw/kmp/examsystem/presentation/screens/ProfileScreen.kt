package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.UserInfo
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationViewModel

/**
 * 个人中心界面
 * 支持跳转到成绩历史和通知中心
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val authRepository: AuthRepository = koinInject()
    val notificationViewModel: NotificationViewModel = koinInject()

    val authState by authRepository.authState.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val scope = rememberCoroutineScope()

    val user = (authState as? AuthState.Authenticated)?.user

    // 子页面路由状态
    var currentSubScreen by remember { mutableStateOf<String?>(null) }

    val isTeacherOrAdmin = user?.role?.uppercase() in listOf("TEACHER", "ADMIN")

    when (currentSubScreen) {
        "grades" -> GradeHistoryScreen(onBack = { currentSubScreen = null })
        "notifications" -> NotificationScreen(onBack = { currentSubScreen = null })
        else -> ProfileMainScreen(
            user = user,
            unreadCount = unreadCount,
            isTeacherOrAdmin = isTeacherOrAdmin,
            onNavigateToGrades = { currentSubScreen = "grades" },
            onNavigateToNotifications = { currentSubScreen = "notifications" },
            onLogout = { scope.launch { authRepository.logout() } }
        )
    }
}

@Composable
private fun ProfileMainScreen(
    user: UserInfo?,
    unreadCount: Long,
    isTeacherOrAdmin: Boolean,
    onNavigateToGrades: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = "个人中心",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // 用户信息卡
                UserInfoCard(user = user)

                Spacer(modifier = Modifier.height(8.dp))

                // 学生功能菜单组
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        MenuItem(
                            icon = Icons.Default.DateRange,
                            title = "考试历史",
                            subtitle = "查看我的成绩记录",
                            onClick = onNavigateToGrades
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        MenuItem(
                            icon = Icons.Default.Notifications,
                            title = "通知中心",
                            subtitle = if (unreadCount > 0) "${unreadCount} 条未读消息" else "查看所有通知",
                            badge = if (unreadCount > 0) unreadCount.toString() else null,
                            onClick = onNavigateToNotifications
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        MenuItem(
                            icon = Icons.Default.Person,
                            title = "个人信息",
                            subtitle = "查看和编辑个人资料",
                            onClick = { }
                        )
                    }
                }




                // 其他菜单组
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        MenuItem(
                            icon = Icons.Default.Settings,
                            title = "设置",
                            onClick = { }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        MenuItem(
                            icon = Icons.Default.Info,
                            title = "帮助与反馈",
                            onClick = { }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        MenuItem(
                            icon = Icons.Default.Info,
                            title = "关于",
                            onClick = { }
                        )
                    }
                }

                // 退出登录按钮
                OutlinedButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("退出登录")
                }
            }
        }
    }
}

@Composable
private fun UserInfoCard(user: UserInfo?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user?.realName?.take(1)?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = user?.realName ?: "未登录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (user != null) {
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val roleLabel = when (user.role.uppercase()) {
                        "STUDENT" -> "学生"
                        "TEACHER" -> "教师"
                        "ADMIN" -> "管理员"
                        else -> user.role
                    }
                    SuggestionChip(
                        onClick = { },
                        label = { Text(roleLabel) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = null
                    )
                } else {
                    Text(
                        text = "请登录以查看更多信息",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = iconTint)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (badge != null) {
                Badge(containerColor = MaterialTheme.colorScheme.error) {
                    Text(badge, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

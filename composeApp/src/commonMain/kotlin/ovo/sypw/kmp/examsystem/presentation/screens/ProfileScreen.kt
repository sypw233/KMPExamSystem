package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.UserInfo
import ovo.sypw.kmp.examsystem.data.dto.UserUpdateRequest
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.data.repository.UserManageRepository
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.presentation.screens.admin.SystemSettingsScreen
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationViewModel

@Composable
fun ProfileScreen() {
    val authRepository: AuthRepository = koinInject()
    val userManageRepository: UserManageRepository = koinInject()
    val notificationViewModel: NotificationViewModel = koinInject()
    val scope = rememberCoroutineScope()

    val authState by authRepository.authState.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    val isTeacherOrAdmin = user?.role?.uppercase() in listOf("TEACHER", "ADMIN")
    val isAdmin = user?.role?.uppercase() == "ADMIN"

    var currentSubScreen by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        notificationViewModel.loadUnreadCount()
    }

    when (currentSubScreen) {
        "grades" -> GradeHistoryScreen(onBack = { currentSubScreen = null })
        "notifications" -> NotificationScreen(onBack = { currentSubScreen = null })
        "system_settings" -> SystemSettingsScreen()
        else -> {
            Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
                ProfileMainScreen(
                    modifier = Modifier.padding(padding),
                    user = user,
                    unreadCount = unreadCount,
                    isTeacherOrAdmin = isTeacherOrAdmin,
                    isAdmin = isAdmin,
                    onNavigateToGrades = { currentSubScreen = "grades" },
                    onNavigateToNotifications = { currentSubScreen = "notifications" },
                    onNavigateToSystemSettings = { currentSubScreen = "system_settings" },
                    onOpenEditProfile = { showEditProfileDialog = true },
                    onOpenChangePassword = { showChangePasswordDialog = true },
                    onLogout = { scope.launch { authRepository.logout() } }
                )
            }
        }
    }

    if (showEditProfileDialog && user != null) {
        EditProfileDialog(
            user = user,
            onDismiss = { showEditProfileDialog = false },
            onConfirm = { realName, email ->
                scope.launch {
                    userManageRepository.updateUser(
                        user.id,
                        UserUpdateRequest(
                            realName = realName,
                            email = email,
                            role = user.role
                        )
                    ).onSuccess {
                        snackbar.showSnackbar("资料更新成功")
                        authRepository.checkAuthState()
                    }.onFailure {
                        snackbar.showSnackbar("更新失败：${it.message}")
                    }
                }
                showEditProfileDialog = false
            }
        )
    }

    if (showChangePasswordDialog && user != null) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { oldPwd, newPwd ->
                if (oldPwd.isBlank()) {
                    scope.launch { snackbar.showSnackbar("请输入旧密码") }
                    return@ChangePasswordDialog
                }
                scope.launch {
                    userManageRepository.resetPassword(user.id, newPwd)
                        .onSuccess { snackbar.showSnackbar("密码更新成功") }
                        .onFailure { snackbar.showSnackbar("更新失败：${it.message}") }
                }
                showChangePasswordDialog = false
            }
        )
    }
}

@Composable
private fun ProfileMainScreen(
    modifier: Modifier = Modifier,
    user: UserInfo?,
    unreadCount: Long,
    isTeacherOrAdmin: Boolean,
    isAdmin: Boolean,
    onNavigateToGrades: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSystemSettings: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize().widthIn(max = 680.dp).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("个人中心", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            UserInfoCard(user = user)

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column {
                    MenuItem(
                        icon = Icons.Default.DateRange,
                        title = "考试历史",
                        subtitle = "查看我的成绩记录",
                        onClick = onNavigateToGrades
                    )
                    MenuItem(
                        icon = Icons.Default.Notifications,
                        title = "通知中心",
                        subtitle = if (unreadCount > 0) "未读 ${unreadCount}" else "查看全部通知",
                        badge = if (unreadCount > 0) unreadCount.toString() else null,
                        onClick = onNavigateToNotifications
                    )
                    MenuItem(
                        icon = Icons.Default.Person,
                        title = "编辑资料",
                        subtitle = "修改姓名与邮箱",
                        onClick = onOpenEditProfile
                    )
                    MenuItem(
                        icon = Icons.Default.Lock,
                        title = "密码管理",
                        subtitle = "修改登录密码",
                        onClick = onOpenChangePassword
                    )
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column {
                    if (isAdmin) {
                        MenuItem(
                            icon = Icons.Default.Settings,
                            title = "系统设置",
                            subtitle = "AI 配置与系统参数",
                            onClick = onNavigateToSystemSettings
                        )
                    }
                    MenuItem(
                        icon = Icons.Default.Info,
                        title = if (isTeacherOrAdmin) "帮助中心" else "帮助与反馈",
                        subtitle = "功能使用说明",
                        onClick = { }
                    )
                }
            }

            OutlinedActionButton(
                text = "退出登录",
                onClick = onLogout,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    user: UserInfo,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var realName by remember { mutableStateOf(user.realName) }
    var email by remember { mutableStateOf(user.email.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑个人资料") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = realName,
                    onValueChange = { realName = it },
                    label = { Text("姓名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(realName.trim(), email.trim().ifBlank { null }) }, enabled = realName.isNotBlank()) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (oldPwd: String, newPwd: String) -> Unit
) {
    var oldPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    val valid = oldPwd.isNotBlank() && newPwd.length >= 6 && newPwd == confirmPwd

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改密码") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = oldPwd,
                    onValueChange = { oldPwd = it },
                    label = { Text("旧密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newPwd,
                    onValueChange = { newPwd = it },
                    label = { Text("新密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = confirmPwd,
                    onValueChange = { confirmPwd = it },
                    label = { Text("确认新密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(oldPwd, newPwd) }, enabled = valid) { Text("更新") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun UserInfoCard(user: UserInfo?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
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
                    user.email?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
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
                        text = "请登录后查看个人信息",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    color: Color
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, color)
    ) {
        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = iconTint)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = textColor)
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

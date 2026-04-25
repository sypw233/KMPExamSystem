package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.UserInfo
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.data.repository.FileRepository
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.presentation.screens.admin.SystemSettingsScreen
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationViewModel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import ovo.sypw.kmp.examsystem.utils.file.rememberFileUtils

@Composable
fun ProfileScreen() {
    val authRepository: AuthRepository = koinInject()
    val fileRepository: FileRepository = koinInject()
    val notificationViewModel: NotificationViewModel = koinInject()
    val scope = rememberCoroutineScope()
    val fileUtils = rememberFileUtils()

    val authState by authRepository.authState.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    val isTeacherOrAdmin = user?.role?.uppercase() in listOf("TEACHER", "ADMIN")
    val isAdmin = user?.role?.uppercase() == "ADMIN"

    var currentSubScreen by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        notificationViewModel.loadUnreadCount()
    }

    when (currentSubScreen) {
        "grades" -> GradeHistoryScreen(onBack = { currentSubScreen = null })
        "notifications" -> NotificationScreen(onBack = { currentSubScreen = null })
        "system_settings" -> SystemSettingsScreen(onBack = { currentSubScreen = null })
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
                    onOpenChangePassword = { showChangePasswordDialog = true },
                    onLogout = { scope.launch { authRepository.logout() } },
                    onOpenEditProfile = { showEditProfileDialog = true },
                    onOpenHelp = { showHelpDialog = true }
                )
            }
        }
    }

    if (showEditProfileDialog && user != null) {
        EditProfileDialog(
            user = user,
            onDismiss = { showEditProfileDialog = false },
            onConfirm = { realName, email, avatarUrl ->
                scope.launch {
                    authRepository.updateProfile(
                        nickname = realName,
                        email = email,
                        avatar = avatarUrl
                    ).onSuccess {
                        snackbar.showSnackbar("资料更新成功")
                        showEditProfileDialog = false
                    }.onFailure {
                        snackbar.showSnackbar("更新失败：${it.message}")
                    }
                }
            },
            onUploadAvatar = { onSuccess, onError ->
                scope.launch {
                    try {
                        val file = fileUtils.selectImage()
                        if (file != null) {
                            val bytes = fileUtils.readBytes(file)
                            fileRepository.uploadImage(bytes, file.name)
                                .onSuccess { response ->
                                    onSuccess(response.fileUrl)
                                }
                                .onFailure { onError(it.message ?: "上传失败") }
                        }
                    } catch (e: Exception) {
                        onError(e.message ?: "上传异常")
                    }
                }
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
                if (newPwd.length < 6) {
                    scope.launch { snackbar.showSnackbar("新密码长度不能少于6位") }
                    return@ChangePasswordDialog
                }
                scope.launch {
                    authRepository.changePassword(oldPwd, newPwd)
                        .onSuccess {
                            snackbar.showSnackbar("密码更新成功")
                            showChangePasswordDialog = false
                        }
                        .onFailure { snackbar.showSnackbar("更新失败：${it.message}") }
                }
            }
        )
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
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
    onOpenChangePassword: () -> Unit,
    onLogout: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenHelp: () -> Unit
) {
    val isStudent = user?.role?.uppercase() == "STUDENT"
    val config = LocalResponsiveConfig.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(if (LocalResponsiveConfig.current.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = 680.dp) else Modifier)
            .verticalScroll(rememberScrollState())
            .padding(config.screenPadding),
        verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UserInfoCard(user = user, onClick = onOpenEditProfile)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                if (isStudent) {
                    MenuItem(
                        icon = Icons.Default.DateRange,
                        title = "考试历史",
                        subtitle = "查看我的成绩记录",
                        onClick = onNavigateToGrades
                    )
                }
                MenuItem(
                    icon = Icons.Default.Notifications,
                    title = "通知中心",
                    subtitle = if (unreadCount > 0) "未读 ${unreadCount}" else "查看全部通知",
                    badge = if (unreadCount > 0) unreadCount.toString() else null,
                    onClick = onNavigateToNotifications
                )
                MenuItem(
                    icon = Icons.Default.Lock,
                    title = "密码管理",
                    subtitle = "修改登录密码",
                    onClick = onOpenChangePassword
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
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
                    onClick = onOpenHelp
                )
            }
        }

        OutlinedActionButton(
            text = "退出登录",
            onClick = onLogout,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))
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

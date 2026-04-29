package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.data.repository.FileRepository
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.presentation.screens.admin.SystemSettingsScreen
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationViewModel
import ovo.sypw.kmp.examsystem.utils.file.rememberFileUtils

@OptIn(ExperimentalComposeUiApi::class)
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

    // 子页面返回处理
    BackHandler(enabled = currentSubScreen != null) {
        currentSubScreen = null
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

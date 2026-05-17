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
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationViewModel
import ovo.sypw.kmp.examsystem.utils.file.rememberFileUtils

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProfileScreen() {
    val authRepository: AuthRepository = koinInject()
    val fileRepository: FileRepository = koinInject()
    val notificationViewModel: NotificationViewModel = koinInject()
    val fileUtils = rememberFileUtils()
    val scope = rememberCoroutineScope()

    val authState by authRepository.authState.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    val isTeacherOrAdmin = user?.role?.uppercase() in listOf("TEACHER", "ADMIN")

    var currentSubScreen by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        notificationViewModel.loadUnreadCount(force = false)
    }

    BackHandler(enabled = currentSubScreen != null) {
        currentSubScreen = null
    }

    when (currentSubScreen) {
        "grades" -> GradeHistoryScreen(onBack = { currentSubScreen = null })
        "notifications" -> NotificationScreen(onBack = { currentSubScreen = null })
        "settings" -> AppSettingsScreen(onBack = { currentSubScreen = null })
        else -> {
            Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
                ProfileMainScreen(
                    modifier = Modifier.padding(padding),
                    user = user,
                    unreadCount = unreadCount,
                    isTeacherOrAdmin = isTeacherOrAdmin,
                    onNavigateToGrades = { currentSubScreen = "grades" },
                    onNavigateToNotifications = { currentSubScreen = "notifications" },
                    onLogout = { scope.launch { authRepository.logout() } },
                    onOpenEditProfile = { showEditProfileDialog = true },
                    onOpenHelp = { showHelpDialog = true },
                    onOpenSettings = { currentSubScreen = "settings" }
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
                        snackbar.showSnackbar("个人资料已更新")
                        showEditProfileDialog = false
                    }.onFailure {
                        snackbar.showSnackbar("更新资料失败：${it.message}")
                    }
                }
            },
            onOpenChangePassword = {
                showEditProfileDialog = false
                showChangePasswordDialog = true
            },
            onUploadAvatar = { onSuccess, onError ->
                scope.launch {
                    try {
                        val file = fileUtils.selectImage()
                        if (file != null) {
                            val bytes = fileUtils.readBytes(file)
                            fileRepository.uploadImage(bytes, file.name, "avatars")
                                .onSuccess { response -> onSuccess(response.fileUrl) }
                                .onFailure { onError(it.message ?: "头像上传失败") }
                        } else {
                            onError("未选择图片")
                        }
                    } catch (e: Exception) {
                        onError(e.message ?: "头像上传失败")
                    }
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { oldPwd, newPwd ->
                scope.launch {
                    authRepository.changePassword(oldPwd, newPwd).onSuccess {
                        snackbar.showSnackbar("密码已修改")
                        showChangePasswordDialog = false
                    }.onFailure {
                        snackbar.showSnackbar("修改密码失败：${it.message}")
                    }
                }
            }
        )
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }
}

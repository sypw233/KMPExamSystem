package ovo.sypw.kmp.examsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.presentation.components.GlobalDialog
import ovo.sypw.kmp.examsystem.presentation.navigation.BottomNavigationBar
import ovo.sypw.kmp.examsystem.presentation.navigation.NavigationManager
import ovo.sypw.kmp.examsystem.presentation.navigation.NavigationScreen
import ovo.sypw.kmp.examsystem.presentation.navigation.SideNavigationRail
import ovo.sypw.kmp.examsystem.presentation.navigation.UserRole
import ovo.sypw.kmp.examsystem.presentation.navigation.rememberNavigationManager
import ovo.sypw.kmp.examsystem.presentation.screens.ExamTakingScreen
import ovo.sypw.kmp.examsystem.presentation.screens.auth.LoginScreen
import ovo.sypw.kmp.examsystem.presentation.screens.auth.RegisterScreen
import ovo.sypw.kmp.examsystem.presentation.theme.AppTheme
import ovo.sypw.kmp.examsystem.utils.DialogManager
import ovo.sypw.kmp.examsystem.utils.Logger
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import ovo.sypw.kmp.examsystem.utils.getResponsiveLayoutConfig

/**
 * 平台特定的 Koin 应用初始化
 */
@Composable
expect fun PlatformKoinApplication(content: @Composable () -> Unit)

/**
 * 应用主组件
 * 根据窗口宽度自适应显示导航界面
 */
@Composable
fun App() {
    Logger.i("APP START ON ${getPlatform().name}")
    setSingletonImageLoaderFactory {
        ImageLoader.Builder(it)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }
    PlatformKoinApplication {
        AppTheme {
            MainAppContent()
        }
    }
}


/**
 * 主应用内容
 */
@Composable
private fun MainAppContent() {
    val authRepository: AuthRepository = koinInject()
    val authState by authRepository.authState.collectAsState()

    val dialogManager: DialogManager = koinInject()
    val currentDialog by dialogManager.currentDialog.collectAsState()

    var showRegisterScreen by remember { mutableStateOf(false) }

    // 初始化认证状态
    LaunchedEffect(Unit) {
        authRepository.checkAuthState()
    }

    // 使用 BoxWithConstraints 获取窗口尺寸，为所有子组件提供响应式配置
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val layoutConfig = getResponsiveLayoutConfig(maxWidth)
        CompositionLocalProvider(LocalResponsiveConfig provides layoutConfig) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (authState) {
                    is AuthState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is AuthState.Unauthenticated, is AuthState.Error -> {
                        // 未登录，显示登录或注册界面
                        if (showRegisterScreen) {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    showRegisterScreen = false
                                },
                                onNavigateToLogin = {
                                    showRegisterScreen = false
                                }
                            )
                        } else {
                            LoginScreen(
                                onLoginSuccess = {
                                    // 登录成功后会自动更新 authState
                                },
                                onNavigateToRegister = {
                                    showRegisterScreen = true
                                }
                            )
                        }
                    }

                    is AuthState.Authenticated -> {
                        // 已登录，显示主界面
                        AuthenticatedContent(authState as AuthState.Authenticated)
                    }
                }

                // 全局弹窗 - 放在最后确保在最顶层显示
                GlobalDialog(
                    config = currentDialog,
                    onDismiss = { dialogManager.dismiss() }
                )
            }
        }
    }
}

/**
 * 已认证内容
 */
@Composable
private fun AuthenticatedContent(authState: AuthState.Authenticated) {
    val navigationManager = rememberNavigationManager()
    val isInExamMode by navigationManager.isInExamMode
    val currentExamId by navigationManager.currentExamId

    // 登录后立即同步用户角色到 NavigationManager
    LaunchedEffect(authState.user.role) {
        navigationManager.setRoleFromString(authState.user.role)
    }

    val config = LocalResponsiveConfig.current

    if (isInExamMode && currentExamId != null) {
        // 考试模式：全屏显示，隐藏所有导航
        ExamTakingScreen(
            examId = currentExamId!!,
            navigationManager = navigationManager,
            onExitExam = {
                // 退出考试后返回考试列表
                navigationManager.navigateTo("exams")
            }
        )
    } else {
        // 正常模式：根据屏幕尺寸显示不同的导航布局
        when (config.screenSize) {
            ResponsiveUtils.ScreenSize.COMPACT, ResponsiveUtils.ScreenSize.MEDIUM -> {
                // 移动端：底部导航布局
                MobileLayout(navigationManager)
            }

            ResponsiveUtils.ScreenSize.EXPANDED -> {
                // 桌面端：侧边导航布局
                DesktopLayout(navigationManager)
            }
        }
    }
}

/**
 * 移动端布局（底部导航）
 */
@Composable
private fun MobileLayout(
    navigationManager: NavigationManager
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 主要内容区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            MainContent(
                navigationManager = navigationManager,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 底部导航栏
        BottomNavigationBar(
            navigationManager = navigationManager
        )
    }
}

/**
 * 桌面端布局（侧边导航）
 */
@Composable
private fun DesktopLayout(
    navigationManager: NavigationManager
) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // 侧边导航栏
        SideNavigationRail(
            navigationManager = navigationManager
        )

        // 主要内容区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            MainContent(
                navigationManager = navigationManager,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * 主要内容区域
 * 根据当前选中的页面显示对应内容
 * @param navigationManager 导航管理器
 * @param modifier 修饰符
 */
@Composable
private fun MainContent(
    navigationManager: NavigationManager,
    modifier: Modifier = Modifier
) {
    val route = navigationManager.currentScreen.value
    NavigationScreen(route, navigationManager)
}

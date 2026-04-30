package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamViewModel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationViewModel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import org.koin.compose.koinInject

/**
 * 仪表盘/首页界面
 * 通知和考试列表均对接真实 API
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToExams: () -> Unit = {},
    onNavigateToCourses: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val authRepository: AuthRepository = koinInject()
    val examViewModel: ExamViewModel = koinInject()
    val notificationViewModel: NotificationViewModel = koinInject()

    val authState by authRepository.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user

    val upcomingExamsState by examViewModel.upcomingExams.collectAsState()
    val notificationState by notificationViewModel.uiState.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val config = LocalResponsiveConfig.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) {
                DesktopDashboardLayout(
                    userName = user?.realName ?: "同学",
                    unreadCount = unreadCount,
                    notificationState = notificationState,
                    upcomingExamsState = upcomingExamsState,
                    onNavigateToExams = onNavigateToExams,
                    onMarkRead = { id -> notificationViewModel.markAsRead(id) },
                    onRetryNotifications = { notificationViewModel.loadNotifications() },
                    onRetryExams = { examViewModel.loadPublishedExams() },
                    config = config
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = config.screenPadding, vertical = config.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(config.verticalSpacing)
                ) {
                    item {
                        GreetingSection(
                            userName = user?.realName ?: "同学",
                            unreadCount = unreadCount,
                            config = config,
                            onNavigateToExams = onNavigateToExams,
                            onNavigateToCourses = onNavigateToCourses,
                            onNavigateToNotifications = onNavigateToNotifications
                        )
                    }

                    item {
                        DashboardNotificationSection(
                            notificationState = notificationState,
                            config = config,
                            onMarkRead = { id -> notificationViewModel.markAsRead(id) },
                            onRetry = { notificationViewModel.loadNotifications() }
                        )
                    }

                    item {
                        DashboardExamsSection(
                            upcomingExamsState = upcomingExamsState,
                            config = config,
                            onNavigateToExams = onNavigateToExams,
                            onRetry = { examViewModel.loadPublishedExams() }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(config.verticalSpacing * 2)) }
                }
            }
        }
    }
}

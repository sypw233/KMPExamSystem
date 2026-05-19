package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ovo.sypw.kmp.examsystem.data.dto.NotificationResponse
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamListUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationUiState
import ovo.sypw.kmp.examsystem.utils.ResponsiveLayoutConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@Composable
internal fun DesktopDashboardLayout(
    userName: String,
    unreadCount: Long,
    notificationState: NotificationUiState,
    upcomingExamsState: ExamListUiState,
    onNavigateToExams: () -> Unit,
    onMarkRead: (Long) -> Unit,
    onNotificationClick: (NotificationResponse) -> Unit,
    onRetryNotifications: () -> Unit,
    onRetryExams: () -> Unit,
    showExamSection: Boolean,
    showTeacherWorkbench: Boolean,
    onNavigateToCourses: () -> Unit,
    onNavigateToQuestionBanks: () -> Unit,
    config: ResponsiveLayoutConfig
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = config.screenPadding, vertical = config.contentPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = ResponsiveUtils.MaxWidths.STANDARD)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(config.verticalSpacing)
        ) {
            GreetingSection(userName = userName, unreadCount = unreadCount, config = config)

            if (showExamSection) {
                DashboardExamsSection(
                    upcomingExamsState = upcomingExamsState,
                    config = config,
                    onNavigateToExams = onNavigateToExams,
                    onRetry = onRetryExams
                )
            }

            DashboardNotificationSection(
                notificationState = notificationState,
                config = config,
                onMarkRead = onMarkRead,
                onNotificationClick = onNotificationClick,
                onRetry = onRetryNotifications
            )

            if (showTeacherWorkbench) {
                TeacherWorkbenchSection(
                    config = config,
                    onNavigateToCourses = onNavigateToCourses,
                    onNavigateToExams = onNavigateToExams,
                    onNavigateToQuestionBanks = onNavigateToQuestionBanks
                )
            }
        }
    }
}

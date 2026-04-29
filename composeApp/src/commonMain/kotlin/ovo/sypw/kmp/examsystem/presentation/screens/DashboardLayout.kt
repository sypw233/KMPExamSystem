package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamListUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationUiState
import ovo.sypw.kmp.examsystem.utils.ResponsiveLayoutConfig

@Composable
internal fun DesktopDashboardLayout(
    userName: String,
    unreadCount: Long,
    notificationState: NotificationUiState,
    upcomingExamsState: ExamListUiState,
    onNavigateToExams: () -> Unit,
    onMarkRead: (Long) -> Unit,
    onRetryNotifications: () -> Unit,
    onRetryExams: () -> Unit,
    config: ResponsiveLayoutConfig
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = config.screenPadding, vertical = config.contentPadding)
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(config.verticalSpacing)
        ) {
            GreetingSection(userName = userName, unreadCount = unreadCount, config = config)
            DashboardNotificationSection(
                notificationState = notificationState,
                config = config,
                onMarkRead = onMarkRead,
                onRetry = onRetryNotifications
            )
        }

        Spacer(modifier = Modifier.width(config.screenPadding))

        Column(
            modifier = Modifier.weight(1.2f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(config.verticalSpacing)
        ) {
            DashboardExamsSection(
                upcomingExamsState = upcomingExamsState,
                config = config,
                onNavigateToExams = onNavigateToExams,
                onRetry = onRetryExams
            )
        }
    }
}

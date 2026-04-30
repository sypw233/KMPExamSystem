package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.NotificationResponse
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationUiState
import ovo.sypw.kmp.examsystem.utils.ResponsiveLayoutConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveLazyVerticalGrid

@Composable
internal fun DashboardNotificationSection(
    notificationState: NotificationUiState,
    config: ResponsiveLayoutConfig,
    onMarkRead: (Long) -> Unit,
    onRetry: () -> Unit
) {
    Column {
        Text(
            text = "系统通知",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = config.verticalSpacing)
        )
        when (notificationState) {
            is NotificationUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
            is NotificationUiState.Error -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "通知加载失败",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = onRetry) {
                        Text("重试", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            is NotificationUiState.Success -> {
                val notifications = notificationState.notifications
                if (notifications.isEmpty()) {
                    Text(
                        "暂无通知",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    ResponsiveLazyVerticalGrid(
                        items = notifications.take(6),  // 显示最多6条通知
                        key = { it.id },
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
                        horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing)
                    ) { notification ->
                        DashboardNotificationCard(
                            notification = notification,
                            onMarkRead = { onMarkRead(notification.id) },
                            config = config
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardNotificationCard(
    notification: NotificationResponse,
    onMarkRead: () -> Unit,
    config: ResponsiveLayoutConfig
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (notification.isRead)
                MaterialTheme.colorScheme.onSurfaceVariant
            else
                MaterialTheme.colorScheme.onPrimaryContainer
        ),
        onClick = onMarkRead
    ) {
        Row(modifier = Modifier.padding(config.cardPadding), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(config.horizontalSpacing * 2))

            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (!notification.isRead) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("新") }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.content,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
                notification.createTime?.let { time ->
                    Text(
                        text = time.take(16).replace("T", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

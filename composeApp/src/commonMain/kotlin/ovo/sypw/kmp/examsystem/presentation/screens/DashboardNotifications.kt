package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ovo.sypw.kmp.examsystem.data.dto.NotificationResponse
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationUiState
import ovo.sypw.kmp.examsystem.utils.ResponsiveLayoutConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveLazyVerticalGrid

@Composable
internal fun DashboardNotificationSection(
    notificationState: NotificationUiState,
    config: ResponsiveLayoutConfig,
    onMarkRead: (Long) -> Unit,
    onNotificationClick: (NotificationResponse) -> Unit,
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
                    DashboardNotificationStack(
                        notifications = notifications.take(6),
                        onMarkRead = onMarkRead,
                        onNotificationClick = onNotificationClick,
                        config = config
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardNotificationStack(
    notifications: List<NotificationResponse>,
    onMarkRead: (Long) -> Unit,
    onNotificationClick: (NotificationResponse) -> Unit,
    config: ResponsiveLayoutConfig
) {
    var expanded by remember(notifications.map { it.id }) { mutableStateOf(false) }

    Column(modifier = Modifier.animateContentSize()) {
        AnimatedContent(
            targetState = expanded || notifications.size == 1,
            transitionSpec = {
                if (targetState) {
                    (
                        fadeIn(animationSpec = tween(180, delayMillis = 70)) +
                            expandVertically(animationSpec = tween(320), expandFrom = Alignment.Top) +
                            slideInVertically(animationSpec = tween(320)) { -it / 10 }
                    ) togetherWith (
                        fadeOut(animationSpec = tween(120)) +
                            shrinkVertically(animationSpec = tween(180), shrinkTowards = Alignment.Top) +
                            slideOutVertically(animationSpec = tween(180)) { -it / 12 }
                    )
                } else {
                    (
                        fadeIn(animationSpec = tween(160, delayMillis = 60)) +
                            expandVertically(animationSpec = tween(240), expandFrom = Alignment.Top) +
                            slideInVertically(animationSpec = tween(240)) { -it / 12 }
                    ) togetherWith (
                        fadeOut(animationSpec = tween(120)) +
                            shrinkVertically(animationSpec = tween(260), shrinkTowards = Alignment.Top) +
                            slideOutVertically(animationSpec = tween(220)) { -it / 10 }
                    )
                }
            },
            label = "DashboardNotificationStack"
        ) { showExpanded ->
            if (!showExpanded && notifications.size > 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    repeat(minOf(2, notifications.size - 1)) { index ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(168.dp)
                                .padding(
                                    top = ((index + 1) * 8).dp,
                                    start = ((index + 1) * 10).dp,
                                    end = ((index + 1) * 10).dp
                                )
                                .zIndex(index.toFloat()),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {}
                    }

                    DashboardNotificationCard(
                        notification = notifications.first(),
                        onClick = {
                            onMarkRead(notifications.first().id)
                            onNotificationClick(notifications.first())
                            expanded = true
                        },
                        config = config,
                        modifier = Modifier.zIndex(10f),
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("${notifications.size}")
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = "展开通知",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        footerContent = {
                            Text(
                                text = "共 ${notifications.size} 条通知，点击展开",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            } else {
                Column {
                if (notifications.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "共 ${notifications.size} 条通知",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { expanded = false }) {
                            Icon(
                                imageVector = Icons.Default.ExpandLess,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("收起")
                        }
                    }
                }

                ResponsiveLazyVerticalGrid(
                    items = notifications,
                    key = { it.id },
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
                    horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing)
                ) { notification ->
                    DashboardNotificationCard(
                        notification = notification,
                        onClick = {
                            onMarkRead(notification.id)
                            onNotificationClick(notification)
                        },
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
    onClick: () -> Unit,
    config: ResponsiveLayoutConfig,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = if (notification.isRead) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 0.dp else 1.dp),
        onClick = onClick
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (trailingContent != null) {
                        trailingContent()
                    } else if (!notification.isRead) {
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
                if (footerContent != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    footerContent()
                }
            }
        }
    }
}

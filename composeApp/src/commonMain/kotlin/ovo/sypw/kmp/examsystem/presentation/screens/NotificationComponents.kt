package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.NotificationResponse
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwipeToDismissNotificationItem(
    notification: NotificationResponse,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit
) {
    val config = LocalResponsiveConfig.current
    val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED

    if (isDesktop) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                NotificationListItem(
                    notification = notification,
                    onMarkRead = onMarkRead
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    } else {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.EndToStart) {
                    onDelete()
                    true
                } else {
                    false
                }
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val color by animateColorAsState(
                    targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.background
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color)
                        .padding(end = 24.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            },
            enableDismissFromStartToEnd = false
        ) {
            NotificationListItem(
                notification = notification,
                onMarkRead = onMarkRead
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SendNotificationDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val isValid = title.isNotBlank() && content.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发送系统通知") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("通知标题 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("通知内容 *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title.trim(), content.trim()) }, enabled = isValid) {
                Text("发送")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun NotificationListItem(
    notification: NotificationResponse,
    onMarkRead: () -> Unit
) {
    val config = LocalResponsiveConfig.current
    Surface(
        onClick = onMarkRead,
        color = if (notification.isRead)
            MaterialTheme.colorScheme.surface
        else
            MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(config.cardPadding),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = notificationTypeColor(notification.type),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = notificationTypeIcon(notification.type),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.isRead) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    notification.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )

                notification.createTime?.let { t ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        t.take(16).replace("T", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun notificationTypeColor(type: String) = when (type) {
    "EXAM_PUBLISHED" -> MaterialTheme.colorScheme.primary
    "EXAM_REMINDER" -> MaterialTheme.colorScheme.tertiary
    "GRADE_RELEASED" -> MaterialTheme.colorScheme.secondary
    "COURSE_UPDATE" -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.outline
}

private fun notificationTypeIcon(type: String): ImageVector = when (type) {
    "EXAM_PUBLISHED" -> Icons.Default.Assignment
    "EXAM_REMINDER" -> Icons.Default.Notifications
    "GRADE_RELEASED" -> Icons.Default.Grade
    "COURSE_UPDATE" -> Icons.Default.Announcement
    else -> Icons.Default.Notifications
}

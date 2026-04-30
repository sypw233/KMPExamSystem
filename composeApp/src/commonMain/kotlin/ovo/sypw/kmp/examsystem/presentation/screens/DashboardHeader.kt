package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.utils.ResponsiveLayoutConfig

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GreetingSection(
    userName: String,
    unreadCount: Long,
    config: ResponsiveLayoutConfig,
    onNavigateToExams: (() -> Unit)? = null,
    onNavigateToCourses: (() -> Unit)? = null,
    onNavigateToNotifications: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = config.contentPadding),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(config.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("今日概览") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                Text(
                    text = "你好，${userName}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "准备好今天的挑战了吗？",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                )

                // 快捷操作chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onNavigateToExams != null) {
                        FilterChip(
                            selected = false,
                            onClick = onNavigateToExams,
                            label = { Text("考试") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                    if (onNavigateToCourses != null) {
                        FilterChip(
                            selected = false,
                            onClick = onNavigateToCourses,
                            label = { Text("课程") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                    if (onNavigateToNotifications != null) {
                        FilterChip(
                            selected = false,
                            onClick = onNavigateToNotifications,
                            label = { Text("通知") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text(
                                if (unreadCount > 99) "99+" else unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            userName.take(1),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@Composable
internal fun GreetingSection(
    userName: String,
    unreadCount: Long,
    config: ResponsiveLayoutConfig
) {
    val isCompact = config.screenSize == ResponsiveUtils.ScreenSize.COMPACT
    val sectionPadding = if (isCompact) 16.dp else config.cardPadding
    val avatarSize = if (isCompact) 56.dp else 64.dp

    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = config.contentPadding),
        shape = if (isCompact) MaterialTheme.shapes.large else MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(sectionPadding),
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
                    text = "你好，$userName",
                    style = if (isCompact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "准备好今天的挑战了吗？",
                    style = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                )
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
                    modifier = Modifier.size(avatarSize)
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

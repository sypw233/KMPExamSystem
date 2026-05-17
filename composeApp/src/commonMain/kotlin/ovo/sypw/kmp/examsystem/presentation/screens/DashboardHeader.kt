package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import ovo.sypw.kmp.examsystem.utils.ResponsiveLayoutConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@Composable
@Suppress("UNUSED_PARAMETER")
internal fun GreetingSection(
    userName: String,
    unreadCount: Long,
    config: ResponsiveLayoutConfig
) {
    val isCompact = config.screenSize == ResponsiveUtils.ScreenSize.COMPACT
    val sectionPadding = if (isCompact) 16.dp else config.cardPadding
    val avatarSize = if (isCompact) 56.dp else 64.dp
    val subtitle = remember(userName) {
        listOf(
            "先看完通知，再安排今天的任务。",
            "课程和考试动态已经为你整理好了。",
            "从最重要的一件事开始，节奏会很顺。",
            "今天也适合把待办清清楚楚地推进。"
        )[Random.nextInt(4)]
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = config.contentPadding,
                bottom = if (isCompact) 8.dp else 12.dp
            ),
        shape = if (isCompact) MaterialTheme.shapes.large else MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(sectionPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "你好，$userName",
                    style = if (isCompact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = subtitle,
                    style = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                )
            }

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

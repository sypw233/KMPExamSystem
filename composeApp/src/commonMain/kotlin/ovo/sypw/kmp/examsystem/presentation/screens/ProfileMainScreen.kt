package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.UserInfo
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@Composable
internal fun ProfileMainScreen(
    modifier: Modifier = Modifier,
    user: UserInfo?,
    unreadCount: Long,
    isTeacherOrAdmin: Boolean,
    onNavigateToGrades: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val isStudent = user?.role?.uppercase() == "STUDENT"
    val config = LocalResponsiveConfig.current
    val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED

    if (isDesktop) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(config.screenPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .widthIn(max = ResponsiveUtils.MaxWidths.STANDARD)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(config.screenPadding)
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.85f)
                        .widthIn(max = ResponsiveUtils.MaxWidths.PROFILE_FORM)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserInfoCard(user = user, onClick = onOpenEditProfile)
                    OutlinedActionButton(
                        text = "退出登录",
                        onClick = onLogout,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1.15f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(config.verticalSpacing)
                ) {
                    Text(
                        text = "功能",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = config.verticalSpacing)
                    )
                    ProfileMenuCards(
                        isStudent = isStudent,
                        isTeacherOrAdmin = isTeacherOrAdmin,
                        unreadCount = unreadCount,
                        onNavigateToGrades = onNavigateToGrades,
                        onNavigateToNotifications = onNavigateToNotifications,
                        onOpenHelp = onOpenHelp,
                        onOpenSettings = onOpenSettings
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(config.screenPadding),
            verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserInfoCard(user = user, onClick = onOpenEditProfile)
            ProfileMenuCards(
                isStudent = isStudent,
                isTeacherOrAdmin = isTeacherOrAdmin,
                unreadCount = unreadCount,
                onNavigateToGrades = onNavigateToGrades,
                onNavigateToNotifications = onNavigateToNotifications,
                onOpenHelp = onOpenHelp,
                onOpenSettings = onOpenSettings
            )
            OutlinedActionButton(
                text = "退出登录",
                onClick = onLogout,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileMenuCards(
    isStudent: Boolean,
    isTeacherOrAdmin: Boolean,
    unreadCount: Long,
    onNavigateToGrades: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            if (isStudent) {
                MenuItem(
                    icon = Icons.Default.DateRange,
                    title = "考试历史",
                    subtitle = "查看历史成绩与提交详情",
                    onClick = onNavigateToGrades
                )
            }
            MenuItem(
                icon = Icons.Default.Notifications,
                title = "通知中心",
                subtitle = if (unreadCount > 0) "未读通知 $unreadCount 条" else "查看全部通知",
                badge = if (unreadCount > 0) unreadCount.toString() else null,
                onClick = onNavigateToNotifications
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            MenuItem(
                icon = Icons.Default.Info,
                title = if (isTeacherOrAdmin) "帮助中心" else "使用说明",
                subtitle = "查看功能说明与使用提示",
                onClick = onOpenHelp
            )
            MenuItem(
                icon = Icons.Default.SettingsApplications,
                title = "设置",
                subtitle = "主题、考试展示、字体大小与外部链接",
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, color)
    ) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = color)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = color)
    }
}

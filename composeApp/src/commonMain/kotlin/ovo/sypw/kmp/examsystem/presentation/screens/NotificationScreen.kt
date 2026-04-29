package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.NotificationResponse
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationViewModel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveLazyVerticalGrid
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import org.koin.compose.koinInject

private sealed interface NotificationGridItem {
    data class Data(val notification: NotificationResponse) : NotificationGridItem
    data object LoadMore : NotificationGridItem
    data object FooterSpacer : NotificationGridItem
}

/**
 * 通知列表独立页面
 * 支持滑动删除、标记已读、全部已读
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onBack: () -> Unit) {
    val viewModel: NotificationViewModel = koinInject()
    val authRepository: AuthRepository = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val authState by authRepository.authState.collectAsState()
    val isAdmin = (authState as? AuthState.Authenticated)?.user?.role?.uppercase() == "ADMIN"

    var showSendDialog by remember { mutableStateOf(false) }
    val config = LocalResponsiveConfig.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("通知")
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text(
                                    if (unreadCount > 99) "99+" else unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = { showSendDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "发送通知")
                        }
                    }
                    if (unreadCount > 0) {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "全部已读")
                        }
                    }
                    IconButton(onClick = { viewModel.loadNotifications() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is NotificationUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is NotificationUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadNotifications() }) { Text("重试") }
                        }
                    }
                }
                is NotificationUiState.Success -> {
                    if (state.notifications.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.outlineVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "暂无通知",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        val hasMore by viewModel.hasMore.collectAsState()
                        val listItems = buildList {
                            addAll(state.notifications.map { NotificationGridItem.Data(it) })
                            if (hasMore) add(NotificationGridItem.LoadMore)
                            add(NotificationGridItem.FooterSpacer)
                        }

                        ResponsiveLazyVerticalGrid(
                            items = listItems,
                            key = {
                                when (it) {
                                    is NotificationGridItem.Data -> it.notification.id
                                    NotificationGridItem.LoadMore -> "load_more"
                                    NotificationGridItem.FooterSpacer -> "footer_spacer"
                                }
                            },
                            modifier = Modifier
                                .then(if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = ResponsiveUtils.MaxWidths.NARROW) else Modifier)
                                .fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = config.screenPadding,
                                vertical = config.verticalSpacing
                            ),
                            verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
                            horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing)
                        ) { item ->
                            when (item) {
                                is NotificationGridItem.Data -> {
                                    SwipeToDismissNotificationItem(
                                        notification = item.notification,
                                        onMarkRead = { viewModel.markAsRead(item.notification.id) },
                                        onDelete = { viewModel.deleteNotification(item.notification.id) }
                                    )
                                }
                                NotificationGridItem.LoadMore -> {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(config.cardPadding),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        TextButton(onClick = { viewModel.loadMore() }) {
                                            Text("加载更多")
                                        }
                                    }
                                }
                                NotificationGridItem.FooterSpacer -> {
                                    Spacer(modifier = Modifier.height(config.verticalSpacing))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSendDialog) {
        SendNotificationDialog(
            onConfirm = { title, content ->
                viewModel.sendNotification(title, content)
                showSendDialog = false
            },
            onDismiss = { showSendDialog = false }
        )
    }
}

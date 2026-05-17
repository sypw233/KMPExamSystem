package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.NotificationResponse
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.presentation.components.common.EmptyState
import ovo.sypw.kmp.examsystem.presentation.components.common.ErrorContent
import ovo.sypw.kmp.examsystem.presentation.components.common.LoadingContent
import ovo.sypw.kmp.examsystem.presentation.components.common.ActionEffect
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationViewModel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveScrollableGrid
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

private sealed interface NotificationGridItem {
    data class Data(val notification: NotificationResponse) : NotificationGridItem
    data object LoadMore : NotificationGridItem
    data object FooterSpacer : NotificationGridItem
}

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
    var showActionMenu by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val config = LocalResponsiveConfig.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("通知")
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text(
                                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showActionMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                        }
                        DropdownMenu(
                            expanded = showActionMenu,
                            onDismissRequest = { showActionMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("刷新通知") },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                                onClick = {
                                    showActionMenu = false
                                    viewModel.loadNotifications()
                                }
                            )
                            if (unreadCount > 0) {
                                DropdownMenuItem(
                                    text = { Text("全部已读") },
                                    leadingIcon = { Icon(Icons.Default.DoneAll, contentDescription = null) },
                                    onClick = {
                                        showActionMenu = false
                                        viewModel.markAllAsRead()
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    onClick = { showSendDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("发送通知") }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        ActionEffect(
            actionState = viewModel.actionState.collectAsState(),
            snackbarHostState = snackbarHostState,
            isSuccess = { it is NotificationActionState.Success },
            isError = { it is NotificationActionState.Error },
            getMessage = {
                when (it) {
                    is NotificationActionState.Success -> it.message
                    is NotificationActionState.Error -> it.message
                    else -> ""
                }
            },
            onConsumed = { viewModel.resetActionState() }
        )
        LaunchedEffect(uiState !is NotificationUiState.Loading) {
            isRefreshing = false
        }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.loadNotifications()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is NotificationUiState.Loading -> {
                    LoadingContent(message = "正在加载通知...")
                }

                is NotificationUiState.Error -> {
                    ErrorContent(message = state.message, onRetry = { viewModel.loadNotifications() })
                }

                is NotificationUiState.Success -> {
                    if (state.notifications.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Notifications,
                            title = "暂无通知",
                            subtitle = "现在还没有新的通知。"
                        )
                    } else {
                        val hasMore by viewModel.hasMore.collectAsState()
                        val listItems = buildList {
                            addAll(state.notifications.map { NotificationGridItem.Data(it) })
                            if (hasMore) add(NotificationGridItem.LoadMore)
                            add(NotificationGridItem.FooterSpacer)
                        }

                        ResponsiveScrollableGrid(
                            items = listItems,
                            key = {
                                when (it) {
                                    is NotificationGridItem.Data -> it.notification.id
                                    NotificationGridItem.LoadMore -> "load_more"
                                    NotificationGridItem.FooterSpacer -> "footer_spacer"
                                }
                            },
                            modifier = Modifier
                                .then(
                                    if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) {
                                        Modifier.widthIn(max = ResponsiveUtils.MaxWidths.NARROW)
                                    } else {
                                        Modifier
                                    }
                                )
                                .fillMaxWidth(),
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
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(config.cardPadding),
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

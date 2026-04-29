package ovo.sypw.kmp.examsystem.presentation.screens.admin

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import ovo.sypw.kmp.examsystem.data.dto.UserResponse
import ovo.sypw.kmp.examsystem.presentation.viewmodel.UserActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.UserListState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.UserManageViewModel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveLazyVerticalGrid
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

/**
 * 管理员用户管理界面
 * 支持：分页列表、角色筛选、关键字搜索、启用/禁用、新建、编辑、删除、重置密码
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManageScreen() {
    val viewModel: UserManageViewModel = koinInject()
    val listState by viewModel.listState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val queryParams by viewModel.queryParams.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val config = LocalResponsiveConfig.current

    var showCreateDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showEditDialog by remember { mutableStateOf<UserResponse?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<UserResponse?>(null) }
    var showResetPwdDialog by remember { mutableStateOf<UserResponse?>(null) }

    var isBatchMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is UserActionState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            is UserActionState.Error -> {
                snackbarHostState.showSnackbar("错误: ${state.message}")
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            if (isBatchMode) {
                TopAppBar(
                    title = { Text("已选择 ${selectedIds.size} 位用户") },
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = { isBatchMode = false; selectedIds = emptySet() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "退出批量")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                val allIds = (listState as? UserListState.Success)?.page?.content?.map { it.id }?.toSet() ?: emptySet()
                                selectedIds = if (selectedIds == allIds) emptySet() else allIds
                            }
                        ) { Text("全选") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            } else {
                TopAppBar(
                    title = { Text("用户管理") },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    actions = {
                        TextButton(onClick = { isBatchMode = true }) { Text("批量") }
                        IconButton(onClick = { viewModel.loadUsers(queryParams) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isBatchMode) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("新建用户") }
                )
            }
        },
        bottomBar = {
            if (isBatchMode && selectedIds.isNotEmpty()) {
                Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("已选 ${selectedIds.size} 位用户", style = MaterialTheme.typography.titleMedium)
                        Button(
                            onClick = { showBatchDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("批量删除")
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FilterBar(
                params = queryParams,
                onParamsChange = { viewModel.loadUsers(it) }
            )

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                when (val state = listState) {
                    is UserListState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is UserListState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "加载失败: ${state.message}",
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.loadUsers(queryParams) }) {
                                    Text("重试")
                                }
                            }
                        }
                    }
                    is UserListState.Success -> {
                        val page = state.page
                        Column(modifier = Modifier.then(if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = ResponsiveUtils.MaxWidths.FULL) else Modifier).fillMaxSize()) {
                            Text(
                                "共 ${page.totalElements} 位用户，第 ${page.number + 1}/${page.totalPages} 页",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            ResponsiveLazyVerticalGrid(
                                items = page.content,
                                key = { it.id },
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
                                horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) { user ->
                                val isSelected = user.id in selectedIds
                                UserCard(
                                    user = user,
                                    isBatchMode = isBatchMode,
                                    isSelected = isSelected,
                                    onToggleSelect = {
                                        selectedIds = if (isSelected) selectedIds - user.id else selectedIds + user.id
                                    },
                                    onEdit = { showEditDialog = user },
                                    onDelete = { showDeleteConfirm = user },
                                    onResetPassword = { showResetPwdDialog = user },
                                    onToggleStatus = {
                                        if (user.status == 1) viewModel.disableUser(user.id)
                                        else viewModel.enableUser(user.id)
                                    }
                                )
                            }
                            if (page.totalPages > 1) {
                                PaginationBar(
                                    currentPage = page.number,
                                    totalPages = page.totalPages,
                                    hasFirst = !page.first,
                                    hasLast = !page.last,
                                    onPageChange = { viewModel.loadPage(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    UserManageDialogHost(
        viewModel = viewModel,
        showCreateDialog = showCreateDialog,
        showEditDialog = showEditDialog,
        showDeleteConfirm = showDeleteConfirm,
        showResetPwdDialog = showResetPwdDialog,
        showBatchDeleteConfirm = showBatchDeleteConfirm,
        selectedIds = selectedIds,
        onDismissCreate = { showCreateDialog = false },
        onDismissEdit = { showEditDialog = null },
        onDismissDelete = { showDeleteConfirm = null },
        onDismissResetPassword = { showResetPwdDialog = null },
        onDismissBatchDelete = { showBatchDeleteConfirm = false },
        onBatchDeleteFinished = {
            showBatchDeleteConfirm = false
            isBatchMode = false
            selectedIds = emptySet()
        }
    )
}

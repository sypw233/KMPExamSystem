package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.UserResponse
import ovo.sypw.kmp.examsystem.presentation.components.management.ManagementPageHeader
import ovo.sypw.kmp.examsystem.presentation.components.management.ManagementPanel
import ovo.sypw.kmp.examsystem.presentation.components.management.ManagementToolbar
import ovo.sypw.kmp.examsystem.presentation.viewmodel.UserActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.UserListState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.UserManageViewModel
import ovo.sypw.kmp.examsystem.utils.DesktopDataTableRow
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveLazyVerticalGrid
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

/**
 * 管理员用户管理界面
 * 支持：分页列表、角色筛选、关键字搜索、启用/禁用、新建、编辑、删除、重置密码
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserManageScreen() {
    val viewModel: UserManageViewModel = koinInject()
    val listState by viewModel.listState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val queryParams by viewModel.queryParams.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val config = LocalResponsiveConfig.current
    val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED

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
            if (isDesktop) {
                Unit
            } else if (isBatchMode) {
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
            if (!isDesktop && !isBatchMode) {
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
                .then(if (isDesktop) Modifier.padding(config.screenPadding) else Modifier),
            verticalArrangement = if (isDesktop) Arrangement.spacedBy(16.dp) else Arrangement.Top
        ) {
            if (isDesktop) {
                ManagementPageHeader(
                    title = if (isBatchMode) "用户批量管理" else "用户管理",
                    subtitle = "维护用户账号、角色、状态和密码重置，支持筛选、分页和批量操作。"
                ) {
                    if (isBatchMode) {
                        TextButton(onClick = { isBatchMode = false; selectedIds = emptySet() }) {
                            Text("退出批量")
                        }
                        TextButton(
                            onClick = {
                                val allIds = (listState as? UserListState.Success)?.page?.content?.map { it.id }?.toSet() ?: emptySet()
                                selectedIds = if (selectedIds == allIds) emptySet() else allIds
                            }
                        ) { Text("全选") }
                    } else {
                        TextButton(onClick = { isBatchMode = true }) { Text("批量") }
                        IconButton(onClick = { viewModel.loadUsers(queryParams) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
                        Button(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("新建用户")
                        }
                    }
                }
            }

            ManagementPanel(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (isDesktop) {
                        // Task 1.2: ManagementToolbar on desktop
                        ManagementToolbar {
                            var keyword by remember(queryParams.keyword) { mutableStateOf(queryParams.keyword ?: "") }
                            OutlinedTextField(
                                value = keyword,
                                onValueChange = { keyword = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("搜索用户名/姓名/邮箱") },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                trailingIcon = {
                                    if (keyword.isNotEmpty()) {
                                        IconButton(onClick = {
                                            keyword = ""
                                            viewModel.loadUsers(queryParams.copy(keyword = null, page = 0))
                                        }) { Icon(Icons.Default.Close, null) }
                                    }
                                },
                                singleLine = true
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(null to "全部", "student" to "学生", "teacher" to "教师", "admin" to "管理员").forEach { (role, label) ->
                                    FilterChip(
                                        selected = queryParams.role == role,
                                        onClick = { viewModel.loadUsers(queryParams.copy(role = role, page = 0)) },
                                        label = { Text(label) }
                                    )
                                }
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(null to "全部状态", 1 to "启用", 0 to "禁用").forEach { (status, label) ->
                                    FilterChip(
                                        selected = queryParams.status == status,
                                        onClick = { viewModel.loadUsers(queryParams.copy(status = status, page = 0)) },
                                        label = { Text(label) }
                                    )
                                }
                            }
                            Button(
                                onClick = { viewModel.loadUsers(queryParams.copy(keyword = keyword.takeIf { it.isNotBlank() }, page = 0)) }
                            ) { Text("搜索") }
                        }
                    } else {
                        FilterBar(
                            params = queryParams,
                            onParamsChange = { viewModel.loadUsers(it) }
                        )
                    }

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
                            if (isDesktop) {
                                // Task 1.3: Desktop table layout
                                // Table header
                                val headerColumns: List<Pair<Float, @Composable () -> Unit>> = buildList {
                                    if (isBatchMode) {
                                        add(0.3f to @Composable {
                                            Checkbox(
                                                checked = selectedIds == (page.content.map { it.id }.toSet()),
                                                onCheckedChange = {
                                                    val allIds = page.content.map { it.id }.toSet()
                                                    selectedIds = if (selectedIds == allIds) emptySet() else allIds
                                                }
                                            )
                                        })
                                    }
                                    add(0.8f to @Composable { Text("用户名", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) })
                                    add(0.8f to @Composable { Text("姓名", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) })
                                    add(0.6f to @Composable { Text("角色", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) })
                                    add(1.2f to @Composable { Text("邮箱", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) })
                                    add(0.5f to @Composable { Text("状态", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) })
                                    add(0.8f to @Composable { Text("操作", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) })
                                }
                                DesktopDataTableRow(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    columns = headerColumns.toTypedArray()
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    items(page.content, key = { it.id }) { user ->
                                        val isSelected = user.id in selectedIds
                                        val rowColumns: List<Pair<Float, @Composable () -> Unit>> = buildList {
                                            if (isBatchMode) {
                                                add(0.3f to @Composable {
                                                    Checkbox(
                                                        checked = isSelected,
                                                        onCheckedChange = {
                                                            selectedIds = if (isSelected) selectedIds - user.id else selectedIds + user.id
                                                        }
                                                    )
                                                })
                                            }
                                            add(0.8f to @Composable {
                                                Text(
                                                    user.username,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            })
                                            add(0.8f to @Composable {
                                                Text(
                                                    user.realName ?: "-",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            })
                                            add(0.6f to @Composable { RoleBadge(role = user.role) })
                                            add(1.2f to @Composable {
                                                Text(
                                                    user.email ?: "-",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            })
                                            add(0.5f to @Composable { StatusBadge(enabled = user.status == 1) })
                                            add(0.8f to @Composable {
                                                Row {
                                                    IconButton(onClick = {
                                                        if (user.status == 1) viewModel.disableUser(user.id)
                                                        else viewModel.enableUser(user.id)
                                                    }) {
                                                        Icon(
                                                            if (user.status == 1) Icons.Default.Block else Icons.Default.CheckCircle,
                                                            contentDescription = if (user.status == 1) "禁用" else "启用",
                                                            tint = if (user.status == 1) MaterialTheme.colorScheme.error
                                                                   else MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                    IconButton(onClick = { showResetPwdDialog = user }) {
                                                        Icon(Icons.Default.LockReset, contentDescription = "重置密码")
                                                    }
                                                    IconButton(onClick = { showEditDialog = user }) {
                                                        Icon(Icons.Default.Person, contentDescription = "编辑")
                                                    }
                                                    IconButton(onClick = { showDeleteConfirm = user }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "删除",
                                                             tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            })
                                        }
                                        DesktopDataTableRow(
                                            modifier = Modifier.then(
                                                if (isSelected) Modifier.background(
                                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                                    MaterialTheme.shapes.small
                                                ) else Modifier
                                            ).padding(horizontal = 12.dp),
                                            columns = rowColumns.toTypedArray()
                                        )
                                    }
                                }
                            } else {
                                // Mobile: card grid layout
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

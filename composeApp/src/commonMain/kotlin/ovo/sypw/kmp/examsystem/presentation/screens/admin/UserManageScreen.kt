package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.UserCreateRequest
import ovo.sypw.kmp.examsystem.data.dto.UserQueryParams
import ovo.sypw.kmp.examsystem.data.dto.UserResponse
import ovo.sypw.kmp.examsystem.data.dto.UserUpdateRequest
import ovo.sypw.kmp.examsystem.presentation.viewmodel.UserActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.UserListState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.UserManageViewModel

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

    // 弹窗状态
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<UserResponse?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<UserResponse?>(null) }
    var showResetPwdDialog by remember { mutableStateOf<UserResponse?>(null) }

    // 批量删除模式
    var isBatchMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    // 监听 actionState 变化显示 Snackbar
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
            // 搜索和筛选栏
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
                        Column(modifier = Modifier.then(if (LocalResponsiveConfig.current.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = 960.dp) else Modifier).fillMaxSize()) {
                            // 统计行
                            Text(
                                "共 ${page.totalElements} 位用户，第 ${page.number + 1}/${page.totalPages} 页",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(page.content, key = { it.id }) { user ->
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
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                            // 分页控制
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

    // 新建用户弹窗
    if (showCreateDialog) {
        CreateUserDialog(
            onConfirm = { req ->
                viewModel.createUser(req)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // 编辑用户弹窗
    showEditDialog?.let { user ->
        EditUserDialog(
            user = user,
            onConfirm = { req ->
                viewModel.updateUser(user.id, req)
                showEditDialog = null
            },
            onDismiss = { showEditDialog = null }
        )
    }

    // 删除确认
    showDeleteConfirm?.let { user ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("删除用户") },
            text = { Text("确定要删除用户「${user.realName ?: user.username}」吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUser(user.id)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("取消") }
            }
        )
    }

    // 重置密码弹窗
    showResetPwdDialog?.let { user ->
        ResetPasswordDialog(
            username = user.realName ?: user.username,
            onConfirm = { pwd ->
                viewModel.resetPassword(user.id, pwd)
                showResetPwdDialog = null
            },
            onDismiss = { showResetPwdDialog = null }
        )
    }

    // 批量删除确认
    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = { Text("批量删除用户") },
            text = { Text("确定要删除选中的 ${selectedIds.size} 位用户吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.batchDeleteUsers(selectedIds.toList())
                        showBatchDeleteConfirm = false
                        isBatchMode = false
                        selectedIds = emptySet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showBatchDeleteConfirm = false }) { Text("取消") } }
        )
    }
}

// ── 筛选栏 ────────────────────────────────────────────────────────────────────

@Composable
private fun FilterBar(params: UserQueryParams, onParamsChange: (UserQueryParams) -> Unit) {
    var keyword by remember(params.keyword) { mutableStateOf(params.keyword ?: "") }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        OutlinedTextField(
            value = keyword,
            onValueChange = { keyword = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索用户名/姓名/邮箱") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (keyword.isNotEmpty()) {
                    IconButton(onClick = {
                        keyword = ""
                        onParamsChange(params.copy(keyword = null, page = 0))
                    }) { Icon(Icons.Default.Close, null) }
                }
            },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(null to "全部", "student" to "学生", "teacher" to "教师", "admin" to "管理员").forEach { (role, label) ->
                FilterChip(
                    selected = params.role == role,
                    onClick = { onParamsChange(params.copy(role = role, page = 0)) },
                    label = { Text(label) }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { onParamsChange(params.copy(keyword = keyword.takeIf { it.isNotBlank() }, page = 0)) }
            ) { Text("搜索") }
        }
    }
}

// ── 用户卡片 ─────────────────────────────────────────────────────────────────

@Composable
private fun UserCard(
    user: UserResponse,
    isBatchMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onResetPassword: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val isEnabled = user.status == 1
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                isEnabled -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isBatchMode) {
                Checkbox(checked = isSelected, onCheckedChange = { onToggleSelect() })
                Spacer(modifier = Modifier.width(8.dp))
            }
            // 用户信息
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user.realName ?: user.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RoleBadge(role = user.role)
                    if (!isEnabled) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                            Text("禁用", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
                Text(
                    "@${user.username}  ${user.email ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 操作按钮
            if (!isBatchMode) {
                Row {
                    IconButton(onClick = onToggleStatus) {
                        Icon(
                            if (isEnabled) Icons.Default.Block else Icons.Default.CheckCircle,
                            contentDescription = if (isEnabled) "禁用" else "启用",
                            tint = if (isEnabled) MaterialTheme.colorScheme.error
                                   else MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onResetPassword) {
                        Icon(Icons.Default.LockReset, contentDescription = "重置密码")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Person, contentDescription = "编辑")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "删除",
                             tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(role: String) {
    val (label, color) = when (role.lowercase()) {
        "admin"   -> "管理员" to MaterialTheme.colorScheme.tertiary
        "teacher" -> "教师" to MaterialTheme.colorScheme.secondary
        else      -> "学生" to MaterialTheme.colorScheme.primary
    }
    Badge(containerColor = color.copy(alpha = 0.15f)) {
        Text(label, color = color, style = MaterialTheme.typography.labelSmall)
    }
}

// ── 分页控制 ─────────────────────────────────────────────────────────────────

@Composable
private fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    hasFirst: Boolean,
    hasLast: Boolean,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { onPageChange(currentPage - 1) }, enabled = hasFirst) {
            Text("上一页")
        }
        Text(
            "${currentPage + 1} / $totalPages",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        TextButton(onClick = { onPageChange(currentPage + 1) }, enabled = hasLast) {
            Text("下一页")
        }
    }
}

// ── 新建用户弹窗 ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateUserDialog(
    onConfirm: (UserCreateRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var realName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("student") }
    var roleExpanded by remember { mutableStateOf(false) }

    val isValid = username.length >= 3 && password.length >= 6 && role.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建用户") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码 * (≥6位)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = realName,
                    onValueChange = { realName = it },
                    label = { Text("真实姓名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = roleDisplayName(role),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("角色 *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        listOf("student" to "学生", "teacher" to "教师", "admin" to "管理员").forEach { (v, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { role = v; roleExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(UserCreateRequest(
                        username = username.trim(),
                        password = password,
                        realName = realName.takeIf { it.isNotBlank() },
                        email = email.takeIf { it.isNotBlank() },
                        role = role
                    ))
                },
                enabled = isValid
            ) { Text("创建") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

// ── 编辑用户弹窗 ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditUserDialog(
    user: UserResponse,
    onConfirm: (UserUpdateRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var realName by remember { mutableStateOf(user.realName ?: "") }
    var email by remember { mutableStateOf(user.email ?: "") }
    var role by remember { mutableStateOf(user.role) }
    var roleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑用户：${user.username}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = realName,
                    onValueChange = { realName = it },
                    label = { Text("真实姓名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = roleDisplayName(role),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("角色") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        listOf("student" to "学生", "teacher" to "教师", "admin" to "管理员").forEach { (v, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { role = v; roleExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(UserUpdateRequest(
                    realName = realName.takeIf { it.isNotBlank() },
                    email = email.takeIf { it.isNotBlank() },
                    role = role
                ))
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

// ── 重置密码弹窗 ─────────────────────────────────────────────────────────────

@Composable
private fun ResetPasswordDialog(
    username: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val isValid = newPassword.length >= 6 && newPassword == confirmPassword

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重置密码：$username") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("新密码 (≥6位)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("确认密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                            Text("两次密码不一致", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(newPassword) }, enabled = isValid) { Text("重置") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

private fun roleDisplayName(role: String) = when (role.lowercase()) {
    "admin"   -> "管理员"
    "teacher" -> "教师"
    else      -> "学生"
}

package ovo.sypw.kmp.examsystem.presentation.screens.teacher

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import ovo.sypw.kmp.examsystem.data.dto.QuestionRequest
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.presentation.navigation.UserRole
import ovo.sypw.kmp.examsystem.presentation.viewmodel.QuestionActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.QuestionUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.QuestionViewModel

/**
 * 题目管理界面（课程管理的二级页面）
 * 管理员显示全部题目，教师显示我的题目
 * 支持：类型筛选、创建、编辑、删除(滑动/按钮)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionManageScreen(
    onBack: () -> Unit,
    userRole: UserRole = UserRole.TEACHER,
    courseId: Long? = null,       // 来源课程(用于显示标题)
    courseName: String? = null
) {
    val viewModel: QuestionViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<QuestionResponse?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<QuestionResponse?>(null) }

    val questionTypes = listOf(
        null to "全部",
        "single" to "单选",
        "multiple" to "多选",
        "true_false" to "判断",
        "fill_blank" to "填空",
        "short_answer" to "简答"
    )

    LaunchedEffect(userRole) {
        viewModel.setRole(userRole)
    }

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is QuestionActionState.Success -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetActionState()
            }
            is QuestionActionState.Error -> {
                snackbarHostState.showSnackbar("错误: ${s.message}")
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("题目管理")
                        if (!courseName.isNullOrBlank()) {
                            Text(
                                courseName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, "刷新")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("新建题目") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(modifier = Modifier.fillMaxSize().widthIn(max = 900.dp)) {
                // 类型筛选
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(questionTypes) { (typeKey, typeLabel) ->
                        FilterChip(
                            selected = selectedType == typeKey,
                            onClick = { viewModel.filterByType(typeKey) },
                            label = { Text(typeLabel) }
                        )
                    }
                }

                when (val state = uiState) {
                    is QuestionUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is QuestionUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { viewModel.load() }) { Text("重试") }
                            }
                        }
                    }
                    is QuestionUiState.Success -> {
                        if (state.questions.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("暂无题目，点击右下角新建", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(state.questions, key = { it.id }) { question ->
                                    SwipeToDismissQuestionItem(
                                        question = question,
                                        onDelete = { showDeleteConfirm = question },
                                        onEdit = { showEditDialog = question }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }

    // 新建对话框
    if (showCreateDialog) {
        QuestionFormDialog(
            title = "新建题目",
            onConfirm = { req ->
                viewModel.createQuestion(req)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // 编辑对话框
    showEditDialog?.let { question ->
        QuestionFormDialog(
            title = "编辑题目",
            initial = question,
            onConfirm = { req ->
                viewModel.updateQuestion(question.id, req)
                showEditDialog = null
            },
            onDismiss = { showEditDialog = null }
        )
    }

    // 删除确认
    showDeleteConfirm?.let { question ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("删除题目") },
            text = { Text("确定要删除该题目吗？") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteQuestion(question.id); showDeleteConfirm = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("取消") } }
        )
    }
}

// ─── 滑动删除题目卡片 ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissQuestionItem(
    question: QuestionResponse,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.background
            )
            Box(
                modifier = Modifier.fillMaxSize().background(color).padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        QuestionCard(question = question, onEdit = onEdit)
    }
}

// ─── 题目卡片 ─────────────────────────────────────────────────────────────────

@Composable
private fun QuestionCard(question: QuestionResponse, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                        Text(
                            questionTypeLabel(question.type),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (!question.difficulty.isNullOrBlank()) {
                        Surface(color = difficultyColor(question.difficulty), shape = MaterialTheme.shapes.small) {
                            Text(
                                difficultyLabel(question.difficulty),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${question.score} 分", style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "编辑", modifier = Modifier.size(16.dp),
                             tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                question.content,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (!question.category.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("# ${question.category}", style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

// ─── 题目表单对话框 ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionFormDialog(
    title: String,
    initial: QuestionResponse? = null,
    onConfirm: (QuestionRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var content by remember { mutableStateOf(initial?.content ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "single") }
    var answer by remember { mutableStateOf(initial?.answer ?: "") }
    var options by remember { mutableStateOf(initial?.options ?: "") }
    var analysis by remember { mutableStateOf(initial?.analysis ?: "") }
    var difficulty by remember { mutableStateOf(initial?.difficulty ?: "medium") }
    var category by remember { mutableStateOf(initial?.category ?: "") }
    var score by remember { mutableStateOf(initial?.score?.toString() ?: "5") }
    var typeExpanded by remember { mutableStateOf(false) }
    var diffExpanded by remember { mutableStateOf(false) }

    val typeOptions = listOf("single" to "单选", "multiple" to "多选", "true_false" to "判断",
                             "fill_blank" to "填空", "short_answer" to "简答")
    val diffOptions = listOf("easy" to "简单", "medium" to "中等", "hard" to "困难")

    val isValid = content.isNotBlank() && answer.isNotBlank() && score.toIntOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("题目内容 *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it },
                                               modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = typeOptions.find { it.first == type }?.second ?: type,
                                onValueChange = {}, readOnly = true,
                                label = { Text("题目类型") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                                typeOptions.forEach { (v, label) ->
                                    DropdownMenuItem(text = { Text(label) }, onClick = { type = v; typeExpanded = false })
                                }
                            }
                        }
                        ExposedDropdownMenuBox(expanded = diffExpanded, onExpandedChange = { diffExpanded = it },
                                               modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = diffOptions.find { it.first == difficulty }?.second ?: difficulty,
                                onValueChange = {}, readOnly = true,
                                label = { Text("难度") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(diffExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = diffExpanded, onDismissRequest = { diffExpanded = false }) {
                                diffOptions.forEach { (v, label) ->
                                    DropdownMenuItem(text = { Text(label) }, onClick = { difficulty = v; diffExpanded = false })
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = options,
                        onValueChange = { options = it },
                        label = { Text("选项(JSON格式，单多选题)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("""["A. 选项一","B. 选项二"]""") }
                    )
                }
                item {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        label = { Text("答案 *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = score,
                            onValueChange = { score = it },
                            label = { Text("分值 *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("分类") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = analysis,
                        onValueChange = { analysis = it },
                        label = { Text("解析") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(QuestionRequest(
                        content = content.trim(),
                        type = type,
                        options = options.takeIf { it.isNotBlank() },
                        answer = answer.trim(),
                        analysis = analysis.takeIf { it.isNotBlank() },
                        difficulty = difficulty,
                        category = category.takeIf { it.isNotBlank() },
                        score = score.toIntOrNull() ?: 5
                    ))
                },
                enabled = isValid
            ) { Text(if (initial == null) "创建" else "保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

// ─── 帮助函数 ─────────────────────────────────────────────────────────────────

internal fun questionTypeLabel(type: String) = when (type) {
    "single" -> "单选题"
    "multiple" -> "多选题"
    "true_false" -> "判断题"
    "fill_blank" -> "填空题"
    "short_answer" -> "简答题"
    else -> type
}

internal fun difficultyLabel(difficulty: String) = when (difficulty.lowercase()) {
    "easy" -> "简单"
    "medium" -> "中等"
    "hard" -> "困难"
    else -> difficulty
}

@Composable
internal fun difficultyColor(difficulty: String) = when (difficulty.lowercase()) {
    "easy" -> MaterialTheme.colorScheme.tertiaryContainer
    "medium" -> MaterialTheme.colorScheme.secondaryContainer
    "hard" -> MaterialTheme.colorScheme.errorContainer
    else -> MaterialTheme.colorScheme.surfaceVariant
}

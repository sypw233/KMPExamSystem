package ovo.sypw.kmp.examsystem.presentation.screens.teacher

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.presentation.navigation.UserRole
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamListUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamViewModel

/**
 * 考试管理界面（教师/管理员共用）
 * Tab 0: 未开始(草稿 status=0)
 * Tab 1: 进行中(已发布 status=1)
 * Tab 2: 已结束(status=2)
 * 支持：新建、编辑、删除(仅草稿)、发布(仅草稿)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherExamManageScreen(
    onBack: () -> Unit,
    userRole: UserRole = UserRole.TEACHER,
    availableCourses: List<CourseResponse> = emptyList()   // 可用课程列表（用于新建考试选课）
) {
    val viewModel: ExamViewModel = koinInject()
    val allExamsState by viewModel.allExams.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("未开始", "进行中", "已结束")

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<ExamResponse?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<ExamResponse?>(null) }
    var composeExam by remember { mutableStateOf<ExamResponse?>(null) }
    var viewSubmissionsExam by remember { mutableStateOf<ExamResponse?>(null) }

    // 设置角色并加载数据
    LaunchedEffect(userRole) {
        viewModel.setRole(userRole)
    }

    // actionState 响应
    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is ExamActionState.Success -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetActionState()
            }
            is ExamActionState.Error -> {
                snackbarHostState.showSnackbar("错误: ${s.message}")
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    if (composeExam != null) {
        ExamComposeScreen(
            examId = composeExam!!.id,
            courseId = composeExam!!.courseId,
            onBack = { composeExam = null }
        )
        return
    }

    if (viewSubmissionsExam != null) {
        ExamSubmissionsScreen(
            examId = viewSubmissionsExam!!.id,
            onBack = { viewSubmissionsExam = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (userRole == UserRole.ADMIN) "考试管理（全部）" else "考试管理") },
                actions = {
                    IconButton(onClick = { viewModel.loadManagerExams() }) {
                        Icon(Icons.Default.Refresh, "刷新")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("新建考试") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                when (val state = allExamsState) {
                    is ExamListUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ExamListUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { viewModel.loadManagerExams() }) { Text("重试") }
                            }
                        }
                    }
                    is ExamListUiState.Success -> {
                        // 按 Tab 过滤
                        val filteredExams = state.exams.filter { exam ->
                            when (selectedTab) {
                                0 -> exam.status == 0             // 草稿（未开始）
                                1 -> exam.status == 1             // 已发布（进行中）
                                2 -> exam.status == 2             // 已结束
                                else -> true
                            }
                        }

                        if (filteredExams.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    when (selectedTab) {
                                        0 -> "暂无草稿考试，点击右下角新建"
                                        1 -> "暂无进行中的考试"
                                        else -> "暂无已结束的考试"
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.widthIn(max = 900.dp).fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredExams, key = { it.id }) { exam ->
                                    ManageExamCard(
                                        exam = exam,
                                        canEdit = exam.status == 0,
                                        onEdit = { showEditDialog = exam },
                                        onDelete = { showDeleteConfirm = exam },
                                        onPublish = { viewModel.publishExam(exam.id) },
                                        onCompose = { composeExam = exam },
                                        onViewSubmissions = { viewSubmissionsExam = exam }
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
        ExamFormDialog(
            title = "新建考试",
            courses = availableCourses,
            onConfirm = { req ->
                viewModel.createExam(req)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // 编辑对话框
    showEditDialog?.let { exam ->
        ExamFormDialog(
            title = "编辑考试",
            initial = exam,
            courses = availableCourses,
            onConfirm = { req ->
                viewModel.updateExam(exam.id, req)
                showEditDialog = null
            },
            onDismiss = { showEditDialog = null }
        )
    }

    // 删除确认
    showDeleteConfirm?.let { exam ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("删除考试") },
            text = { Text("确定要删除考试「${exam.title}」吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteExam(exam.id); showDeleteConfirm = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("取消") } }
        )
    }
}

// ─── 考试卡片 ─────────────────────────────────────────────────────────────────

@Composable
private fun ManageExamCard(
    exam: ExamResponse,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPublish: () -> Unit,
    onCompose: (() -> Unit)? = null,
    onViewSubmissions: (() -> Unit)? = null
) {
    val statusColor = when (exam.status) {
        0 -> MaterialTheme.colorScheme.outlineVariant
        1 -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val statusLabel = when (exam.status) {
        0 -> "草稿"
        1 -> "进行中"
        2 -> "已结束"
        else -> "未知"
    }
    val statusIcon = when (exam.status) {
        0 -> Icons.Default.HourglassBottom
        1 -> Icons.Default.PlayArrow
        else -> Icons.Default.Stop
    }

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
                // 课程标签
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        exam.courseName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                // 状态标签
                Surface(color = statusColor, shape = MaterialTheme.shapes.small) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(statusIcon, null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(statusLabel, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(exam.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (!exam.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(exam.description, style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("${exam.questionCount} 题", style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.outline)
                Text("满分 ${exam.totalScore}", style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.outline)
                Text("${exam.duration} 分钟", style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.outline)
            }

            // 操作区（仅草稿可编辑/删除/发布/组卷）
            if (canEdit) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("编辑")
                    }
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("删除")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    androidx.compose.material3.OutlinedButton(onClick = { onCompose?.invoke() }) {
                        Text("组卷")
                    }
                    Button(onClick = onPublish) { Text("发布") }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { onViewSubmissions?.invoke() }) {
                        Text("批阅与记录")
                    }
                }
            }
        }
    }
}

// ─── 考试表单对话框 ────────────────────────────────────────────────────────────

@Composable
private fun ExamFormDialog(
    title: String,
    initial: ExamResponse? = null,
    courses: List<CourseResponse> = emptyList(),
    onConfirm: (ExamRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var examTitle by remember { mutableStateOf(initial?.title ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var duration by remember { mutableStateOf(initial?.duration?.toString() ?: "60") }
    var totalScore by remember { mutableStateOf(initial?.totalScore?.toString() ?: "100") }
    var startTime by remember { mutableStateOf(initial?.startTime ?: "") }
    var endTime by remember { mutableStateOf(initial?.endTime ?: "") }
    val courseId = initial?.courseId ?: courses.firstOrNull()?.id ?: 0L

    val isValid = examTitle.isNotBlank() && duration.toIntOrNull() != null
            && totalScore.toIntOrNull() != null && startTime.isNotBlank() && endTime.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = examTitle,
                    onValueChange = { examTitle = it },
                    label = { Text("考试名称 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("时长(分钟) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalScore,
                        onValueChange = { totalScore = it },
                        label = { Text("总分 *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("开始时间 (ISO格式) *") },
                    singleLine = true,
                    placeholder = { Text("2024-01-01T09:00:00") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("结束时间 (ISO格式) *") },
                    singleLine = true,
                    placeholder = { Text("2024-01-01T11:00:00") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(ExamRequest(
                        title = examTitle.trim(),
                        description = description.takeIf { it.isNotBlank() },
                        courseId = courseId,
                        startTime = startTime.trim(),
                        endTime = endTime.trim(),
                        duration = duration.toInt(),
                        totalScore = totalScore.toInt()
                    ))
                },
                enabled = isValid
            ) { Text(if (initial == null) "创建" else "保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

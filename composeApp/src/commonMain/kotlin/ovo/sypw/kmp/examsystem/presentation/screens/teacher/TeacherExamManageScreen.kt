package ovo.sypw.kmp.examsystem.presentation.screens.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import org.koin.compose.koinInject
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.utils.StringUtils.format
import ovo.sypw.kmp.examsystem.data.dto.ExamRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.presentation.navigation.UserRole
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseViewModel
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
    availableCourses: List<CourseResponse> = emptyList()
) {
    val viewModel: ExamViewModel = koinInject()
    val courseViewModel: CourseViewModel = koinInject()
    val allExamsState by viewModel.allExams.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val courseUiState by courseViewModel.allCoursesState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val courses = availableCourses.takeIf { it.isNotEmpty() }
        ?: (courseUiState as? CourseUiState.Success)?.courses ?: emptyList()

    LaunchedEffect(Unit) {
        if (availableCourses.isEmpty()) courseViewModel.loadAllCourses()
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("未开始", "进行中", "已结束")

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<ExamResponse?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<ExamResponse?>(null) }
    var composeExam by remember { mutableStateOf<ExamResponse?>(null) }
    var randomComposeExam by remember { mutableStateOf<ExamResponse?>(null) }
    var viewSubmissionsExam by remember { mutableStateOf<ExamResponse?>(null) }

    // 批量删除模式状态
    var isBatchMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(userRole) {
        viewModel.setRole(userRole)
    }

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

    composeExam?.let { exam ->
        ExamComposeScreen(
            examId = exam.id,
            courseId = exam.courseId,
            onBack = { composeExam = null }
        )
        return
    }

    randomComposeExam?.let { exam ->
        ExamComposeScreen(
            examId = exam.id,
            courseId = exam.courseId,
            autoOpenRandomCompose = true,
            onBack = { randomComposeExam = null }
        )
        return
    }

    viewSubmissionsExam?.let { exam ->
        ExamSubmissionsScreen(
            examId = exam.id,
            onBack = { viewSubmissionsExam = null }
        )
        return
    }

    Scaffold(
        topBar = {
            if (isBatchMode) {
                TopAppBar(
                    title = { Text("已选择 ${selectedIds.size} 项") },
                    navigationIcon = {
                        IconButton(onClick = { isBatchMode = false; selectedIds = emptySet() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "退出批量")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                val currentIds = (allExamsState as? ExamListUiState.Success)?.exams
                                    ?.filter { it.status == 0 }
                                    ?.map { it.id }
                                    ?.toSet() ?: emptySet()
                                selectedIds = if (selectedIds == currentIds) emptySet() else currentIds
                            }
                        ) { Text("全选") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            } else {
                TopAppBar(
                    title = { Text(if (userRole == UserRole.ADMIN) "考试管理（全部）" else "考试管理") },
                    actions = {
                        if (selectedTab == 0) {
                            TextButton(onClick = { isBatchMode = true }) { Text("批量") }
                        }
                        IconButton(onClick = { viewModel.loadManagerExams() }) {
                            Icon(Icons.Default.Refresh, "刷新")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 && !isBatchMode) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("新建考试") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isBatchMode && selectedIds.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("已选 ${selectedIds.size} 项", style = MaterialTheme.typography.titleMedium)
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
                        val filteredExams = state.exams.filter { exam ->
                            when (selectedTab) {
                                0 -> exam.status == 0
                                1 -> exam.status == 1
                                2 -> exam.status == 2
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
                                modifier = Modifier.then(if (LocalResponsiveConfig.current.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = 900.dp) else Modifier).fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredExams, key = { it.id }) { exam ->
                                    val isSelected = exam.id in selectedIds
                                    ManageExamCard(
                                        exam = exam,
                                        canEdit = exam.status == 0 && !isBatchMode,
                                        isBatchMode = isBatchMode && exam.status == 0,
                                        isSelected = isSelected,
                                        onToggleSelect = {
                                            selectedIds = if (isSelected) selectedIds - exam.id else selectedIds + exam.id
                                        },
                                        onEdit = { showEditDialog = exam },
                                        onDelete = { showDeleteConfirm = exam },
                                        onPublish = { viewModel.publishExam(exam.id) },
                                        onCompose = { composeExam = exam },
                                        onRandomCompose = { randomComposeExam = exam },
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
            courses = courses,
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
            courses = courses,
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

    // 批量删除确认
    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = { Text("批量删除考试") },
            text = { Text("确定要删除选中的 ${selectedIds.size} 项草稿考试吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.batchDeleteExams(selectedIds.toList())
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

// ─── 考试卡片 ─────────────────────────────────────────────────────────────────

@Composable
private fun ManageExamCard(
    exam: ExamResponse,
    canEdit: Boolean,
    isBatchMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPublish: () -> Unit,
    onCompose: (() -> Unit)? = null,
    onRandomCompose: (() -> Unit)? = null,
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
        modifier = Modifier.fillMaxWidth().clickable(
            enabled = isBatchMode,
            onClick = onToggleSelect
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isBatchMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isSelected, onCheckedChange = { onToggleSelect() })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(exam.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Text("${exam.duration ?: "-"} 分钟", style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.outline)
            }

            } // close else

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
                    TextButton(onClick = { onRandomCompose?.invoke() }) {
                        Text("智能组卷", style = MaterialTheme.typography.labelSmall)
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var selectedCourseId by remember { mutableStateOf(initial?.courseId ?: courses.firstOrNull()?.id ?: 0L) }
    var courseExpanded by remember { mutableStateOf(false) }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val isValid = examTitle.isNotBlank() && duration.toIntOrNull() != null
            && totalScore.toIntOrNull() != null && startTime.isNotBlank() && endTime.isNotBlank()
            && selectedCourseId > 0

    Column {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // 课程选择器
                    ExposedDropdownMenuBox(expanded = courseExpanded, onExpandedChange = { courseExpanded = it }) {
                        OutlinedTextField(
                            value = courses.find { it.id == selectedCourseId }?.courseName ?: "请选择课程",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("所属课程 *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(courseExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = courseExpanded, onDismissRequest = { courseExpanded = false }) {
                            courses.forEach { course ->
                                DropdownMenuItem(
                                    text = { Text(course.courseName) },
                                    onClick = { selectedCourseId = course.id; courseExpanded = false }
                                )
                            }
                        }
                    }
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
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = totalScore,
                            onValueChange = { totalScore = it },
                            label = { Text("总分 *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    // 开始时间
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("开始时间", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = if (startTime.isBlank()) "未选择" else formatDateTimeForDisplay(startTime),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (startTime.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                                        else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showStartTimePicker = true }) {
                            Icon(Icons.Default.CalendarToday, "选择开始时间")
                        }
                    }
                    // 结束时间
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("结束时间", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = if (endTime.isBlank()) "未选择" else formatDateTimeForDisplay(endTime),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (endTime.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                                        else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showEndTimePicker = true }) {
                            Icon(Icons.Default.CalendarToday, "选择结束时间")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm(ExamRequest(
                            title = examTitle.trim(),
                            description = description.takeIf { it.isNotBlank() },
                            courseId = selectedCourseId,
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

        if (showStartTimePicker) {
            DateTimePickerDialog(
                initialDateTime = startTime,
                onConfirm = { startTime = it; showStartTimePicker = false },
                onDismiss = { showStartTimePicker = false }
            )
        }
        if (showEndTimePicker) {
            DateTimePickerDialog(
                initialDateTime = endTime,
                onConfirm = { endTime = it; showEndTimePicker = false },
                onDismiss = { showEndTimePicker = false }
            )
        }
    }
}

// ─── 日期时间格式化工具 ─────────────────────────────────────────────────────────

private fun formatDateTimeForDisplay(isoDateTime: String): String {
    if (isoDateTime.isBlank()) return ""
    val normalized = isoDateTime.replace('T', ' ')
    val parts = normalized.split(" ")
    if (parts.size < 2) return isoDateTime
    val datePart = parts[0]
    val timePart = parts[1].take(5)
    return "$datePart $timePart"
}

private fun parseDateTimeComponents(isoDateTime: String): List<Int> {
    return try {
        val normalized = isoDateTime.replace('T', ' ')
        val parts = normalized.split(" ")
        val dateParts = parts[0].split("-")
        val timeParts = parts[1].split(":")
        listOf(
            dateParts[0].toInt(),
            dateParts[1].toInt(),
            dateParts[2].toInt(),
            timeParts[0].toInt(),
            timeParts[1].toInt()
        )
    } catch (_: Exception) {
        listOf(2024, 1, 1, 9, 0)
    }
}

private fun formatDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): String {
    return "%04d-%02d-%02dT%02d:%02d:00".format(year, month, day, hour, minute)
}

// ─── 日期时间选择器对话框（Material3 官方组件）──────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)
@Composable
private fun DateTimePickerDialog(
    initialDateTime: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val components = remember(initialDateTime) { parseDateTimeComponents(initialDateTime) }

    val initialDateMillis = LocalDate(
        components[0], components[1], components[2]
    ).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )
    val timePickerState = rememberTimePickerState(
        initialHour = components[3],
        initialMinute = components[4],
        is24Hour = true
    )

    // false = 显示 TimeInput（文字输入），true = 显示 TimePicker（表盘）
    var showClockFace by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期时间") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DatePicker(state = datePickerState)

                Spacer(modifier = Modifier.height(12.dp))

                // 时间区域：TimeInput + 切换按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showClockFace) {
                        TimePicker(state = timePickerState)
                    } else {
                        TimeInput(state = timePickerState)
                    }
                }

                // 切换按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showClockFace = !showClockFace }) {
                        Icon(
                            if (showClockFace) Icons.Default.Edit else Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showClockFace) "手动输入" else "表盘选择")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        val result = formatDateTime(
                            selectedDate.year,
                            selectedDate.monthNumber,
                            selectedDate.dayOfMonth,
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        onConfirm(result)
                    }
                },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

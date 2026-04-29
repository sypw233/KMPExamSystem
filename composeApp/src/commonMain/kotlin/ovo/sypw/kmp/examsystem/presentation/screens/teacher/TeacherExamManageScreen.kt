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
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.presentation.navigation.UserRole
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseViewModel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamListUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamViewModel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

/**
 * 考试管理界面（教师/管理员共用）
 * Tab 0: 未开始(草稿 status=0)
 * Tab 1: 进行中(已发布 status=1)
 * Tab 2: 已结束(status=2)
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
    val config = LocalResponsiveConfig.current

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
        val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = if (isDesktop) {
                        Modifier.widthIn(max = ResponsiveUtils.MaxWidths.STANDARD).fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
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
                                modifier = Modifier.then(if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = ResponsiveUtils.MaxWidths.STANDARD) else Modifier).fillMaxSize(),
                                contentPadding = PaddingValues(config.screenPadding),
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

    TeacherExamManageDialogHost(
        viewModel = viewModel,
        courses = courses,
        showCreateDialog = showCreateDialog,
        showEditDialog = showEditDialog,
        showDeleteConfirm = showDeleteConfirm,
        showBatchDeleteConfirm = showBatchDeleteConfirm,
        selectedIds = selectedIds,
        onDismissCreate = { showCreateDialog = false },
        onDismissEdit = { showEditDialog = null },
        onDismissDelete = { showDeleteConfirm = null },
        onDismissBatchDelete = { showBatchDeleteConfirm = false },
        onBatchDeleteFinished = {
            showBatchDeleteConfirm = false
            isBatchMode = false
            selectedIds = emptySet()
        }
    )
}

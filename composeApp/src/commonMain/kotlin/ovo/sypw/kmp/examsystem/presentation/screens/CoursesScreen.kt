package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.data.repository.CourseRepository
import ovo.sypw.kmp.examsystem.data.repository.ExamRepository
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.presentation.navigation.UserRole
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseViewModel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.EnrollState
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveLazyVerticalGrid
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(role: UserRole? = null) {
    val courseViewModel: CourseViewModel = koinInject()
    val authRepository: AuthRepository = koinInject()

    val authState by authRepository.authState.collectAsState()
    val effectiveRole = role ?: UserRole.from((authState as? AuthState.Authenticated)?.user?.role)
    val isManager = effectiveRole == UserRole.ADMIN || effectiveRole == UserRole.TEACHER

    if (isManager) {
        CourseManageScreen(courseViewModel = courseViewModel, userRole = effectiveRole)
    } else {
        StudentCourseScreen(courseViewModel = courseViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseManageScreen(courseViewModel: CourseViewModel, userRole: UserRole) {
    val allCoursesState by courseViewModel.allCoursesState.collectAsState()
    val myCoursesState by courseViewModel.myCoursesState.collectAsState()
    val actionState by courseViewModel.actionState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val config = LocalResponsiveConfig.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<CourseResponse?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<CourseResponse?>(null) }
    var showEnrollmentDialog by remember { mutableStateOf<CourseResponse?>(null) }

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is CourseActionState.Success -> {
                snackbar.showSnackbar(s.message)
                courseViewModel.resetActionState()
            }
            is CourseActionState.Error -> {
                snackbar.showSnackbar(s.message)
                courseViewModel.resetActionState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (userRole == UserRole.ADMIN) "全部课程" else "我的授课") },
                actions = {
                    IconButton(onClick = {
                        if (userRole == UserRole.ADMIN) courseViewModel.loadAllCourses() else courseViewModel.loadMyCourses()
                    }) { Icon(Icons.Default.Refresh, contentDescription = "刷新") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("新建课程") }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val state = if (userRole == UserRole.ADMIN) allCoursesState else myCoursesState
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            when (val s = state) {
                is CourseUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                is CourseUiState.Error -> {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(s.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = {
                            if (userRole == UserRole.ADMIN) courseViewModel.loadAllCourses() else courseViewModel.loadMyCourses()
                        }) { Text("重试") }
                    }
                }
                is CourseUiState.Success -> {
                    if (s.courses.isEmpty()) {
                        Text("暂无课程", modifier = Modifier.padding(top = 32.dp))
                    } else {
                        ResponsiveLazyVerticalGrid(
                            items = s.courses,
                            key = { it.id },
                            modifier = Modifier.fillMaxSize().then(if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = 900.dp) else Modifier),
                            contentPadding = PaddingValues(
                                start = config.screenPadding,
                                end = config.screenPadding,
                                top = config.screenPadding,
                                bottom = config.screenPadding + 80.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
                            horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing)
                        ) { course ->
                            ManageCourseCard(
                                course = course,
                                onEdit = { showEditDialog = course },
                                onDelete = { showDeleteConfirm = course },
                                onManageEnrollments = { showEnrollmentDialog = course }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CourseFormDialog(
            title = "创建课程",
            isAdmin = userRole == UserRole.ADMIN,
            onConfirm = {
                courseViewModel.createCourse(it)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    showEditDialog?.let { course ->
        CourseFormDialog(
            title = "编辑课程",
            initial = course,
            isAdmin = userRole == UserRole.ADMIN,
            onConfirm = {
                courseViewModel.updateCourse(course.id, it)
                showEditDialog = null
            },
            onDismiss = { showEditDialog = null }
        )
    }

    showDeleteConfirm?.let { course ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("删除课程") },
            text = { Text("确定删除课程「${course.courseName}」吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        courseViewModel.deleteCourse(course.id)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("取消") } }
        )
    }

    showEnrollmentDialog?.let { course ->
        EnrollmentManageDialog(course = course, courseViewModel = courseViewModel, onDismiss = { showEnrollmentDialog = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentCourseScreen(courseViewModel: CourseViewModel) {
    val authRepository: AuthRepository = koinInject()
    val courseRepository: CourseRepository = koinInject()
    val examRepository: ExamRepository = koinInject()
    val scope = rememberCoroutineScope()

    val allCoursesState by courseViewModel.allCoursesState.collectAsState()
    val myCoursesState by courseViewModel.myCoursesState.collectAsState()
    val enrollState by courseViewModel.enrollState.collectAsState()
    val authState by authRepository.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }
    var withdrawingCourse by remember { mutableStateOf<CourseResponse?>(null) }
    var examDetailCourse by remember { mutableStateOf<CourseResponse?>(null) }
    var courseExams by remember { mutableStateOf<List<ExamResponse>>(emptyList()) }
    var loadingCourseExams by remember { mutableStateOf(false) }
    val config = LocalResponsiveConfig.current

    LaunchedEffect(enrollState) {
        when (val state = enrollState) {
            is EnrollState.Success -> {
                snackbarHostState.showSnackbar("选课成功: ${state.enrollment.courseName}")
                courseViewModel.resetEnrollState()
            }
            is EnrollState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                courseViewModel.resetEnrollState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("课程中心") },
                actions = {
                    IconButton(onClick = {
                        if (selectedTab == 0) courseViewModel.loadAllCourses() else courseViewModel.loadMyCourses()
                    }) { Icon(Icons.Default.Refresh, contentDescription = "刷新") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                listOf("全部", "已选").forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
            AnimatedContent(targetState = selectedTab) { tab ->
                val state = if (tab == 0) allCoursesState else myCoursesState
                StudentCourseList(
                    state = state,
                    showEnrollButton = tab == 0,
                    enrollingState = enrollState,
                    onEnroll = { courseViewModel.enrollCourse(it) },
                    onRetry = { if (tab == 0) courseViewModel.loadAllCourses() else courseViewModel.loadMyCourses() },
                    onWithdraw = { course -> withdrawingCourse = course },
                    onViewCourseExams = { course ->
                        examDetailCourse = course
                        loadingCourseExams = true
                        scope.launch {
                            courseExams = examRepository.getExamsByCourse(course.id).getOrDefault(emptyList())
                            loadingCourseExams = false
                        }
                    }
                )
            }
        }
    }

    withdrawingCourse?.let { course ->
        AlertDialog(
            onDismissRequest = { withdrawingCourse = null },
            title = { Text("退课确认") },
            text = { Text("确定退选课程「${course.courseName}」吗？") },
            confirmButton = {
                Button(onClick = {
                    val currentUserId = (authState as? AuthState.Authenticated)?.user?.id
                    if (currentUserId != null) {
                        scope.launch {
                            courseRepository.removeStudentFromCourse(course.id, currentUserId)
                                .onSuccess {
                                    snackbarHostState.showSnackbar("已退课: ${course.courseName}")
                                    courseViewModel.loadMyCourses()
                                    courseViewModel.loadAllCourses()
                                }
                                .onFailure { snackbarHostState.showSnackbar("退课失败: ${it.message}") }
                        }
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("未登录") }
                    }
                    withdrawingCourse = null
                }) { Text("确认") }
            },
            dismissButton = { TextButton(onClick = { withdrawingCourse = null }) { Text("取消") } }
        )
    }

    examDetailCourse?.let { course ->
        AlertDialog(
            onDismissRequest = { examDetailCourse = null },
            title = { Text("课程考试 - ${course.courseName}") },
            text = {
                if (loadingCourseExams) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (courseExams.isEmpty()) {
                    Text("该课程暂无考试")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(courseExams, key = { it.id }) { exam ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                    Text(exam.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text("满分 ${exam.totalScore} · ${exam.duration ?: "-"} 分钟", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { examDetailCourse = null }) { Text("关闭") } },
            dismissButton = {}
        )
    }
}

@Composable
private fun StudentCourseList(
    state: CourseUiState,
    showEnrollButton: Boolean,
    enrollingState: EnrollState,
    onEnroll: (Long) -> Unit,
    onRetry: () -> Unit,
    onWithdraw: (CourseResponse) -> Unit,
    onViewCourseExams: (CourseResponse) -> Unit
) {
    val config = LocalResponsiveConfig.current
    when (state) {
        is CourseUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is CourseUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onRetry) { Text("重试") }
                }
            }
        }
        is CourseUiState.Success -> {
            if (state.courses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无课程")
                }
            } else {
                ResponsiveLazyVerticalGrid(
                    items = state.courses,
                    key = { it.id },
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(config.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
                    horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing)
                ) { course ->
                    StudentCourseCard(
                        course = course,
                        showEnrollButton = showEnrollButton,
                        isEnrolling = enrollingState is EnrollState.Loading,
                        onEnroll = { onEnroll(course.id) },
                        onWithdraw = { onWithdraw(course) },
                        onViewCourseExams = { onViewCourseExams(course) }
                    )
                }
            }
        }
    }
}

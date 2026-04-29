package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseViewModel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.EnrollState
import ovo.sypw.kmp.examsystem.utils.Logger
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StudentCourseScreen(courseViewModel: CourseViewModel) {
    val authRepository: AuthRepository = koinInject()
    val courseRepository: CourseRepository = koinInject()
    val scope = rememberCoroutineScope()

    val allCoursesState by courseViewModel.allCoursesState.collectAsState()
    val myCoursesState by courseViewModel.myCoursesState.collectAsState()
    val enrollState by courseViewModel.enrollState.collectAsState()
    val authState by authRepository.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    var withdrawingCourse by remember { mutableStateOf<CourseResponse?>(null) }
    var examDetailCourse by remember { mutableStateOf<CourseResponse?>(null) }
    var courseExams by remember { mutableStateOf<List<ExamResponse>>(emptyList()) }
    var loadingCourseExams by remember { mutableStateOf(false) }

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
        LaunchedEffect(allCoursesState, myCoursesState) {
            isRefreshing = false
        }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                if (selectedTab == 0) courseViewModel.loadAllCourses() else courseViewModel.loadMyCourses()
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                            courseExams = courseRepository.getCourseExams(course.id).getOrElse {
                                Logger.w("StudentCourseScreen", "加载课程考试失败: ${it.message}")
                                emptyList()
                            }
                            loadingCourseExams = false
                        }
                    }
                )
            }
        }
        }
    }

    withdrawingCourse?.let { course ->
        WithdrawCourseDialog(
            course = course,
            currentUserId = (authState as? AuthState.Authenticated)?.user?.id,
            snackbarHostState = snackbarHostState,
            onConfirm = { userId ->
                scope.launch {
                    courseRepository.removeStudentFromCourse(course.id, userId)
                        .onSuccess {
                            snackbarHostState.showSnackbar("已退课: ${course.courseName}")
                            courseViewModel.loadMyCourses()
                            courseViewModel.loadAllCourses()
                        }
                        .onFailure { snackbarHostState.showSnackbar("退课失败: ${it.message}") }
                }
                withdrawingCourse = null
            },
            onDismiss = { withdrawingCourse = null }
        )
    }

    examDetailCourse?.let { course ->
        CourseExamsDialog(
            course = course,
            exams = courseExams,
            isLoading = loadingCourseExams,
            onDismiss = { examDetailCourse = null }
        )
    }
}

@Composable
private fun WithdrawCourseDialog(
    course: CourseResponse,
    currentUserId: Long?,
    snackbarHostState: SnackbarHostState,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("退课确认") },
        text = { Text("确定退选课程「${course.courseName}」吗？") },
        confirmButton = {
            TextButton(onClick = {
                if (currentUserId != null) {
                    onConfirm(currentUserId)
                } else {
                    scope.launch { snackbarHostState.showSnackbar("未登录") }
                    onDismiss()
                }
            }) { Text("确认") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun CourseExamsDialog(
    course: CourseResponse,
    exams: List<ExamResponse>,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("课程考试 - ${course.courseName}") },
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (exams.isEmpty()) {
                Text("该课程暂无考试")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(exams, key = { it.id }) { exam ->
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
        dismissButton = {}
    )
}

package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseViewModel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.EnrollState

import ovo.sypw.kmp.examsystem.presentation.navigation.UserRole

/**
 * 课程列表界面
 * 学生可浏览选课；教师/管理员可管理课程（增删改）
 * @param role 当前用户角色（可选，未传入时从 AuthRepository 读取）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(role: UserRole? = null) {
    val courseViewModel: CourseViewModel = koinInject()
    val authRepository: AuthRepository = koinInject()

    val authState by authRepository.authState.collectAsState()
    val effectiveRole = role ?: UserRole.from(
        (authState as? AuthState.Authenticated)?.user?.role
    )
    val isStudent = effectiveRole == UserRole.STUDENT

    val allCoursesState by courseViewModel.allCoursesState.collectAsState()
    val myCoursesState by courseViewModel.myCoursesState.collectAsState()
    val enrollState by courseViewModel.enrollState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("全部课程", "我的课程")
    val snackbarHostState = remember { SnackbarHostState() }

    // 监听选课结果
    LaunchedEffect(enrollState) {
        when (val state = enrollState) {
            is EnrollState.Success -> {
                snackbarHostState.showSnackbar("选课成功：${state.enrollment.courseName}")
                courseViewModel.resetEnrollState()
            }
            is EnrollState.Error -> {
                snackbarHostState.showSnackbar("选课失败：${state.message}")
                courseViewModel.resetEnrollState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("课程") },
                actions = {
                    IconButton(onClick = {
                        if (selectedTab == 0) courseViewModel.loadAllCourses()
                        else courseViewModel.loadMyCourses()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(modifier = Modifier.fillMaxSize().widthIn(max = 800.dp)) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                AnimatedContent(targetState = selectedTab) { tab ->
                    when (tab) {
                        0 -> CourseList(
                            state = allCoursesState,
                            showEnrollButton = isStudent,
                            enrollingState = enrollState,
                            onEnroll = { courseViewModel.enrollCourse(it) },
                            onRetry = { courseViewModel.loadAllCourses() }
                        )
                        else -> CourseList(
                            state = myCoursesState,
                            showEnrollButton = false,
                            enrollingState = enrollState,
                            onEnroll = {},
                            onRetry = { courseViewModel.loadMyCourses() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseList(
    state: CourseUiState,
    showEnrollButton: Boolean,
    enrollingState: EnrollState,
    onEnroll: (Long) -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        is CourseUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is CourseUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) { Text("重试") }
                }
            }
        }
        is CourseUiState.Success -> {
            if (state.courses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "暂无课程",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.courses, key = { it.id }) { course ->
                        CourseCard(
                            course = course,
                            showEnrollButton = showEnrollButton,
                            isEnrolling = enrollingState is EnrollState.Loading,
                            onEnroll = { onEnroll(course.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseCard(
    course: CourseResponse,
    showEnrollButton: Boolean,
    isEnrolling: Boolean,
    onEnroll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 课程图标
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = course.teacherName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!course.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 选课人数
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${course.enrollmentCount} 人已选",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (showEnrollButton) {
                    FilledTonalButton(
                        onClick = onEnroll,
                        enabled = !isEnrolling,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(if (isEnrolling) "处理中..." else "选课", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

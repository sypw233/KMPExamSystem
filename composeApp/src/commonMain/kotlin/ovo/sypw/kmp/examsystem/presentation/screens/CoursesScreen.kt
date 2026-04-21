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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.CourseRequest
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.data.repository.CourseRepository
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.presentation.navigation.UserRole
import ovo.sypw.kmp.examsystem.presentation.screens.teacher.QuestionManageScreen
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseViewModel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.EnrollState

/**
 * 课程界面（角色分发）
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
    val isManager = effectiveRole == UserRole.ADMIN || effectiveRole == UserRole.TEACHER

    if (isManager) {
        CourseManageScreen(courseViewModel = courseViewModel, userRole = effectiveRole)
    } else {
        StudentCourseScreen(courseViewModel = courseViewModel)
    }
}

// ─── 管理员/教师：课程管理界面 ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseManageScreen(courseViewModel: CourseViewModel, userRole: UserRole) {
    val allCoursesState by courseViewModel.allCoursesState.collectAsState()
    val myCoursesState by courseViewModel.myCoursesState.collectAsState()
    val actionState by courseViewModel.actionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 导航到题目管理
    var questionNavCourse by rememberSaveable { mutableStateOf<CourseResponse?>(null) }

    // 对话框
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<CourseResponse?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<CourseResponse?>(null) }
    var showEnrollmentDialog by remember { mutableStateOf<CourseResponse?>(null) }

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is CourseActionState.Success -> {
                snackbarHostState.showSnackbar(s.message)
                courseViewModel.resetActionState()
            }
            is CourseActionState.Error -> {
                snackbarHostState.showSnackbar("错误: ${s.message}")
                courseViewModel.resetActionState()
            }
            else -> Unit
        }
    }

    if (questionNavCourse != null) {
        QuestionManageScreen(
            onBack = { questionNavCourse = null },
            userRole = userRole,
            courseId = questionNavCourse!!.id,
            courseName = questionNavCourse!!.courseName
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (userRole == UserRole.ADMIN) "全部课程管理" else "我的授课管理") },
                actions = {
                    IconButton(onClick = {
                        if (userRole == UserRole.ADMIN) courseViewModel.loadAllCourses()
                        else courseViewModel.loadMyCourses()
                    }) { Icon(Icons.Default.Refresh, "刷新") }
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val state = if (userRole == UserRole.ADMIN) allCoursesState else myCoursesState
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            when (val s = state) {
                is CourseUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CourseUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(s.message, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = {
                                if (userRole == UserRole.ADMIN) courseViewModel.loadAllCourses()
                                else courseViewModel.loadMyCourses()
                            }) { Text("重试") }
                        }
                    }
                }
                is CourseUiState.Success -> {
                    if (s.courses.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("暂无课程，点击右下角新建", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.widthIn(max = 900.dp).fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(s.courses, key = { it.id }) { course ->
                                ManageCourseCard(
                                    course = course,
                                    onEdit = { showEditDialog = course },
                                    onDelete = { showDeleteConfirm = course },
                                    onManageQuestions = { questionNavCourse = course },
                                    onManageEnrollments = { showEnrollmentDialog = course }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CourseFormDialog(
            title = "新建课程",
            onConfirm = { req -> courseViewModel.createCourse(req); showCreateDialog = false },
            onDismiss = { showCreateDialog = false }
        )
    }

    showEditDialog?.let { course ->
        CourseFormDialog(
            title = "编辑课程",
            initial = course,
            onConfirm = { req -> courseViewModel.updateCourse(course.id, req); showEditDialog = null },
            onDismiss = { showEditDialog = null }
        )
    }

    showDeleteConfirm?.let { course ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("删除课程") },
            text = { Text("确定要删除课程「${course.courseName}」吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = { courseViewModel.deleteCourse(course.id); showDeleteConfirm = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("取消") } }
        )
    }

    showEnrollmentDialog?.let { course ->
        EnrollmentManageDialog(
            course = course,
            courseViewModel = courseViewModel,
            onDismiss = { showEnrollmentDialog = null }
        )
    }
}

// ─── 选课管理对话框 ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnrollmentManageDialog(
    course: CourseResponse,
    courseViewModel: CourseViewModel,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val courseRepository: CourseRepository = koinInject()
    val students by courseViewModel.courseStudents.collectAsState()
    
    var showAddStudent by remember { mutableStateOf(false) }

    LaunchedEffect(course.id) {
        courseViewModel.loadCourseStudents(course.id)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.People, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("选课管理: ${course.courseName}")
        } },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (students.isEmpty()) {
                    Text("该课程目前没有被任何人选修", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        modifier = Modifier.height(250.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(students, key = { it.id }) { enroll ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(enroll.teacherName.takeIf { it.isNotBlank() } ?: "学生", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("加入时间: ${enroll.enrollTime ?: "未知"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(
                                        onClick = {
                                            // TODO: Backend does not return studentId in enrollment response directly, assume id is the right reference or delete by enrollment id?
                                            // For now we map that our enrollment api accepts studentId or enrollmentId.
                                            coroutineScope.launch {
                                                courseRepository.removeStudentFromCourse(course.id, enroll.id).onSuccess {
                                                    courseViewModel.loadCourseStudents(course.id)
                                                }
                                            }
                                        }
                                    ) { Icon(Icons.Default.PersonRemove, "移除学生", tint = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }

                if (showAddStudent) {
                    Spacer(modifier = Modifier.height(16.dp))
                    var studentIdInput by remember { mutableStateOf("") }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = studentIdInput,
                            onValueChange = { studentIdInput = it },
                            label = { Text("请输入学生ID") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            studentIdInput.toLongOrNull()?.let { sid ->
                                coroutineScope.launch {
                                    courseRepository.addStudentToCourse(course.id, sid).onSuccess {
                                        courseViewModel.loadCourseStudents(course.id)
                                        showAddStudent = false
                                    }
                                }
                            }
                        }, enabled = studentIdInput.isNotBlank()) {
                            Text("添加")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { showAddStudent = !showAddStudent }) {
                Icon(if (showAddStudent) Icons.Default.Add else Icons.Default.PersonAdd, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showAddStudent) "取消添加" else "分配学生")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

// ─── 课程管理卡片 ─────────────────────────────────────────────────────────────

@Composable
private fun ManageCourseCard(
    course: CourseResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onManageQuestions: () -> Unit,
    onManageEnrollments: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LibraryBooks, null, tint = MaterialTheme.colorScheme.onPrimaryContainer,
                             modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        course.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(13.dp),
                                 tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(course.teacherName, style = MaterialTheme.typography.bodySmall,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, null, modifier = Modifier.size(13.dp),
                                 tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${course.enrollmentCount} 人", style = MaterialTheme.typography.bodySmall,
                                 color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
                // 操作按钮：题目管理放在右上方
                FilledTonalButton(onClick = onManageQuestions) {
                    Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("题目", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (!course.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(course.description, style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3,
                     overflow = TextOverflow.Ellipsis)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onManageEnrollments) {
                    Icon(Icons.Default.People, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("修改选课(${course.enrollmentCount})")
                }
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("编辑信息")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除")
                }
            }
        }
    }
}

// ─── 课程表单对话框 ───────────────────────────────────────────────────────────

@Composable
private fun CourseFormDialog(
    title: String,
    initial: CourseResponse? = null,
    onConfirm: (CourseRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.courseName ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var teacherIdStr by remember { mutableStateOf(initial?.teacherId?.toString() ?: "") }
    
    val isValid = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("课程名称 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("课程描述") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = teacherIdStr,
                    onValueChange = { teacherIdStr = it },
                    label = { Text("指定教师 ID (选填)") },
                    singleLine = true,
                    placeholder = { Text("填入数字 ID修改教师") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(CourseRequest(
                        courseName = name.trim(),
                        description = description.takeIf { it.isNotBlank() },
                        teacherId = teacherIdStr.trim().toLongOrNull()
                    ))
                },
                enabled = isValid
            ) { Text(if (initial == null) "创建" else "保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

// ─── 学生：浏览选课界面 ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentCourseScreen(courseViewModel: CourseViewModel) {
    val allCoursesState by courseViewModel.allCoursesState.collectAsState()
    val myCoursesState by courseViewModel.myCoursesState.collectAsState()
    val enrollState by courseViewModel.enrollState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(enrollState) {
        when (val state = enrollState) {
            is EnrollState.Success -> {
                snackbarHostState.showSnackbar("选课成功：${state.enrollment.courseName}")
                courseViewModel.resetEnrollState()
            }
            is EnrollState.Error -> {
                snackbarHostState.showSnackbar("选课/退课失败：${state.message}")
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
                    }) { Icon(Icons.Default.Refresh, "刷新") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                listOf("全部课程", "已选课程").forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index },
                        text = { Text(title) })
                }
            }
            AnimatedContent(targetState = selectedTab) { tab ->
                val state = if (tab == 0) allCoursesState else myCoursesState
                StudentCourseList(
                    state = state,
                    showEnrollButton = tab == 0,
                    enrollingState = enrollState,
                    onEnroll = { courseViewModel.enrollCourse(it) },
                    onRetry = { if (tab == 0) courseViewModel.loadAllCourses() else courseViewModel.loadMyCourses() }
                )
            }
        }
    }
}

@Composable
private fun StudentCourseList(
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
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onRetry) { Text("重试") }
                }
            }
        }
        is CourseUiState.Success -> {
            if (state.courses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无课程", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.courses, key = { it.id }) { course ->
                        StudentCourseCard(
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
private fun StudentCourseCard(
    course: CourseResponse,
    showEnrollButton: Boolean,
    isEnrolling: Boolean,
    onEnroll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LibraryBooks, null, tint = MaterialTheme.colorScheme.onPrimaryContainer,
                             modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(course.courseName, style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp),
                             tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(course.teacherName, style = MaterialTheme.typography.bodySmall,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (!course.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(course.description, style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.People, null, modifier = Modifier.size(16.dp),
                         tint = MaterialTheme.colorScheme.outline)
                    Text("${course.enrollmentCount} 人已选", style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.outline)
                }
                if (showEnrollButton) {
                    FilledTonalButton(
                        onClick = onEnroll,
                        enabled = !isEnrolling,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(if (isEnrolling) "处理中..." else "选课",
                             style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                        Text("已选该课程", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(6.dp))
                    }
                }
            }
        }
    }
}

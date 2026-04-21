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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
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
import ovo.sypw.kmp.examsystem.data.dto.EnrollmentResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.data.repository.CourseRepository
import ovo.sypw.kmp.examsystem.data.repository.ExamRepository
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.presentation.navigation.UserRole
import ovo.sypw.kmp.examsystem.presentation.screens.teacher.QuestionManageScreen
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseViewModel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.EnrollState

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

    var questionNavCourse by rememberSaveable { mutableStateOf<CourseResponse?>(null) }
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
                title = { Text(if (userRole == UserRole.ADMIN) "All Courses" else "My Teaching Courses") },
                actions = {
                    IconButton(onClick = {
                        if (userRole == UserRole.ADMIN) courseViewModel.loadAllCourses() else courseViewModel.loadMyCourses()
                    }) { Icon(Icons.Default.Refresh, contentDescription = "refresh") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Course") }
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
                        }) { Text("Retry") }
                    }
                }
                is CourseUiState.Success -> {
                    if (s.courses.isEmpty()) {
                        Text("No course yet", modifier = Modifier.padding(top = 32.dp))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().widthIn(max = 900.dp),
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
            title = "Create Course",
            onConfirm = {
                courseViewModel.createCourse(it)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    showEditDialog?.let { course ->
        CourseFormDialog(
            title = "Edit Course",
            initial = course,
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
            title = { Text("Delete Course") },
            text = { Text("Delete course ${course.courseName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        courseViewModel.deleteCourse(course.id)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") } }
        )
    }

    showEnrollmentDialog?.let { course ->
        EnrollmentManageDialog(course = course, courseViewModel = courseViewModel, onDismiss = { showEnrollmentDialog = null })
    }
}

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
                        Icon(Icons.Default.LibraryBooks, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(course.courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(course.teacherName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FilledTonalButton(onClick = onManageQuestions) {
                    Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Questions")
                }
            }

            if (!course.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(course.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onManageEnrollments) {
                    Icon(Icons.Default.People, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Enrollments")
                }
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Course Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = teacherIdStr, onValueChange = { teacherIdStr = it }, label = { Text("Teacher ID (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        CourseRequest(
                            courseName = name.trim(),
                            description = description.takeIf { it.isNotBlank() },
                            teacherId = teacherIdStr.toLongOrNull()
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnrollmentManageDialog(
    course: CourseResponse,
    courseViewModel: CourseViewModel,
    onDismiss: () -> Unit
) {
    val courseRepository: CourseRepository = koinInject()
    val scope = rememberCoroutineScope()
    val students by courseViewModel.courseStudents.collectAsState()
    var showAddStudent by remember { mutableStateOf(false) }
    var studentIdInput by remember { mutableStateOf("") }

    LaunchedEffect(course.id) {
        courseViewModel.loadCourseStudents(course.id)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enrollments: ${course.courseName}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (students.isEmpty()) {
                    Text("No students yet")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(240.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(students, key = { it.id }) { student ->
                            EnrollmentCard(
                                enrollment = student,
                                onRemove = {
                                    scope.launch {
                                        courseRepository.removeStudentFromCourse(course.id, student.id)
                                        courseViewModel.loadCourseStudents(course.id)
                                    }
                                }
                            )
                        }
                    }
                }
                if (showAddStudent) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = studentIdInput,
                            onValueChange = { studentIdInput = it },
                            label = { Text("Student ID") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(onClick = {
                            val sid = studentIdInput.toLongOrNull() ?: return@Button
                            scope.launch {
                                courseRepository.addStudentToCourse(course.id, sid)
                                studentIdInput = ""
                                showAddStudent = false
                                courseViewModel.loadCourseStudents(course.id)
                            }
                        }) { Text("Add") }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { showAddStudent = !showAddStudent }) {
                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showAddStudent) "Hide" else "Assign Student")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun EnrollmentCard(
    enrollment: EnrollmentResponse,
    onRemove: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(enrollment.teacherName.ifBlank { "Student" }, fontWeight = FontWeight.Bold)
                Text("enroll: ${enrollment.enrollTime ?: "-"}", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(
                onClick = onRemove,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Remove")
            }
        }
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

    LaunchedEffect(enrollState) {
        when (val state = enrollState) {
            is EnrollState.Success -> {
                snackbarHostState.showSnackbar("Enrolled: ${state.enrollment.courseName}")
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
                title = { Text("Courses") },
                actions = {
                    IconButton(onClick = {
                        if (selectedTab == 0) courseViewModel.loadAllCourses() else courseViewModel.loadMyCourses()
                    }) { Icon(Icons.Default.Refresh, contentDescription = "refresh") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                listOf("All", "Enrolled").forEachIndexed { index, title ->
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
            title = { Text("Withdraw course") },
            text = { Text("Withdraw from ${course.courseName}?") },
            confirmButton = {
                Button(onClick = {
                    val currentUserId = (authState as? AuthState.Authenticated)?.user?.id
                    if (currentUserId != null) {
                        scope.launch {
                            courseRepository.removeStudentFromCourse(course.id, currentUserId)
                                .onSuccess {
                                    snackbarHostState.showSnackbar("Withdrawn from ${course.courseName}")
                                    courseViewModel.loadMyCourses()
                                    courseViewModel.loadAllCourses()
                                }
                                .onFailure { snackbarHostState.showSnackbar("Withdraw failed: ${it.message}") }
                        }
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Not logged in") }
                    }
                    withdrawingCourse = null
                }) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { withdrawingCourse = null }) { Text("Cancel") } }
        )
    }

    examDetailCourse?.let { course ->
        AlertDialog(
            onDismissRequest = { examDetailCourse = null },
            title = { Text("Course exams - ${course.courseName}") },
            text = {
                if (loadingCourseExams) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (courseExams.isEmpty()) {
                    Text("No exam linked to this course")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(courseExams, key = { it.id }) { exam ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                    Text(exam.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text("Score ${exam.totalScore} · ${exam.duration} min", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { examDetailCourse = null }) { Text("Close") } },
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
                    Button(onClick = onRetry) { Text("Retry") }
                }
            }
        }
        is CourseUiState.Success -> {
            if (state.courses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No course")
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
                            onEnroll = { onEnroll(course.id) },
                            onWithdraw = { onWithdraw(course) },
                            onViewCourseExams = { onViewCourseExams(course) }
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
    onEnroll: () -> Unit,
    onWithdraw: () -> Unit,
    onViewCourseExams: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LibraryBooks, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(course.courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(course.teacherName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (!course.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(course.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.People, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("${course.enrollmentCount} enrolled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
                if (showEnrollButton) {
                    FilledTonalButton(onClick = onEnroll, enabled = !isEnrolling, modifier = Modifier.height(32.dp)) {
                        Text(if (isEnrolling) "Processing..." else "Enroll")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onViewCourseExams) { Text("Exams") }
                        FilledTonalButton(
                            onClick = onWithdraw,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.height(32.dp)
                        ) { Text("Withdraw") }
                    }
                }
            }
        }
    }
}

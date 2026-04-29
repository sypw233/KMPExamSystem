package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.presentation.navigation.UserRole
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseViewModel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveLazyVerticalGrid
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CourseManageScreen(courseViewModel: CourseViewModel, userRole: UserRole) {
    val allCoursesState by courseViewModel.allCoursesState.collectAsState()
    val myCoursesState by courseViewModel.myCoursesState.collectAsState()
    val actionState by courseViewModel.actionState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val config = LocalResponsiveConfig.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
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
                scrollBehavior = scrollBehavior,
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
        LaunchedEffect(allCoursesState, myCoursesState) {
            isRefreshing = false
        }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                if (userRole == UserRole.ADMIN) courseViewModel.loadAllCourses() else courseViewModel.loadMyCourses()
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
        val state = if (userRole == UserRole.ADMIN) allCoursesState else myCoursesState
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
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
                            modifier = Modifier
                                .then(
                                    if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) {
                                        Modifier.widthIn(max = ResponsiveUtils.MaxWidths.FULL)
                                    } else {
                                        Modifier
                                    }
                                )
                                .fillMaxSize(),
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
    }

    CourseManageDialogs(
        userRole = userRole,
        courseViewModel = courseViewModel,
        showCreateDialog = showCreateDialog,
        showEditDialog = showEditDialog,
        showDeleteConfirm = showDeleteConfirm,
        showEnrollmentDialog = showEnrollmentDialog,
        onDismissCreate = { showCreateDialog = false },
        onDismissEdit = { showEditDialog = null },
        onDismissDelete = { showDeleteConfirm = null },
        onDismissEnrollment = { showEnrollmentDialog = null }
    )
}

@Composable
private fun CourseManageDialogs(
    userRole: UserRole,
    courseViewModel: CourseViewModel,
    showCreateDialog: Boolean,
    showEditDialog: CourseResponse?,
    showDeleteConfirm: CourseResponse?,
    showEnrollmentDialog: CourseResponse?,
    onDismissCreate: () -> Unit,
    onDismissEdit: () -> Unit,
    onDismissDelete: () -> Unit,
    onDismissEnrollment: () -> Unit
) {
    if (showCreateDialog) {
        CourseFormDialog(
            title = "创建课程",
            onConfirm = {
                courseViewModel.createCourse(it)
                onDismissCreate()
            },
            onDismiss = onDismissCreate
        )
    }

    showEditDialog?.let { course ->
        CourseFormDialog(
            title = "编辑课程",
            initial = course,
            onConfirm = {
                courseViewModel.updateCourse(course.id, it)
                onDismissEdit()
            },
            onDismiss = onDismissEdit
        )
    }

    showDeleteConfirm?.let { course ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("删除课程") },
            text = { Text("确定删除课程「${course.courseName}」吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        courseViewModel.deleteCourse(course.id)
                        onDismissDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = onDismissDelete) { Text("取消") } }
        )
    }

    showEnrollmentDialog?.let { course ->
        EnrollmentManageDialog(course = course, courseViewModel = courseViewModel, onDismiss = onDismissEnrollment)
    }
}

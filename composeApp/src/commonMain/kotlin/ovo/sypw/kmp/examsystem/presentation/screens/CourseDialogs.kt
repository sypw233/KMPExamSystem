package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ovo.sypw.kmp.examsystem.data.dto.CourseRequest
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.data.dto.EnrollmentResponse
import ovo.sypw.kmp.examsystem.data.repository.CourseRepository
import ovo.sypw.kmp.examsystem.presentation.components.StudentSelector
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseFormDialog(
    title: String,
    initial: CourseResponse? = null,
    onConfirm: (CourseRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.courseName ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    val normalizedName = name.trim()
    val normalizedDescription = description.trim()
    val isValid = normalizedName.isNotBlank() && normalizedName.length <= 100 && normalizedDescription.length <= 500

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("课程名称 *") },
                    supportingText = { Text("${normalizedName.length}/100") },
                    isError = normalizedName.length > 100,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("课程描述") },
                    supportingText = { Text("${normalizedDescription.length}/500") },
                    isError = normalizedDescription.length > 500,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        CourseRequest(
                            courseName = normalizedName,
                            description = normalizedDescription.ifBlank { null },
                            status = initial?.status ?: 1
                        )
                    )
                },
                enabled = isValid
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentManageDialog(
    course: CourseResponse,
    courseViewModel: CourseViewModel,
    onDismiss: () -> Unit
) {
    val courseRepository: CourseRepository = koinInject()
    val scope = rememberCoroutineScope()
    val students by courseViewModel.courseStudents.collectAsState()
    var showStudentSelector by remember { mutableStateOf(false) }
    var selectedStudentIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    LaunchedEffect(course.id) {
        courseViewModel.loadCourseStudents(course.id)
    }

    if (showStudentSelector) {
        StudentSelector(
            selectedStudentIds = selectedStudentIds,
            onSelectionChange = { selectedStudentIds = it },
            onConfirm = {
                showStudentSelector = false
                scope.launch {
                    courseRepository.batchAddStudentsToCourse(course.id, selectedStudentIds.toList())
                    selectedStudentIds = emptySet()
                    courseViewModel.loadCourseStudents(course.id)
                }
            },
            onDismiss = {
                showStudentSelector = false
                selectedStudentIds = emptySet()
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选课管理: ${course.courseName}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (students.isEmpty()) {
                    Text("暂无学生")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(240.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(students, key = { it.id }) { student ->
                            EnrollmentCard(
                                enrollment = student,
                                onRemove = {
                                    scope.launch {
                                        courseRepository.removeStudentFromCourse(course.id, student.studentId)
                                        courseViewModel.loadCourseStudents(course.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { showStudentSelector = true }) {
                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("批量添加学生")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
fun EnrollmentCard(
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
                Text(enrollment.studentName.ifBlank { "学生" }, fontWeight = FontWeight.Bold)
                Text("选课时间: ${enrollment.enrollmentTime ?: "-"}", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(
                onClick = onRemove,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("移除")
            }
        }
    }
}

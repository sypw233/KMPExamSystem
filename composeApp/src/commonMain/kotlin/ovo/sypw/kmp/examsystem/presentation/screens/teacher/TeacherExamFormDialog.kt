package ovo.sypw.kmp.examsystem.presentation.screens.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamFormDialog(
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
                    ExposedDropdownMenuBox(expanded = courseExpanded, onExpandedChange = { courseExpanded = it }) {
                        OutlinedTextField(
                            value = courses.find { it.id == selectedCourseId }?.courseName ?: "请选择课程",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("所属课程 *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(courseExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                        )
                        ExposedDropdownMenu(expanded = courseExpanded, onDismissRequest = { courseExpanded = false }) {
                            courses.forEach { course ->
                                DropdownMenuItem(
                                    text = { Text(course.courseName) },
                                    onClick = {
                                        selectedCourseId = course.id
                                        courseExpanded = false
                                    }
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
                    DateTimeField(
                        label = "开始时间",
                        value = startTime,
                        onClick = { showStartTimePicker = true }
                    )
                    DateTimeField(
                        label = "结束时间",
                        value = endTime,
                        onClick = { showEndTimePicker = true }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm(
                            ExamRequest(
                                title = examTitle.trim(),
                                description = description.takeIf { it.isNotBlank() },
                                courseId = selectedCourseId,
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                duration = duration.toInt(),
                                totalScore = totalScore.toInt()
                            )
                        )
                    },
                    enabled = isValid
                ) { Text(if (initial == null) "创建" else "保存") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
        )

        if (showStartTimePicker) {
            DateTimePickerDialog(
                initialDateTime = startTime,
                onConfirm = {
                    startTime = it
                    showStartTimePicker = false
                },
                onDismiss = { showStartTimePicker = false }
            )
        }
        if (showEndTimePicker) {
            DateTimePickerDialog(
                initialDateTime = endTime,
                onConfirm = {
                    endTime = it
                    showEndTimePicker = false
                },
                onDismiss = { showEndTimePicker = false }
            )
        }
    }
}

@Composable
private fun DateTimeField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (value.isBlank()) "未选择" else formatDateTimeForDisplay(value),
                style = MaterialTheme.typography.bodyLarge,
                color = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onClick) {
            Icon(Icons.Default.CalendarToday, "选择$label")
        }
    }
}

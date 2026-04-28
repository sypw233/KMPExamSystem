package ovo.sypw.kmp.examsystem.presentation.screens.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.StringUtils.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamFormDialog(
    title: String,
    initial: ExamResponse? = null,
    courses: List<CourseResponse> = emptyList(),
    onConfirm: (ExamRequest) -> Unit,
    onDismiss: () -> Unit
) {
    val config = LocalResponsiveConfig.current
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

fun formatDateTimeForDisplay(isoDateTime: String): String {
    if (isoDateTime.isBlank()) return ""
    val normalized = isoDateTime.replace('T', ' ')
    val parts = normalized.split(" ")
    if (parts.size < 2) return isoDateTime
    val datePart = parts[0]
    val timePart = parts[1].take(5)
    return "$datePart $timePart"
}

fun parseDateTimeComponents(isoDateTime: String): List<Int> {
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

fun formatDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): String {
    return "%04d-%02d-%02dT%02d:%02d:00".format(year, month, day, hour, minute)
}

// ─── 日期时间选择器对话框（Material3 官方组件）──────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)
@Composable
fun DateTimePickerDialog(
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

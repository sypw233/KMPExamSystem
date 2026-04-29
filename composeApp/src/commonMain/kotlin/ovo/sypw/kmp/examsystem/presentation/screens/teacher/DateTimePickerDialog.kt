package ovo.sypw.kmp.examsystem.presentation.screens.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import ovo.sypw.kmp.examsystem.utils.StringUtils.format

internal fun formatDateTimeForDisplay(isoDateTime: String): String {
    if (isoDateTime.isBlank()) return ""
    val normalized = isoDateTime.replace('T', ' ')
    val parts = normalized.split(" ")
    if (parts.size < 2) return isoDateTime
    val datePart = parts[0]
    val timePart = parts[1].take(5)
    return "$datePart $timePart"
}

private fun parseDateTimeComponents(isoDateTime: String): List<Int> {
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

private fun formatDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): String {
    return "%04d-%02d-%02dT%02d:%02d:00".format(year, month, day, hour, minute)
}

@OptIn(ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)
@Composable
internal fun DateTimePickerDialog(
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

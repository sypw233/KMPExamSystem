package ovo.sypw.kmp.examsystem.presentation.screens.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig

@Composable
fun ManageExamCard(
    exam: ExamResponse,
    canEdit: Boolean,
    isBatchMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPublish: () -> Unit,
    onCompose: (() -> Unit)? = null,
    onRandomCompose: (() -> Unit)? = null,
    onViewSubmissions: (() -> Unit)? = null
) {
    val config = LocalResponsiveConfig.current
    val statusColor = when (exam.status) {
        0 -> MaterialTheme.colorScheme.outlineVariant
        1 -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val statusLabel = when (exam.status) {
        0 -> "草稿"
        1 -> "进行中"
        2 -> "已结束"
        else -> "未知"
    }
    val statusIcon = when (exam.status) {
        0 -> Icons.Default.HourglassBottom
        1 -> Icons.Default.PlayArrow
        else -> Icons.Default.Stop
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(
            enabled = isBatchMode,
            onClick = onToggleSelect
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(config.cardPadding)) {
            if (isBatchMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isSelected, onCheckedChange = { onToggleSelect() })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(exam.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        exam.courseName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Surface(color = statusColor, shape = MaterialTheme.shapes.small) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(statusIcon, null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(statusLabel, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(exam.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (!exam.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(exam.description, style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing)) {
                Text("${exam.questionCount} 题", style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.outline)
                Text("满分 ${exam.totalScore}", style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.outline)
                Text("${exam.duration ?: "-"} 分钟", style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.outline)
            }

            } // close else

            if (canEdit) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("编辑")
                    }
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("删除")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { onRandomCompose?.invoke() }) {
                        Text("智能组卷", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(onClick = { onCompose?.invoke() }) {
                        Text("组卷")
                    }
                    Button(onClick = onPublish) { Text("发布") }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { onViewSubmissions?.invoke() }) {
                        Text("批阅与记录")
                    }
                }
            }
        }
    }
}

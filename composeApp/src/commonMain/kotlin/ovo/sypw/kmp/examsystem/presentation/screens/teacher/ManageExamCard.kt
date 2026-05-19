package ovo.sypw.kmp.examsystem.presentation.screens.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamStatisticsResponse
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.StringUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManageExamCard(
    exam: ExamResponse,
    statistics: ExamStatisticsResponse? = null,
    canEdit: Boolean,
    isBatchMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPublish: () -> Unit,
    onCompose: (() -> Unit)? = null,
    onViewSubmissions: (() -> Unit)? = null
) {
    val config = LocalResponsiveConfig.current
    val isBeforeStart = exam.status == 1 && StringUtils.isFutureDateTime(exam.startTime)
    val statusColor = when {
        isBeforeStart -> MaterialTheme.colorScheme.tertiaryContainer
        exam.status == 0 -> MaterialTheme.colorScheme.outlineVariant
        exam.status == 1 -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val statusLabel = when {
        isBeforeStart -> "考试时间未到"
        exam.status == 0 -> "草稿"
        exam.status == 1 -> "进行中"
        exam.status == 2 -> "已结束"
        else -> "未知"
    }
    val statusIcon = when {
        isBeforeStart -> Icons.Default.Schedule
        exam.status == 0 -> Icons.Default.HourglassBottom
        exam.status == 1 -> Icons.Default.PlayArrow
        else -> Icons.Default.Stop
    }
    val examTimeText = formatExamTime(exam)
    val submittedText = formatSubmittedText(statistics)
    val gradingText = formatGradingText(exam, statistics)

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
                // 课程名称和状态标签
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
                Spacer(modifier = Modifier.height(10.dp))

                ExamInfoLine(
                    icon = Icons.Default.Schedule,
                    label = "考试时间",
                    value = examTimeText
                )
                Spacer(modifier = Modifier.height(6.dp))
                ExamInfoLine(
                    icon = Icons.Default.People,
                    label = "提交人数",
                    value = submittedText
                )
                Spacer(modifier = Modifier.height(6.dp))
                ExamInfoLine(
                    icon = Icons.Default.CheckCircle,
                    label = "批阅情况",
                    value = gradingText
                )
                Spacer(modifier = Modifier.height(10.dp))

                // 考试信息标签，使用 FlowRow 避免挤压
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ExamMetricChip(Icons.Default.Quiz, "${exam.questionCount} 题")
                    ExamMetricChip(Icons.Default.CheckCircle, "满分 ${exam.totalScore}")
                    ExamMetricChip(Icons.Default.Timer, "${exam.duration ?: "-"} 分钟")
                    statistics?.averageScore?.let { ExamMetricChip(Icons.Default.People, "均分 $it") }
                }

            } // close else

            if (canEdit) {
                Spacer(modifier = Modifier.height(10.dp))
                // 操作按钮，使用 FlowRow 避免挤压
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = onPublish,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) { Text("发布") }
                    OutlinedButton(
                        onClick = { onCompose?.invoke() },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("组卷")
                    }
                    TextButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("编辑")
                    }
                    TextButton(
                        onClick = onDelete,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("删除")
                    }
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

@Composable
private fun ExamInfoLine(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExamMetricChip(
    icon: ImageVector,
    text: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatExamTime(exam: ExamResponse): String {
    val start = exam.startTime?.takeIf { it.isNotBlank() }?.let { StringUtils.formatDateTime(it) }
    val end = exam.endTime?.takeIf { it.isNotBlank() }?.let { StringUtils.formatDateTime(it) }
    return when {
        start != null && end != null -> "$start 至 $end"
        start != null -> "开始 $start"
        end != null -> "结束 $end"
        else -> "未设置"
    }
}

private fun formatSubmittedText(statistics: ExamStatisticsResponse?): String {
    if (statistics == null) return "统计同步中"
    val total = statistics.totalStudents
    return if (total > 0) {
        "${statistics.submittedCount}/$total 人"
    } else {
        "${statistics.submittedCount} 人"
    }
}

private fun formatGradingText(exam: ExamResponse, statistics: ExamStatisticsResponse?): String {
    if (statistics == null) {
        return if (exam.needsGrading) "需人工批阅" else "客观题自动评分"
    }
    return when {
        exam.needsGrading || statistics.pendingGradingCount > 0 ->
            "已批阅 ${statistics.gradedCount}，待批阅 ${statistics.pendingGradingCount}"
        statistics.submittedCount > 0 -> "已完成 ${statistics.submittedCount} 份评分"
        else -> if (exam.needsGrading) "暂无提交" else "暂无提交，自动评分"
    }
}

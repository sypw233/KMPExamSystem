package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.utils.QuestionUtils
import ovo.sypw.kmp.examsystem.utils.StringUtils.format

@Composable
internal fun QuestionItem(
    number: Int,
    examQuestion: ExamQuestionResponse,
    currentAnswer: String,
    onAnswerChange: (String) -> Unit,
    onToggleMultiple: (String) -> Unit
) {
    val question = examQuestion.question ?: return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "$number. [${QuestionUtils.questionTypeLabel(question.type)}]",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${examQuestion.score} 分",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = question.content,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (question.type) {
                "single" -> {
                    // 【修复 SEC-01】使用 QuestionUtils.parseOptionsJson 正确解析 JSON 数组
                    val options = QuestionUtils.parseOptionsJson(question.options)
                    options.forEachIndexed { index, option ->
                        val letter = ('A' + index).toString()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentAnswer == letter,
                                onClick = { onAnswerChange(letter) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$letter. $option", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                "multiple" -> {
                    val options = QuestionUtils.parseOptionsJson(question.options)
                    val selectedSet = if (currentAnswer.isBlank()) emptySet() else currentAnswer.split(",").toSet()
                    options.forEachIndexed { index, option ->
                        val letter = ('A' + index).toString()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedSet.contains(letter),
                                onCheckedChange = { onToggleMultiple(letter) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$letter. $option", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                "true_false" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = currentAnswer == "true", onClick = { onAnswerChange("true") })
                            Text("正确", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = currentAnswer == "false", onClick = { onAnswerChange("false") })
                            Text("错误", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                else -> {
                    OutlinedTextField(
                        value = currentAnswer,
                        onValueChange = onAnswerChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("答案") },
                        minLines = if (question.type == "short_answer") 4 else 2,
                        shape = MaterialTheme.shapes.small
                    )
                }
            }
        }
    }
}

@Composable
internal fun ExamResultSummary(
    totalScore: Int?,
    objectiveScore: Int?,
    needsGrading: Boolean,
    onExit: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            // 【UX-03】添加状态图标和更清晰的视觉层级
            Icon(
                imageVector = if (needsGrading) Icons.Default.HourglassEmpty else Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = if (needsGrading)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "交卷成功",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (needsGrading) {
                Text(
                    "主观题正在等待教师评分。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (objectiveScore != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "客观题得分: $objectiveScore",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                if (objectiveScore != null) {
                    Text(
                        "客观题得分: $objectiveScore",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    "总分: ${totalScore ?: "--"}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text("返回首页") }
        }
    }
}

internal fun formatExamTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, secs) else "%02d:%02d".format(minutes, secs)
}

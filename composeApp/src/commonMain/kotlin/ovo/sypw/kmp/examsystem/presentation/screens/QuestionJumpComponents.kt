package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.questionType
import ovo.sypw.kmp.examsystem.utils.QuestionUtils

internal data class QuestionJumpEntry(
    val index: Int,
    val number: Int,
    val questionType: String,
    val typeLabel: String
)

internal data class QuestionJumpGroup(
    val typeLabel: String,
    val entries: List<QuestionJumpEntry>
)

internal fun buildQuestionJumpGroups(
    questions: List<ExamQuestionResponse>
): List<QuestionJumpGroup> {
    val orderedTypes = listOf("single", "multiple", "true_false", "fill_blank", "short_answer")
    val grouped = questions.mapIndexedNotNull { index, examQuestion ->
        val question = examQuestion.question ?: return@mapIndexedNotNull null
        QuestionJumpEntry(
            index = index,
            number = index + 1,
            questionType = question.questionType.value,
            typeLabel = QuestionUtils.questionTypeLabel(question.type)
        )
    }.groupBy { it.questionType }

    return orderedTypes.mapNotNull { typeKey ->
        grouped[typeKey]?.let { QuestionJumpGroup(it.first().typeLabel, it) }
    } + grouped.filterKeys { it !in orderedTypes }.values.map { entries ->
        QuestionJumpGroup(entries.first().typeLabel, entries)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuestionJumpPanel(
    questions: List<ExamQuestionResponse>,
    selectedIndex: Int?,
    onQuestionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "题号跳转"
) {
    var expanded by remember { mutableStateOf(false) }
    val groups = remember(questions) { buildQuestionJumpGroups(questions) }

    if (groups.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                        label = { Text(title) }
                    )
                    Text(
                        text = "${questions.size} 题",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "收起" else "展开")
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    groups.forEach { group ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = group.typeLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                group.entries.forEach { entry ->
                                    FilterChip(
                                        selected = selectedIndex == entry.index,
                                        onClick = { onQuestionSelected(entry.index) },
                                        label = { Text(entry.number.toString()) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

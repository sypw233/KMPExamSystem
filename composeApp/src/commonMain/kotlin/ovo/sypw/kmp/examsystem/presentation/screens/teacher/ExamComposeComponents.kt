package ovo.sypw.kmp.examsystem.presentation.screens.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.SectionRule
import ovo.sypw.kmp.examsystem.presentation.viewmodel.RandomComposeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RandomComposeDialog(
    config: RandomComposeState.Configuring,
    targetScore: Int?,
    onConfirm: (bankId: Long, expectedTotalScore: Int?, sections: List<SectionRule>, shuffle: Boolean, lenient: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedBankId by remember(config) { mutableStateOf(config.selectedBankId) }
    var expectedTotalScore by remember(config) { mutableStateOf(config.expectedTotalScore?.toString() ?: targetScore?.toString() ?: "") }
    var sections by remember(config) { mutableStateOf(config.sections) }
    var shuffleQuestions by remember(config) { mutableStateOf(config.shuffleQuestions) }
    var lenientMode by remember(config) { mutableStateOf(config.lenientMode) }
    val totalCalculatedScore = sections.sumOf { it.count * it.scorePerQuestion }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("智能随机组卷") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp).verticalScroll(rememberScrollState())) {
                if (config.banks.isEmpty()) {
                    Text("暂无可用题库，请先创建题库", color = MaterialTheme.colorScheme.error)
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    val selectedBank = config.banks.find { it.id == selectedBankId }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedBank?.name ?: "选择题库",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("题库") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            config.banks.forEach { bank ->
                                DropdownMenuItem(
                                    text = { Text(bank.name) },
                                    onClick = {
                                        selectedBankId = bank.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = expectedTotalScore,
                    onValueChange = { expectedTotalScore = it.filter { c -> c.isDigit() } },
                    label = { Text("期望总分 (可选)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "组卷规则 (当前配置总分: $totalCalculatedScore)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                sections.forEachIndexed { index, rule ->
                    SectionRuleItem(
                        rule = rule,
                        onUpdate = { updated ->
                            sections = sections.toMutableList().apply { set(index, updated) }
                        },
                        onDelete = {
                            sections = sections.toMutableList().apply { removeAt(index) }
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                OutlinedButton(
                    onClick = {
                        sections = sections + SectionRule(
                            type = "single",
                            count = 10,
                            scorePerQuestion = 2
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加规则")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = shuffleQuestions, onCheckedChange = { shuffleQuestions = it })
                    Text("打乱题目顺序", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = lenientMode, onCheckedChange = { lenientMode = it })
                    Text("宽松模式 (难度不足时自动补齐)", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bankId = selectedBankId ?: return@Button
                    val score = expectedTotalScore.toIntOrNull()
                    onConfirm(bankId, score, sections, shuffleQuestions, lenientMode)
                },
                enabled = selectedBankId != null && sections.isNotEmpty()
            ) {
                Text("开始组卷")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
internal fun ComposeQuestionCard(
    question: QuestionResponse,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onToggle,
                modifier = Modifier.padding(top = 4.dp)
            )
            Column(modifier = Modifier.weight(1f).padding(top = 12.dp, bottom = 8.dp, end = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val typeText = when (question.type) {
                        "single" -> "单选题"
                        "multiple" -> "多选题"
                        "true_false" -> "判断题"
                        "fill_blank" -> "填空题"
                        "short_answer" -> "简答题"
                        else -> question.type
                    }
                    Text("[$typeText]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("${question.score} 分", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(question.content, style = MaterialTheme.typography.bodyMedium)
                if (!question.difficulty.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "难度: ${question.difficulty}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionRuleItem(
    rule: SectionRule,
    onUpdate: (SectionRule) -> Unit,
    onDelete: () -> Unit
) {
    val typeOptions = listOf(
        "single" to "单选题",
        "multiple" to "多选题",
        "true_false" to "判断题",
        "fill_blank" to "填空题",
        "short_answer" to "简答题"
    )
    var typeExpanded by remember { mutableStateOf(false) }
    val typeLabel = typeOptions.find { it.first == rule.type }?.second ?: rule.type

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = typeLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("题型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        typeOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    onUpdate(rule.copy(type = value))
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = rule.count.toString(),
                    onValueChange = { onUpdate(rule.copy(count = it.toIntOrNull() ?: 0)) },
                    label = { Text("数量") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = rule.scorePerQuestion.toString(),
                    onValueChange = { onUpdate(rule.copy(scorePerQuestion = it.toIntOrNull() ?: 0)) },
                    label = { Text("每题分值") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

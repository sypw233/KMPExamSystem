package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.QuestionRequest
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.utils.QuestionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionFormDialog(
    initial: QuestionResponse? = null,
    onConfirm: (QuestionRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var content by remember { mutableStateOf(initial?.content ?: "") }
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(initial?.type ?: "single") }
    var difficultyExpanded by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf(initial?.difficulty ?: "medium") }
    var options by remember {
        val initialOptions = QuestionUtils.parseOptionsJson(initial?.options)
        mutableStateOf(initialOptions.toMutableList())
    }
    var answer by remember { mutableStateOf(initial?.answer ?: "") }
    var score by remember { mutableStateOf(initial?.score?.toString() ?: "5") }
    var category by remember { mutableStateOf(initial?.category ?: "") }
    var analysis by remember { mutableStateOf(initial?.analysis ?: "") }

    val showOptions = selectedType == "single" || selectedType == "multiple"
    val isFormValid = isQuestionFormValid(
        content = content,
        type = selectedType,
        options = options,
        answer = answer,
        score = score
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "新建题目" else "编辑题目") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("题目内容") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = QuestionUtils.questionTypeOptions.first { it.first == selectedType }.second,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("题目类型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        QuestionUtils.questionTypeOptions.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                selectedType = value
                                typeExpanded = false
                                if (value != "single" && value != "multiple") {
                                    options = mutableListOf()
                                } else if (options.isEmpty()) {
                                    options = mutableListOf("", "")
                                }
                                answer = ""
                            })
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = difficultyExpanded,
                    onExpandedChange = { difficultyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = QuestionUtils.difficultyOptions.first { it.first == selectedDifficulty }.second,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("难度") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = difficultyExpanded, onDismissRequest = { difficultyExpanded = false }) {
                        QuestionUtils.difficultyOptions.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                selectedDifficulty = value
                                difficultyExpanded = false
                            })
                        }
                    }
                }

                if (showOptions) {
                    Text("选项管理", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    options.forEachIndexed { index, option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${('A' + index)}.",
                                modifier = Modifier.width(28.dp),
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = option,
                                onValueChange = { newValue ->
                                    options = options.toMutableList().also { it[index] = newValue }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("选项内容") }
                            )
                            IconButton(onClick = {
                                options = options.toMutableList().also { it.removeAt(index) }
                                answer = remapAnswerAfterOptionRemoval(answer, index, selectedType == "multiple")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "删除选项", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    TextButton(onClick = {
                        options = options.toMutableList().also { it.add("") }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加选项")
                    }
                    // ── 可视化答案选择 ──
                    Text("正确答案", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    when (selectedType) {
                        "single" -> {
                            options.forEachIndexed { idx, opt ->
                                if (opt.isNotBlank()) {
                                    val letter = ('A' + idx).toString()
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = answer == letter, onClick = { answer = letter })
                                        Text("$letter. $opt", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                        "multiple" -> {
                            val selectedSet = answer.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableSet()
                            options.forEachIndexed { idx, opt ->
                                if (opt.isNotBlank()) {
                                    val letter = ('A' + idx).toString()
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = letter in selectedSet,
                                            onCheckedChange = { checked ->
                                                if (checked) selectedSet.add(letter) else selectedSet.remove(letter)
                                                answer = selectedSet.sorted().joinToString(",")
                                            }
                                        )
                                        Text("$letter. $opt", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedType == "true_false") {
                    Text("正确答案", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = answer == "true",
                            onClick = { answer = "true" },
                            label = { Text("正确") }
                        )
                        FilterChip(
                            selected = answer == "false",
                            onClick = { answer = "false" },
                            label = { Text("错误") }
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        label = { Text("答案") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("答案内容") }
                    )
                }

                OutlinedTextField(
                    value = score,
                    onValueChange = { score = it.filter { c -> c.isDigit() } },
                    label = { Text("分值") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("分类 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = analysis,
                    onValueChange = { analysis = it },
                    label = { Text("解析 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val optionsJson = if (showOptions) {
                        QuestionUtils.buildOptionsJson(options).takeIf { it.isNotBlank() }
                    } else {
                        null
                    }

                    onConfirm(
                        QuestionRequest(
                            content = content.trim(),
                            type = selectedType,
                            options = optionsJson,
                            answer = answer.trim(),
                            analysis = analysis.trim().ifBlank { null },
                            difficulty = selectedDifficulty,
                            category = category.trim().ifBlank { null },
                            score = score.toIntOrNull()?.coerceAtLeast(1) ?: 5
                        )
                    )
                },
                enabled = isFormValid
            ) {
                Text(if (initial == null) "创建" else "保存")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

private fun isQuestionFormValid(
    content: String,
    type: String,
    options: List<String>,
    answer: String,
    score: String
): Boolean {
    val scoreValue = score.toIntOrNull()
    if (content.isBlank() || scoreValue == null || scoreValue <= 0) return false

    return when (type) {
        "single", "multiple" -> {
            val validLetters = options.mapIndexedNotNull { index, option ->
                if (option.isBlank()) null else ('A' + index).toString()
            }.toSet()
            val answerLetters = answer.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toSet()

            val answerCountValid = if (type == "single") answerLetters.size == 1 else answerLetters.isNotEmpty()
            validLetters.size >= 2 &&
                answerCountValid &&
                answerLetters.all { it in validLetters }
        }

        "true_false" -> answer == "true" || answer == "false"
        else -> answer.isNotBlank()
    }
}

private fun remapAnswerAfterOptionRemoval(
    answer: String,
    removedIndex: Int,
    multiple: Boolean
): String {
    val selectedIndexes = answer.split(",")
        .mapNotNull { part -> part.trim().firstOrNull()?.uppercaseChar()?.minus('A') }
        .filter { it >= 0 }

    val remapped = selectedIndexes.mapNotNull { index ->
        when {
            index == removedIndex -> null
            index > removedIndex -> ('A' + index - 1).toString()
            else -> ('A' + index).toString()
        }
    }.distinct().sorted()

    return if (multiple) {
        remapped.joinToString(",")
    } else {
        remapped.firstOrNull().orEmpty()
    }
}

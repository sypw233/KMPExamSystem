package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.presentation.navigation.NavigationManager
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamTakingUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamTakingViewModel

/**
 * 考试答题界面（全屏模式）
 * 接入 ExamTakingViewModel，支持真实题目加载和答案提交
 */
@Composable
fun ExamTakingScreen(
    examId: Long,
    navigationManager: NavigationManager,
    onExitExam: () -> Unit
) {
    val viewModel: ExamTakingViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val answers by viewModel.answers.collectAsState()

    // 进入时开始考试
    LaunchedEffect(examId) {
        viewModel.enterExam(examId)
    }

    when (val state = uiState) {
        is ExamTakingUiState.Loading, ExamTakingUiState.Idle -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("正在加载考试...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        is ExamTakingUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        navigationManager.exitExamMode()
                        viewModel.reset()
                        onExitExam()
                    }) { Text("返回") }
                }
            }
        }
        is ExamTakingUiState.Submitted -> {
            ExamResultSummary(
                totalScore = state.submission.totalScore,
                objectiveScore = state.submission.objectiveScore,
                needsGrading = (state.submission.totalScore ?: 0) == 0 && state.submission.subjectiveScore == null,
                onExit = {
                    navigationManager.exitExamMode()
                    viewModel.reset()
                    onExitExam()
                }
            )
        }
        is ExamTakingUiState.Ready -> {
            ExamContent(
                exam = state,
                answers = answers,
                onAnswerChange = { qId, ans -> viewModel.updateAnswer(qId, ans) },
                onToggleMultiple = { qId, opt -> viewModel.toggleMultipleChoice(qId, opt) },
                onSubmit = { viewModel.submitExam() },
                onExit = {
                    navigationManager.exitExamMode()
                    viewModel.reset()
                    onExitExam()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamContent(
    exam: ExamTakingUiState.Ready,
    answers: Map<String, String>,
    onAnswerChange: (Long, String) -> Unit,
    onToggleMultiple: (Long, String) -> Unit,
    onSubmit: () -> Unit,
    onExit: () -> Unit
) {
    var remainingSeconds by remember { mutableStateOf(exam.exam.duration * 60) }
    var showExitDialog by remember { mutableStateOf(false) }

    // 计时器
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
        // 时间到自动提交
        onSubmit()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(exam.exam.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "剩余: ${formatExamTime(remainingSeconds)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (remainingSeconds < 600) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "退出考试")
                    }
                },
                actions = {
                    Button(
                        onClick = { showExitDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("提交试卷")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(exam.questions) { index, examQuestion ->
                    val question = examQuestion.question ?: return@itemsIndexed
                    QuestionItem(
                        number = index + 1,
                        examQuestion = examQuestion,
                        currentAnswer = answers[question.id.toString()] ?: "",
                        onAnswerChange = { answer -> onAnswerChange(question.id, answer) },
                        onToggleMultiple = { option -> onToggleMultiple(question.id, option) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("确认提交试卷?") },
            text = {
                val answered = answers.size
                val total = exam.questions.size
                Text("已答 $answered / $total 题，提交后无法修改。")
            },
            confirmButton = {
                Button(onClick = {
                    showExitDialog = false
                    onSubmit()
                }) { Text("确认提交") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("继续作答") }
            }
        )
    }
}

@Composable
private fun QuestionItem(
    number: Int,
    examQuestion: ExamQuestionResponse,
    currentAnswer: String,
    onAnswerChange: (String) -> Unit,
    onToggleMultiple: (String) -> Unit
) {
    val question = examQuestion.question ?: return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 题号、类型和分值
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$number. [${questionTypeLabel(question.type)}]",
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
                    val options = parseOptions(question.options)
                    options.forEachIndexed { index, option ->
                        val letter = ('A' + index).toString()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
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
                    val options = parseOptions(question.options)
                    val selectedSet = if (currentAnswer.isBlank()) emptySet()
                    else currentAnswer.split(",").toSet()
                    options.forEachIndexed { index, option ->
                        val letter = ('A' + index).toString()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
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
                        label = { Text("请输入答案") },
                        minLines = if (question.type == "short_answer") 4 else 2,
                        shape = MaterialTheme.shapes.small
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamResultSummary(
    totalScore: Int?,
    objectiveScore: Int?,
    needsGrading: Boolean,
    onExit: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("提交成功！", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            if (needsGrading) {
                Text("您的答卷已提交，主观题等待教师评分。", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("客观题得分: $objectiveScore", style = MaterialTheme.typography.titleMedium)
                Text("总分: ${totalScore ?: "--"}", style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text("返回首页") }
        }
    }
}

private fun formatExamTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, secs)
    else "%02d:%02d".format(minutes, secs)
}

private fun questionTypeLabel(type: String): String = when (type) {
    "single" -> "单选"
    "multiple" -> "多选"
    "true_false" -> "判断"
    "fill_blank" -> "填空"
    "short_answer" -> "简答"
    else -> type
}

private fun parseOptions(optionsJson: String?): List<String> {
    if (optionsJson.isNullOrBlank()) return emptyList()
    return try {
        optionsJson.trim('[', ']')
            .split("\",\"")
            .map { it.trim('"', ' ') }
            .filter { it.isNotBlank() }
    } catch (_: Exception) {
        emptyList()
    }
}

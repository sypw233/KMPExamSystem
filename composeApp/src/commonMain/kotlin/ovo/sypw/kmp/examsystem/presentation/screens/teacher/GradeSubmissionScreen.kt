package ovo.sypw.kmp.examsystem.presentation.screens.teacher

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.presentation.viewmodel.GradeActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.GradeSubmissionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeSubmissionScreen(
    submissionId: Long,
    viewModel: GradeSubmissionViewModel,
    onBack: () -> Unit
) {
    val submission by viewModel.currentSubmission.collectAsState()
    val questions by viewModel.currentQuestions.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 存储分数的 Map: questionId -> score string
    val scoreMap = remember { mutableStateMapOf<Long, String>() }
    // 存储评价的 Map: questionId -> comment string
    val commentMap = remember { mutableStateMapOf<Long, String>() }

    LaunchedEffect(submissionId) {
        viewModel.loadSubmissionDetail(submissionId)
    }

    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is GradeActionState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
                onBack() // back on success
            }
            is GradeActionState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    // 解析学生答案 (JSON 字符串 -> Map)
    val userAnswers: Map<String, String> = remember(submission?.answers) {
        try {
            val jsonStr = submission?.answers ?: return@remember emptyMap()
            if (jsonStr.isNotBlank()) Json.decodeFromString(jsonStr) else emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // 过滤出主观题 (包括简答题和部分填空题，默认短答题 short_answer)
    val subjectiveQuestions = questions.filter { it.question?.type == "short_answer" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("批改试卷 - ${submission?.userName ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (submission == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header Info
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("考试: ${submission?.examTitle ?: ""}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("客观题得分: ${submission?.objectiveScore ?: 0}", color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Button(
                        onClick = {
                            val scoreMapData = scoreMap.mapNotNull { (qId, strScore) ->
                                val score = strScore.toIntOrNull() ?: return@mapNotNull null
                                qId.toString() to score
                            }.toMap()
                            viewModel.submitGrades(submissionId, scoreMapData)
                        },
                        enabled = actionState !is GradeActionState.Loading
                    ) {
                        Text("保存批改")
                    }
                }
            }

            if (subjectiveQuestions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("该试卷没有需要手动批改的主观题", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(subjectiveQuestions, key = { it.questionId }) { eq ->
                        GradeQuestionItem(
                            examQuestion = eq,
                            studentAnswer = userAnswers[eq.questionId.toString()] ?: "（未作答）",
                            currentScore = scoreMap[eq.questionId] ?: "",
                            currentComment = commentMap[eq.questionId] ?: "",
                            onScoreChange = { scoreMap[eq.questionId] = it },
                            onCommentChange = { commentMap[eq.questionId] = it },
                            onRequestAiGrade = { callback ->
                                scope.launch {
                                    val studentAnswer = userAnswers[eq.questionId.toString()] ?: ""
                                    val res = viewModel.requestAiGrade(eq.questionId, studentAnswer, eq.score)
                                    res.onSuccess { aiRes ->
                                        scoreMap[eq.questionId] = aiRes.suggestedScore.toString()
                                        commentMap[eq.questionId] = aiRes.explanation ?: "AI 认为该答案合理"
                                    }.onFailure {
                                        snackbarHostState.showSnackbar(it.message ?: "AI 判分失败")
                                    }
                                    callback()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GradeQuestionItem(
    examQuestion: ExamQuestionResponse,
    studentAnswer: String,
    currentScore: String,
    currentComment: String,
    onScoreChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onRequestAiGrade: (() -> Unit) -> Unit
) {
    val q = examQuestion.question ?: return
    var isLoadingAi by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 题目
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("题目 ${examQuestion.orderNum}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${examQuestion.score} 分", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(q.content, style = MaterialTheme.typography.bodyMedium)

            // 参考答案
            if (!q.answer.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("参考答案", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(q.answer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // 学生回答
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("学生回答", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(studentAnswer, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 批改区
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                OutlinedTextField(
                    value = currentScore,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) onScoreChange(it) },
                    label = { Text("得分") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(80.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = currentComment,
                    onValueChange = onCommentChange,
                    label = { Text("批语（可选）") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = {
                        isLoadingAi = true
                        onRequestAiGrade { isLoadingAi = false }
                    },
                    enabled = !isLoadingAi
                ) {
                    if (isLoadingAi) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text("AI 判分")
                }
            }
        }
    }
}

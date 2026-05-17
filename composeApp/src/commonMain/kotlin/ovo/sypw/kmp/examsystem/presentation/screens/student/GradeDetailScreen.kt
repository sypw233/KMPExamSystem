package ovo.sypw.kmp.examsystem.presentation.screens.student

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.presentation.components.management.ManagementPageHeader
import ovo.sypw.kmp.examsystem.presentation.components.management.ManagementPanel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import androidx.compose.foundation.layout.Arrangement
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionType
import ovo.sypw.kmp.examsystem.data.dto.SubjectiveGradeDetail
import ovo.sypw.kmp.examsystem.data.dto.questionType
import ovo.sypw.kmp.examsystem.presentation.viewmodel.GradeSubmissionViewModel
import ovo.sypw.kmp.examsystem.utils.QuestionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeDetailScreen(
    submissionId: Long,
    viewModel: GradeSubmissionViewModel = koinInject(),
    onBack: () -> Unit
) {
    val submission by viewModel.currentSubmission.collectAsState()
    val questions by viewModel.currentQuestions.collectAsState()
    val detailError by viewModel.detailError.collectAsState()
    val config = LocalResponsiveConfig.current
    val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED

    LaunchedEffect(submissionId) {
        viewModel.loadSubmissionDetail(submissionId)
    }

    val currentSubmission = submission
    if (currentSubmission == null) {
        val error = detailError
        if (isDesktop) {
            Box(modifier = Modifier.fillMaxSize().padding(config.screenPadding), contentAlignment = Alignment.Center) {
                if (error == null) {
                    CircularProgressIndicator()
                } else {
                    DetailErrorContent(error, onRetry = { viewModel.loadSubmissionDetail(submissionId) }, onBack = onBack)
                }
            }
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("答卷解析详情") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    if (error == null) {
                        CircularProgressIndicator()
                    } else {
                        DetailErrorContent(error, onRetry = { viewModel.loadSubmissionDetail(submissionId) }, onBack = onBack)
                    }
                }
            }
        }
        return
    }

    // 解析学生答案 (JSON 字符串 -> Map)
    val userAnswers: Map<String, String> = remember(currentSubmission.answers) {
        try {
            val jsonStr = currentSubmission.answers ?: return@remember emptyMap()
            if (jsonStr.isNotBlank()) Json.decodeFromString(jsonStr) else emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // 兼容新旧两种 submitDetail 结构：列表结构，以及后端按 questionId 分组的对象结构。
    val detailMap: Map<Long, SubjectiveGradeDetail> = remember(currentSubmission.submitDetail) {
        parseSubmitDetail(currentSubmission.submitDetail)
    }

    if (isDesktop) {
        // 桌面端布局
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(config.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ManagementPageHeader(
                title = "答卷解析详情",
                subtitle = "查看考试答卷的详细解析和得分情况"
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            }

            ManagementPanel(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Info
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Text("考试总得分: ${currentSubmission.totalScore ?: "批改中"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(questions.sortedBy { it.orderNum }, key = { it.questionId }) { eq ->
                            DetailQuestionItem(
                                examQuestion = eq,
                                studentAnswer = userAnswers[eq.questionId.toString()] ?: "",
                                gradeDetail = detailMap[eq.questionId]
                            )
                        }
                    }
                }
            }
        }
    } else {
        // 移动端布局
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("答卷解析详情") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Header Info
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Text("考试总得分: ${currentSubmission.totalScore ?: "批改中"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(questions.sortedBy { it.orderNum }, key = { it.questionId }) { eq ->
                        DetailQuestionItem(
                            examQuestion = eq,
                            studentAnswer = userAnswers[eq.questionId.toString()] ?: "",
                            gradeDetail = detailMap[eq.questionId]
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailErrorContent(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRetry) {
                Text("重试")
            }
            TextButton(onClick = onBack) {
                Text("返回")
            }
        }
    }
}

@Composable
private fun DetailQuestionItem(
    examQuestion: ExamQuestionResponse,
    studentAnswer: String,
    gradeDetail: SubjectiveGradeDetail?
) {
    val q = examQuestion.question ?: return

    // 客观题的对错判断
    val isObjective = q.questionType in listOf(QuestionType.SINGLE, QuestionType.MULTIPLE, QuestionType.TRUE_FALSE)
    val hasAnswered = studentAnswer.isNotBlank()

    // 多选题区分"全对""部分正确""全错"
    val isCorrect: Boolean
    val isPartial: Boolean
    if (!isObjective || !hasAnswered) {
        isCorrect = false
        isPartial = false
    } else if (q.questionType == QuestionType.MULTIPLE) {
        val correctSet = (q.answer ?: "").split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        val studentSet = studentAnswer.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        isCorrect = studentSet == correctSet
        isPartial = !isCorrect && studentSet.all { it in correctSet }
    } else {
        isCorrect = studentAnswer == q.answer
        isPartial = false
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 题型与编号
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                Text("题目 ${examQuestion.orderNum} [${QuestionUtils.questionTypeLabel(q.type)}]", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${examQuestion.score} 分", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(q.content, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // 学生回答
            val answerColor = when {
                !isObjective -> MaterialTheme.colorScheme.onSurfaceVariant
                !hasAnswered -> MaterialTheme.colorScheme.onSurfaceVariant
                isCorrect -> MaterialTheme.colorScheme.tertiary
                isPartial -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }

            Surface(
                color = answerColor.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("你的回答", style = MaterialTheme.typography.labelMedium, color = answerColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (hasAnswered) studentAnswer else "未作答", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 正确答案与解析
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("正确答案", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(q.answer ?: "略", style = MaterialTheme.typography.bodyMedium)

                    if (!q.analysis.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("解析: ${q.analysis}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // 本题得分与批语
            if (gradeDetail != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("本题得分: ${gradeDetail.score}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        if (!gradeDetail.comment.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("教师批语: ${gradeDetail.comment}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
    }
}

@Serializable
private data class SubmissionQuestionDetail(
    val score: Int? = null,
    val comment: String? = null,
    val answer: String? = null,
    val maxScore: Int? = null
)

private fun parseSubmitDetail(jsonStr: String?): Map<Long, SubjectiveGradeDetail> {
    if (jsonStr.isNullOrBlank()) return emptyMap()
    return try {
        Json.decodeFromString<List<SubjectiveGradeDetail>>(jsonStr).associateBy { it.questionId }
    } catch (_: Exception) {
        try {
            Json.decodeFromString<Map<String, SubmissionQuestionDetail>>(jsonStr)
                .mapNotNull { (questionIdText, detail) ->
                    val questionId = questionIdText.toLongOrNull() ?: return@mapNotNull null
                    val score = detail.score ?: return@mapNotNull null
                    questionId to SubjectiveGradeDetail(
                        questionId = questionId,
                        score = score,
                        comment = detail.comment
                    )
                }
                .toMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }
}

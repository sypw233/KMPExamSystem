package ovo.sypw.kmp.examsystem.presentation.screens.student

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.SubjectiveGradeDetail
import ovo.sypw.kmp.examsystem.presentation.viewmodel.GradeSubmissionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeDetailScreen(
    submissionId: Long,
    viewModel: GradeSubmissionViewModel = koinInject(),
    onBack: () -> Unit
) {
    val submission by viewModel.currentSubmission.collectAsState()
    val questions by viewModel.currentQuestions.collectAsState()

    LaunchedEffect(submissionId) {
        viewModel.loadSubmissionDetail(submissionId)
    }

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
        val currentSubmission = submission
        if (currentSubmission == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
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

        // 解析批改详情 (JSON 字符串 -> List)
        val submitDetailList: List<SubjectiveGradeDetail> = remember(currentSubmission.submitDetail) {
            try {
                val jsonStr = currentSubmission.submitDetail ?: return@remember emptyList()
                if (jsonStr.isNotBlank()) Json.decodeFromString(jsonStr) else emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
        val detailMap = submitDetailList.associateBy { it.questionId }

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

            LazyColumn(modifier = Modifier.fillMaxSize().then(if (LocalResponsiveConfig.current.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = 900.dp) else Modifier)) {
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

@Composable
private fun DetailQuestionItem(
    examQuestion: ExamQuestionResponse,
    studentAnswer: String,
    gradeDetail: SubjectiveGradeDetail?
) {
    val q = examQuestion.question ?: return
    
    // 客观题的对错判断: 对于非主观题，如果完全匹配即正确 (简化逻辑，多选可能有半对的情况)
    val isObjective = q.type != "short_answer"
    val isCorrect = isObjective && studentAnswer == q.answer
    val hasAnswered = studentAnswer.isNotBlank()

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 题型与编号
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                Text("题目 ${examQuestion.orderNum} [${q.type}]", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${examQuestion.score} 分", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(q.content, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // 学生回答
            val answerColor = if (isObjective) {
                if (!hasAnswered) MaterialTheme.colorScheme.onSurfaceVariant
                else if (isCorrect) androidx.compose.ui.graphics.Color(0xFF4CAF50)
                else MaterialTheme.colorScheme.error
            } else MaterialTheme.colorScheme.onSurfaceVariant

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

            // 主观题得分与批语
            if (!isObjective && gradeDetail != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("得分: ${gradeDetail.score}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
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

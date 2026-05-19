package ovo.sypw.kmp.examsystem.presentation.screens.teacher

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ovo.sypw.kmp.examsystem.data.dto.AiBatchGradingResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionType
import ovo.sypw.kmp.examsystem.data.dto.SubmissionResponse
import ovo.sypw.kmp.examsystem.data.dto.questionType
import ovo.sypw.kmp.examsystem.presentation.components.common.ActionEffect
import ovo.sypw.kmp.examsystem.presentation.components.common.LoadingContent
import ovo.sypw.kmp.examsystem.presentation.viewmodel.GradeActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.GradeSubmissionViewModel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.QuestionUtils
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

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
    val config = LocalResponsiveConfig.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scoreMap = remember { mutableStateMapOf<Long, String>() }
    val aiCommentMap = remember { mutableStateMapOf<Long, String>() }
    val aiCommentVisibleMap = remember { mutableStateMapOf<Long, Boolean>() }
    var batchAiLoading by remember { mutableStateOf(false) }

    LaunchedEffect(submissionId) {
        scoreMap.clear()
        aiCommentMap.clear()
        aiCommentVisibleMap.clear()
        viewModel.loadSubmissionDetail(submissionId)
    }

    val userAnswers: Map<String, String> = remember(submission?.answers) {
        parseAnswerMap(submission?.answers)
    }

    val gradeableQuestions = remember(questions) {
        questions.filter { it.question != null }
    }
    val subjectiveQuestions = remember(gradeableQuestions) {
        gradeableQuestions.filter { it.question?.questionType in setOf(QuestionType.FILL_BLANK, QuestionType.SHORT_ANSWER) }
    }
    val objectiveScore = remember(submission) {
        submission?.objectiveScore
            ?: ((submission?.totalScore ?: 0) - (submission?.subjectiveScore ?: 0)).coerceAtLeast(0)
    }

    LaunchedEffect(submission?.submitDetail, gradeableQuestions, userAnswers) {
        val snapshot = parseGradeDetailSnapshot(submission?.submitDetail)
        gradeableQuestions.forEach { examQuestion ->
            val question = examQuestion.question ?: return@forEach
            val questionId = examQuestion.questionId
            if (!scoreMap.containsKey(questionId)) {
                val existingScore = snapshot.questionScores[questionId]
                    ?: inferObjectiveScore(question, examQuestion.score, userAnswers[questionId.toString()])
                existingScore?.let { scoreMap[questionId] = it.toString() }
            }
            snapshot.aiComments[questionId]?.let { comment ->
                aiCommentMap[questionId] = comment
                aiCommentVisibleMap[questionId] = true
            }
        }
    }

    ActionEffect(
        actionState = viewModel.actionState.collectAsState(),
        snackbarHostState = snackbarHostState,
        isSuccess = { it is GradeActionState.Success },
        isError = { it is GradeActionState.Error },
        getMessage = {
            when (it) {
                is GradeActionState.Success -> it.message
                is GradeActionState.Error -> it.message
                else -> ""
            }
        },
        onConsumed = { viewModel.resetActionState() },
        onSuccess = { onBack() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("批改试卷 - ${submission?.userName.orEmpty()}") },
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
        val currentSubmission = submission
        if (currentSubmission == null) {
            LoadingContent(message = "正在加载答卷详情...")
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = config.screenPadding,
                end = config.screenPadding,
                top = 12.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                GradeSubmissionHeader(
                    submission = currentSubmission,
                    questionCount = gradeableQuestions.size,
                    subjectiveCount = subjectiveQuestions.size,
                    objectiveScore = objectiveScore,
                    batchAiLoading = batchAiLoading,
                    isSaving = actionState is GradeActionState.Loading,
                    onBatchAiGrade = {
                        if (subjectiveQuestions.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("当前试卷没有可 AI 评分的主观题") }
                            return@GradeSubmissionHeader
                        }
                        batchAiLoading = true
                        scope.launch {
                            viewModel.requestBatchAiGrade(submissionId)
                                .onSuccess { response ->
                                    applyBatchAiResult(response, scoreMap, aiCommentMap, aiCommentVisibleMap)
                                    snackbarHostState.showSnackbar("AI 已完成 ${response.gradedCount} 道题评分")
                                }
                                .onFailure {
                                    snackbarHostState.showSnackbar(it.message ?: "AI 批量评分失败")
                                }
                            batchAiLoading = false
                        }
                    },
                    onSave = {
                        val missingItems = gradeableQuestions.filter { scoreMap[it.questionId].isNullOrBlank() }
                        if (missingItems.isNotEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("请补全所有题目的得分后再保存") }
                            return@GradeSubmissionHeader
                        }

                        val invalidItems = gradeableQuestions.filter { examQuestion ->
                            val score = scoreMap[examQuestion.questionId]?.toIntOrNull()
                            score == null || score !in 0..examQuestion.score
                        }
                        if (invalidItems.isNotEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("存在超出分数范围的题目，请检查") }
                            return@GradeSubmissionHeader
                        }

                        val scoreMapData = gradeableQuestions.associate { examQuestion ->
                            examQuestion.questionId to (scoreMap[examQuestion.questionId]?.toIntOrNull() ?: 0)
                        }
                        viewModel.submitGrades(submissionId, scoreMapData)
                    }
                )
            }

            if (gradeableQuestions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("当前试卷暂无题目", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(gradeableQuestions, key = { it.questionId }) { examQuestion ->
                    GradeQuestionItem(
                        examQuestion = examQuestion,
                        studentAnswer = userAnswers[examQuestion.questionId.toString()].orEmpty(),
                        currentScore = scoreMap[examQuestion.questionId].orEmpty(),
                        currentAiComment = aiCommentMap[examQuestion.questionId].orEmpty(),
                        showAiComment = aiCommentVisibleMap[examQuestion.questionId] == true,
                        onScoreChange = { scoreMap[examQuestion.questionId] = it },
                        onAiCommentChange = { aiCommentMap[examQuestion.questionId] = it },
                        onRequestAiGrade = { callback ->
                            scope.launch {
                                val studentAnswer = userAnswers[examQuestion.questionId.toString()].orEmpty()
                                viewModel.requestAiGrade(examQuestion.questionId, studentAnswer, examQuestion.score)
                                    .onSuccess { aiRes ->
                                        scoreMap[examQuestion.questionId] = aiRes.suggestedScore.toString()
                                        aiCommentMap[examQuestion.questionId] =
                                            aiRes.explanation ?: "AI 已给出评分建议，请结合参考答案复核。"
                                        aiCommentVisibleMap[examQuestion.questionId] = true
                                    }
                                    .onFailure {
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

@Composable
private fun GradeSubmissionHeader(
    submission: SubmissionResponse,
    questionCount: Int,
    subjectiveCount: Int,
    objectiveScore: Int,
    batchAiLoading: Boolean,
    isSaving: Boolean,
    onBatchAiGrade: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = ResponsiveUtils.MaxWidths.STANDARD),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = submission.examTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "学生：${submission.userName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "客观题 $objectiveScore 分",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("共 $questionCount 题") },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp)) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("主观题 $subjectiveCount 题") },
                    leadingIcon = { Icon(Icons.Default.EditNote, null, modifier = Modifier.size(16.dp)) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBatchAiGrade,
                    enabled = !batchAiLoading && !isSaving
                ) {
                    if (batchAiLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI 批量评分")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSave,
                    enabled = !isSaving && !batchAiLoading
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("保存批改")
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
    currentAiComment: String,
    showAiComment: Boolean,
    onScoreChange: (String) -> Unit,
    onAiCommentChange: (String) -> Unit,
    onRequestAiGrade: (() -> Unit) -> Unit
) {
    val question = examQuestion.question ?: return
    val isSubjective = question.questionType in setOf(QuestionType.FILL_BLANK, QuestionType.SHORT_ANSWER)
    var isLoadingAi by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = ResponsiveUtils.MaxWidths.STANDARD),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "题目 ${examQuestion.orderNum.coerceAtLeast(examQuestion.sequence)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = QuestionUtils.questionTypeLabel(question.type),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "${examQuestion.score} 分",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(question.content, style = MaterialTheme.typography.bodyLarge)

            val options = remember(question.options) { QuestionUtils.parseOptionsJson(question.options).filter { it.isNotBlank() } }
            if (options.isNotEmpty() && question.questionType != QuestionType.FILL_BLANK && question.questionType != QuestionType.SHORT_ANSWER) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("题目选项", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        options.forEachIndexed { index, option ->
                            Text("${('A' + index)}. $option", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            AnswerBlock(
                title = "参考答案",
                content = question.answer?.takeIf { it.isNotBlank() } ?: "未设置参考答案",
                supporting = question.analysis?.takeIf { it.isNotBlank() }?.let { "解析：$it" },
                tonal = true
            )
            AnswerBlock(
                title = "学生答案",
                content = formatStudentAnswer(studentAnswer),
                supporting = if (isSubjective) null else "客观题支持人工复核修正得分",
                tonal = false
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentScore,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty()) {
                            onScoreChange(newValue)
                        } else {
                            val intVal = newValue.toIntOrNull()
                            if (intVal != null && intVal in 0..examQuestion.score) {
                                onScoreChange(newValue)
                            }
                        }
                    },
                    label = { Text("得分") },
                    supportingText = { Text("0-${examQuestion.score} 分") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(116.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isSubjective) {
                    OutlinedButton(
                        onClick = {
                            isLoadingAi = true
                            onRequestAiGrade { isLoadingAi = false }
                        },
                        enabled = !isLoadingAi
                    ) {
                        if (isLoadingAi) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI 判分")
                    }
                }
            }

            if (showAiComment) {
                OutlinedTextField(
                    value = currentAiComment,
                    onValueChange = onAiCommentChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("AI 批语") },
                    leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null) },
                    minLines = 3
                )
            }
        }
    }
}

@Composable
private fun AnswerBlock(
    title: String,
    content: String,
    supporting: String?,
    tonal: Boolean
) {
    Surface(
        color = if (tonal) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(content, style = MaterialTheme.typography.bodyMedium)
            if (!supporting.isNullOrBlank()) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class GradeDetailSnapshot(
    val questionScores: Map<Long, Int> = emptyMap(),
    val aiComments: Map<Long, String> = emptyMap()
)

private fun parseAnswerMap(jsonStr: String?): Map<String, String> {
    if (jsonStr.isNullOrBlank()) return emptyMap()
    return runCatching { Json.decodeFromString<Map<String, String>>(jsonStr) }.getOrDefault(emptyMap())
}

private fun parseGradeDetailSnapshot(jsonStr: String?): GradeDetailSnapshot {
    if (jsonStr.isNullOrBlank()) return GradeDetailSnapshot()
    return runCatching {
        when (val root = Json.parseToJsonElement(jsonStr)) {
            is JsonObject -> parseGradeDetailObject(root)
            is JsonArray -> parseGradeDetailArray(root)
            else -> GradeDetailSnapshot()
        }
    }.getOrDefault(GradeDetailSnapshot())
}

private fun parseGradeDetailObject(root: JsonObject): GradeDetailSnapshot {
    val scores = root["questionScores"]?.jsonObject?.mapNotNull { (key, value) ->
        val questionId = key.toLongOrNull() ?: return@mapNotNull null
        val score = value.jsonPrimitive.intOrNull ?: return@mapNotNull null
        questionId to score
    }?.toMap().orEmpty()

    val aiComments = root["aiDetails"]?.jsonArray?.mapNotNull { detail ->
        val detailObject = detail as? JsonObject ?: return@mapNotNull null
        val questionId = detailObject["questionId"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: detailObject["questionId"]?.jsonPrimitive?.intOrNull?.toLong()
            ?: return@mapNotNull null
        val explanation = detailObject["explanation"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: return@mapNotNull null
        questionId to explanation
    }?.toMap().orEmpty()

    return GradeDetailSnapshot(questionScores = scores, aiComments = aiComments)
}

private fun parseGradeDetailArray(root: JsonArray): GradeDetailSnapshot {
    val scores = mutableMapOf<Long, Int>()
    val comments = mutableMapOf<Long, String>()
    root.forEach { item ->
        val obj = item as? JsonObject ?: return@forEach
        val questionId = obj["questionId"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: obj["questionId"]?.jsonPrimitive?.intOrNull?.toLong()
            ?: return@forEach
        obj["score"]?.jsonPrimitive?.intOrNull?.let { scores[questionId] = it }
        obj["comment"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }?.let { comments[questionId] = it }
    }
    return GradeDetailSnapshot(scores, comments)
}

private fun applyBatchAiResult(
    response: AiBatchGradingResponse,
    scoreMap: MutableMap<Long, String>,
    aiCommentMap: MutableMap<Long, String>,
    aiCommentVisibleMap: MutableMap<Long, Boolean>
) {
    response.details.forEach { detail ->
        scoreMap[detail.questionId] = detail.suggestedScore.toString()
        aiCommentMap[detail.questionId] = detail.explanation
        aiCommentVisibleMap[detail.questionId] = true
    }
}

private fun inferObjectiveScore(question: QuestionResponse, maxScore: Int, studentAnswer: String?): Int? {
    if (question.questionType !in setOf(QuestionType.SINGLE, QuestionType.MULTIPLE, QuestionType.TRUE_FALSE)) return null
    val reference = question.answer?.takeIf { it.isNotBlank() } ?: return null
    val answer = studentAnswer?.takeIf { it.isNotBlank() } ?: return 0
    return if (normalizeAnswerForCompare(reference) == normalizeAnswerForCompare(answer)) maxScore else 0
}

private fun normalizeAnswerForCompare(value: String): String =
    value.split(",")
        .map { it.trim().lowercase() }
        .filter { it.isNotBlank() }
        .sorted()
        .joinToString(",")

private fun formatStudentAnswer(value: String): String =
    value.takeIf { it.isNotBlank() } ?: "未作答"

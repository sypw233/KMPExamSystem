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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.presentation.components.common.adaptiveDialogModifier
import ovo.sypw.kmp.examsystem.presentation.components.common.adaptiveDialogProperties
import ovo.sypw.kmp.examsystem.presentation.components.common.LoadingContent
import ovo.sypw.kmp.examsystem.data.storage.LocalStorage
import ovo.sypw.kmp.examsystem.presentation.navigation.ActiveExamSession
import ovo.sypw.kmp.examsystem.presentation.navigation.NavigationManager
import ovo.sypw.kmp.examsystem.presentation.settings.AppSettingsStore
import ovo.sypw.kmp.examsystem.presentation.settings.ExamDisplayMode
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamTakingUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamTakingViewModel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@Composable
fun ExamTakingScreen(
    examId: Long,
    navigationManager: NavigationManager,
    onExitExam: () -> Unit
) {
    val viewModel: ExamTakingViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val answers by viewModel.answers.collectAsState()
    val submitErrorMessage by viewModel.submitErrorMessage.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val localStorage: LocalStorage = koinInject()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(examId) {
        viewModel.enterExam(examId)
    }

    // 监听提交错误，用 Snackbar 展示（不覆盖 uiState，保留答题状态）
    LaunchedEffect(submitErrorMessage) {
        val msg = submitErrorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSubmitError()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ExamTakingUiState.Loading, ExamTakingUiState.Idle -> {
                LoadingContent(message = "正在加载考试...")
            }
            is ExamTakingUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        // 若 submission 已创建（currentExamId 有效），提供重试按钮
                        Button(onClick = { viewModel.enterExam(examId) }) {
                            Text("重试")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = {
                            navigationManager.exitExamMode()
                            viewModel.reset()
                            onExitExam()
                        }) {
                            Text("退出考试")
                        }
                    }
                }
            }
            is ExamTakingUiState.Submitted -> {
                LaunchedEffect(state.submission.id) {
                    localStorage.remove(ActiveExamSession.ACTIVE_EXAM_ID)
                    localStorage.remove(ActiveExamSession.FOCUS_LOST_AT)
                    if (state.exitAfterSubmit) {
                        navigationManager.exitExamMode()
                        viewModel.reset()
                        onExitExam()
                    }
                }
                // 【修复 BUG-06】needsGrading 改为检查 submission.status < 2（未批改）
                // 而非依赖 totalScore == 0 && subjectiveScore == null（客观 0 分会误判）
                val needsGrading = state.submission.status < 2 &&
                    state.submission.subjectiveScore == null
                ExamResultSummary(
                    totalScore = state.submission.totalScore,
                    objectiveScore = state.submission.objectiveScore,
                    needsGrading = needsGrading,
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
                    isSubmitting = isSubmitting,
                    onAnswerChange = { qId, ans -> viewModel.updateAnswer(qId, ans) },
                    onToggleMultiple = { qId, opt -> viewModel.toggleMultipleChoice(qId, opt) },
                    onRecordProctoringEvent = { event, desc -> viewModel.recordProctoringEvent(event, desc) },
                    onSubmit = { exitAfterSubmit -> viewModel.submitExam(exitAfterSubmit) },
                    localStorage = localStorage,
                    onExit = {
                        navigationManager.exitExamMode()
                        viewModel.reset()
                        onExitExam()
                    }
                )
            }
        }

        // 全局 Snackbar（提交错误等）
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalTime::class)
@Composable
private fun ExamContent(
    exam: ExamTakingUiState.Ready,
    answers: Map<Long, String>,
    isSubmitting: Boolean,
    onAnswerChange: (Long, String) -> Unit,
    onToggleMultiple: (Long, String) -> Unit,
    onRecordProctoringEvent: (String, String?) -> Unit,
    onSubmit: (Boolean) -> Unit,
    localStorage: LocalStorage,
    onExit: () -> Unit
) {
    val hasDuration = (exam.exam.duration ?: 0) > 0
    // 【修复 BUG-04】使用精确的 mutableIntState 配合 TimeSource 避免 delay 累计偏差
    var remainingSeconds by remember { mutableIntStateOf(if (hasDuration) (exam.exam.duration ?: 0) * 60 else -1) }
    var showExitDialog by remember { mutableStateOf(false) }
    var hasPendingFocusWarning by remember { mutableStateOf(false) }
    var focusViolationCount by remember { mutableStateOf(0) }
    var showViolationWarning by remember { mutableStateOf(false) }
    var violationWarningMessage by remember { mutableStateOf("") }
    var currentQuestionIndex by remember(exam.questions.size) { mutableIntStateOf(0) }
    val appSettings by AppSettingsStore.settings.collectAsState()
    val switchLimit = exam.exam.maxSwitchCount?.takeIf { it > 0 } ?: 3
    val windowFocused = LocalWindowInfo.current.isWindowFocused
    val config = LocalResponsiveConfig.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 返回键处理：弹出交卷确认对话框
    BackHandler {
        showExitDialog = true
    }

    // 【修复 BUG-04】精确倒计时：记录起始 TimeMark，每秒对齐到整秒
    LaunchedEffect(Unit) {
        if (hasDuration) {
            val startMark = TimeSource.Monotonic.markNow()
            val initialSeconds = remainingSeconds
            while (remainingSeconds > 0) {
                delay(1000)
                val elapsed = startMark.elapsedNow().inWholeSeconds
                remainingSeconds = maxOf(0, initialSeconds - elapsed.toInt())
            }
            onSubmit(true)
        }
    }

    LaunchedEffect(windowFocused) {
        if (!windowFocused) {
            if (showExitDialog || showViolationWarning) return@LaunchedEffect
            if (!hasPendingFocusWarning) {
                focusViolationCount += 1
                hasPendingFocusWarning = true
                localStorage.saveLong(ActiveExamSession.FOCUS_LOST_AT, focusViolationCount.toLong())
                onRecordProctoringEvent(
                    "blur",
                    "考试窗口失焦，第 $focusViolationCount 次"
                )
                violationWarningMessage = "检测到离开考试界面或应用失焦，本次行为已上报服务器。当前记录 $focusViolationCount 次。"
                if (focusViolationCount >= switchLimit) {
                    onSubmit(false)
                }
            }
            return@LaunchedEffect
        }
        val persistedViolation = localStorage.getLong(ActiveExamSession.FOCUS_LOST_AT, 0L)
        if (!hasPendingFocusWarning && persistedViolation <= 0L) return@LaunchedEffect
        hasPendingFocusWarning = false
        localStorage.remove(ActiveExamSession.FOCUS_LOST_AT)
        if (focusViolationCount < switchLimit) {
            showViolationWarning = true
        }
    }

    // 【UX-04】三档颜色：正常→橙色（<30min）→红色（<10min）
    val timerColor: Color = when {
        remainingSeconds < 600 -> MaterialTheme.colorScheme.error
        remainingSeconds < 1800 -> MaterialTheme.colorScheme.tertiary  // 橙色警告
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(exam.exam.title, style = MaterialTheme.typography.titleMedium)
                        if (hasDuration) {
                            Text(
                                text = "剩余时间: ${formatExamTime(remainingSeconds)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = timerColor
                            )
                        }
                        if (exam.exam.strictMode) {
                            Text(
                                text = "监考: 切屏 $focusViolationCount 次",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (focusViolationCount >= switchLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("交卷")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.then(
                    if (LocalResponsiveConfig.current.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = ResponsiveUtils.MaxWidths.EXAM_TAKING) else Modifier
                ).fillMaxSize(),
                contentPadding = PaddingValues(horizontal = config.screenPadding, vertical = config.contentPadding),
                verticalArrangement = Arrangement.spacedBy(config.verticalSpacing)
            ) {
                item {
                    QuestionJumpPanel(
                        questions = exam.questions,
                        selectedIndex = if (appSettings.examDisplayMode == ExamDisplayMode.SINGLE_QUESTION) currentQuestionIndex else null,
                        onQuestionSelected = { questionIndex ->
                            if (appSettings.examDisplayMode == ExamDisplayMode.SINGLE_QUESTION) {
                                currentQuestionIndex = questionIndex
                            } else {
                                scope.launch {
                                    listState.animateScrollToItem(questionIndex + 1)
                                }
                            }
                        }
                    )
                }

                if (appSettings.examDisplayMode == ExamDisplayMode.SINGLE_QUESTION && exam.questions.isNotEmpty()) {
                    item {
                        val index = currentQuestionIndex.coerceIn(0, exam.questions.lastIndex)
                        val examQuestion = exam.questions[index]
                        val question = examQuestion.question
                        if (question != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(config.verticalSpacing)) {
                                Text(
                                    text = "第 ${index + 1} / ${exam.questions.size} 题",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                QuestionItem(
                                    number = index + 1,
                                    examQuestion = examQuestion,
                                    currentAnswer = answers[question.id] ?: "",
                                    onAnswerChange = { answer -> onAnswerChange(question.id, answer) },
                                    onToggleMultiple = { option -> onToggleMultiple(question.id, option) }
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(
                                        onClick = { currentQuestionIndex = (currentQuestionIndex - 1).coerceAtLeast(0) },
                                        enabled = currentQuestionIndex > 0
                                    ) {
                                        Text("上一题")
                                    }
                                    Button(
                                        onClick = { currentQuestionIndex = (currentQuestionIndex + 1).coerceAtMost(exam.questions.lastIndex) },
                                        enabled = currentQuestionIndex < exam.questions.lastIndex
                                    ) {
                                        Text("下一题")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    itemsIndexed(exam.questions, key = { _, eq -> eq.questionId }) { index, examQuestion ->
                        val question = examQuestion.question ?: return@itemsIndexed
                        QuestionItem(
                            number = index + 1,
                            examQuestion = examQuestion,
                            currentAnswer = answers[question.id] ?: "",
                            onAnswerChange = { answer -> onAnswerChange(question.id, answer) },
                            onToggleMultiple = { option -> onToggleMultiple(question.id, option) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    if (showExitDialog) {
        val questionIds = exam.questions.map { it.question?.id ?: it.questionId }
        val answered = questionIds.count { questionId -> !answers[questionId].isNullOrBlank() }
        val total = questionIds.size
        val unanswered = total - answered
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            modifier = adaptiveDialogModifier(),
            properties = adaptiveDialogProperties(),
            title = { Text("确认交卷?") },
            text = {
                Column {
                    Text("已答 $answered / $total 题。交卷后不可再修改。")
                    if (unanswered > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "还有 $unanswered 道题未作答。",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onSubmit(true)
                    },
                    colors = if (unanswered > 0)
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    else ButtonDefaults.buttonColors()
                ) {
                    Text("交卷")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("继续答题")
                }
            }
        )
    }

    if (showViolationWarning) {
        AlertDialog(
            onDismissRequest = { showViolationWarning = false },
            modifier = adaptiveDialogModifier(),
            properties = adaptiveDialogProperties(),
            title = { Text("监考提醒") },
            text = { Text(violationWarningMessage) },
            confirmButton = {
                Button(onClick = { showViolationWarning = false }) {
                    Text("继续考试")
                }
            }
        )
    }
}

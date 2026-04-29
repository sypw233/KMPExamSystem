package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ovo.sypw.kmp.examsystem.presentation.navigation.NavigationManager
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamTakingUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamTakingViewModel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import org.koin.compose.koinInject
import kotlin.time.TimeMark
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("正在加载考试...")
                    }
                }
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
                    onSubmit = { viewModel.submitExam() },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamContent(
    exam: ExamTakingUiState.Ready,
    answers: Map<Long, String>,
    isSubmitting: Boolean,
    onAnswerChange: (Long, String) -> Unit,
    onToggleMultiple: (Long, String) -> Unit,
    onRecordProctoringEvent: (String, String?) -> Unit,
    onSubmit: () -> Unit,
    onExit: () -> Unit
) {
    val hasDuration = (exam.exam.duration ?: 0) > 0
    // 【修复 BUG-04】使用精确的 mutableIntState 配合 TimeSource 避免 delay 累计偏差
    var remainingSeconds by remember { mutableIntStateOf(if (hasDuration) (exam.exam.duration ?: 0) * 60 else -1) }
    var showExitDialog by remember { mutableStateOf(false) }
    var lastLostFocusMark by remember { mutableStateOf<TimeMark?>(null) }
    var focusViolationCount by remember { mutableStateOf(0) }
    var showForceSubmitDialog by remember { mutableStateOf(false) }
    val strictThreshold = exam.exam.maxSwitchCount?.takeIf { it > 0 } ?: 3
    val windowFocused = LocalWindowInfo.current.isWindowFocused
    val config = LocalResponsiveConfig.current

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
            onSubmit()
        }
    }

    LaunchedEffect(windowFocused) {
        if (!windowFocused) {
            lastLostFocusMark = TimeSource.Monotonic.markNow()
            return@LaunchedEffect
        }
        val lostMark = lastLostFocusMark ?: return@LaunchedEffect
        val lostMs = lostMark.elapsedNow().inWholeMilliseconds
        lastLostFocusMark = null
        if (lostMs < 3000) return@LaunchedEffect

        focusViolationCount += 1
        onRecordProctoringEvent(
            "WINDOW_FOCUS_LOST",
            "lost_focus_ms=$lostMs,count=$focusViolationCount"
        )

        if (exam.exam.strictMode && focusViolationCount >= strictThreshold) {
            showForceSubmitDialog = true
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
                                text = "监考: 切屏 $focusViolationCount/$strictThreshold 次",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (focusViolationCount >= strictThreshold) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
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
                modifier = Modifier.fillMaxSize().then(
                    if (LocalResponsiveConfig.current.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = 800.dp) else Modifier
                ),
                contentPadding = PaddingValues(horizontal = config.screenPadding, vertical = config.contentPadding),
                verticalArrangement = Arrangement.spacedBy(config.verticalSpacing)
            ) {
                itemsIndexed(exam.questions) { index, examQuestion ->
                    val question = examQuestion.question ?: return@itemsIndexed
                    QuestionItem(
                        number = index + 1,
                        examQuestion = examQuestion,
                        currentAnswer = answers[question.id] ?: "",
                        onAnswerChange = { answer -> onAnswerChange(question.id, answer) },
                        onToggleMultiple = { option -> onToggleMultiple(question.id, option) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    // 【UX-01】交卷确认弹窗：当未答题数较多时显示明显警告
    if (showExitDialog) {
        val answered = answers.size
        val total = exam.questions.size
        val unanswered = total - answered
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("确认交卷?") },
            text = {
                Column {
                    Text("已答 $answered / $total 题。交卷后不可再修改。")
                    if (unanswered > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ 还有 $unanswered 道题未作答！",
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
                        onSubmit()
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

    if (showForceSubmitDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("监考警告") },
            text = { Text("检测到多次切屏/失去焦点行为，系统将自动提交试卷。") },
            confirmButton = {
                Button(onClick = {
                    showForceSubmitDialog = false
                    onSubmit()
                }) {
                    Text("立即交卷")
                }
            },
            dismissButton = {}
        )
    }
}

package ovo.sypw.kmp.examsystem.presentation.screens.teacher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.presentation.components.common.ActionEffect
import ovo.sypw.kmp.examsystem.presentation.components.common.ErrorContent
import ovo.sypw.kmp.examsystem.presentation.components.common.LoadingContent
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamComposeUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamComposeViewModel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.RandomComposeState
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.QuestionUtils
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamComposeScreen(
    examId: Long,
    courseId: Long,
    autoOpenRandomCompose: Boolean = false,
    onBack: () -> Unit
) {
    val viewModel: ExamComposeViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val randomComposeState by viewModel.randomComposeState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(examId, courseId) {
        viewModel.loadComposeData(examId, courseId)
    }

    LaunchedEffect(autoOpenRandomCompose, uiState) {
        if (autoOpenRandomCompose && uiState is ExamComposeUiState.Success) {
            viewModel.openRandomComposeConfig()
        }
    }

    ActionEffect(
        actionState = viewModel.actionState.collectAsState(),
        snackbarHostState = snackbarHostState,
        isSuccess = { it is ExamActionState.Success },
        isError = { it is ExamActionState.Error },
        getMessage = { when (it) { is ExamActionState.Success -> it.message; is ExamActionState.Error -> it.message; else -> "" } },
        onConsumed = { viewModel.resetActionState() }
    )

    // 只在成功时关闭组卷弹窗
    LaunchedEffect(randomComposeState) {
        when (val state = randomComposeState) {
            is RandomComposeState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetRandomComposeState()
            }
            else -> Unit
        }
    }

    if (randomComposeState is RandomComposeState.Configuring) {
        val config = randomComposeState as RandomComposeState.Configuring
        RandomComposeDialog(
            config = config,
            targetScore = (uiState as? ExamComposeUiState.Success)?.exam?.totalScore,
            onConfirm = { bankId, expectedTotalScore, sections, shuffle, lenient ->
                viewModel.composeRandomExam(bankId, expectedTotalScore, sections, shuffle, lenient)
            },
            onDismiss = { viewModel.resetRandomComposeState() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选题组卷") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState is ExamComposeUiState.Success) {
                        TextButton(onClick = { viewModel.openRandomComposeConfig() }) {
                            Icon(Icons.Default.AutoFixHigh, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("智能组卷")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            when (val state = uiState) {
                is ExamComposeUiState.Loading -> {
                    LoadingContent(message = "加载组卷数据...")
                }
                is ExamComposeUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.loadComposeData(examId, courseId) }
                    )
                }
                is ExamComposeUiState.Success -> {
                    val screenConfig = LocalResponsiveConfig.current
                    val isDesktop = screenConfig.screenSize == ResponsiveUtils.ScreenSize.EXPANDED

                    if (isDesktop) {
                        // 桌面端: 左右双栏 (已选题目 | 题库选题)
                        DesktopComposeLayout(
                            state = state,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // 移动端: Tab 切换
                        MobileComposeLayout(
                            state = state,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

/**
 * 桌面端双栏布局: 左侧已选题目, 右侧题库选题
 */
@Composable
private fun DesktopComposeLayout(
    state: ExamComposeUiState.Success,
    viewModel: ExamComposeViewModel,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        // 左侧: 考试信息 + 已选题目
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp)
        ) {
            ExamScoreHeader(state)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "已选题目 (${state.examQuestions.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.examQuestions.isEmpty()) {
                    item {
                        Text(
                            "尚未选择题目, 请从右侧题库中添加",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(state.examQuestions, key = { it.questionId }) { examQuestion ->
                        SelectedQuestionCard(
                            examQuestion = examQuestion,
                            onRemove = { viewModel.removeQuestionFromExam(examQuestion.questionId) }
                        )
                    }
                }
            }
        }

        // 分隔线
        HorizontalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))

        // 右侧: 题库选题
        Column(
            modifier = Modifier.weight(1.2f).fillMaxHeight().padding(16.dp)
        ) {
            Text(
                "题库选题",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            QuestionBankPanel(
                allQuestions = state.courseQuestions,
                selectedQuestionIds = state.examQuestions.map { it.questionId }.toSet(),
                onAdd = { questionId -> viewModel.addQuestionToExam(questionId, 5) },
                onRemove = { questionId -> viewModel.removeQuestionFromExam(questionId) }
            )
        }
    }
}

/**
 * 移动端 Tab 布局
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MobileComposeLayout(
    state: ExamComposeUiState.Success,
    viewModel: ExamComposeViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier) {
        ExamScoreHeader(state)

        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("已选 (${state.examQuestions.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("题库选题") }
            )
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.examQuestions.isEmpty()) {
                        item {
                            Text(
                                "尚未选择题目, 请切换到题库选题",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(state.examQuestions, key = { it.questionId }) { examQuestion ->
                            SelectedQuestionCard(
                                examQuestion = examQuestion,
                                onRemove = { viewModel.removeQuestionFromExam(examQuestion.questionId) }
                            )
                        }
                    }
                }
            }
            1 -> {
                QuestionBankPanel(
                    allQuestions = state.courseQuestions,
                    selectedQuestionIds = state.examQuestions.map { it.questionId }.toSet(),
                    onAdd = { questionId -> viewModel.addQuestionToExam(questionId, 5) },
                    onRemove = { questionId -> viewModel.removeQuestionFromExam(questionId) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * 考试分数概览头部
 */
@Composable
private fun ExamScoreHeader(state: ExamComposeUiState.Success) {
    val currentScore = state.examQuestions.sumOf { it.score }
    val targetScore = state.exam.totalScore
    val isScoreMatched = currentScore == targetScore

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = state.exam.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "已选 ${state.examQuestions.size} 题",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currentScore / $targetScore 分",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (currentScore > targetScore) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (isScoreMatched) "分数已达标" else "注意分数匹配",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isScoreMatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 已选题目卡片 (带移除按钮)
 */
@Composable
private fun SelectedQuestionCard(
    examQuestion: ExamQuestionResponse,
    onRemove: () -> Unit
) {
    val question = examQuestion.question ?: return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "[${QuestionUtils.questionTypeLabel(question.type)}]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${examQuestion.score} 分",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    question.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onRemove) {
                Text("移除", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/**
 * 题库选题面板 (带搜索和筛选)
 */
@Composable
private fun QuestionBankPanel(
    allQuestions: List<QuestionResponse>,
    selectedQuestionIds: Set<Long>,
    onAdd: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchKeyword by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf<String?>(null) }
    var filtersExpanded by remember { mutableStateOf(false) }

    val filteredQuestions by remember(allQuestions, searchKeyword, filterType) {
        derivedStateOf {
            allQuestions.filter { q ->
                val matchSearch = searchKeyword.isBlank() || q.content.contains(searchKeyword, ignoreCase = true)
                val matchType = filterType == null || q.type == filterType
                matchSearch && matchType
            }
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchKeyword,
                onValueChange = { searchKeyword = it },
                modifier = Modifier.weight(1f).height(48.dp),
                placeholder = { Text("搜索题目内容...", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall
            )
            OutlinedButton(
                onClick = { filtersExpanded = !filtersExpanded },
                modifier = Modifier.height(48.dp),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (filtersExpanded) "收起" else "筛选", maxLines = 1)
            }
        }

        AnimatedVisibility(
            visible = filtersExpanded,
            enter = fadeIn() + expandVertically(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp)
            ) {
                FilterChip(
                    selected = filterType == null,
                    onClick = { filterType = null },
                    label = { Text("全部") }
                )
                QuestionUtils.questionTypeOptions.forEach { (value, label) ->
                    FilterChip(
                        selected = filterType == value,
                        onClick = { filterType = if (filterType == value) null else value },
                        label = { Text(label) }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "共 ${filteredQuestions.size} 题, 已选 ${selectedQuestionIds.size} 题",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 题目列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredQuestions.isEmpty()) {
                item {
                    Text(
                        "没有匹配的题目",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(filteredQuestions, key = { it.id }) { question ->
                    val isSelected = question.id in selectedQuestionIds
                    ComposeQuestionCard(
                        question = question,
                        isSelected = isSelected,
                        onToggle = { selected ->
                            if (selected) onAdd(question.id) else onRemove(question.id)
                        }
                    )
                }
            }
        }
    }
}

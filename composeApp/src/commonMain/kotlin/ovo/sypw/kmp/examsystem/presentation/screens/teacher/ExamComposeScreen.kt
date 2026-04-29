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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamComposeUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamComposeViewModel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.RandomComposeState
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
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

    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is ExamActionState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            is ExamActionState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(randomComposeState) {
        when (val state = randomComposeState) {
            is RandomComposeState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetRandomComposeState()
            }
            is RandomComposeState.Error -> {
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ExamComposeUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadComposeData(examId, courseId) }) { Text("重试") }
                        }
                    }
                }
                is ExamComposeUiState.Success -> {
                    val currentScore = state.examQuestions.sumOf { it.score }
                    val targetScore = state.exam.totalScore
                    val isScoreMatched = currentScore == targetScore
                    val screenConfig = LocalResponsiveConfig.current

                    Column(
                        modifier = Modifier.then(
                            if (screenConfig.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = ResponsiveUtils.MaxWidths.EXAM_COMPOSE) else Modifier
                        ).fillMaxSize()
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer
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

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (state.courseQuestions.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        Text("当前课程没有题目，请先在题目管理中添加", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            } else {
                                items(state.courseQuestions, key = { it.id }) { question ->
                                    val isSelected = state.examQuestions.any { it.questionId == question.id }
                                    ComposeQuestionCard(
                                        question = question,
                                        isSelected = isSelected,
                                        onToggle = { selected ->
                                            if (selected) {
                                                viewModel.addQuestionToExam(question.id, question.score)
                                            } else {
                                                viewModel.removeQuestionFromExam(question.id)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

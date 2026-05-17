package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.presentation.components.common.EmptyState
import ovo.sypw.kmp.examsystem.presentation.components.common.ErrorContent
import ovo.sypw.kmp.examsystem.presentation.components.common.LoadingContent
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamListUiState
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveLayoutConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveScrollableGrid

@Composable
internal fun DesktopExamLayout(
    selectedTab: Int,
    tabs: List<String>,
    onTabChange: (Int) -> Unit,
    notStartedState: ExamListUiState,
    endedState: ExamListUiState,
    selectedExam: ExamResponse?,
    onSelectExam: (ExamResponse?) -> Unit,
    onStartExam: (Long) -> Unit,
    onRetry: () -> Unit,
    config: ResponsiveLayoutConfig
) {
    val currentState = if (selectedTab == 0) notStartedState else endedState
    val showStartButton = selectedTab == 0

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = config.screenPadding, vertical = config.contentPadding)
    ) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { onTabChange(index) },
                        text = { Text(title) }
                    )
                }
            }
            ExamList(
                state = currentState,
                showScore = !showStartButton,
                showStartButton = showStartButton,
                isDesktop = true,
                selectedExamId = selectedExam?.id,
                onSelectExam = onSelectExam,
                onStartExam = onStartExam,
                onRetry = onRetry
            )
        }

        Spacer(modifier = Modifier.width(config.screenPadding))

        Box(modifier = Modifier.weight(0.8f).fillMaxHeight()) {
            if (selectedExam != null) {
                ExamPreviewCard(
                    exam = selectedExam,
                    showStartButton = showStartButton,
                    onStartExam = { onStartExam(selectedExam.id) }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "点击左侧考试查看详情",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
internal fun ExamList(
    state: ExamListUiState,
    showScore: Boolean,
    showStartButton: Boolean,
    isDesktop: Boolean,
    selectedExamId: Long? = null,
    onSelectExam: ((ExamResponse?) -> Unit)? = null,
    onStartExam: (Long) -> Unit,
    onRetry: () -> Unit
) {
    val config = LocalResponsiveConfig.current
    when (state) {
        is ExamListUiState.Loading -> {
            LoadingContent(message = "加载考试列表...")
        }
        is ExamListUiState.Error -> {
            ErrorContent(message = state.message, onRetry = onRetry)
        }
        is ExamListUiState.Success -> {
            if (state.exams.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Quiz,
                    title = "暂无考试",
                    subtitle = "还没有可参加的考试, 请稍后再来"
                )
            } else if (isDesktop) {
                ResponsiveScrollableGrid(
                    items = state.exams,
                    key = { it.id },
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = config.verticalSpacing),
                    verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
                    horizontalArrangement = Arrangement.spacedBy(config.verticalSpacing)
                ) { exam ->
                    ExamCard(
                        exam = exam,
                        showScore = showScore,
                        showStartButton = false,
                        isSelected = exam.id == selectedExamId,
                        onClick = { onSelectExam?.invoke(exam) }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(config.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(config.verticalSpacing)
                ) {
                    items(state.exams, key = { it.id }) { exam ->
                        ExamCard(
                            exam = exam,
                            showScore = showScore,
                            showStartButton = showStartButton,
                            onClick = { onStartExam(exam.id) }
                        )
                    }
                }
            }
        }
    }
}

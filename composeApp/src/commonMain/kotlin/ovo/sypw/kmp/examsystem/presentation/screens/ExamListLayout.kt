package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamListUiState
import ovo.sypw.kmp.examsystem.utils.ResponsiveLayoutConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveLazyVerticalGrid

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
    when (state) {
        is ExamListUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ExamListUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) { Text("重试") }
                }
            }
        }
        is ExamListUiState.Success -> {
            if (state.exams.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "暂无考试",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (isDesktop) {
                ResponsiveLazyVerticalGrid(
                    items = state.exams,
                    key = { it.id },
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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

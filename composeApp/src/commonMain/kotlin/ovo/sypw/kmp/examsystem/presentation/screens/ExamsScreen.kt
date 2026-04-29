package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.presentation.navigation.NavigationManager
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamViewModel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import org.koin.compose.koinInject

/**
 * 考试列表界面
 * Tab 0: 可参加的考试（已发布）
 * Tab 1: 已结束的考试
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(
    navigationManager: NavigationManager,
    onStartExam: (Long) -> Unit = {}
) {
    val examViewModel: ExamViewModel = koinInject()
    val notStartedState by examViewModel.notStartedExams.collectAsState()
    val endedState by examViewModel.endedExams.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("可参加", "已结束")
    val config = LocalResponsiveConfig.current
    val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED
    var selectedExam by remember { mutableStateOf<ExamResponse?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的考试") },
                actions = {
                    IconButton(onClick = {
                        if (selectedTab == 0) examViewModel.loadPublishedExams()
                        else examViewModel.loadEndedExams()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isDesktop) {
                DesktopExamLayout(
                    selectedTab = selectedTab,
                    tabs = tabs,
                    onTabChange = {
                        selectedTab = it
                        selectedExam = null
                    },
                    notStartedState = notStartedState,
                    endedState = endedState,
                    selectedExam = selectedExam,
                    onSelectExam = { selectedExam = it },
                    onStartExam = { examId ->
                        if (navigationManager.enterExamMode(examId)) {
                            onStartExam(examId)
                        }
                    },
                    onRetry = {
                        if (selectedTab == 0) examViewModel.loadPublishedExams()
                        else examViewModel.loadEndedExams()
                    },
                    config = config
                )
            } else {
                Column(modifier = Modifier.fillMaxSize().then(Modifier.widthIn(max = ResponsiveUtils.MaxWidths.NARROW))) {
                    PrimaryTabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    AnimatedContent(targetState = selectedTab) { tab ->
                        when (tab) {
                            0 -> ExamList(
                                state = notStartedState,
                                showScore = false,
                                showStartButton = true,
                                isDesktop = false,
                                onStartExam = { examId ->
                                    if (navigationManager.enterExamMode(examId)) {
                                        onStartExam(examId)
                                    }
                                },
                                onRetry = { examViewModel.loadPublishedExams() }
                            )
                            else -> ExamList(
                                state = endedState,
                                showScore = true,
                                showStartButton = false,
                                isDesktop = false,
                                onStartExam = {},
                                onRetry = { examViewModel.loadEndedExams() }
                            )
                        }
                    }
                }
            }
        }
    }
}

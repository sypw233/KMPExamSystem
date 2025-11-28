package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.presentation.navigation.NavigationManager

/**
 * 我的考试列表界面
 * 显示未开始的考试、进行中的考试和已完成的考试
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(
    navigationManager: NavigationManager
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("未开始", "进行中", "已完成")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的考试") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 标签页选择器
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // 考试列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // 未开始的考试
                        items(getDemoNotStartedExams()) { exam ->
                            ExamListItem(
                                exam = exam,
                                showStartButton = false,
                                onStartExam = { }
                            )
                        }
                    }
                    1 -> {
                        // 进行中的考试
                        items(getDemoOngoingExams()) { exam ->
                            ExamListItem(
                                exam = exam,
                                showStartButton = true,
                                onStartExam = {
                                    // 进入考试模式
                                    if (navigationManager.enterExamMode(exam.id)) {
                                        // 成功进入考试模式
                                        // TODO: 导航到考试界面
                                    } else {
                                        // 已有考试进行中，显示提示
                                        // TODO: 显示 Snackbar 提示
                                    }
                                }
                            )
                        }
                    }
                    2 -> {
                        // 已完成的考试
                        items(getDemoCompletedExams()) { exam ->
                            ExamListItem(
                                exam = exam,
                                showStartButton = false,
                                showScore = true,
                                onStartExam = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamListItem(
    exam: DemoExamItem,
    showStartButton: Boolean = false,
    showScore: Boolean = false,
    onStartExam: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = exam.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "课程：${exam.courseName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "时间：${exam.startTime} - ${exam.endTime}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            
            Text(
                text = "时长：${exam.duration} 分钟",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            if (showScore) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "成绩：${exam.score ?: "--"} / ${exam.totalScore}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showStartButton) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onStartExam,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始考试")
                }
            }
        }
    }
}

// 示例数据
private data class DemoExamItem(
    val id: Long,
    val title: String,
    val courseName: String,
    val startTime: String,
    val endTime: String,
    val duration: Int,
    val totalScore: Int,
    val score: Int? = null
)

private fun getDemoNotStartedExams() = listOf(
    DemoExamItem(1, "操作系统期末考试", "操作系统", "2024-12-25 10:00", "2024-12-25 12:00", 120, 100)
)

private fun getDemoOngoingExams() = listOf(
    DemoExamItem(2, "数据结构第三章测验", "数据结构与算法", "2024-12-15 14:00", "2024-12-15 15:30", 90, 100),
    DemoExamItem(3, "计算机网络期中考试", "计算机网络", "2024-12-15 09:00", "2024-12-15 11:00", 120, 100)
)

private fun getDemoCompletedExams() = listOf(
    DemoExamItem(4, "Java 程序设计期中考试", "Java 程序设计", "2024-11-20 10:00", "2024-11-20 12:00", 120, 100, 85),
    DemoExamItem(5, "数据库系统第二章测验", "数据库系统", "2024-11-15 14:00", "2024-11-15 15:00", 60, 50, 42)
)

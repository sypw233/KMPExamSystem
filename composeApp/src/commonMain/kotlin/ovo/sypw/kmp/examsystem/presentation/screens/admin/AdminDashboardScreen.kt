package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveLazyVerticalGrid
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.SystemOverviewResponse
import ovo.sypw.kmp.examsystem.presentation.viewmodel.AdminDashboardUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.AdminDashboardViewModel
import ovo.sypw.kmp.examsystem.utils.StringUtils.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen() {
    val viewModel: AdminDashboardViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val config = LocalResponsiveConfig.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("管理仪表盘") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is AdminDashboardUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                is AdminDashboardUiState.Error -> Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { viewModel.refresh() }) {
                        Text("重试")
                    }
                }
                is AdminDashboardUiState.Success -> {
                    val data = state.data
                    val statPairs = listOf(
                        "用户" to data.overview.totalUsers.toString(),
                        "学生" to data.overview.studentCount.toString(),
                        "教师" to data.overview.teacherCount.toString(),
                        "管理员" to data.overview.adminCount.toString(),
                        "课程" to data.overview.totalCourses.toString(),
                        "考试" to data.overview.totalExams.toString(),
                        "题目" to data.overview.totalQuestions.toString(),
                        "提交" to data.overview.totalSubmissions.toString()
                    )
                    val rowCount = (statPairs.size + config.columnCount - 1) / config.columnCount
                    val estimatedCardHeight = config.cardPadding * 2 + 100.dp
                    val gridHeight = estimatedCardHeight * rowCount + config.verticalSpacing * (rowCount - 1)
                    Box(
                        modifier = Modifier.fillMaxSize().then(if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = 1000.dp) else Modifier).padding(config.screenPadding)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(config.verticalSpacing)
                        ) {
                            item {
                                ResponsiveLazyVerticalGrid(
                                    items = statPairs,
                                    modifier = Modifier.fillMaxWidth().height(gridHeight),
                                    verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
                                    horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing)
                                ) { (title, value) ->
                                    StatCard(title, value, Modifier.fillMaxWidth(), config.cardPadding)
                                }
                            }
                            item {
                                Text(
                                    "课程通过率概览",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            items(data.topCourseStats, key = { it.courseName }) { item ->
                                CourseStatBar(
                                    title = item.courseName,
                                    highestScore = item.highestScore,
                                    lowestScore = item.lowestScore,
                                    averageScore = item.averageScore
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier, cardPadding: Dp = 16.dp) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CourseStatBar(
    title: String,
    highestScore: Int,
    lowestScore: Int,
    averageScore: Double
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("平均: %.1f  最高: %d  最低: %d".format(averageScore, highestScore, lowestScore))
            Box(
                modifier = Modifier.fillMaxWidth().height(10.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val scoreRatio = (averageScore / 100.0).coerceIn(0.0, 1.0).toFloat()
                Box(
                    modifier = Modifier.fillMaxWidth(scoreRatio).height(10.dp)
                        .background(Color(0xFF2E7D32))
                )
            }
        }
    }
}

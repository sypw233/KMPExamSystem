package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.StatisticsUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.StatisticsViewModel

/**
 * 成绩历史页面
 * 展示学生的考试统计和每次成绩记录
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeHistoryScreen(onBack: () -> Unit) {
    val viewModel: StatisticsViewModel = koinInject()
    val authRepository: AuthRepository = koinInject()

    val authState by authRepository.authState.collectAsState()
    val userId = (authState as? AuthState.Authenticated)?.user?.id
    val uiState by viewModel.uiState.collectAsState()
    var detailSubmissionId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(userId) {
        userId?.let { viewModel.loadStudentStatistics(it) }
    }

    detailSubmissionId?.let { id ->
        ovo.sypw.kmp.examsystem.presentation.screens.student.GradeDetailScreen(
            submissionId = id,
            onBack = { detailSubmissionId = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("考试历史") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
            when (val state = uiState) {
                is StatisticsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is StatisticsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { userId?.let { viewModel.loadStudentStatistics(it) } }) {
                                Text("重试")
                            }
                        }
                    }
                }
                is StatisticsUiState.Success -> {
                    GradeHistoryContent(statistics = state.statistics, onRecordClick = { detailSubmissionId = it })
                }
            }
        }
    }
}

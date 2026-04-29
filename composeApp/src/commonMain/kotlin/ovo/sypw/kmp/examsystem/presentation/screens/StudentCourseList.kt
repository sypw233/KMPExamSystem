package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.EnrollState
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveLazyVerticalGrid
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@Composable
internal fun StudentCourseList(
    state: CourseUiState,
    showEnrollButton: Boolean,
    enrollingState: EnrollState,
    onEnroll: (Long) -> Unit,
    onRetry: () -> Unit,
    onWithdraw: (CourseResponse) -> Unit,
    onViewCourseExams: (CourseResponse) -> Unit
) {
    val config = LocalResponsiveConfig.current
    when (state) {
        is CourseUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is CourseUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onRetry) { Text("重试") }
                }
            }
        }
        is CourseUiState.Success -> {
            if (state.courses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无课程")
                }
            } else {
                ResponsiveLazyVerticalGrid(
                    items = state.courses,
                    key = { it.id },
                    modifier = Modifier
                        .then(if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = ResponsiveUtils.MaxWidths.STANDARD) else Modifier)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(config.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
                    horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing)
                ) { course ->
                    StudentCourseCard(
                        course = course,
                        showEnrollButton = showEnrollButton,
                        isEnrolling = enrollingState is EnrollState.Loading,
                        onEnroll = { onEnroll(course.id) },
                        onWithdraw = { onWithdraw(course) },
                        onViewCourseExams = { onViewCourseExams(course) }
                    )
                }
            }
        }
    }
}

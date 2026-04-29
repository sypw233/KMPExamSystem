package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.CourseStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.SystemOverviewResponse
import ovo.sypw.kmp.examsystem.data.repository.CourseRepository
import ovo.sypw.kmp.examsystem.data.repository.StatisticsRepository
import ovo.sypw.kmp.examsystem.utils.Logger

data class AdminDashboardData(
    val overview: SystemOverviewResponse = SystemOverviewResponse(),
    val topCourseStats: List<CourseStatisticsResponse> = emptyList()
)

sealed interface AdminDashboardUiState {
    data object Loading : AdminDashboardUiState
    data class Success(val data: AdminDashboardData) : AdminDashboardUiState
    data class Error(val message: String) : AdminDashboardUiState
}

class AdminDashboardViewModel(
    private val statisticsRepository: StatisticsRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AdminDashboardUiState>(AdminDashboardUiState.Loading)
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = AdminDashboardUiState.Loading

            val overviewResult = statisticsRepository.getSystemOverview()
            val overview = overviewResult.getOrElse {
                _uiState.value = AdminDashboardUiState.Error(it.message ?: "加载概览数据失败")
                return@launch
            }

            val topCourseStats = coroutineScope {
                val courseResult = courseRepository.loadAllCourses()
                if (courseResult.isFailure) {
                    Logger.w("AdminDashboardViewModel", "加载课程列表失败: ${courseResult.exceptionOrNull()?.message}")
                }
                val courseIds = courseResult.getOrDefault(emptyList()).take(4).map { it.id }
                courseIds.map { courseId ->
                    async { statisticsRepository.getCourseStatistics(courseId).getOrNull() }
                }.mapNotNull { it.await() }
            }

            _uiState.value = AdminDashboardUiState.Success(
                AdminDashboardData(
                    overview = overview,
                    topCourseStats = topCourseStats
                )
            )
        }
    }
}

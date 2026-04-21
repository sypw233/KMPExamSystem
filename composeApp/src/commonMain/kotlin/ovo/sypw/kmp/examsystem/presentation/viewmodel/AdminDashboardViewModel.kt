package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.CourseStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.SystemOverviewResponse
import ovo.sypw.kmp.examsystem.data.repository.CourseRepository
import ovo.sypw.kmp.examsystem.data.repository.StatisticsRepository

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
                _uiState.value = AdminDashboardUiState.Error(it.message ?: "Failed to load overview")
                return@launch
            }

            val topCourseStats = mutableListOf<CourseStatisticsResponse>()
            val courseIds = courseRepository.loadAllCourses().getOrDefault(emptyList()).take(4).map { it.id }
            courseIds.forEach { courseId ->
                statisticsRepository.getCourseStatistics(courseId).getOrNull()?.let { topCourseStats.add(it) }
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

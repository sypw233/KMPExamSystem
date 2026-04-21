package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.StudentStatisticsResponse
import ovo.sypw.kmp.examsystem.data.repository.StatisticsRepository

/**
 * 统计分析 UI 状态
 */
sealed interface StatisticsUiState {
    data object Loading : StatisticsUiState
    data class Success(val statistics: StudentStatisticsResponse) : StatisticsUiState
    data class Error(val message: String) : StatisticsUiState
}

/**
 * 统计分析 ViewModel
 */
class StatisticsViewModel(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    /**
     * 加载指定学生的统计
     */
    fun loadStudentStatistics(userId: Long) {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.Loading
            statisticsRepository.getStudentStatistics(userId)
                .onSuccess { stats ->
                    _uiState.value = StatisticsUiState.Success(stats)
                }
                .onFailure { e ->
                    _uiState.value = StatisticsUiState.Error(e.message ?: "加载统计数据失败")
                }
        }
    }
}

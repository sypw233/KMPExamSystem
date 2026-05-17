package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.StudentStatisticsResponse
import ovo.sypw.kmp.examsystem.data.repository.StatisticsRepository

sealed interface StatisticsUiState {
    data object Loading : StatisticsUiState
    data class Success(val statistics: StudentStatisticsResponse) : StatisticsUiState
    data class Error(val message: String) : StatisticsUiState
}

class StatisticsViewModel(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    private var loadedUserId: Long? = null

    fun loadStudentStatistics(userId: Long, force: Boolean = false) {
        if (!force && loadedUserId == userId && _uiState.value is StatisticsUiState.Success) {
            return
        }
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.Loading
            statisticsRepository.getStudentStatistics(userId)
                .onSuccess { stats ->
                    loadedUserId = userId
                    _uiState.value = StatisticsUiState.Success(stats)
                }
                .onFailure { e ->
                    _uiState.value = StatisticsUiState.Error(e.message ?: "鍔犺浇缁熻鏁版嵁澶辫触")
                }
        }
    }
}

package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.AiConfigRequest
import ovo.sypw.kmp.examsystem.data.dto.AiConfigResponse
import ovo.sypw.kmp.examsystem.data.repository.AiGradingRepository

sealed interface SystemSettingsUiState {
    data object Loading : SystemSettingsUiState
    data class Success(val configs: List<AiConfigResponse>) : SystemSettingsUiState
    data class Error(val message: String) : SystemSettingsUiState
}

sealed interface SystemSettingsActionState {
    data object Idle : SystemSettingsActionState
    data object Loading : SystemSettingsActionState
    data class Success(val message: String) : SystemSettingsActionState
    data class Error(val message: String) : SystemSettingsActionState
}

class SystemSettingsViewModel(
    private val aiGradingRepository: AiGradingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<SystemSettingsUiState>(SystemSettingsUiState.Loading)
    val uiState: StateFlow<SystemSettingsUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<SystemSettingsActionState>(SystemSettingsActionState.Idle)
    val actionState: StateFlow<SystemSettingsActionState> = _actionState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = SystemSettingsUiState.Loading
            aiGradingRepository.getAiConfigs()
                .onSuccess { _uiState.value = SystemSettingsUiState.Success(it) }
                .onFailure { _uiState.value = SystemSettingsUiState.Error(it.message ?: "Failed to load AI config") }
        }
    }

    fun saveConfig(configKey: String, configValue: String) {
        _actionState.value = SystemSettingsActionState.Loading
        viewModelScope.launch {
            aiGradingRepository.updateAiConfig(
                AiConfigRequest(
                    configKey = configKey.trim(),
                    configValue = configValue.trim()
                )
            ).onSuccess {
                _actionState.value = SystemSettingsActionState.Success("AI config updated")
                refresh()
            }.onFailure {
                _actionState.value = SystemSettingsActionState.Error(it.message ?: "Save failed")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = SystemSettingsActionState.Idle
    }
}

package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.AiConfigRequest
import ovo.sypw.kmp.examsystem.data.dto.AiConfigResponse
import ovo.sypw.kmp.examsystem.data.repository.AiGradingRepository

enum class AiModelMode(val label: String) {
    DEFAULT("默认模型"),
    CUSTOM("自定义模型")
}

enum class AiCustomProvider(
    val label: String,
    val baseUrl: String?,
    val placeholderModel: String
) {
    OPENAI("OpenAI 兼容", "https://api.openai.com/v1", "gpt-4o-mini"),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com/v1", "deepseek-chat"),
    QWEN("通义千问", "https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus"),
    CUSTOM_URL("自定义 Base URL", null, "custom-model")
}

data class AiSettingsForm(
    val mode: AiModelMode = AiModelMode.DEFAULT,
    val customProvider: AiCustomProvider = AiCustomProvider.OPENAI,
    val customBaseUrl: String = "",
    val customModelName: String = "",
    val customApiKey: String = "",
    val systemPrompt: String = "",
    val temperature: String = "0.3",
    val maxTokens: String = "500",
    val batchConcurrency: String = "5"
) {
    val effectiveCustomBaseUrl: String
        get() = customProvider.baseUrl ?: customBaseUrl.trim()
}

data class AiFixedPreset(
    val id: String,
    val label: String,
    val provider: String,
    val modelName: String,
    val baseUrl: String,
    val description: String
)

val DEFAULT_KIMI_PRESET = AiFixedPreset(
    id = "kimi_default",
    label = "Kimi",
    provider = "Moonshot AI",
    modelName = "kimi-k2.6",
    baseUrl = "https://api.moonshot.ai/v1",
    description = "固定默认模型，不在页面中开放切换。"
)

sealed interface SystemSettingsUiState {
    data object Loading : SystemSettingsUiState
    data class Success(val form: AiSettingsForm) : SystemSettingsUiState
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
                .onSuccess { configs -> _uiState.value = SystemSettingsUiState.Success(configs.toForm()) }
                .onFailure { _uiState.value = SystemSettingsUiState.Error(it.message ?: "加载 AI 配置失败") }
        }
    }

    fun saveSettings(form: AiSettingsForm) {
        _actionState.value = SystemSettingsActionState.Loading
        viewModelScope.launch {
            runCatching {
                buildSaveRequests(form).forEach { request ->
                    aiGradingRepository.updateAiConfig(request).getOrThrow()
                }
            }.onSuccess {
                _actionState.value = SystemSettingsActionState.Success("AI 配置已更新")
                refresh()
            }.onFailure {
                _actionState.value = SystemSettingsActionState.Error(it.message ?: "保存失败")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = SystemSettingsActionState.Idle
    }

    private fun buildSaveRequests(form: AiSettingsForm): List<AiConfigRequest> {
        val requests = mutableListOf(
            AiConfigRequest("provider_mode", if (form.mode == AiModelMode.DEFAULT) "preset" else "custom"),
            AiConfigRequest("system_prompt", form.systemPrompt.trim()),
            AiConfigRequest("temperature", form.temperature.trim()),
            AiConfigRequest("max_tokens", form.maxTokens.trim()),
            AiConfigRequest("ai_batch_concurrency", form.batchConcurrency.trim())
        )
        if (form.mode == AiModelMode.DEFAULT) {
            requests += AiConfigRequest("provider_preset", DEFAULT_KIMI_PRESET.id)
            requests += AiConfigRequest("model_name", DEFAULT_KIMI_PRESET.modelName)
            requests += AiConfigRequest("api_base_url", DEFAULT_KIMI_PRESET.baseUrl)
        } else {
            requests += AiConfigRequest("provider_preset", form.customProvider.name.lowercase())
            requests += AiConfigRequest("model_name", form.customModelName.trim())
            requests += AiConfigRequest("api_base_url", form.effectiveCustomBaseUrl)
            requests += AiConfigRequest("api_key", form.customApiKey.trim())
        }
        return requests
    }
}

private fun List<AiConfigResponse>.toForm(): AiSettingsForm {
    val configMap = associateBy({ it.configKey }, { it.configValue })
    val presetId = configMap["provider_preset"]
    val modelName = configMap["model_name"].orEmpty()
    val baseUrl = configMap["api_base_url"].orEmpty()
    val mode = if (configMap["provider_mode"] == "custom") AiModelMode.CUSTOM else AiModelMode.DEFAULT
    val provider = when {
        presetId == AiCustomProvider.DEEPSEEK.name.lowercase() || baseUrl.contains("deepseek") -> AiCustomProvider.DEEPSEEK
        presetId == AiCustomProvider.QWEN.name.lowercase() || baseUrl.contains("dashscope") -> AiCustomProvider.QWEN
        presetId == AiCustomProvider.OPENAI.name.lowercase() || baseUrl.contains("openai") -> AiCustomProvider.OPENAI
        baseUrl.isNotBlank() -> AiCustomProvider.CUSTOM_URL
        else -> AiCustomProvider.OPENAI
    }
    return AiSettingsForm(
        mode = mode,
        customProvider = provider,
        customBaseUrl = if (provider == AiCustomProvider.CUSTOM_URL) baseUrl else "",
        customModelName = modelName.ifBlank { provider.placeholderModel },
        customApiKey = configMap["api_key"].orEmpty(),
        systemPrompt = configMap["system_prompt"].orEmpty(),
        temperature = configMap["temperature"].orEmpty().ifBlank { "0.3" },
        maxTokens = configMap["max_tokens"].orEmpty().ifBlank { "500" },
        batchConcurrency = configMap["ai_batch_concurrency"].orEmpty().ifBlank { "5" }
    )
}

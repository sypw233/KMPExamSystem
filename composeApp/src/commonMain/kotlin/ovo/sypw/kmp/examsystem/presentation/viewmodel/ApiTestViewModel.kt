package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ovo.sypw.kmp.examsystem.data.dto.ApiTestRequest
import ovo.sypw.kmp.examsystem.data.dto.ApiTestResponse
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.domain.repository.ApiTestRepository
import ovo.sypw.kmp.examsystem.utils.Logger

/**
 * API测试ViewModel
 * 管理API测试界面的状态和业务逻辑
 */
class ApiTestViewModel(
    private val apiTestRepository: ApiTestRepository
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow(ApiTestUiState())
    val uiState: StateFlow<ApiTestUiState> = _uiState.asStateFlow()

    // 当前测试结果
    private val _currentTestResult = MutableStateFlow<ApiTestResponse?>(null)
    val currentTestResult: StateFlow<ApiTestResponse?> = _currentTestResult.asStateFlow()

    /**
     * 更新请求URL
     */
    fun updateUrl(url: String) {
        _uiState.value = _uiState.value.copy(url = url)
    }

    /**
     * 更新请求方法
     */
    fun updateMethod(method: String) {
        _uiState.value = _uiState.value.copy(method = method)
    }

    /**
     * 更新请求头
     */
    fun updateHeaders(headers: Map<String, String>) {
        _uiState.value = _uiState.value.copy(headers = headers)
    }

    /**
     * 更新请求体
     */
    fun updateBody(body: String) {
        _uiState.value = _uiState.value.copy(body = body)
    }

    /**
     * 更新查询参数
     */
    fun updateQueryParams(queryParams: Map<String, String>) {
        _uiState.value = _uiState.value.copy(queryParams = queryParams)
    }

    /**
     * 更新超时时间
     */
    fun updateTimeout(timeout: Long) {
        _uiState.value = _uiState.value.copy(timeout = timeout)
    }

    /**
     * 执行API测试
     */
    fun executeTest() {
        val currentState = _uiState.value

        if (currentState.url.isBlank()) {
            _uiState.value = currentState.copy(
                isLoading = false,
                errorMessage = "请输入有效的URL"
            )
            return
        }

        val request = ApiTestRequest(
            url = currentState.url,
            method = currentState.method,
            headers = currentState.headers,
            body = currentState.body,
            queryParams = currentState.queryParams,
            timeout = currentState.timeout
        )

        Logger.d("ApiTestViewModel", "开始执行API测试: ${request.method} ${request.url}")

        apiTestRepository.executeApiTest(request)
            .onEach { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = currentState.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }

                    is NetworkResult.Success -> {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                        _currentTestResult.value = result.data
                        Logger.d("ApiTestViewModel", "API测试成功完成")
                    }

                    is NetworkResult.Error -> {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                        Logger.e("ApiTestViewModel", "API测试失败: ${result.message}")
                    }

                }
            }
            .catch { exception ->
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "未知错误"
                )
                Logger.e("ApiTestViewModel", "API测试异常", exception)
            }
            .launchIn(viewModelScope)
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * 清除测试结果
     */
    fun clearResult() {
        _currentTestResult.value = null
    }
}

/**
 * API测试UI状态
 */
data class ApiTestUiState(
    val url: String = "",
    val method: String = "GET",
    val headers: Map<String, String> = emptyMap(),
    val body: String = "",
    val queryParams: Map<String, String> = emptyMap(),
    val timeout: Long = 30000L,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
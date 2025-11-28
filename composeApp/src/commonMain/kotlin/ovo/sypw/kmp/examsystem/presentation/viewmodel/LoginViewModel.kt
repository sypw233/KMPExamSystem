package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import ovo.sypw.kmp.examsystem.data.dto.LoginRequest
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository

/**
 * 登录界面 ViewModel
 */
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    /**
     * 更新用户名
     */
    fun updateUsername(value: String) {
        _username.value = value
    }

    /**
     * 更新密码
     */
    fun updatePassword(value: String) {
        _password.value = value
    }

    /**
     * 登录
     */
    fun login() {
        if (_username.value.isBlank()) {
            _uiState.value = LoginUiState.Error("请输入用户名")
            return
        }

        if (_password.value.isBlank()) {
            _uiState.value = LoginUiState.Error("请输入密码")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            val result = authRepository.login(
                LoginRequest(
                    username = _username.value,
                    password = _password.value
                )
            )

            _uiState.value = result.fold(
                onSuccess = { LoginUiState.Success },
                onFailure = { LoginUiState.Error(it.message ?: "登录失败") }
            )
        }
    }

    /**
     * 重置状态
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

/**
 * 登录界面 UI 状态
 */
sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

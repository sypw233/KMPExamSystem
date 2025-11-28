package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import ovo.sypw.kmp.examsystem.data.dto.RegisterRequest
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository

/**
 * 注册界面 ViewModel
 */
class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _realName = MutableStateFlow("")
    val realName: StateFlow<String> = _realName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _role = MutableStateFlow("student")
    val role: StateFlow<String> = _role.asStateFlow()

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
     * 更新真实姓名
     */
    fun updateRealName(value: String) {
        _realName.value = value
    }

    /**
     * 更新邮箱
     */
    fun updateEmail(value: String) {
        _email.value = value
    }

    /**
     * 更新角色
     */
    fun updateRole(value: String) {
        _role.value = value
    }

    /**
     * 注册
     */
    fun register() {
        // 表单验证
        if (_username.value.isBlank()) {
            _uiState.value = RegisterUiState.Error("请输入用户名")
            return
        }

        if (_username.value.length < 3 || _username.value.length > 50) {
            _uiState.value = RegisterUiState.Error("用户名长度必须在3-50之间")
            return
        }

        if (_password.value.isBlank()) {
            _uiState.value = RegisterUiState.Error("请输入密码")
            return
        }

        if (_password.value.length < 6 || _password.value.length > 20) {
            _uiState.value = RegisterUiState.Error("密码长度必须在6-20之间")
            return
        }

        if (_realName.value.isBlank()) {
            _uiState.value = RegisterUiState.Error("请输入真实姓名")
            return
        }

        if (_email.value.isNotBlank()) {
            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
            if (!emailRegex.matches(_email.value)) {
                _uiState.value = RegisterUiState.Error("邮箱格式不正确")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            val result = authRepository.register(
                RegisterRequest(
                    username = _username.value,
                    password = _password.value,
                    realName = _realName.value,
                    role = _role.value,
                    email = _email.value.ifBlank { null }
                )
            )

            _uiState.value = result.fold(
                onSuccess = { RegisterUiState.Success },
                onFailure = { RegisterUiState.Error(it.message ?: "注册失败") }
            )
        }
    }

    /**
     * 重置状态
     */
    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}

/**
 * 注册界面 UI 状态
 */
sealed class RegisterUiState {
    data object Idle : RegisterUiState()
    data object Loading : RegisterUiState()
    data object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

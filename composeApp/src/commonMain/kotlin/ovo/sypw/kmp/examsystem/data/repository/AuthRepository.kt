package ovo.sypw.kmp.examsystem.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ovo.sypw.kmp.examsystem.data.api.AuthApi
import ovo.sypw.kmp.examsystem.data.dto.LoginRequest
import ovo.sypw.kmp.examsystem.data.dto.RegisterRequest
import ovo.sypw.kmp.examsystem.data.dto.UserInfo
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage
import ovo.sypw.kmp.examsystem.domain.AuthState

/**
 * 认证仓库
 * 管理用户认证状态和 Token 存储
 */
class AuthRepository(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * 用户登录
     * @param request 登录请求
     * @return 是否成功
     */
    suspend fun login(request: LoginRequest): Result<UserInfo> {
        return try {
            _authState.value = AuthState.Loading
            
            val response = authApi.login(request)
            
            if (response.code == 200 && response.data != null) {
                // 保存 Token
                tokenStorage.saveAccessToken(response.data.accessToken)
                tokenStorage.saveRefreshToken(response.data.refreshToken)
                
                // 获取用户信息
                val userInfoResponse = authApi.getCurrentUser()
                if (userInfoResponse.code == 200 && userInfoResponse.data != null) {
                    _authState.value = AuthState.Authenticated(userInfoResponse.data)
                    Result.success(userInfoResponse.data)
                } else {
                    _authState.value = AuthState.Error(userInfoResponse.message)
                    Result.failure(Exception(userInfoResponse.message))
                }
            } else {
                _authState.value = AuthState.Error(response.message)
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "登录失败")
            Result.failure(e)
        }
    }

    /**
     * 用户注册
     * @param request 注册请求
     * @return 是否成功
     */
    suspend fun register(request: RegisterRequest): Result<UserInfo> {
        return try {
            _authState.value = AuthState.Loading
            
            val response = authApi.register(request)
            
            if (response.code == 200 && response.data != null) {
                // 保存 Token
                tokenStorage.saveAccessToken(response.data.accessToken)
                tokenStorage.saveRefreshToken(response.data.refreshToken)
                
                // 获取用户信息
                val userInfoResponse = authApi.getCurrentUser()
                if (userInfoResponse.code == 200 && userInfoResponse.data != null) {
                    _authState.value = AuthState.Authenticated(userInfoResponse.data)
                    Result.success(userInfoResponse.data)
                } else {
                    _authState.value = AuthState.Error(userInfoResponse.message)
                    Result.failure(Exception(userInfoResponse.message))
                }
            } else {
                _authState.value = AuthState.Error(response.message)
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "注册失败")
            Result.failure(e)
        }
    }

    /**
     * 登出
     */
    suspend fun logout() {
        try {
            authApi.logout()
        } finally {
            tokenStorage.clearTokens()
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * 检查并初始化认证状态
     */
    suspend fun checkAuthState() {
        if (tokenStorage.hasValidToken()) {
            try {
                val response = authApi.getCurrentUser()
                if (response.code == 200 && response.data != null) {
                    _authState.value = AuthState.Authenticated(response.data)
                } else {
                    // Token 无效，清除
                    tokenStorage.clearTokens()
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                tokenStorage.clearTokens()
                _authState.value = AuthState.Unauthenticated
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * 刷新 Token
     * @return 是否成功
     */
    suspend fun refreshToken(): Boolean {
        return try {
            val refreshToken = tokenStorage.getRefreshToken() ?: return false
            val response = authApi.refreshToken(refreshToken)
            
            if (response.code == 200 && response.data != null) {
                tokenStorage.saveAccessToken(response.data.accessToken)
                tokenStorage.saveRefreshToken(response.data.refreshToken)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取当前用户信息
     */
    suspend fun getCurrentUser(): UserInfo? {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.user
            else -> null
        }
    }
}

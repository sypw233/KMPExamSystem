package ovo.sypw.kmp.examsystem.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ovo.sypw.kmp.examsystem.data.api.AuthApi
import ovo.sypw.kmp.examsystem.data.dto.ChangePasswordRequest
import ovo.sypw.kmp.examsystem.data.dto.LoginRequest
import ovo.sypw.kmp.examsystem.data.dto.RegisterRequest
import ovo.sypw.kmp.examsystem.data.dto.UserInfo
import ovo.sypw.kmp.examsystem.data.dto.UserProfileRequest
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.utils.Logger

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
                // 先获取用户信息（验证 Token 确实可用），成功后再持久化保存
                val userInfoResponse = authApi.getCurrentUser(response.data.accessToken)
                if (userInfoResponse.code == 200 && userInfoResponse.data != null) {
                    tokenStorage.saveAccessToken(response.data.accessToken)
                    tokenStorage.saveRefreshToken(response.data.refreshToken)
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
                // 先获取用户信息（验证 Token 确实可用），成功后再持久化保存
                val userInfoResponse = authApi.getCurrentUser(response.data.accessToken)
                if (userInfoResponse.code == 200 && userInfoResponse.data != null) {
                    tokenStorage.saveAccessToken(response.data.accessToken)
                    tokenStorage.saveRefreshToken(response.data.refreshToken)
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
     * 登出（JWT 无状态认证，纯前端清除 Token）
     */
    suspend fun logout() {
        tokenStorage.clearTokens()
        _authState.value = AuthState.Unauthenticated
    }

    /**
     * 检查并初始化认证状态
     */
    suspend fun checkAuthState() {
        val accessToken = tokenStorage.getAccessToken()
        val storedRefreshToken = tokenStorage.getRefreshToken()
        if (accessToken.isNullOrBlank() && storedRefreshToken.isNullOrBlank()) {
            _authState.value = AuthState.Unauthenticated
            return
        }

        _authState.value = AuthState.Loading

        if (!accessToken.isNullOrBlank() && authenticateWithToken(accessToken)) return

        if (refreshToken() && authenticateWithToken(tokenStorage.getAccessToken())) return

        tokenStorage.clearTokens()
        _authState.value = AuthState.Unauthenticated
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
            Logger.e("AuthRepository", "Token刷新失败: ${e.message}")
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

    private suspend fun authenticateWithToken(token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        return try {
            val response = authApi.getCurrentUser(token)
            if (response.code == 200 && response.data != null) {
                _authState.value = AuthState.Authenticated(response.data)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 修改密码（用户自助修改）
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val token = tokenStorage.getAccessToken() ?: throw Exception("未登录")
            val response = authApi.changePassword(token, ChangePasswordRequest(oldPassword, newPassword))
            if (response.code == 200) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 修改个人信息（当前登录用户）
     */
    suspend fun updateProfile(nickname: String?, email: String?, avatar: String?): Result<Unit> {
        return try {
            val token = tokenStorage.getAccessToken() ?: throw Exception("未登录")
            val response = authApi.updateProfile(token, UserProfileRequest(nickname, email, avatar))
            if (response.code == 200) {
                checkAuthState()
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package ovo.sypw.kmp.examsystem.data.api

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.AuthResponse
import ovo.sypw.kmp.examsystem.data.dto.LoginRequest
import ovo.sypw.kmp.examsystem.data.dto.RegisterRequest
import ovo.sypw.kmp.examsystem.data.dto.UserInfo
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult

/**
 * 认证 API 服务
 * 处理用户登录、注册、Token 刷新等操作
 */
class AuthApi : BaseApiService() {

    companion object {
        private const val AUTH_ENDPOINT = "/api/auth"
    }

    /**
     * 用户登录
     * @param request 登录请求
     * @return 包含 Token 和用户信息的响应
     */
    suspend fun login(request: LoginRequest): ApiResponse<AuthResponse> {
        val result = post(
            endpoint = "$AUTH_ENDPOINT/login",
            body = request
        )
        
        return when (result) {
            is NetworkResult.Success -> {
                result.data.toApiResponse<AuthResponse>()
            }
            is NetworkResult.Error -> {
                ApiResponse(code = 500, message = result.message, data = null)
            }
        }
    }

    /**
     * 用户注册
     * @param request 注册请求
     * @return 包含 Token 和用户信息的响应
     */
    suspend fun register(request: RegisterRequest): ApiResponse<AuthResponse> {
        val result = post(
            endpoint = "$AUTH_ENDPOINT/register",
            body = request
        )
        
        return when (result) {
            is NetworkResult.Success -> {
                result.data.toApiResponse<AuthResponse>()
            }
            is NetworkResult.Error -> {
                ApiResponse(code = 500, message = result.message, data = null)
            }
        }
    }

    /**
     * 刷新 Token
     * @param refreshToken 刷新令牌
     * @return 新的 Token 信息
     */
    suspend fun refreshToken(refreshToken: String): ApiResponse<AuthResponse> {
        val result = post(
            endpoint = "$AUTH_ENDPOINT/refresh",
            body = mapOf("refreshToken" to refreshToken)
        )
        
        return when (result) {
            is NetworkResult.Success -> {
                result.data.toApiResponse<AuthResponse>()
            }
            is NetworkResult.Error -> {
                ApiResponse(code = 500, message = result.message, data = null)
            }
        }
    }

    /**
     * 获取当前用户信息
     * 需要 Token 认证
     * @param token 访问令牌
     * @return 用户信息
     */
    suspend fun getCurrentUser(token: String): ApiResponse<UserInfo> {
        val result = getWithToken(
            endpoint = "$AUTH_ENDPOINT/me",
            token = token
        )
        
        return when (result) {
            is NetworkResult.Success -> {
                result.data.toApiResponse<UserInfo>()
            }
            is NetworkResult.Error -> {
                ApiResponse(code = 500, message = result.message, data = null)
            }
        }
    }

    /**
     * 登出
     * 需要 Token 认证
     * @param token 访问令牌
     * @return 登出结果
     */
    suspend fun logout(token: String): ApiResponse<Unit> {
        val result = postWithToken(
            endpoint = "$AUTH_ENDPOINT/logout",
            token = token
        )
        
        return when (result) {
            is NetworkResult.Success -> {
                ApiResponse(code = 200, message = "登出成功", data = Unit)
            }
            is NetworkResult.Error -> {
                ApiResponse(code = 500, message = result.message, data = null)
            }
        }
    }
}

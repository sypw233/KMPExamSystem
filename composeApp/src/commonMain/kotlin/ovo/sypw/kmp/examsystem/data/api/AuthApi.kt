package ovo.sypw.kmp.examsystem.data.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.AuthResponse
import ovo.sypw.kmp.examsystem.data.dto.LoginRequest
import ovo.sypw.kmp.examsystem.data.dto.RegisterRequest
import ovo.sypw.kmp.examsystem.data.dto.UserInfo

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
        return try {
            httpClient.post("$AUTH_ENDPOINT/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            ApiResponse(code = 500, message = e.message ?: "登录失败", data = null)
        }
    }

    /**
     * 用户注册
     * @param request 注册请求
     * @return 包含 Token 和用户信息的响应
     */
    suspend fun register(request: RegisterRequest): ApiResponse<AuthResponse> {
        return try {
            httpClient.post("$AUTH_ENDPOINT/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            ApiResponse(code = 500, message = e.message ?: "注册失败", data = null)
        }
    }

    /**
     * 刷新 Token
     * @param refreshToken 刷新令牌
     * @return 新的 Token 信息
     */
    suspend fun refreshToken(refreshToken: String): ApiResponse<AuthResponse> {
        return try {
            httpClient.post("$AUTH_ENDPOINT/refresh") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("refreshToken" to refreshToken))
            }.body()
        } catch (e: Exception) {
            ApiResponse(code = 500, message = e.message ?: "刷新 Token 失败", data = null)
        }
    }

    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    suspend fun getCurrentUser(): ApiResponse<UserInfo> {
        return try {
            httpClient.get("$AUTH_ENDPOINT/me").body()
        } catch (e: Exception) {
            ApiResponse(code = 500, message = e.message ?: "获取用户信息失败", data = null)
        }
    }

    /**
     * 登出
     * @return 登出结果
     */
    suspend fun logout(): ApiResponse<Unit> {
        return try {
            httpClient.post("$AUTH_ENDPOINT/logout") {
                contentType(ContentType.Application.Json)
            }.body()
        } catch (e: Exception) {
            ApiResponse(code = 500, message = e.message ?: "登出失败", data = null)
        }
    }
}

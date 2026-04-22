package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * 认证响应
 * 包含访问令牌和刷新令牌
 */
@Serializable
data class AuthResponse(
    val id: Long,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val username: String,
    val nickname: String? = null,
    val realName: String? = null,
    val role: String,
    val email: String? = null,
    val avatar: String? = null
)

package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * 登录请求
 */
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

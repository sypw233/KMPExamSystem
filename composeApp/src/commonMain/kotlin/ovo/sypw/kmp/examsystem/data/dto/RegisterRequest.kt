package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * 注册请求
 */
@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val realName: String,
    val role: String,  // student 或 teacher
    val email: String? = null
)

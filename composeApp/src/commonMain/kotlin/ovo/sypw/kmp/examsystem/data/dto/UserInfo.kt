package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * 用户信息
 */
@Serializable
data class UserInfo(
    val id: Long,
    val username: String,
    val realName: String,
    val nickname: String? = null,
    val email: String? = null,
    val role: String,  // STUDENT, TEACHER, ADMIN
    val avatar: String? = null,
    val createdAt: String? = null
)

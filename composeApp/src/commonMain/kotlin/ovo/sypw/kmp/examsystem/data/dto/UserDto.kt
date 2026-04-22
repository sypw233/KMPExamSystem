package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * 用户响应数据（管理员视角，含 status 字段）
 */
@Serializable
data class UserResponse(
    val id: Long,
    val username: String,
    val nickname: String? = null,
    val realName: String? = null,
    val role: String,
    val email: String? = null,
    val avatar: String? = null,
    val status: Int = 1,         // 1-启用, 0-禁用
    val createTime: String? = null
)

/**
 * 创建用户请求（管理员）
 * @param username 用户名, 3-50位字母数字下划线
 * @param password 密码, 6-20位
 * @param realName 真实姓名（可选）
 * @param role admin | teacher | student
 * @param email 邮箱（可选）
 */
@Serializable
data class UserCreateRequest(
    val username: String,
    val password: String,
    val realName: String? = null,
    val role: String,
    val email: String? = null
)

/**
 * 更新用户信息请求（所有字段可选）
 */
@Serializable
data class UserUpdateRequest(
    val realName: String? = null,
    val email: String? = null,
    val role: String? = null,
    val status: Int? = null
)

/**
 * 重置密码请求（管理员用）
 */
@Serializable
data class ResetPasswordRequest(
    val newPassword: String
)

/**
 * 修改密码请求（用户自助修改，需验证旧密码）
 */
@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

/**
 * 分页用户列表
 */
@Serializable
data class PageUserResponse(
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val size: Int = 20,
    val content: List<UserResponse> = emptyList(),
    val number: Int = 0,        // 当前页码（从 0 开始）
    val first: Boolean = true,
    val last: Boolean = true,
    val numberOfElements: Int = 0,
    val empty: Boolean = true
)

/**
 * 修改个人信息请求（当前登录用户）
 */
@Serializable
data class UserProfileRequest(
    val nickname: String? = null,
    val email: String? = null,
    val avatar: String? = null
)

/**
 * 用户分页查询参数
 */
data class UserQueryParams(
    val role: String? = null,
    val status: Int? = null,
    val keyword: String? = null,
    val page: Int = 0,
    val size: Int = 20
)

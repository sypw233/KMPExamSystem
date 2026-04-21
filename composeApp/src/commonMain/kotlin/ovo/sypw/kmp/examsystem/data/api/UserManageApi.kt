package ovo.sypw.kmp.examsystem.data.api

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.PageUserResponse
import ovo.sypw.kmp.examsystem.data.dto.ResetPasswordRequest
import ovo.sypw.kmp.examsystem.data.dto.UserCreateRequest
import ovo.sypw.kmp.examsystem.data.dto.UserQueryParams
import ovo.sypw.kmp.examsystem.data.dto.UserResponse
import ovo.sypw.kmp.examsystem.data.dto.UserUpdateRequest
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.parseData

/**
 * 用户管理 API（仅管理员，全部 9 个接口）
 * Base path: /api/admin/users
 */
class UserManageApi : BaseApiService() {

    companion object {
        private const val USER_ENDPOINT = "/api/admin/users"
    }

    /**
     * 分页查询用户列表
     * GET /api/admin/users?role=&status=&keyword=&page=&size=
     */
    suspend fun getUsers(token: String, params: UserQueryParams): ApiResponse<PageUserResponse> {
        val queryParams = buildMap<String, Any> {
            params.role?.let { put("role", it) }
            params.status?.let { put("status", it) }
            params.keyword?.let { put("keyword", it) }
            put("page", params.page)
            put("size", params.size)
        }
        val result = getWithToken(endpoint = USER_ENDPOINT, token = token, parameters = queryParams)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * 按角色查询全部用户（不分页）
     * GET /api/admin/users/role/{role}
     */
    suspend fun getUsersByRole(token: String, role: String): ApiResponse<List<UserResponse>> {
        val result = getWithToken(endpoint = "$USER_ENDPOINT/role/$role", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * 查询用户详情
     * GET /api/admin/users/{id}
     */
    suspend fun getUserDetail(token: String, userId: Long): ApiResponse<UserResponse> {
        val result = getWithToken(endpoint = "$USER_ENDPOINT/$userId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * 新建用户
     * POST /api/admin/users
     */
    suspend fun createUser(token: String, request: UserCreateRequest): ApiResponse<UserResponse> {
        val result = postWithToken(endpoint = USER_ENDPOINT, token = token, body = request)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * 更新用户信息（所有字段可选）
     * PUT /api/admin/users/{id}
     */
    suspend fun updateUser(token: String, userId: Long, request: UserUpdateRequest): ApiResponse<UserResponse> {
        val result = putWithToken(endpoint = "$USER_ENDPOINT/$userId", token = token, body = request)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * 删除用户（不能删除自己）
     * DELETE /api/admin/users/{id}
     */
    suspend fun deleteUser(token: String, userId: Long): ApiResponse<Unit> {
        val result = deleteWithToken(endpoint = "$USER_ENDPOINT/$userId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, Unit)
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * 重置用户密码
     * POST /api/admin/users/{id}/reset-password
     */
    suspend fun resetPassword(token: String, userId: Long, newPassword: String): ApiResponse<Unit> {
        val result = postWithToken(
            endpoint = "$USER_ENDPOINT/$userId/reset-password",
            token = token,
            body = ResetPasswordRequest(newPassword)
        )
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, Unit)
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * 启用用户账号
     * PUT /api/admin/users/{id}/enable
     */
    suspend fun enableUser(token: String, userId: Long): ApiResponse<UserResponse> {
        val result = putWithToken(endpoint = "$USER_ENDPOINT/$userId/enable", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * 禁用用户账号（不能禁用自己）
     * PUT /api/admin/users/{id}/disable
     */
    suspend fun disableUser(token: String, userId: Long): ApiResponse<UserResponse> {
        val result = putWithToken(endpoint = "$USER_ENDPOINT/$userId/disable", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }
}

package ovo.sypw.kmp.examsystem.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ovo.sypw.kmp.examsystem.data.api.UserManageApi
import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.BatchDeleteRequest
import ovo.sypw.kmp.examsystem.data.dto.BatchDeleteResult
import ovo.sypw.kmp.examsystem.data.dto.PageUserResponse
import ovo.sypw.kmp.examsystem.data.dto.UserCreateRequest
import ovo.sypw.kmp.examsystem.data.dto.UserQueryParams
import ovo.sypw.kmp.examsystem.data.dto.UserResponse
import ovo.sypw.kmp.examsystem.data.dto.UserUpdateRequest
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

/**
 * 用户管理仓库（管理员专属）
 */
class UserManageRepository(
    private val userManageApi: UserManageApi,
    private val tokenStorage: TokenStorage
) {
    private val _userPage = MutableStateFlow<PageUserResponse?>(null)
    val userPage: StateFlow<PageUserResponse?> = _userPage.asStateFlow()

    private val _usersByRole = MutableStateFlow<List<UserResponse>>(emptyList())
    val usersByRole: StateFlow<List<UserResponse>> = _usersByRole.asStateFlow()

    /** 分页查询用户 */
    suspend fun loadUsers(params: UserQueryParams): Result<PageUserResponse> = runWithToken { token ->
        val r = userManageApi.getUsers(token, params)
        if (r.code == 200 && r.data != null) { _userPage.value = r.data; r.data }
        else throw Exception(r.message)
    }

    /** 按角色查全量用户（复用分页接口取全量） */
    suspend fun loadUsersByRole(role: String): Result<List<UserResponse>> = runWithToken { token ->
        fetchUserPages { page, size -> userManageApi.getUsersByRole(token, role, page, size) }.also {
            _usersByRole.value = it
        }
    }

    /** 查单个用户详情 */
    suspend fun getUserDetail(userId: Long): Result<UserResponse> = runWithToken { token ->
        val r = userManageApi.getUserDetail(token, userId)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /** 新建用户 */
    suspend fun createUser(request: UserCreateRequest): Result<UserResponse> = runWithToken { token ->
        val r = userManageApi.createUser(token, request)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /** 更新用户信息 */
    suspend fun updateUser(userId: Long, request: UserUpdateRequest): Result<UserResponse> = runWithToken { token ->
        val r = userManageApi.updateUser(token, userId, request)
        if (r.code == 200 && r.data != null) {
            updateLocalUser(r.data)
            r.data
        } else throw Exception(r.message)
    }

    /** 删除用户 */
    suspend fun deleteUser(userId: Long): Result<Unit> = runWithToken { token ->
        val r = userManageApi.deleteUser(token, userId)
        if (r.code == 200) {
            _userPage.value = _userPage.value?.let { page ->
                page.copy(content = page.content.filter { it.id != userId })
            }
            Unit
        } else throw Exception(r.message)
    }

    /** 重置密码 */
    suspend fun resetPassword(userId: Long, newPassword: String): Result<Unit> = runWithToken { token ->
        val r = userManageApi.resetPassword(token, userId, newPassword)
        if (r.code == 200) Unit else throw Exception(r.message)
    }

    /** 启用用户 */
    suspend fun enableUser(userId: Long): Result<UserResponse> = runWithToken { token ->
        val r = userManageApi.enableUser(token, userId)
        if (r.code == 200 && r.data != null) { updateLocalUser(r.data); r.data }
        else throw Exception(r.message)
    }

    /** 禁用用户 */
    suspend fun disableUser(userId: Long): Result<UserResponse> = runWithToken { token ->
        val r = userManageApi.disableUser(token, userId)
        if (r.code == 200 && r.data != null) { updateLocalUser(r.data); r.data }
        else throw Exception(r.message)
    }

    /** 批量删除用户 */
    suspend fun batchDeleteUsers(ids: List<Long>): Result<BatchDeleteResult> = runWithToken { token ->
        val r = userManageApi.batchDeleteUsers(token, BatchDeleteRequest(ids))
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    private fun updateLocalUser(updated: UserResponse) {
        _userPage.value = _userPage.value?.let { page ->
            page.copy(content = page.content.map { if (it.id == updated.id) updated else it })
        }
    }

    private suspend fun fetchUserPages(
        requestPage: suspend (page: Int, size: Int) -> ApiResponse<PageUserResponse>
    ): List<UserResponse> {
        val users = mutableListOf<UserResponse>()
        var page = 0
        val size = 100
        var hasNextPage: Boolean

        do {
            val response = requestPage(page, size)
            if (response.code != 200) throw Exception(response.message)
            val data = response.data ?: break
            users += data.content
            page += 1
            hasNextPage = !data.last && page < data.totalPages
        } while (hasNextPage)

        return users.distinctBy { it.id }
    }

    private suspend fun <T> runWithToken(block: suspend (String) -> T): Result<T> {
        return try {
            val token = tokenStorage.getAccessToken() ?: throw Exception("未登录")
            Result.success(block(token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.PageUserResponse
import ovo.sypw.kmp.examsystem.data.dto.UserCreateRequest
import ovo.sypw.kmp.examsystem.data.dto.UserQueryParams
import ovo.sypw.kmp.examsystem.data.dto.UserResponse
import ovo.sypw.kmp.examsystem.data.dto.UserUpdateRequest
import ovo.sypw.kmp.examsystem.data.repository.UserManageRepository

// ── UI 状态 ────────────────────────────────────────────────────────────────

sealed interface UserListState {
    data object Loading : UserListState
    data class Success(val page: PageUserResponse) : UserListState
    data class Error(val message: String) : UserListState
}

sealed interface UserActionState {
    data object Idle : UserActionState
    data object Loading : UserActionState
    data class Success(val message: String) : UserActionState
    data class Error(val message: String) : UserActionState
}

/**
 * 用户管理 ViewModel（管理员专用）
 */
class UserManageViewModel(
    private val userManageRepository: UserManageRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<UserListState>(UserListState.Loading)
    val listState: StateFlow<UserListState> = _listState.asStateFlow()

    private val _actionState = MutableStateFlow<UserActionState>(UserActionState.Idle)
    val actionState: StateFlow<UserActionState> = _actionState.asStateFlow()

    // 当前筛选条件（驱动 UI 过滤器回显）
    private val _queryParams = MutableStateFlow(UserQueryParams())
    val queryParams: StateFlow<UserQueryParams> = _queryParams.asStateFlow()

    // 当前正在编辑/查看的用户
    private val _selectedUser = MutableStateFlow<UserResponse?>(null)
    val selectedUser: StateFlow<UserResponse?> = _selectedUser.asStateFlow()

    init {
        loadUsers()
    }

    /** 加载用户列表（可带查询条件） */
    fun loadUsers(params: UserQueryParams = _queryParams.value) {
        _queryParams.value = params
        _listState.value = UserListState.Loading
        viewModelScope.launch {
            userManageRepository.loadUsers(params).fold(
                onSuccess = { _listState.value = UserListState.Success(it) },
                onFailure = { _listState.value = UserListState.Error(it.message ?: "加载失败") }
            )
        }
    }

    /** 翻页 */
    fun loadPage(page: Int) {
        loadUsers(_queryParams.value.copy(page = page))
    }

    /** 选择当前操作的用户 */
    fun selectUser(user: UserResponse?) {
        _selectedUser.value = user
    }

    /** 新建用户 */
    fun createUser(request: UserCreateRequest) {
        if (_actionState.value is UserActionState.Loading) return
        _actionState.value = UserActionState.Loading
        viewModelScope.launch {
            userManageRepository.createUser(request).fold(
                onSuccess = {
                    _actionState.value = UserActionState.Success("用户 ${it.username} 创建成功")
                    loadUsers()
                },
                onFailure = { _actionState.value = UserActionState.Error(it.message ?: "创建失败") }
            )
        }
    }

    /** 更新用户 */
    fun updateUser(userId: Long, request: UserUpdateRequest) {
        if (_actionState.value is UserActionState.Loading) return
        _actionState.value = UserActionState.Loading
        viewModelScope.launch {
            userManageRepository.updateUser(userId, request).fold(
                onSuccess = {
                    _actionState.value = UserActionState.Success("更新成功")
                    loadUsers()
                },
                onFailure = { _actionState.value = UserActionState.Error(it.message ?: "更新失败") }
            )
        }
    }

    /** 删除用户 */
    fun deleteUser(userId: Long) {
        if (_actionState.value is UserActionState.Loading) return
        _actionState.value = UserActionState.Loading
        viewModelScope.launch {
            userManageRepository.deleteUser(userId).fold(
                onSuccess = {
                    _actionState.value = UserActionState.Success("用户已删除")
                    loadUsers()
                },
                onFailure = { _actionState.value = UserActionState.Error(it.message ?: "删除失败") }
            )
        }
    }

    /** 重置密码 */
    fun resetPassword(userId: Long, newPassword: String) {
        if (_actionState.value is UserActionState.Loading) return
        _actionState.value = UserActionState.Loading
        viewModelScope.launch {
            userManageRepository.resetPassword(userId, newPassword).fold(
                onSuccess = { _actionState.value = UserActionState.Success("密码已重置") },
                onFailure = { _actionState.value = UserActionState.Error(it.message ?: "重置失败") }
            )
        }
    }

    /** 启用用户 */
    fun enableUser(userId: Long) {
        if (_actionState.value is UserActionState.Loading) return
        _actionState.value = UserActionState.Loading
        viewModelScope.launch {
            userManageRepository.enableUser(userId).fold(
                onSuccess = {
                    _actionState.value = UserActionState.Success("用户已启用")
                    loadUsers()
                },
                onFailure = { _actionState.value = UserActionState.Error(it.message ?: "操作失败") }
            )
        }
    }

    /** 禁用用户 */
    fun disableUser(userId: Long) {
        if (_actionState.value is UserActionState.Loading) return
        _actionState.value = UserActionState.Loading
        viewModelScope.launch {
            userManageRepository.disableUser(userId).fold(
                onSuccess = {
                    _actionState.value = UserActionState.Success("用户已禁用")
                    loadUsers()
                },
                onFailure = { _actionState.value = UserActionState.Error(it.message ?: "操作失败") }
            )
        }
    }

    /** 批量删除用户 */
    fun batchDeleteUsers(ids: List<Long>) {
        if (ids.isEmpty()) return
        if (_actionState.value is UserActionState.Loading) return
        _actionState.value = UserActionState.Loading
        viewModelScope.launch {
            userManageRepository.batchDeleteUsers(ids).fold(
                onSuccess = { result ->
                    val msg = "已删除 ${result.successCount} 位用户" +
                            if (result.failedCount > 0) "，${result.failedCount} 位失败" else ""
                    _actionState.value = UserActionState.Success(msg)
                    loadUsers()
                },
                onFailure = { _actionState.value = UserActionState.Error(it.message ?: "批量删除失败") }
            )
        }
    }

    /** 清除操作状态（弹窗关闭后调用） */
    fun resetActionState() {
        _actionState.value = UserActionState.Idle
    }
}

package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.CreateNotificationRequest
import ovo.sypw.kmp.examsystem.data.dto.NotificationResponse
import ovo.sypw.kmp.examsystem.data.repository.NotificationRepository

/**
 * 通知列表 UI 状态
 */
sealed interface NotificationUiState {
    data object Loading : NotificationUiState
    data class Success(val notifications: List<NotificationResponse>) : NotificationUiState
    data class Error(val message: String) : NotificationUiState
}

/** 通知操作状态 */
sealed interface NotificationActionState {
    data object Idle : NotificationActionState
    data class Success(val message: String) : NotificationActionState
    data class Error(val message: String) : NotificationActionState
}

/**
 * 通知 ViewModel
 */
class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<NotificationActionState>(NotificationActionState.Idle)
    val actionState: StateFlow<NotificationActionState> = _actionState.asStateFlow()

    val unreadCount = notificationRepository.unreadCount

    // 分页状态
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val pageSize = 20

    init {
        loadNotifications()
        loadUnreadCount()
    }

    /**
     * 加载通知列表
     */
    fun loadNotifications(page: Int = 0) {
        viewModelScope.launch {
            if (page == 0) _uiState.value = NotificationUiState.Loading
            notificationRepository.loadNotifications(page, pageSize)
                .onSuccess { list ->
                    _currentPage.value = page
                    _hasMore.value = list.size >= pageSize
                    _uiState.value = NotificationUiState.Success(
                        notificationRepository.notifications.value
                    )
                }
                .onFailure { e ->
                    if (page == 0) {
                        _uiState.value = NotificationUiState.Error(e.message ?: "加载通知失败")
                    } else {
                        _actionState.value = NotificationActionState.Error(e.message ?: "加载更多失败")
                    }
                }
        }
    }

    /**
     * 加载下一页
     */
    fun loadMore() {
        if (_hasMore.value) {
            loadNotifications(_currentPage.value + 1)
        }
    }

    /**
     * 刷新未读数
     */
    fun loadUnreadCount() {
        viewModelScope.launch {
            notificationRepository.loadUnreadCount()
        }
    }

    /**
     * 标记为已读
     */
    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
                .onSuccess { updateSuccessState() }
                .onFailure { e ->
                    _actionState.value = NotificationActionState.Error(e.message ?: "标记已读失败")
                }
        }
    }

    /**
     * 全部标记为已读
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
                .onSuccess { updateSuccessState() }
                .onFailure { e ->
                    _actionState.value = NotificationActionState.Error(e.message ?: "全部标记已读失败")
                }
        }
    }

    /**
     * 删除通知
     */
    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
                .onSuccess { updateSuccessState() }
                .onFailure { e ->
                    _actionState.value = NotificationActionState.Error(e.message ?: "删除通知失败")
                }
        }
    }

    /**
     * 发送自定义通知（管理员）
     */
    fun sendNotification(title: String, content: String, type: String = "ANNOUNCEMENT") {
        if (title.isBlank() || content.isBlank()) {
            _actionState.value = NotificationActionState.Error("标题和内容不能为空")
            return
        }
        viewModelScope.launch {
            notificationRepository.sendNotification(
                CreateNotificationRequest(title = title, content = content, type = type)
            ).onSuccess {
                _actionState.value = NotificationActionState.Success("通知发送成功")
                loadNotifications(0)
                loadUnreadCount()
            }.onFailure { e ->
                _actionState.value = NotificationActionState.Error(e.message ?: "发送通知失败")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = NotificationActionState.Idle
    }

    private fun updateSuccessState() {
        _uiState.value = NotificationUiState.Success(notificationRepository.notifications.value)
    }
}

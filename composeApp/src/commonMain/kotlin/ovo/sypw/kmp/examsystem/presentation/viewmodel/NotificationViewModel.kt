package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

/**
 * 通知 ViewModel
 */
class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    val unreadCount = notificationRepository.unreadCount

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
            notificationRepository.loadNotifications(page)
                .onSuccess { list ->
                    _uiState.value = NotificationUiState.Success(
                        notificationRepository.notifications.value
                    )
                }
                .onFailure { e ->
                    if (page == 0) {
                        _uiState.value = NotificationUiState.Error(e.message ?: "加载通知失败")
                    }
                }
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
            updateSuccessState()
        }
    }

    /**
     * 全部标记为已读
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
            updateSuccessState()
        }
    }

    /**
     * 删除通知
     */
    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
            updateSuccessState()
        }
    }

    private fun updateSuccessState() {
        _uiState.value = NotificationUiState.Success(notificationRepository.notifications.value)
    }
}

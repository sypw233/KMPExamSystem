package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.CreateNotificationRequest
import ovo.sypw.kmp.examsystem.data.dto.NotificationResponse
import ovo.sypw.kmp.examsystem.data.repository.NotificationRepository

sealed interface NotificationUiState {
    data object Loading : NotificationUiState
    data class Success(val notifications: List<NotificationResponse>) : NotificationUiState
    data class Error(val message: String) : NotificationUiState
}

sealed interface NotificationActionState {
    data object Idle : NotificationActionState
    data class Success(val message: String) : NotificationActionState
    data class Error(val message: String) : NotificationActionState
}

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(
        notificationRepository.notifications.value.takeIf { it.isNotEmpty() }
            ?.let { NotificationUiState.Success(it) }
            ?: NotificationUiState.Loading
    )
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<NotificationActionState>(NotificationActionState.Idle)
    val actionState: StateFlow<NotificationActionState> = _actionState.asStateFlow()

    val unreadCount = notificationRepository.unreadCount

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val pageSize = 20
    private var isLoadingFirstPage = false
    private var isLoadingUnreadCount = false
    private var hasLoadedUnreadCount = unreadCount.value > 0

    init {
        if (notificationRepository.notifications.value.isEmpty()) {
            loadNotifications(force = false)
        }
        if (!hasLoadedUnreadCount) {
            loadUnreadCount(force = false)
        }
    }

    fun loadNotifications(page: Int = 0, force: Boolean = true) {
        val cachedNotifications = notificationRepository.notifications.value
        if (page == 0) {
            if (!force && cachedNotifications.isNotEmpty()) {
                _uiState.value = NotificationUiState.Success(cachedNotifications)
                return
            }
            if (!force && _uiState.value is NotificationUiState.Success) return
            if (isLoadingFirstPage) return
            isLoadingFirstPage = true
        }
        viewModelScope.launch {
            if (page == 0 && (force || cachedNotifications.isEmpty())) {
                _uiState.value = NotificationUiState.Loading
            }
            notificationRepository.loadNotifications(page, pageSize)
                .onSuccess { list ->
                    _currentPage.value = page
                    _hasMore.value = list.size >= pageSize
                    _uiState.value = NotificationUiState.Success(notificationRepository.notifications.value)
                }
                .onFailure { e ->
                    if (page == 0) {
                        _uiState.value = NotificationUiState.Error(e.message ?: "加载通知失败")
                    } else {
                        _actionState.value = NotificationActionState.Error(e.message ?: "加载更多通知失败")
                    }
                }
            if (page == 0) {
                isLoadingFirstPage = false
            }
        }
    }

    fun loadMore() {
        if (_hasMore.value) {
            loadNotifications(_currentPage.value + 1)
        }
    }

    fun loadUnreadCount(force: Boolean = true) {
        if (!force && hasLoadedUnreadCount) return
        if (isLoadingUnreadCount) return
        isLoadingUnreadCount = true
        viewModelScope.launch {
            notificationRepository.loadUnreadCount().onSuccess {
                hasLoadedUnreadCount = true
            }
            isLoadingUnreadCount = false
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
                .onSuccess {
                    updateSuccessState()
                    loadUnreadCount(force = true)
                }
                .onFailure { e ->
                    _actionState.value = NotificationActionState.Error(e.message ?: "标记已读失败")
                }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
                .onSuccess {
                    updateSuccessState()
                    loadUnreadCount(force = true)
                }
                .onFailure { e ->
                    _actionState.value = NotificationActionState.Error(e.message ?: "全部标记已读失败")
                }
        }
    }

    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
                .onSuccess {
                    updateSuccessState()
                    loadUnreadCount(force = true)
                }
                .onFailure { e ->
                    _actionState.value = NotificationActionState.Error(e.message ?: "删除通知失败")
                }
        }
    }

    fun sendNotification(title: String, content: String, type: String = "SYSTEM_ANNOUNCEMENT") {
        if (title.isBlank() || content.isBlank()) {
            _actionState.value = NotificationActionState.Error("标题和内容不能为空")
            return
        }
        viewModelScope.launch {
            notificationRepository.sendNotification(
                CreateNotificationRequest(title = title, content = content, type = type)
            ).onSuccess {
                _actionState.value = NotificationActionState.Success("通知已发送")
                loadNotifications(0)
                loadUnreadCount(force = true)
            }.onFailure { e ->
                _actionState.value = NotificationActionState.Error(e.message ?: "发送通知失败")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = NotificationActionState.Idle
    }

    private fun updateSuccessState() {
        val currentList = notificationRepository.notifications.value
        _uiState.value = NotificationUiState.Success(currentList)

        val expectedSize = (_currentPage.value + 1) * pageSize
        if (currentList.size < expectedSize) {
            _currentPage.value = if (currentList.isEmpty()) 0 else (currentList.size - 1) / pageSize
            _hasMore.value = currentList.isNotEmpty()
        }
    }
}

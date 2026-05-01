package ovo.sypw.kmp.examsystem.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ovo.sypw.kmp.examsystem.data.api.NotificationApi
import ovo.sypw.kmp.examsystem.data.dto.CreateNotificationRequest
import ovo.sypw.kmp.examsystem.data.dto.NotificationResponse
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

/**
 * 通知仓库
 */
class NotificationRepository(
    private val notificationApi: NotificationApi,
    tokenStorage: TokenStorage
) : BaseRepository(tokenStorage) {

    private val _notifications = MutableStateFlow<List<NotificationResponse>>(emptyList())
    val notifications: StateFlow<List<NotificationResponse>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()

    /**
     * 加载通知列表
     */
    suspend fun loadNotifications(page: Int = 0, size: Int = 20): Result<List<NotificationResponse>> = runWithToken { token ->
        val response = notificationApi.getNotifications(token, page, size)
        if (response.code == 200 && response.data != null) {
            val items = response.data.content
            if (page == 0) {
                _notifications.value = items
            } else {
                // 按 id 去重，防止快速翻页导致同一页数据被追加多次
                val existingIds = _notifications.value.map { it.id }.toSet()
                val newItems = items.filter { it.id !in existingIds }
                _notifications.value = _notifications.value + newItems
            }
            items
        } else {
            throw Exception(response.message)
        }
    }

    /**
     * 获取未读通知数
     */
    suspend fun loadUnreadCount(): Result<Long> = runWithToken { token ->
        val response = notificationApi.getUnreadCount(token)
        if (response.code == 200 && response.data != null) {
            _unreadCount.value = response.data.count
            response.data.count
        } else {
            throw Exception(response.message)
        }
    }

    /**
     * 标记为已读（带乐观更新和回滚）
     */
    suspend fun markAsRead(notificationId: Long): Result<Unit> = runWithToken { token ->
        val wasUnread = _notifications.value.find { it.id == notificationId }?.isRead == false
        val previousCount = _unreadCount.value

        // 乐观更新：先修改 UI 状态
        _notifications.value = _notifications.value.map { n ->
            if (n.id == notificationId) n.copy(isRead = true) else n
        }
        if (wasUnread && _unreadCount.value > 0) {
            _unreadCount.value = _unreadCount.value - 1
        }

        val response = notificationApi.markAsRead(token, notificationId)
        if (response.code == 200) {
            Unit
        } else {
            // 回滚乐观更新
            _notifications.value = _notifications.value.map { n ->
                if (n.id == notificationId) n.copy(isRead = false) else n
            }
            if (wasUnread) {
                _unreadCount.value = previousCount
            }
            throw Exception(response.message)
        }
    }

    /**
     * 全部标记为已读
     */
    suspend fun markAllAsRead(): Result<Unit> = runWithToken { token ->
        val response = notificationApi.markAllAsRead(token)
        if (response.code == 200) {
            _notifications.value = _notifications.value.map { it.copy(isRead = true) }
            _unreadCount.value = 0
        } else {
            throw Exception(response.message)
        }
    }

    /**
     * 删除通知（带乐观更新和回滚）
     */
    suspend fun deleteNotification(notificationId: Long): Result<Unit> = runWithToken { token ->
        val deletedNotification = _notifications.value.find { it.id == notificationId }
        val wasUnread = deletedNotification?.isRead == false
        val previousNotifications = _notifications.value
        val previousCount = _unreadCount.value

        // 乐观更新：先移除通知
        _notifications.value = _notifications.value.filter { it.id != notificationId }
        if (wasUnread && _unreadCount.value > 0) {
            _unreadCount.value = _unreadCount.value - 1
        }

        val response = notificationApi.deleteNotification(token, notificationId)
        if (response.code == 200) {
            Unit
        } else {
            // 回滚乐观更新
            _notifications.value = previousNotifications
            _unreadCount.value = previousCount
            throw Exception(response.message)
        }
    }

    /**
     * 发送自定义通知（管理员）
     */
    suspend fun sendNotification(request: CreateNotificationRequest): Result<String> = runWithToken { token ->
        val response = notificationApi.sendNotification(token, request)
        if (response.code == 200 && response.data != null) {
            response.data
        } else {
            throw Exception(response.message)
        }
    }

}

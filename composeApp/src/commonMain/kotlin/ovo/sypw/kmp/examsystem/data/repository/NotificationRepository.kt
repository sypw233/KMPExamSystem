package ovo.sypw.kmp.examsystem.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ovo.sypw.kmp.examsystem.data.api.NotificationApi
import ovo.sypw.kmp.examsystem.data.dto.NotificationResponse
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

/**
 * 通知仓库
 */
class NotificationRepository(
    private val notificationApi: NotificationApi,
    private val tokenStorage: TokenStorage
) {

    private val _notifications = MutableStateFlow<List<NotificationResponse>>(emptyList())
    val notifications: StateFlow<List<NotificationResponse>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()

    /**
     * 加载通知列表
     */
    suspend fun loadNotifications(page: Int = 0, size: Int = 20): Result<List<NotificationResponse>> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return Result.failure(Exception("未登录"))
            val response = notificationApi.getNotifications(token, page, size)
            if (response.code == 200 && response.data != null) {
                val items = response.data.content
                if (page == 0) {
                    _notifications.value = items
                } else {
                    _notifications.value = _notifications.value + items
                }
                Result.success(items)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取未读通知数
     */
    suspend fun loadUnreadCount(): Result<Long> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return Result.failure(Exception("未登录"))
            val response = notificationApi.getUnreadCount(token)
            if (response.code == 200 && response.data != null) {
                _unreadCount.value = response.data.count
                Result.success(response.data.count)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 标记为已读
     */
    suspend fun markAsRead(notificationId: Long): Result<Unit> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return Result.failure(Exception("未登录"))
            val response = notificationApi.markAsRead(token, notificationId)
            if (response.code == 200) {
                _notifications.value = _notifications.value.map { n ->
                    if (n.id == notificationId) n.copy(read = true) else n
                }
                loadUnreadCount()
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 全部标记为已读
     */
    suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return Result.failure(Exception("未登录"))
            val response = notificationApi.markAllAsRead(token)
            if (response.code == 200) {
                _notifications.value = _notifications.value.map { it.copy(read = true) }
                _unreadCount.value = 0
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除通知
     */
    suspend fun deleteNotification(notificationId: Long): Result<Unit> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return Result.failure(Exception("未登录"))
            val response = notificationApi.deleteNotification(token, notificationId)
            if (response.code == 200) {
                _notifications.value = _notifications.value.filter { it.id != notificationId }
                loadUnreadCount()
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

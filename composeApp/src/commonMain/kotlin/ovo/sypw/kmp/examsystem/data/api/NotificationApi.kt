package ovo.sypw.kmp.examsystem.data.api

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.CreateNotificationRequest
import ovo.sypw.kmp.examsystem.data.dto.NotificationResponse
import ovo.sypw.kmp.examsystem.data.dto.PageNotificationResponse
import ovo.sypw.kmp.examsystem.data.dto.UnreadCountResponse
import io.ktor.client.HttpClient
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.parseData

/**
 * 通知相关 API 服务
 * @param httpClient 共享的HTTP客户端实例
 */
class NotificationApi(httpClient: HttpClient) : BaseApiService(httpClient) {

    companion object {
        private const val NOTIFICATION_ENDPOINT = "/api/notifications"
    }

    /**
     * 获取通知列表（分页）
     */
    suspend fun getNotifications(token: String, page: Int = 0, size: Int = 20): ApiResponse<PageNotificationResponse> {
        val result = getWithToken(
            endpoint = NOTIFICATION_ENDPOINT,
            token = token,
            parameters = mapOf("page" to page, "size" to size)
        )
        return when (result) {
            is NetworkResult.Success -> {
                val data = result.data.parseData<PageNotificationResponse>()
                ApiResponse(code = result.data.code, message = result.data.msg, data = data)
            }
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }

    /**
     * 获取未读通知数量
     */
    suspend fun getUnreadCount(token: String): ApiResponse<UnreadCountResponse> {
        val result = getWithToken(endpoint = "$NOTIFICATION_ENDPOINT/unread-count", token = token)
        return when (result) {
            is NetworkResult.Success -> {
                val data = result.data.parseData<UnreadCountResponse>()
                ApiResponse(code = result.data.code, message = result.data.msg, data = data)
            }
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }

    /**
     * 标记指定通知为已读
     */
    suspend fun markAsRead(token: String, notificationId: Long): ApiResponse<Unit> {
        val result = putWithToken(endpoint = "$NOTIFICATION_ENDPOINT/$notificationId/read", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(code = result.data.code, message = result.data.msg, data = Unit)
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }

    /**
     * 全部标记为已读
     */
    suspend fun markAllAsRead(token: String): ApiResponse<Unit> {
        val result = putWithToken(endpoint = "$NOTIFICATION_ENDPOINT/read-all", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(code = result.data.code, message = result.data.msg, data = Unit)
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }

    /**
     * 删除通知
     */
    suspend fun deleteNotification(token: String, notificationId: Long): ApiResponse<Unit> {
        val result = deleteWithToken(endpoint = "$NOTIFICATION_ENDPOINT/$notificationId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(code = result.data.code, message = result.data.msg, data = Unit)
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }

    /**
     * 发送自定义通知（管理员）
     * @param token 访问令牌
     * @param request 发送通知请求
     * @return 操作结果消息
     */
    suspend fun sendNotification(token: String, request: CreateNotificationRequest): ApiResponse<String> {
        val result = postWithToken(
            endpoint = NOTIFICATION_ENDPOINT,
            token = token,
            body = request
        )
        return when (result) {
            is NetworkResult.Success -> ApiResponse(code = result.data.code, message = result.data.msg, data = result.data.parseData())
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }
}

package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * 通知响应数据
 */
@Serializable
data class NotificationResponse(
    val id: Long,
    val userId: Long = 0,
    val type: String,          // EXAM_PUBLISHED, EXAM_REMINDER, GRADE_RELEASED, COURSE_UPDATE, SYSTEM_ANNOUNCEMENT
    val title: String,
    val content: String,
    val relatedId: Long? = null,
    val createTime: String? = null,
    val isRead: Boolean = false
)

/**
 * 未读通知数量响应
 */
@Serializable
data class UnreadCountResponse(
    val count: Long
)

/**
 * 分页通知响应
 */
@Serializable
data class PageNotificationResponse(
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val size: Int = 20,
    val content: List<NotificationResponse> = emptyList(),
    val number: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true,
    val numberOfElements: Int = 0,
    val empty: Boolean = true
)

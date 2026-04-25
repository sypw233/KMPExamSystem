package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * 批量删除请求
 */
@Serializable
data class BatchDeleteRequest(
    val ids: List<Long>
)

/**
 * 批量删除失败详情
 */
@Serializable
data class FailedDetail(
    val id: Long,
    val reason: String
)

/**
 * 批量删除结果
 */
@Serializable
data class BatchDeleteResult(
    val successCount: Int = 0,
    val failedCount: Int = 0,
    val successIds: List<Long> = emptyList(),
    val failedDetails: List<FailedDetail> = emptyList()
)

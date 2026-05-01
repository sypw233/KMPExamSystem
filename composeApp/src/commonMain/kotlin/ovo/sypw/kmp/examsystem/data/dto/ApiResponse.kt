package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * 统一 API 响应格式
 */
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null
)

/**
 * 通用分页响应格式
 * 匹配后端 Spring Data Page 结构, 所有分页接口共用
 * @param T 分页内容的元素类型
 */
@Serializable
data class PageResponse<T>(
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val size: Int = 20,
    val content: List<T> = emptyList(),
    val number: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true,
    val numberOfElements: Int = 0,
    val empty: Boolean = true
)

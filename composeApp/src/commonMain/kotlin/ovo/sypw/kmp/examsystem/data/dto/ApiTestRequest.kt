package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * API测试请求数据模型
 * 用于封装API测试的请求参数
 */
@Serializable
data class ApiTestRequest(
    /**
     * 请求URL
     */
    val url: String,

    /**
     * HTTP方法 (GET, POST, PUT, DELETE等)
     */
    val method: String = "GET",

    /**
     * 请求头
     */
    val headers: Map<String, String> = emptyMap(),

    /**
     * 请求体内容
     */
    val body: String = "",

    /**
     * 查询参数
     */
    val queryParams: Map<String, String> = emptyMap(),

    /**
     * 请求超时时间（毫秒）
     */
    val timeout: Long = 30000L
)

/**
 * API测试响应数据模型
 * 用于封装API测试的响应结果
 */
@Serializable
data class ApiTestResponse(
    /**
     * HTTP状态码
     */
    val statusCode: Int,

    /**
     * 状态描述
     */
    val statusText: String,

    /**
     * 响应头
     */
    val headers: Map<String, String>,

    /**
     * 响应体内容
     */
    val body: String,

    /**
     * 响应时间（毫秒）
     */
    val responseTime: Long,

    /**
     * 请求是否成功
     */
    val isSuccess: Boolean
)
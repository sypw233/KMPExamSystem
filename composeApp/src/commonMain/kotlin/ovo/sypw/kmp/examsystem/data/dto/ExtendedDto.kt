package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * 题库响应数据
 */
@Serializable
data class QuestionBankResponse(
    val id: Long,
    val name: String,
    val description: String? = null,
    val creatorId: Long,
    val creatorName: String,
    val questionCount: Int = 0,
    val createTime: String? = null
)

/**
 * 创建/更新题库请求
 */
@Serializable
data class QuestionBankRequest(
    val name: String,
    val description: String? = null
)

/**
 * 题目导入结果
 */
@Serializable
data class ImportResultResponse(
    val successCount: Int = 0,
    val failCount: Int = 0,
    val errors: List<ImportErrorDetail> = emptyList()
)

/**
 * 导入错误详情
 */
@Serializable
data class ImportErrorDetail(
    val row: Int,
    val field: String? = null,
    val message: String
)

/**
 * 文件上传响应
 */
@Serializable
data class FileUploadResponse(
    val key: String,
    val url: String,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val contentType: String? = null
)

/**
 * AI 配置请求
 */
@Serializable
data class AiConfigRequest(
    val provider: String,
    val apiKey: String,
    val model: String,
    val baseUrl: String? = null,
    val promptTemplate: String? = null
)

/**
 * AI 配置响应
 */
@Serializable
data class AiConfigResponse(
    val id: Long? = null,
    val provider: String,
    val model: String,
    val baseUrl: String? = null,
    val enabled: Boolean = false
)

/**
 * AI 判题请求
 */
@Serializable
data class AiGradingRequest(
    val submissionId: Long,
    val questionId: Long
)

/**
 * AI 判题响应
 */
@Serializable
data class AiGradingResponse(
    val questionId: Long,
    val suggestedScore: Int,
    val feedback: String? = null,
    val confidence: Double? = null
)

/**
 * 题目统计响应
 */
@Serializable
data class QuestionStatisticsResponse(
    val questionId: Long,
    val content: String,
    val totalAnswers: Int = 0,
    val correctCount: Int = 0,
    val correctRate: Double = 0.0,
    val averageScore: Double = 0.0
)

/**
 * 课程统计响应
 */
@Serializable
data class CourseStatisticsResponse(
    val courseId: Long,
    val courseName: String,
    val totalStudents: Int = 0,
    val totalExams: Int = 0,
    val averageScore: Double = 0.0,
    val passRate: Double = 0.0
)

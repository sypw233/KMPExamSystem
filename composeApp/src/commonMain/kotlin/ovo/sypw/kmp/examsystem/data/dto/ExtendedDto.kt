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
    val taskId: String = "",
    val totalRows: Int = 0,
    val successCount: Int = 0,
    val failedCount: Int = 0,
    val errors: List<ImportErrorDetail> = emptyList()
)

@Serializable
data class ImportErrorDetail(
    val row: Int,
    val reason: String
)

/**
 * 文件上传响应
 */
@Serializable
data class FileUploadResponse(
    val fileKey: String,
    val fileUrl: String,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val contentType: String? = null
)

/**
 * AI 配置请求
 */
@Serializable
data class AiConfigRequest(
    val configKey: String,
    val configValue: String
)

/**
 * AI 配置响应
 */
@Serializable
data class AiConfigResponse(
    val id: Long? = null,
    val configKey: String,
    val configValue: String,
    val description: String? = null
)

/**
 * AI 判题请求
 */
@Serializable
data class AiGradingRequest(
    val questionId: Long,
    val studentAnswer: String,
    val maxScore: Int
)

/**
 * AI 判题响应
 */
@Serializable
data class AiGradingResponse(
    val questionId: Long,
    val maxScore: Int,
    val suggestedScore: Int,
    val explanation: String? = null,
    val strengths: List<String> = emptyList(),
    val improvements: List<String> = emptyList()
)

/**
 * 题目统计响应
 */
@Serializable
data class QuestionStatisticsResponse(
    val questionId: Long,
    val questionContent: String? = null,
    val questionType: String? = null,
    val usageCount: Int = 0,
    val totalAttempts: Int = 0,
    val correctCount: Int = 0,
    val accuracy: Double = 0.0,
    val optionDistribution: Map<String, Int> = emptyMap()
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
    val averageScore: Double? = null,
    val highestScore: Int? = null,
    val lowestScore: Int? = null
)

/**
 * AI 单题评分详情
 */
@Serializable
data class AiGradingDetail(
    val questionId: Long,
    val questionContent: String,
    val suggestedScore: Int,
    val maxScore: Int,
    val explanation: String,
    val strengths: List<String> = emptyList(),
    val improvements: List<String> = emptyList()
)

/**
 * AI 批量评分请求
 */
@Serializable
data class AiBatchGradingRequest(
    val submissionId: Long,
    val concurrency: Int? = null
)

/**
 * AI 批量评分响应
 */
@Serializable
data class AiBatchGradingResponse(
    val submissionId: Long,
    val gradedCount: Int = 0,
    val totalSuggestedScore: Int = 0,
    val objectiveScore: Int? = null,
    val details: List<AiGradingDetail> = emptyList()
)

/**
 * 分页题库响应
 */
@Serializable
data class PageQuestionBankResponse(
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val size: Int = 20,
    val content: List<QuestionBankResponse> = emptyList(),
    val number: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true,
    val numberOfElements: Int = 0,
    val empty: Boolean = true
)

package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * 考试响应数据
 */
@Serializable
data class ExamResponse(
    val id: Long,
    val title: String,
    val description: String? = null,
    val courseId: Long,
    val courseName: String,
    val creatorId: Long,
    val creatorName: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val duration: Int? = null,
    val totalScore: Int,
    val status: Int = 0,                   // 0-草稿, 1-已发布, 2-已结束
    val statusDescription: String? = null,
    val needsGrading: Boolean = false,
    val questionCount: Long = 0,
    val allowedPlatforms: String? = null,  // desktop, mobile, both
    val strictMode: Boolean = false,
    val maxSwitchCount: Int? = null,
    val fullscreenRequired: Boolean = false,
    val createTime: String? = null
)

/**
 * 题目响应数据
 */
@Serializable
data class QuestionResponse(
    val id: Long,
    val content: String,
    val type: String,                      // single, multiple, true_false, fill_blank, short_answer
    val options: String? = null,           // JSON数组字符串
    val answer: String? = null,
    val analysis: String? = null,
    val difficulty: String? = null,        // easy, medium, hard
    val category: String? = null,
    val score: Int = 0,
    val creatorId: Long? = null,
    val creatorName: String? = null,
    val bankCount: Long = 0,
    val createTime: String? = null
)

/**
 * 考试题目关联响应
 */
@Serializable
data class ExamQuestionResponse(
    val id: Long,
    val examId: Long,
    val questionId: Long,
    val orderNum: Int,
    val score: Int,
    val question: QuestionResponse? = null
)

/**
 * 创建/更新考试请求
 */
@Serializable
data class ExamRequest(
    val title: String,
    val description: String? = null,
    val courseId: Long,
    val startTime: String,
    val endTime: String,
    val duration: Int,
    val totalScore: Int,
    val allowedPlatforms: String = "both",
    val strictMode: Boolean = false,
    val maxSwitchCount: Int = 3,
    val fullscreenRequired: Boolean = false
)

/**
 * 创建题目请求
 */
@Serializable
data class QuestionRequest(
    val content: String,
    val type: String,
    val options: String? = null,
    val answer: String,
    val analysis: String? = null,
    val difficulty: String = "medium",
    val category: String? = null,
    val score: Int = 5
)

/**
 * 添加题目到考试的请求
 */
@Serializable
data class ExamQuestionRequest(
    val questionId: Long,
    val sequence: Int,
    val score: Int
)


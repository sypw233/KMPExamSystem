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
    val id: Long? = null,
    val examId: Long,
    val questionId: Long,
    val score: Int = 0,
    // 后端扁平字段（原始响应）
    val questionContent: String? = null,
    val questionType: String? = null,
    val questionDifficulty: String? = null,
    val sequence: Int = 0,
    // 前端嵌套兼容字段（Repository 层自动填充）
    val orderNum: Int = 0,
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
    val needsGrading: Boolean = false,
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

/**
 * 分页考试响应
 */
@Serializable
data class PageExamResponse(
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val size: Int = 20,
    val content: List<ExamResponse> = emptyList(),
    val number: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true,
    val numberOfElements: Int = 0,
    val empty: Boolean = true
)

/**
 * 学生考试试卷题目响应（不含答案和解析）
 */
@Serializable
data class ExamPaperQuestionResponse(
    val examId: Long,
    val questionId: Long,
    val questionContent: String,
    val questionType: String,
    val questionDifficulty: String,
    val options: String? = null,
    val score: Int = 0,
    val sequence: Int = 0
)

/**
 * 智能随机组卷选项
 */
@Serializable
data class ComposeOptions(
    val shuffleQuestions: Boolean = true,
    val lenientMode: Boolean = false
)

/**
 * 单条组卷规则（按题型）
 */
@Serializable
data class SectionRule(
    val type: String,                        // single, multiple, true_false, fill_blank, short_answer
    val count: Int,
    val scorePerQuestion: Int,
    val difficultyDistribution: Map<String, Int>? = null   // key: easy/medium/hard, value: 数量
)

/**
 * 智能随机组卷请求
 */
@Serializable
data class ComposeRandomExamRequest(
    val bankId: Long,
    val expectedTotalScore: Int? = null,
    val sections: List<SectionRule>,
    val options: ComposeOptions? = null
)

/**
 * 分页题目响应
 */
@Serializable
data class PageQuestionResponse(
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val size: Int = 20,
    val content: List<QuestionResponse> = emptyList(),
    val number: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true,
    val numberOfElements: Int = 0,
    val empty: Boolean = true
)


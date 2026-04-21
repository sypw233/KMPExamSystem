package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * 提交考试答案请求
 */
@Serializable
data class SubmissionRequest(
    val examId: Long,
    val answers: Map<String, String>   // questionId -> answer 字符串
)

/**
 * 考试提交记录响应
 */
@Serializable
data class SubmissionResponse(
    val id: Long,
    val examId: Long,
    val examTitle: String,
    val userId: Long,
    val userName: String,
    val answers: String? = null,           // JSON 字符串
    val objectiveScore: Int? = null,
    val subjectiveScore: Int? = null,
    val totalScore: Int? = null,
    val status: Int = 0,                   // 0-进行中, 1-已提交, 2-已批改
    val statusDescription: String? = null,
    val switchCount: Int = 0,
    val startTime: String? = null,
    val submitTime: String? = null,
    val submitDetail: String? = null       // 评分详情 JSON 字符串
)

/**
 * 开始考试响应（返回 submissionId 用于后续操作）
 */
@Serializable
data class StartExamResponse(
    val submissionId: Long,
    val examId: Long,
    val startTime: String? = null
)

/**
 * 记录监考事件请求
 */
@Serializable
data class ProctoringEventRequest(
    val submissionId: Long,
    val eventType: String,                 // SWITCH_TAB, EXIT_FULLSCREEN, etc
    val description: String? = null
)

/**
 * 主观题手动评分请求
 */
@Serializable
data class GradeRequest(
    val questionId: Long,
    val score: Int,
    val comment: String? = null
)

/**
 * 单道主观题评分详情
 */
@Serializable
data class SubjectiveGradeDetail(
    val questionId: Long,
    val score: Int,
    val comment: String? = null
)


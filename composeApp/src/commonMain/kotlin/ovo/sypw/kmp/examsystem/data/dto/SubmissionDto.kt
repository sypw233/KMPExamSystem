package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * 提交考试答案请求
 */
@Serializable
data class SubmissionRequest(
    val examId: Long,
    val answers: Map<Long, String>     // questionId -> answer 字符串
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
 * 记录监考事件请求
 */
@Serializable
data class ProctoringEventRequest(
    val examId: Long,
    val eventType: String,                 // tab_switch, exit_fullscreen, blur
    val detail: String? = null
)

/**
 * 主观题手动评分请求
 */
@Serializable
data class GradeRequest(
    val questionScores: Map<Long, Int>     // questionId -> score
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

/**
 * 监考事件响应
 */
@Serializable
data class ProctoringEventResponse(
    val recorded: Boolean = false,
    val autoSubmitted: Boolean = false
)

/**
 * 监考数据详情响应
 */
@Serializable
data class ProctoringDataResponse(
    val submissionId: Long,
    val examId: Long,
    val userId: Long,
    val switchCount: Int = 0,
    val proctoringData: JsonObject = JsonObject(emptyMap()),
    val status: Int = 0
)

/** 分页提交记录响应 */
typealias PageSubmissionResponse = PageResponse<SubmissionResponse>

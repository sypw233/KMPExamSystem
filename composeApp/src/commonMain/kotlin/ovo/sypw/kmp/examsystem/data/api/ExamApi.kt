package ovo.sypw.kmp.examsystem.data.api

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.BatchDeleteRequest
import ovo.sypw.kmp.examsystem.data.dto.BatchDeleteResult
import ovo.sypw.kmp.examsystem.data.dto.ComposeRandomExamRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamPaperQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.data.dto.PageExamResponse
import io.ktor.client.HttpClient
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.parseData

/**
 * 考试相关 API 服务
 * @param httpClient 共享的HTTP客户端实例
 */
class ExamApi(httpClient: HttpClient) : BaseApiService(httpClient) {

    companion object {
        private const val EXAM_ENDPOINT = "/api/exams"
    }

    /** 查询考试列表（分页） */
    suspend fun getAllExams(
        token: String,
        page: Int = 0,
        size: Int = 20,
        status: Int? = null,
        courseId: Long? = null
    ): ApiResponse<PageExamResponse> {
        val params = buildMap<String, Any> {
            put("page", page)
            put("size", size)
            status?.let { put("status", it) }
            courseId?.let { put("courseId", it) }
        }
        val result = getWithToken(endpoint = EXAM_ENDPOINT, token = token, parameters = params)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 获取考试详情 */
    suspend fun getExamDetail(token: String, examId: Long): ApiResponse<ExamResponse> {
        val result = getWithToken(endpoint = "$EXAM_ENDPOINT/$examId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 获取我的考试（教师：创建的；学生：可参加的） */
    suspend fun getMyExams(token: String): ApiResponse<List<ExamResponse>> {
        val result = getWithToken(endpoint = "$EXAM_ENDPOINT/my", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 学生：获取可参加的考试 */
    suspend fun getMyAvailableExams(token: String): ApiResponse<List<ExamResponse>> {
        val result = getWithToken(endpoint = "$EXAM_ENDPOINT/my-available", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 学生：获取已完成的考试 */
    suspend fun getMyCompletedExams(token: String): ApiResponse<List<ExamResponse>> {
        val result = getWithToken(endpoint = "$EXAM_ENDPOINT/my-completed", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 按状态筛选考试 (0-草稿, 1-已发布, 2-已结束) */
    suspend fun getExamsByStatus(
        token: String,
        status: Int,
        page: Int = 0,
        size: Int = 20
    ): ApiResponse<PageExamResponse> {
        return getAllExams(token = token, page = page, size = size, status = status)
    }

    /** 获取指定课程的所有考试 */
    suspend fun getExamsByCourse(
        token: String,
        courseId: Long,
        page: Int = 0,
        size: Int = 20
    ): ApiResponse<PageExamResponse> {
        val result = getWithToken(
            endpoint = "$EXAM_ENDPOINT/course/$courseId",
            token = token,
            parameters = mapOf("page" to page, "size" to size)
        )
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 获取考试的所有题目（按顺序） */
    suspend fun getExamQuestions(token: String, examId: Long): ApiResponse<List<ExamQuestionResponse>> {
        val result = getWithToken(endpoint = "$EXAM_ENDPOINT/$examId/questions", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 创建考试（教师/管理员） */
    suspend fun createExam(token: String, request: ExamRequest): ApiResponse<ExamResponse> {
        val result = postWithToken(endpoint = EXAM_ENDPOINT, token = token, body = request)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 更新考试（仅草稿状态） */
    suspend fun updateExam(token: String, examId: Long, request: ExamRequest): ApiResponse<ExamResponse> {
        val result = putWithToken(endpoint = "$EXAM_ENDPOINT/$examId", token = token, body = request)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 删除考试 */
    suspend fun deleteExam(token: String, examId: Long): ApiResponse<Unit> {
        val result = deleteWithToken(endpoint = "$EXAM_ENDPOINT/$examId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, Unit)
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 发布考试（POST /api/exams/{id}/publish） */
    suspend fun publishExam(token: String, examId: Long): ApiResponse<ExamResponse> {
        val result = postWithToken(
            endpoint = "$EXAM_ENDPOINT/$examId/publish",
            token = token
        )
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 添加题目到考试 */
    suspend fun addQuestionToExam(
        token: String,
        examId: Long,
        request: ExamQuestionRequest
    ): ApiResponse<ExamQuestionResponse> {
        val result = postWithToken(endpoint = "$EXAM_ENDPOINT/$examId/questions", token = token, body = request)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 从考试移除题目（仅草稿状态） */
    suspend fun removeQuestionFromExam(token: String, examId: Long, questionId: Long): ApiResponse<Unit> {
        val result = deleteWithToken(endpoint = "$EXAM_ENDPOINT/$examId/questions/$questionId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, Unit)
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 部分更新考试（如发布草稿考试） */
    suspend fun patchExam(token: String, examId: Long, status: Int): ApiResponse<ExamResponse> {
        val result = patchWithToken(
            endpoint = "$EXAM_ENDPOINT/$examId",
            token = token,
            parameters = mapOf("status" to status)
        )
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * 批量删除考试
     * @param token 访问令牌
     * @param request 批量删除请求（ID列表）
     * @return 批量删除结果
     */
    suspend fun batchDeleteExams(token: String, request: BatchDeleteRequest): ApiResponse<BatchDeleteResult> {
        val result = postWithToken(
            endpoint = "$EXAM_ENDPOINT/batch-delete",
            token = token,
            body = request
        )
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * 获取考试试卷（学生）
     * 学生获取考试题目列表（不含答案和解析）
     * @param token 访问令牌
     * @param examId 考试ID
     * @return 试卷题目列表
     */
    suspend fun getExamPaper(token: String, examId: Long): ApiResponse<List<ExamPaperQuestionResponse>> {
        val result = getWithToken(endpoint = "$EXAM_ENDPOINT/$examId/paper", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * 智能随机组卷
     * 根据组卷规则从指定题库中随机抽取题目生成试卷
     * 会清空考试原有题目，重新生成；仅草稿状态的考试可以组卷
     * @param token 访问令牌
     * @param examId 考试ID
     * @param request 组卷请求
     * @return 更新后的考试详情
     */
    suspend fun composeRandomExam(
        token: String,
        examId: Long,
        request: ComposeRandomExamRequest
    ): ApiResponse<ExamResponse> {
        val result = postWithToken(
            endpoint = "$EXAM_ENDPOINT/$examId/compose-random",
            token = token,
            body = request
        )
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }
}

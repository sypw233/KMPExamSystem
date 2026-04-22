package ovo.sypw.kmp.examsystem.data.api

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.parseData

/**
 * 考试相关 API 服务
 */
class ExamApi : BaseApiService() {

    companion object {
        private const val EXAM_ENDPOINT = "/api/exams"
    }

    /** 获取所有考试列表 */
    suspend fun getAllExams(token: String): ApiResponse<List<ExamResponse>> {
        val result = getWithToken(endpoint = EXAM_ENDPOINT, token = token)
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

    /** 按状态筛选考试 (0-草稿, 1-已发布, 2-已结束) */
    suspend fun getExamsByStatus(token: String, status: Int): ApiResponse<List<ExamResponse>> {
        val result = getWithToken(endpoint = "$EXAM_ENDPOINT/status/$status", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 获取指定课程的所有考试 */
    suspend fun getExamsByCourse(token: String, courseId: Long): ApiResponse<List<ExamResponse>> {
        val result = getWithToken(endpoint = "$EXAM_ENDPOINT/course/$courseId", token = token)
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

    /** 发布考试（POST） */
    suspend fun publishExam(token: String, examId: Long): ApiResponse<ExamResponse> {
        val result = postWithToken(endpoint = "$EXAM_ENDPOINT/$examId/publish", token = token)
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
}

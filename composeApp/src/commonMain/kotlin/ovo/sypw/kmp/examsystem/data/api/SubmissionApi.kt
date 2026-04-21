package ovo.sypw.kmp.examsystem.data.api

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.GradeRequest
import ovo.sypw.kmp.examsystem.data.dto.ProctoringEventRequest
import ovo.sypw.kmp.examsystem.data.dto.StartExamResponse
import ovo.sypw.kmp.examsystem.data.dto.SubmissionRequest
import ovo.sypw.kmp.examsystem.data.dto.SubmissionResponse
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.parseData

/**
 * 答题与评分相关 API 服务
 */
class SubmissionApi : BaseApiService() {

    companion object {
        private const val SUBMISSION_ENDPOINT = "/api/submissions"
    }

    /**
     * 开始考试（创建答题记录）
     */
    suspend fun startExam(token: String, examId: Long): ApiResponse<StartExamResponse> {
        val result = postWithToken(
            endpoint = "$SUBMISSION_ENDPOINT/start",
            token = token,
            body = mapOf("examId" to examId)
        )
        return when (result) {
            is NetworkResult.Success -> {
                val data = result.data.parseData<StartExamResponse>()
                ApiResponse(code = result.data.code, message = result.data.msg, data = data)
            }
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }

    /**
     * 提交考试答案
     */
    suspend fun submitExam(token: String, request: SubmissionRequest): ApiResponse<SubmissionResponse> {
        val result = postWithToken(
            endpoint = SUBMISSION_ENDPOINT,
            token = token,
            body = request
        )
        return when (result) {
            is NetworkResult.Success -> {
                val data = result.data.parseData<SubmissionResponse>()
                ApiResponse(code = result.data.code, message = result.data.msg, data = data)
            }
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }

    /**
     * 获取提交详情
     */
    suspend fun getSubmissionDetail(token: String, submissionId: Long): ApiResponse<SubmissionResponse> {
        val result = getWithToken(endpoint = "$SUBMISSION_ENDPOINT/$submissionId", token = token)
        return when (result) {
            is NetworkResult.Success -> {
                val data = result.data.parseData<SubmissionResponse>()
                ApiResponse(code = result.data.code, message = result.data.msg, data = data)
            }
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }

    /**
     * 获取学生的所有成绩记录
     */
    suspend fun getMyGrades(token: String, userId: Long): ApiResponse<List<SubmissionResponse>> {
        val result = getWithToken(endpoint = "$SUBMISSION_ENDPOINT/grades/$userId", token = token)
        return when (result) {
            is NetworkResult.Success -> {
                val data = result.data.parseData<List<SubmissionResponse>>()
                ApiResponse(code = result.data.code, message = result.data.msg, data = data)
            }
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }

    /**
     * 记录监考事件（切屏、全屏退出等）
     */
    suspend fun recordProctoringEvent(token: String, request: ProctoringEventRequest): ApiResponse<Unit> {
        val result = postWithToken(
            endpoint = "$SUBMISSION_ENDPOINT/proctoring",
            token = token,
            body = request
        )
        return when (result) {
            is NetworkResult.Success -> ApiResponse(code = result.data.code, message = result.data.msg, data = Unit)
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }

    /**
     * 主观题手动评分（教师）
     * @param submissionId 提交记录 ID
     * @param grades 各主观题评分列表
     */
    suspend fun gradeSubmission(
        token: String,
        submissionId: Long,
        grades: List<GradeRequest>
    ): ApiResponse<SubmissionResponse> {
        val result = postWithToken(
            endpoint = "$SUBMISSION_ENDPOINT/$submissionId/grade",
            token = token,
            body = mapOf("grades" to grades)
        )
        return when (result) {
            is NetworkResult.Success -> {
                val data = result.data.parseData<SubmissionResponse>()
                ApiResponse(code = result.data.code, message = result.data.msg, data = data)
            }
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }

    /**
     * 获取某场考试的所有提交记录（教师/管理员）
     */
    suspend fun getExamSubmissions(token: String, examId: Long): ApiResponse<List<SubmissionResponse>> {
        val result = getWithToken(endpoint = "$SUBMISSION_ENDPOINT/exam/$examId", token = token)
        return when (result) {
            is NetworkResult.Success -> {
                val data = result.data.parseData<List<SubmissionResponse>>()
                ApiResponse(code = result.data.code, message = result.data.msg, data = data)
            }
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }
}


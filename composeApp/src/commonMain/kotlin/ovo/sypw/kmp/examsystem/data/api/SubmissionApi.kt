package ovo.sypw.kmp.examsystem.data.api

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.GradeRequest
import ovo.sypw.kmp.examsystem.data.dto.PageSubmissionResponse
import ovo.sypw.kmp.examsystem.data.dto.ProctoringEventRequest
import ovo.sypw.kmp.examsystem.data.dto.ProctoringEventResponse
import ovo.sypw.kmp.examsystem.data.dto.SubmissionRequest
import ovo.sypw.kmp.examsystem.data.dto.SubmissionResponse
import io.ktor.client.HttpClient
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.parseData

/**
 * 答题与评分相关 API 服务
 * @param httpClient 共享的HTTP客户端实例
 */
class SubmissionApi(httpClient: HttpClient) : BaseApiService(httpClient) {

    companion object {
        private const val SUBMISSION_ENDPOINT = "/api/submissions"
    }

    /**
     * 开始考试（创建答题记录），后端返回 SubmissionResponse
     * POST /api/exams/{examId}/submissions
     */
    suspend fun startExam(token: String, examId: Long): ApiResponse<SubmissionResponse> {
        val result = postWithToken(
            endpoint = "/api/exams/$examId/submissions",
            token = token
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
        val result = getWithToken(endpoint = "$SUBMISSION_ENDPOINT/user/$userId", token = token)
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
     * @param grades 评分 Map（questionId -> score）
     */
    suspend fun gradeSubmission(
        token: String,
        submissionId: Long,
        grades: Map<Long, Int>
    ): ApiResponse<SubmissionResponse> {
        val result = postWithToken(
            endpoint = "$SUBMISSION_ENDPOINT/$submissionId/grade",
            token = token,
            body = GradeRequest(questionScores = grades)
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
     * 获取某次提交的监考记录
     */
    suspend fun getProctoringEvents(token: String, submissionId: Long): ApiResponse<List<ProctoringEventResponse>> {
        val result = getWithToken(endpoint = "$SUBMISSION_ENDPOINT/$submissionId/proctoring", token = token)
        return when (result) {
            is NetworkResult.Success -> {
                val data = result.data.parseData<List<ProctoringEventResponse>>()
                ApiResponse(code = result.data.code, message = result.data.msg, data = data)
            }
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }

    /**
     * 获取某次考试的所有提交记录
     * GET /api/submissions/exam/{examId}
     */
    suspend fun getSubmissionsByExamId(token: String, examId: Long): ApiResponse<List<SubmissionResponse>> {
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

    /**
     * 分页查询提交记录
     */
    suspend fun querySubmissions(
        token: String,
        examId: Long? = null,
        userId: Long? = null,
        page: Int = 0,
        size: Int = 20
    ): ApiResponse<PageSubmissionResponse> {
        val params = buildMap<String, Any> {
            put("page", page)
            put("size", size)
            examId?.let { put("examId", it) }
            userId?.let { put("userId", it) }
        }
        val result = getWithToken(endpoint = SUBMISSION_ENDPOINT, token = token, parameters = params)
        return when (result) {
            is NetworkResult.Success -> {
                val data = result.data.parseData<PageSubmissionResponse>()
                ApiResponse(code = result.data.code, message = result.data.msg, data = data)
            }
            is NetworkResult.Error -> ApiResponse(code = 500, message = result.message, data = null)
            else -> ApiResponse(code = 500, message = "未知状态", data = null)
        }
    }
}


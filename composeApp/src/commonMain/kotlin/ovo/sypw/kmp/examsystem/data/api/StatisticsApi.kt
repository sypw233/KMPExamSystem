package ovo.sypw.kmp.examsystem.data.api

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.CourseStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamScoreExportRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.StudentStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.SystemOverviewResponse
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.client.HttpClient
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.parseData

/**
 * 统计分析 API 服务（全部 5 个接口）
 * @param httpClient 共享的HTTP客户端实例
 */
class StatisticsApi(httpClient: HttpClient) : BaseApiService(httpClient) {

    companion object {
        private const val STATISTICS_ENDPOINT = "/api/statistics"
    }

    /** 学生成绩统计 */
    suspend fun getStudentStatistics(token: String, userId: Long): ApiResponse<StudentStatisticsResponse> {
        val result = getWithToken(endpoint = "$STATISTICS_ENDPOINT/student/$userId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 考试统计（教师/管理员） */
    suspend fun getExamStatistics(token: String, examId: Long): ApiResponse<ExamStatisticsResponse> {
        val result = getWithToken(endpoint = "$STATISTICS_ENDPOINT/exam/$examId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 系统总览（管理员） */
    suspend fun getSystemOverview(token: String): ApiResponse<SystemOverviewResponse> {
        val result = getWithToken(endpoint = "$STATISTICS_ENDPOINT/overview", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 题目统计（答题情况/正确率） */
    suspend fun getQuestionStatistics(token: String, questionId: Long): ApiResponse<QuestionStatisticsResponse> {
        val result = getWithToken(endpoint = "$STATISTICS_ENDPOINT/question/$questionId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 课程统计 */
    suspend fun getCourseStatistics(token: String, courseId: Long): ApiResponse<CourseStatisticsResponse> {
        val result = getWithToken(endpoint = "$STATISTICS_ENDPOINT/course/$courseId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 导出考试统计数据 */
    suspend fun exportExamStatistics(
        token: String,
        examId: Long,
        request: ExamScoreExportRequest = ExamScoreExportRequest()
    ): ApiResponse<ByteArray> {
        return try {
            val response = httpClient.post(HttpClientConfig.getApiUrl("$STATISTICS_ENDPOINT/exam/$examId/export")) {
                header(io.ktor.http.HttpHeaders.Authorization, "Bearer $token")
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == io.ktor.http.HttpStatusCode.OK) {
                val bytes = response.body<ByteArray>()
                ApiResponse(200, "导出成功", bytes)
            } else {
                ApiResponse(response.status.value, "导出失败", null)
            }
        } catch (e: Exception) {
            ApiResponse(500, e.message ?: "导出异常", null)
        }
    }
}

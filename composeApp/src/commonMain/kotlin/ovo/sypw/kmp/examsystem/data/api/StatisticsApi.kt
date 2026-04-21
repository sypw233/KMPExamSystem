package ovo.sypw.kmp.examsystem.data.api

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.CourseStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.StudentStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.SystemOverviewResponse
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.parseData

/**
 * 统计分析 API 服务（全部 5 个接口）
 */
class StatisticsApi : BaseApiService() {

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
}

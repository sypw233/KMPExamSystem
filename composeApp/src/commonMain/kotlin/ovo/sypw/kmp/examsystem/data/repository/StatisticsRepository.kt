package ovo.sypw.kmp.examsystem.data.repository

import ovo.sypw.kmp.examsystem.data.api.StatisticsApi
import ovo.sypw.kmp.examsystem.data.dto.CourseStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamScoreExportRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.StudentStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.SystemOverviewResponse
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

/**
 * Statistics repository.
 */
class StatisticsRepository(
    private val statisticsApi: StatisticsApi,
    private val tokenStorage: TokenStorage
) {
    suspend fun getStudentStatistics(userId: Long): Result<StudentStatisticsResponse> {
        return runWithToken { token ->
            val response = statisticsApi.getStudentStatistics(token, userId)
            if (response.code == 200 && response.data != null) response.data
            else throw Exception(response.message)
        }
    }

    suspend fun getExamStatistics(examId: Long): Result<ExamStatisticsResponse> {
        return runWithToken { token ->
            val response = statisticsApi.getExamStatistics(token, examId)
            if (response.code == 200 && response.data != null) response.data
            else throw Exception(response.message)
        }
    }

    suspend fun getSystemOverview(): Result<SystemOverviewResponse> {
        return runWithToken { token ->
            val response = statisticsApi.getSystemOverview(token)
            if (response.code == 200 && response.data != null) response.data
            else throw Exception(response.message)
        }
    }

    suspend fun getCourseStatistics(courseId: Long): Result<CourseStatisticsResponse> {
        return runWithToken { token ->
            val response = statisticsApi.getCourseStatistics(token, courseId)
            if (response.code == 200 && response.data != null) response.data
            else throw Exception(response.message)
        }
    }

    suspend fun getQuestionStatistics(questionId: Long): Result<QuestionStatisticsResponse> {
        return runWithToken { token ->
            val response = statisticsApi.getQuestionStatistics(token, questionId)
            if (response.code == 200 && response.data != null) response.data
            else throw Exception(response.message)
        }
    }

    suspend fun exportExamStatistics(examId: Long, request: ExamScoreExportRequest = ExamScoreExportRequest()): Result<ByteArray> {
        return runWithToken { token ->
            val response = statisticsApi.exportExamStatistics(token, examId, request)
            if (response.code == 200 && response.data != null) response.data
            else throw Exception(response.message)
        }
    }

    private suspend fun <T> runWithToken(block: suspend (String) -> T): Result<T> {
        return try {
            val token = tokenStorage.getAccessToken() ?: throw Exception("未登录")
            Result.success(block(token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

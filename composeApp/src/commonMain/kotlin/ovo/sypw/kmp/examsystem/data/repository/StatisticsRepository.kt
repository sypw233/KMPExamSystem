package ovo.sypw.kmp.examsystem.data.repository

import ovo.sypw.kmp.examsystem.data.api.StatisticsApi
import ovo.sypw.kmp.examsystem.data.dto.ExamStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.StudentStatisticsResponse
import ovo.sypw.kmp.examsystem.data.dto.SystemOverviewResponse
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

/**
 * 统计分析仓库
 */
class StatisticsRepository(
    private val statisticsApi: StatisticsApi,
    private val tokenStorage: TokenStorage
) {

    /**
     * 获取当前登录学生的统计数据
     */
    suspend fun getStudentStatistics(userId: Long): Result<StudentStatisticsResponse> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return Result.failure(Exception("未登录"))
            val response = statisticsApi.getStudentStatistics(token, userId)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取考试统计（教师/管理员）
     */
    suspend fun getExamStatistics(examId: Long): Result<ExamStatisticsResponse> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return Result.failure(Exception("未登录"))
            val response = statisticsApi.getExamStatistics(token, examId)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取系统概览（管理员）
     */
    suspend fun getSystemOverview(): Result<SystemOverviewResponse> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return Result.failure(Exception("未登录"))
            val response = statisticsApi.getSystemOverview(token)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

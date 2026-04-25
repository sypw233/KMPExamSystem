package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * 成绩记录（用于学生统计）
 */
@Serializable
data class StudentScoreRecord(
    val examId: Long,
    val examTitle: String,
    val score: Int = 0,
    val submitTime: String? = null,
    val submissionId: Long? = null
)

/**
 * 学生统计响应
 */
@Serializable
data class StudentStatisticsResponse(
    val studentId: Long,
    val studentName: String,
    val totalExams: Int = 0,
    val averageScore: Double = 0.0,
    val highestScore: Int = 0,
    val lowestScore: Int = 0,
    val scores: List<StudentScoreRecord> = emptyList()
)

/**
 * 考试统计响应（教师/管理员）
 */
@Serializable
data class ExamStatisticsResponse(
    val examId: Long,
    val examTitle: String,
    val totalStudents: Int = 0,
    val submittedCount: Int = 0,
    val completionRate: Double = 0.0,
    val averageScore: Double = 0.0,
    val highestScore: Int = 0,
    val lowestScore: Int = 0,
    val passCount: Int = 0,
    val passRate: Double = 0.0,
    val scoreDistribution: Map<String, Int> = emptyMap()
)

/**
 * 系统概览（管理员）
 */
@Serializable
data class SystemOverviewResponse(
    val totalUsers: Int = 0,
    val studentCount: Int = 0,
    val teacherCount: Int = 0,
    val adminCount: Int = 0,
    val totalCourses: Int = 0,
    val totalExams: Int = 0,
    val totalQuestions: Int = 0,
    val totalSubmissions: Int = 0
)

/**
 * 导出考试成绩请求
 */
@Serializable
data class ExamScoreExportRequest(
    val includeStudentId: Boolean = true,
    val includeStudentName: Boolean = true,
    val includeScore: Boolean = true,
    val includeSubmitTime: Boolean = true,
    val includeStatus: Boolean = true,
    val includeSwitchCount: Boolean = true,
    val includeProctoringAbnormal: Boolean = false
)

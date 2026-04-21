package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * 成绩记录（用于学生统计）
 */
@Serializable
data class StudentScoreRecord(
    val submissionId: Long,
    val examId: Long,
    val examTitle: String,
    val courseName: String,
    val totalScore: Int? = null,
    val objectiveScore: Int? = null,
    val subjectiveScore: Int? = null,
    val submitTime: String? = null,
    val status: Int = 0
)

/**
 * 学生统计响应
 */
@Serializable
data class StudentStatisticsResponse(
    val userId: Long,
    val userName: String,
    val totalExams: Int = 0,
    val completedExams: Int = 0,
    val averageScore: Double = 0.0,
    val highestScore: Int = 0,
    val scoreRecords: List<StudentScoreRecord> = emptyList()
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
    val averageScore: Double = 0.0,
    val highestScore: Int = 0,
    val lowestScore: Int = 0,
    val passRate: Double = 0.0
)

/**
 * 系统概览（管理员）
 */
@Serializable
data class SystemOverviewResponse(
    val totalUsers: Long = 0,
    val totalStudents: Long = 0,
    val totalTeachers: Long = 0,
    val totalCourses: Long = 0,
    val totalExams: Long = 0,
    val totalSubmissions: Long = 0
)

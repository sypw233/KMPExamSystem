package ovo.sypw.kmp.examsystem.data.dto

import kotlinx.serialization.Serializable

/**
 * 课程响应数据
 */
@Serializable
data class CourseResponse(
    val id: Long,
    val courseName: String,
    val description: String? = null,
    val teacherId: Long,
    val teacherName: String,
    val status: Int = 1,
    val enrollmentCount: Long = 0,
    val createTime: String? = null
)

/**
 * 选课记录响应数据
 */
@Serializable
data class EnrollmentResponse(
    val id: Long,
    val studentId: Long,
    val studentName: String,
    val courseId: Long,
    val courseName: String,
    val enrollmentTime: String? = null,
    val status: Int = 1
)

/**
 * 创建/更新课程请求
 */
@Serializable
data class CourseRequest(
    val courseName: String,
    val description: String? = null,
    val teacherId: Long? = null
)


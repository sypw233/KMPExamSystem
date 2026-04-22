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
    val teacherId: Long? = null,
    val status: Int = 1
)

/**
 * 分页课程响应
 */
@Serializable
data class PageCourseResponse(
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val size: Int = 20,
    val content: List<CourseResponse> = emptyList(),
    val number: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true,
    val numberOfElements: Int = 0,
    val empty: Boolean = true
)


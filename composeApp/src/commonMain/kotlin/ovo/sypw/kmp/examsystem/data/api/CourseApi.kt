package ovo.sypw.kmp.examsystem.data.api

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.CourseRequest
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.data.dto.EnrollmentResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.data.dto.PageCourseResponse
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.parseData

/**
 * 课程相关 API 服务
 */
class CourseApi : BaseApiService() {

    companion object {
        private const val COURSE_ENDPOINT = "/api/courses"
    }

    /** 获取所有活跃课程（分页） */
    suspend fun getAllActiveCourses(token: String, page: Int = 0, size: Int = 20): ApiResponse<PageCourseResponse> {
        val result = getWithToken(
            endpoint = COURSE_ENDPOINT,
            token = token,
            parameters = mapOf("page" to page, "size" to size)
        )
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 获取课程详情 */
    suspend fun getCourseDetail(token: String, courseId: Long): ApiResponse<CourseResponse> {
        val result = getWithToken(endpoint = "$COURSE_ENDPOINT/$courseId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 获取我的课程（教师返回创建的，学生返回已选的） */
    suspend fun getMyCourses(token: String): ApiResponse<List<CourseResponse>> {
        val result = getWithToken(endpoint = "$COURSE_ENDPOINT/my", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 学生选课 */
    suspend fun enrollCourse(token: String, courseId: Long): ApiResponse<EnrollmentResponse> {
        val result = postWithToken(endpoint = "$COURSE_ENDPOINT/$courseId/enroll", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 获取我的选课记录 */
    suspend fun getMyEnrollments(token: String): ApiResponse<List<EnrollmentResponse>> {
        val result = getWithToken(endpoint = "$COURSE_ENDPOINT/my-enrollments", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 创建课程（教师/管理员） */
    suspend fun createCourse(token: String, request: CourseRequest): ApiResponse<CourseResponse> {
        val result = postWithToken(endpoint = COURSE_ENDPOINT, token = token, body = request)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 更新课程 */
    suspend fun updateCourse(token: String, courseId: Long, request: CourseRequest): ApiResponse<CourseResponse> {
        val result = putWithToken(endpoint = "$COURSE_ENDPOINT/$courseId", token = token, body = request)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 删除课程 */
    suspend fun deleteCourse(token: String, courseId: Long): ApiResponse<Unit> {
        val result = deleteWithToken(endpoint = "$COURSE_ENDPOINT/$courseId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, Unit)
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 获取课程下的考试列表 */
    suspend fun getCourseExams(token: String, courseId: Long): ApiResponse<List<ExamResponse>> {
        val result = getWithToken(endpoint = "$COURSE_ENDPOINT/$courseId/exams", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 获取选课学生列表（教师/管理员） */
    suspend fun getCourseStudents(token: String, courseId: Long): ApiResponse<List<EnrollmentResponse>> {
        val result = getWithToken(endpoint = "$COURSE_ENDPOINT/$courseId/students", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 管理员/教师添加选课学生 */
    suspend fun addStudentToCourse(token: String, courseId: Long, studentId: Long): ApiResponse<EnrollmentResponse> {
        val result = postWithToken(endpoint = "$COURSE_ENDPOINT/$courseId/students/$studentId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 管理员/教师移除选课学生 / 学生退课 */
    suspend fun removeStudentFromCourse(token: String, courseId: Long, studentId: Long): ApiResponse<Unit> {
        val result = deleteWithToken(endpoint = "$COURSE_ENDPOINT/$courseId/students/$studentId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, Unit)
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * 批量添加学生到课程
     * @param token 访问令牌
     * @param courseId 课程ID
     * @param studentIds 学生ID列表
     * @return 选课记录列表
     */
    suspend fun batchAddStudentsToCourse(
        token: String,
        courseId: Long,
        studentIds: List<Long>
    ): ApiResponse<List<EnrollmentResponse>> {
        val result = postWithToken(
            endpoint = "$COURSE_ENDPOINT/$courseId/students",
            token = token,
            body = studentIds
        )
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }
}

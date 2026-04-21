package ovo.sypw.kmp.examsystem.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ovo.sypw.kmp.examsystem.data.api.CourseApi
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.data.dto.EnrollmentResponse
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

/**
 * 课程仓库
 * 负责协调 CourseApi 与 TokenStorage，管理课程相关数据
 */
class CourseRepository(
    private val courseApi: CourseApi,
    private val tokenStorage: TokenStorage
) {

    private val _allCourses = MutableStateFlow<List<CourseResponse>>(emptyList())
    val allCourses: StateFlow<List<CourseResponse>> = _allCourses.asStateFlow()

    private val _myCourses = MutableStateFlow<List<CourseResponse>>(emptyList())
    val myCourses: StateFlow<List<CourseResponse>> = _myCourses.asStateFlow()

    private val _myEnrollments = MutableStateFlow<List<EnrollmentResponse>>(emptyList())
    val myEnrollments: StateFlow<List<EnrollmentResponse>> = _myEnrollments.asStateFlow()

    /**
     * 获取所有活跃课程列表
     */
    suspend fun loadAllCourses(): Result<List<CourseResponse>> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return Result.failure(Exception("未登录"))
            val response = courseApi.getAllActiveCourses(token)
            if (response.code == 200 && response.data != null) {
                _allCourses.value = response.data
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取我的课程（教师：创建的；学生：已选的）
     */
    suspend fun loadMyCourses(): Result<List<CourseResponse>> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return Result.failure(Exception("未登录"))
            val response = courseApi.getMyCourses(token)
            if (response.code == 200 && response.data != null) {
                _myCourses.value = response.data
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 学生选课
     * @param courseId 课程ID
     */
    suspend fun enrollCourse(courseId: Long): Result<EnrollmentResponse> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return Result.failure(Exception("未登录"))
            val response = courseApi.enrollCourse(token, courseId)
            if (response.code == 200 && response.data != null) {
                loadMyEnrollments()
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取我的选课记录
     */
    suspend fun loadMyEnrollments(): Result<List<EnrollmentResponse>> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return Result.failure(Exception("未登录"))
            val response = courseApi.getMyEnrollments(token)
            if (response.code == 200 && response.data != null) {
                _myEnrollments.value = response.data
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

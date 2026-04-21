package ovo.sypw.kmp.examsystem.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ovo.sypw.kmp.examsystem.data.api.CourseApi
import ovo.sypw.kmp.examsystem.data.dto.CourseRequest
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
            if (response.code == 200) {
                val data = response.data ?: emptyList()
                _allCourses.value = data
                Result.success(data)
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
            if (response.code == 200) {
                val data = response.data ?: emptyList()
                _myCourses.value = data
                Result.success(data)
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

    /** 创建课程（教师/管理员） */
    suspend fun createCourse(request: CourseRequest): Result<CourseResponse> = runWithToken { token ->
        val r = courseApi.createCourse(token, request)
        if (r.code == 200 && r.data != null) {
            _allCourses.value = _allCourses.value + r.data
            r.data
        } else throw Exception(r.message)
    }

    /** 更新课程 */
    suspend fun updateCourse(courseId: Long, request: CourseRequest): Result<CourseResponse> = runWithToken { token ->
        val r = courseApi.updateCourse(token, courseId, request)
        if (r.code == 200 && r.data != null) {
            _allCourses.value = _allCourses.value.map { if (it.id == courseId) r.data else it }
            r.data
        } else throw Exception(r.message)
    }

    /** 删除课程 */
    suspend fun deleteCourse(courseId: Long): Result<Unit> = runWithToken { token ->
        val r = courseApi.deleteCourse(token, courseId)
        if (r.code == 200) {
            _allCourses.value = _allCourses.value.filter { it.id != courseId }
            Unit
        } else throw Exception(r.message)
    }

    /** 获取选课学生 */
    suspend fun getCourseStudents(courseId: Long): Result<List<EnrollmentResponse>> = runWithToken { token ->
        val r = courseApi.getCourseStudents(token, courseId)
        if (r.code == 200) r.data ?: emptyList() else throw Exception(r.message)
    }

    /** 添加学生到课程 */
    suspend fun addStudentToCourse(courseId: Long, studentId: Long): Result<EnrollmentResponse> = runWithToken { token ->
        val r = courseApi.addStudentToCourse(token, courseId, studentId)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /** 移除选课学生 */
    suspend fun removeStudentFromCourse(courseId: Long, studentId: Long): Result<Unit> = runWithToken { token ->
        val r = courseApi.removeStudentFromCourse(token, courseId, studentId)
        if (r.code == 200) Unit else throw Exception(r.message)
    }

    /**
     * 获取我的选课记录
     */
    suspend fun loadMyEnrollments(): Result<List<EnrollmentResponse>> {
        return try {
            val token = tokenStorage.getAccessToken() ?: return Result.failure(Exception("未登录"))
            val response = courseApi.getMyEnrollments(token)
            if (response.code == 200) {
                val data = response.data ?: emptyList()
                _myEnrollments.value = data
                Result.success(data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
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

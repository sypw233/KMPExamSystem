package ovo.sypw.kmp.examsystem.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ovo.sypw.kmp.examsystem.data.api.CourseApi
import ovo.sypw.kmp.examsystem.data.dto.CourseRequest
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.data.dto.EnrollmentResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage
import ovo.sypw.kmp.examsystem.utils.Logger
import ovo.sypw.kmp.examsystem.utils.SearchUtils

/**
 * 课程仓库
 * 负责协调 CourseApi 与 TokenStorage，管理课程相关数据
 */
class CourseRepository(
    private val courseApi: CourseApi,
    tokenStorage: TokenStorage
) : BaseRepository(tokenStorage) {

    private val _allCourses = MutableStateFlow<List<CourseResponse>>(emptyList())
    val allCourses: StateFlow<List<CourseResponse>> = _allCourses.asStateFlow()

    private val _myCourses = MutableStateFlow<List<CourseResponse>>(emptyList())
    val myCourses: StateFlow<List<CourseResponse>> = _myCourses.asStateFlow()

    private val _myEnrollments = MutableStateFlow<List<EnrollmentResponse>>(emptyList())
    val myEnrollments: StateFlow<List<EnrollmentResponse>> = _myEnrollments.asStateFlow()

    /**
     * 获取所有活跃课程列表（分页，支持搜索/筛选）
     * @param keyword 搜索关键词（可选）
     * @param status 课程状态筛选（可选）
     * @param teacherId 教师ID筛选（可选）
     */
    suspend fun loadAllCourses(
        keyword: String? = null,
        status: Int? = null,
        teacherId: Long? = null
    ): Result<List<CourseResponse>> = runWithToken { token ->
        fetchAllPages(
            requestPage = { page, size -> courseApi.getAllActiveCourses(token, page, size, keyword, status, teacherId) },
            content = { it.content },
            last = { it.last },
            totalPages = { it.totalPages },
            distinctKey = { it.id }
        ).let {
            SearchUtils.sortByPriority(it, keyword) { course ->
                listOf(course.courseName, course.teacherName, course.description)
            }
        }.also {
            _allCourses.value = it
        }
    }

    /**
     * 获取我的课程（教师：创建的；学生：已选的）
     */
    suspend fun loadMyCourses(): Result<List<CourseResponse>> = runWithToken { token ->
        val response = courseApi.getMyCourses(token)
        if (response.code == 200) {
            val data = response.data ?: emptyList()
            _myCourses.value = data
            data
        } else {
            throw Exception(response.message)
        }
    }

    /**
     * 学生选课
     * @param courseId 课程ID
     */
    suspend fun enrollCourse(courseId: Long): Result<EnrollmentResponse> = runWithToken { token ->
        val response = courseApi.enrollCourse(token, courseId)
        if (response.code == 200 && response.data != null) {
            // 刷新选课记录缓存
            val enrollResult = loadMyEnrollments()
            if (enrollResult.isFailure) {
                Logger.w("CourseRepository", "选课成功但刷新选课记录失败: ${enrollResult.exceptionOrNull()?.message}")
            }
            response.data
        } else {
            throw Exception(response.message)
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

    /** 获取课程详情 */
    suspend fun getCourseDetail(courseId: Long): Result<CourseResponse> = runWithToken { token ->
        val r = courseApi.getCourseDetail(token, courseId)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /** 获取课程下的考试列表 */
    suspend fun getCourseExams(courseId: Long): Result<List<ExamResponse>> = runWithToken { token ->
        fetchAllPages(
            requestPage = { page, size -> courseApi.getCourseExams(token, courseId, page, size) },
            content = { it.content },
            last = { it.last },
            totalPages = { it.totalPages },
            distinctKey = { it.id }
        )
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

    /** 批量添加学生到课程 */
    suspend fun batchAddStudentsToCourse(courseId: Long, studentIds: List<Long>): Result<List<EnrollmentResponse>> = runWithToken { token ->
        val r = courseApi.batchAddStudentsToCourse(token, courseId, studentIds)
        if (r.code == 200) r.data ?: emptyList() else throw Exception(r.message)
    }

    /** 移除选课学生 */
    suspend fun removeStudentFromCourse(courseId: Long, studentId: Long): Result<Unit> = runWithToken { token ->
        val r = courseApi.removeStudentFromCourse(token, courseId, studentId)
        if (r.code == 200) Unit else throw Exception(r.message)
    }

    /**
     * 获取我的选课记录
     */
    suspend fun loadMyEnrollments(): Result<List<EnrollmentResponse>> = runWithToken { token ->
        val response = courseApi.getMyEnrollments(token)
        if (response.code == 200) {
            val data = response.data ?: emptyList()
            _myEnrollments.value = data
            data
        } else {
            throw Exception(response.message)
        }
    }

}

package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.CourseRequest
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.data.dto.EnrollmentResponse
import ovo.sypw.kmp.examsystem.data.repository.CourseRepository

/** 课程列表 UI 状态 */
sealed interface CourseUiState {
    data object Loading : CourseUiState
    data class Success(val courses: List<CourseResponse>) : CourseUiState
    data class Error(val message: String) : CourseUiState
}

/** 选课操作状态 */
sealed interface EnrollState {
    data object Idle : EnrollState
    data object Loading : EnrollState
    data class Success(val enrollment: EnrollmentResponse) : EnrollState
    data class Error(val message: String) : EnrollState
}

/** 课程管理操作状态 */
sealed interface CourseActionState {
    data object Idle : CourseActionState
    data object Loading : CourseActionState
    data class Success(val message: String) : CourseActionState
    data class Error(val message: String) : CourseActionState
}

/**
 * 课程 ViewModel
 * 管理课程列表、选课、CRUD
 */
class CourseViewModel(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _allCoursesState = MutableStateFlow<CourseUiState>(CourseUiState.Loading)
    val allCoursesState: StateFlow<CourseUiState> = _allCoursesState.asStateFlow()

    private val _myCoursesState = MutableStateFlow<CourseUiState>(CourseUiState.Loading)
    val myCoursesState: StateFlow<CourseUiState> = _myCoursesState.asStateFlow()

    private val _enrollState = MutableStateFlow<EnrollState>(EnrollState.Idle)
    val enrollState: StateFlow<EnrollState> = _enrollState.asStateFlow()

    private val _actionState = MutableStateFlow<CourseActionState>(CourseActionState.Idle)
    val actionState: StateFlow<CourseActionState> = _actionState.asStateFlow()

    // 当前正在编辑/查看的课程学生列表
    private val _courseStudents = MutableStateFlow<List<EnrollmentResponse>>(emptyList())
    val courseStudents: StateFlow<List<EnrollmentResponse>> = _courseStudents.asStateFlow()

    init {
        loadAllCourses()
        loadMyCourses()
    }

    /** 加载所有活跃课程 */
    fun loadAllCourses() {
        viewModelScope.launch {
            _allCoursesState.value = CourseUiState.Loading
            courseRepository.loadAllCourses()
                .onSuccess { _allCoursesState.value = CourseUiState.Success(it) }
                .onFailure { _allCoursesState.value = CourseUiState.Error(it.message ?: "加载课程失败") }
        }
    }

    /** 加载我的课程 */
    fun loadMyCourses() {
        viewModelScope.launch {
            _myCoursesState.value = CourseUiState.Loading
            courseRepository.loadMyCourses()
                .onSuccess { _myCoursesState.value = CourseUiState.Success(it) }
                .onFailure { _myCoursesState.value = CourseUiState.Error(it.message ?: "加载我的课程失败") }
        }
    }

    /** 学生选课 */
    fun enrollCourse(courseId: Long) {
        viewModelScope.launch {
            _enrollState.value = EnrollState.Loading
            courseRepository.enrollCourse(courseId)
                .onSuccess { _enrollState.value = EnrollState.Success(it); loadMyCourses() }
                .onFailure { _enrollState.value = EnrollState.Error(it.message ?: "选课失败") }
        }
    }

    /** 新建课程 */
    fun createCourse(request: CourseRequest) {
        _actionState.value = CourseActionState.Loading
        viewModelScope.launch {
            courseRepository.createCourse(request).fold(
                onSuccess = {
                    _actionState.value = CourseActionState.Success("课程「${it.courseName}」创建成功")
                    loadAllCourses(); loadMyCourses()
                },
                onFailure = { _actionState.value = CourseActionState.Error(it.message ?: "创建失败") }
            )
        }
    }

    /** 更新课程 */
    fun updateCourse(courseId: Long, request: CourseRequest) {
        _actionState.value = CourseActionState.Loading
        viewModelScope.launch {
            courseRepository.updateCourse(courseId, request).fold(
                onSuccess = {
                    _actionState.value = CourseActionState.Success("课程更新成功")
                    loadAllCourses(); loadMyCourses()
                },
                onFailure = { _actionState.value = CourseActionState.Error(it.message ?: "更新失败") }
            )
        }
    }

    /** 删除课程 */
    fun deleteCourse(courseId: Long) {
        _actionState.value = CourseActionState.Loading
        viewModelScope.launch {
            courseRepository.deleteCourse(courseId).fold(
                onSuccess = {
                    _actionState.value = CourseActionState.Success("课程已删除")
                    loadAllCourses(); loadMyCourses()
                },
                onFailure = { _actionState.value = CourseActionState.Error(it.message ?: "删除失败") }
            )
        }
    }

    /** 加载选课学生列表 */
    fun loadCourseStudents(courseId: Long) {
        viewModelScope.launch {
            courseRepository.getCourseStudents(courseId)
                .onSuccess { _courseStudents.value = it }
                .onFailure { e ->
                    _actionState.value = CourseActionState.Error("加载学生列表失败: ${e.message}")
                }
        }
    }

    fun resetEnrollState() { _enrollState.value = EnrollState.Idle }
    fun resetActionState() { _actionState.value = CourseActionState.Idle }
}

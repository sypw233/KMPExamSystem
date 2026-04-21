package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.CourseResponse
import ovo.sypw.kmp.examsystem.data.dto.EnrollmentResponse
import ovo.sypw.kmp.examsystem.data.repository.CourseRepository

/**
 * 课程列表 UI 状态
 */
sealed interface CourseUiState {
    data object Loading : CourseUiState
    data class Success(val courses: List<CourseResponse>) : CourseUiState
    data class Error(val message: String) : CourseUiState
}

/**
 * 选课操作状态
 */
sealed interface EnrollState {
    data object Idle : EnrollState
    data object Loading : EnrollState
    data class Success(val enrollment: EnrollmentResponse) : EnrollState
    data class Error(val message: String) : EnrollState
}

/**
 * 课程 ViewModel
 * 管理课程列表、选课和我的课程等状态
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

    init {
        loadAllCourses()
        loadMyCourses()
    }

    /**
     * 加载所有活跃课程
     */
    fun loadAllCourses() {
        viewModelScope.launch {
            _allCoursesState.value = CourseUiState.Loading
            courseRepository.loadAllCourses()
                .onSuccess { courses ->
                    _allCoursesState.value = CourseUiState.Success(courses)
                }
                .onFailure { e ->
                    _allCoursesState.value = CourseUiState.Error(e.message ?: "加载课程失败")
                }
        }
    }

    /**
     * 加载我的课程
     */
    fun loadMyCourses() {
        viewModelScope.launch {
            _myCoursesState.value = CourseUiState.Loading
            courseRepository.loadMyCourses()
                .onSuccess { courses ->
                    _myCoursesState.value = CourseUiState.Success(courses)
                }
                .onFailure { e ->
                    _myCoursesState.value = CourseUiState.Error(e.message ?: "加载我的课程失败")
                }
        }
    }

    /**
     * 学生选课
     * @param courseId 课程ID
     */
    fun enrollCourse(courseId: Long) {
        viewModelScope.launch {
            _enrollState.value = EnrollState.Loading
            courseRepository.enrollCourse(courseId)
                .onSuccess { enrollment ->
                    _enrollState.value = EnrollState.Success(enrollment)
                    loadMyCourses()
                }
                .onFailure { e ->
                    _enrollState.value = EnrollState.Error(e.message ?: "选课失败")
                }
        }
    }

    /**
     * 重置选课状态
     */
    fun resetEnrollState() {
        _enrollState.value = EnrollState.Idle
    }
}

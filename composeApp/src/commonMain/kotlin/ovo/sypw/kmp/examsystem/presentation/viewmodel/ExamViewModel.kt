package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.data.repository.ExamRepository
import ovo.sypw.kmp.examsystem.presentation.navigation.UserRole

/** 考试列表 UI 状态 */
sealed interface ExamListUiState {
    data object Loading : ExamListUiState
    data class Success(val exams: List<ExamResponse>) : ExamListUiState
    data class Error(val message: String) : ExamListUiState
}

/** 考试详情 UI 状态 */
sealed interface ExamDetailUiState {
    data object Idle : ExamDetailUiState
    data object Loading : ExamDetailUiState
    data class Success(val exam: ExamResponse, val questions: List<ExamQuestionResponse>) : ExamDetailUiState
    data class Error(val message: String) : ExamDetailUiState
}

/** 考试操作状态 */
sealed interface ExamActionState {
    data object Idle : ExamActionState
    data object Loading : ExamActionState
    data class Success(val message: String) : ExamActionState
    data class Error(val message: String) : ExamActionState
}

/**
 * 考试 ViewModel（管理员/教师通用）
 * 按3种 Tab 加载：未开始(draft=0) | 进行中(published=1) | 已结束(ended=2)
 */
class ExamViewModel(
    private val examRepository: ExamRepository
) : ViewModel() {

    // 管理视角：全部考试列表（管理员/教师用 "我的考试" vs "全部考试"）
    private val _allExams = MutableStateFlow<ExamListUiState>(ExamListUiState.Loading)
    val allExams: StateFlow<ExamListUiState> = _allExams.asStateFlow()

    // 学生视角：未开始（已发布）
    private val _notStartedExams = MutableStateFlow<ExamListUiState>(ExamListUiState.Loading)
    val notStartedExams: StateFlow<ExamListUiState> = _notStartedExams.asStateFlow()

    // 已结束
    private val _endedExams = MutableStateFlow<ExamListUiState>(ExamListUiState.Loading)
    val endedExams: StateFlow<ExamListUiState> = _endedExams.asStateFlow()

    // 学生首页 - 即将开始
    private val _upcomingExams = MutableStateFlow<ExamListUiState>(ExamListUiState.Loading)
    val upcomingExams: StateFlow<ExamListUiState> = _upcomingExams.asStateFlow()

    // 考试详情
    private val _examDetail = MutableStateFlow<ExamDetailUiState>(ExamDetailUiState.Idle)
    val examDetail: StateFlow<ExamDetailUiState> = _examDetail.asStateFlow()

    // 操作反馈
    private val _actionState = MutableStateFlow<ExamActionState>(ExamActionState.Idle)
    val actionState: StateFlow<ExamActionState> = _actionState.asStateFlow()

    // 当前角色（影响调用哪套 API）
    private var userRole: UserRole = UserRole.UNKNOWN

    init {
        loadPublishedExams()
        loadEndedExams()
    }

    /** 设置用户角色，管理员加载全量，教师加载我的 */
    fun setRole(role: UserRole) {
        userRole = role
        loadManagerExams()
    }

    /** 管理员/教师视角：加载考试列表（管理员看全部，教师看我的） */
    fun loadManagerExams() {
        _allExams.value = ExamListUiState.Loading
        viewModelScope.launch {
            val result = if (userRole == UserRole.ADMIN)
                examRepository.loadAllExams()
            else
                examRepository.loadMyExams()
            result.fold(
                onSuccess = { _allExams.value = ExamListUiState.Success(it) },
                onFailure = { _allExams.value = ExamListUiState.Error(it.message ?: "加载失败") }
            )
        }
    }

    /** 加载已发布的考试（状态=1），学生端使用 */
    fun loadPublishedExams() {
        viewModelScope.launch {
            _notStartedExams.value = ExamListUiState.Loading
            examRepository.loadExamsByStatus(1)
                .onSuccess { exams ->
                    _notStartedExams.value = ExamListUiState.Success(exams)
                    _upcomingExams.value = ExamListUiState.Success(exams.take(3))
                }
                .onFailure { e ->
                    _notStartedExams.value = ExamListUiState.Error(e.message ?: "加载考试失败")
                }
        }
    }

    /** 加载已结束的考试（状态=2） */
    fun loadEndedExams() {
        viewModelScope.launch {
            _endedExams.value = ExamListUiState.Loading
            examRepository.loadExamsByStatus(2)
                .onSuccess { _endedExams.value = ExamListUiState.Success(it) }
                .onFailure { e -> _endedExams.value = ExamListUiState.Error(e.message ?: "加载历史考试失败") }
        }
    }

    /** 加载考试详情及题目 */
    fun loadExamDetail(examId: Long) {
        viewModelScope.launch {
            _examDetail.value = ExamDetailUiState.Loading
            val exam = examRepository.getExamDetail(examId).getOrElse { e ->
                _examDetail.value = ExamDetailUiState.Error(e.message ?: "加载考试详情失败")
                return@launch
            }
            val questions = examRepository.getExamQuestions(examId).getOrDefault(emptyList())
            _examDetail.value = ExamDetailUiState.Success(exam, questions)
        }
    }

    /** 创建考试 */
    fun createExam(request: ExamRequest) {
        if (_actionState.value is ExamActionState.Loading) return
        _actionState.value = ExamActionState.Loading
        viewModelScope.launch {
            examRepository.createExam(request).fold(
                onSuccess = {
                    _actionState.value = ExamActionState.Success("考试「${it.title}」创建成功")
                    loadManagerExams()
                },
                onFailure = { _actionState.value = ExamActionState.Error(it.message ?: "创建失败") }
            )
        }
    }

    /** 更新考试 */
    fun updateExam(examId: Long, request: ExamRequest) {
        if (_actionState.value is ExamActionState.Loading) return
        _actionState.value = ExamActionState.Loading
        viewModelScope.launch {
            examRepository.updateExam(examId, request).fold(
                onSuccess = {
                    _actionState.value = ExamActionState.Success("考试更新成功")
                    loadManagerExams()
                },
                onFailure = { _actionState.value = ExamActionState.Error(it.message ?: "更新失败") }
            )
        }
    }

    /** 删除考试 */
    fun deleteExam(examId: Long) {
        if (_actionState.value is ExamActionState.Loading) return
        _actionState.value = ExamActionState.Loading
        viewModelScope.launch {
            examRepository.deleteExam(examId).fold(
                onSuccess = {
                    _actionState.value = ExamActionState.Success("考试已删除")
                    loadManagerExams()
                },
                onFailure = { _actionState.value = ExamActionState.Error(it.message ?: "删除失败") }
            )
        }
    }

    /** 批量删除考试 */
    fun batchDeleteExams(ids: List<Long>) {
        if (ids.isEmpty()) return
        if (_actionState.value is ExamActionState.Loading) return
        _actionState.value = ExamActionState.Loading
        viewModelScope.launch {
            examRepository.batchDeleteExams(ids).fold(
                onSuccess = { result ->
                    val msg = "已删除 ${result.successCount} 项考试" +
                            if (result.failedCount > 0) "，${result.failedCount} 项失败" else ""
                    _actionState.value = ExamActionState.Success(msg)
                    loadManagerExams()
                },
                onFailure = { _actionState.value = ExamActionState.Error(it.message ?: "批量删除失败") }
            )
        }
    }

    /** 发布考试 */
    fun publishExam(examId: Long) {
        if (_actionState.value is ExamActionState.Loading) return
        _actionState.value = ExamActionState.Loading
        viewModelScope.launch {
            examRepository.publishExam(examId).fold(
                onSuccess = {
                    _actionState.value = ExamActionState.Success("考试已发布")
                    loadManagerExams()
                },
                onFailure = { _actionState.value = ExamActionState.Error(it.message ?: "发布失败") }
            )
        }
    }

    fun resetExamDetail() { _examDetail.value = ExamDetailUiState.Idle }
    fun resetActionState() { _actionState.value = ExamActionState.Idle }
}

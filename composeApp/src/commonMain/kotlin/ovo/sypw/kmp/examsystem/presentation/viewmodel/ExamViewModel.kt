package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.repository.ExamRepository

/**
 * 考试列表 UI 状态
 */
sealed interface ExamListUiState {
    data object Loading : ExamListUiState
    data class Success(val exams: List<ExamResponse>) : ExamListUiState
    data class Error(val message: String) : ExamListUiState
}

/**
 * 考试详情 UI 状态
 */
sealed interface ExamDetailUiState {
    data object Idle : ExamDetailUiState
    data object Loading : ExamDetailUiState
    data class Success(val exam: ExamResponse, val questions: List<ExamQuestionResponse>) : ExamDetailUiState
    data class Error(val message: String) : ExamDetailUiState
}

/**
 * 考试 ViewModel
 * 管理考试列表（分 Tab 按状态筛选）和考试详情
 */
class ExamViewModel(
    private val examRepository: ExamRepository
) : ViewModel() {

    // Tab 0:未开始(1-已发布), Tab 1:进行中(1-已发布，时间范围内), Tab 2:已结束(2)
    private val _notStartedExams = MutableStateFlow<ExamListUiState>(ExamListUiState.Loading)
    val notStartedExams: StateFlow<ExamListUiState> = _notStartedExams.asStateFlow()

    private val _endedExams = MutableStateFlow<ExamListUiState>(ExamListUiState.Loading)
    val endedExams: StateFlow<ExamListUiState> = _endedExams.asStateFlow()

    private val _upcomingExams = MutableStateFlow<ExamListUiState>(ExamListUiState.Loading)
    val upcomingExams: StateFlow<ExamListUiState> = _upcomingExams.asStateFlow()

    private val _examDetail = MutableStateFlow<ExamDetailUiState>(ExamDetailUiState.Idle)
    val examDetail: StateFlow<ExamDetailUiState> = _examDetail.asStateFlow()

    init {
        loadPublishedExams()
        loadEndedExams()
    }

    /**
     * 加载已发布的考试（状态=1）
     */
    fun loadPublishedExams() {
        viewModelScope.launch {
            _notStartedExams.value = ExamListUiState.Loading
            examRepository.loadExamsByStatus(1)
                .onSuccess { exams ->
                    _notStartedExams.value = ExamListUiState.Success(exams)
                    // 同时更新首页用的即将开始考试列表（取前3条）
                    _upcomingExams.value = ExamListUiState.Success(exams.take(3))
                }
                .onFailure { e ->
                    _notStartedExams.value = ExamListUiState.Error(e.message ?: "加载考试失败")
                }
        }
    }

    /**
     * 加载已结束的考试（状态=2）
     */
    fun loadEndedExams() {
        viewModelScope.launch {
            _endedExams.value = ExamListUiState.Loading
            examRepository.loadExamsByStatus(2)
                .onSuccess { exams ->
                    _endedExams.value = ExamListUiState.Success(exams)
                }
                .onFailure { e ->
                    _endedExams.value = ExamListUiState.Error(e.message ?: "加载历史考试失败")
                }
        }
    }

    /**
     * 加载考试详情及题目
     */
    fun loadExamDetail(examId: Long) {
        viewModelScope.launch {
            _examDetail.value = ExamDetailUiState.Loading
            val examResult = examRepository.getExamDetail(examId)
            examResult.onFailure { e ->
                _examDetail.value = ExamDetailUiState.Error(e.message ?: "加载考试详情失败")
                return@launch
            }
            val exam = examResult.getOrNull()!!

            val questionsResult = examRepository.getExamQuestions(examId)
            val questions = questionsResult.getOrDefault(emptyList())
            _examDetail.value = ExamDetailUiState.Success(exam, questions)
        }
    }

    /**
     * 重置考试详情状态
     */
    fun resetExamDetail() {
        _examDetail.value = ExamDetailUiState.Idle
    }
}

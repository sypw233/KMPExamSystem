package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.data.repository.ExamRepository
import ovo.sypw.kmp.examsystem.data.repository.QuestionRepository

sealed interface ExamComposeUiState {
    data object Loading : ExamComposeUiState
    data class Success(
        val exam: ExamResponse,
        val examQuestions: List<ExamQuestionResponse>,
        val courseQuestions: List<QuestionResponse>
    ) : ExamComposeUiState
    data class Error(val message: String) : ExamComposeUiState
}

class ExamComposeViewModel(
    private val examRepository: ExamRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExamComposeUiState>(ExamComposeUiState.Loading)
    val uiState: StateFlow<ExamComposeUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<ExamActionState>(ExamActionState.Idle)
    val actionState: StateFlow<ExamActionState> = _actionState.asStateFlow()

    // Keep track of current context to reload easily
    private var currentExamId: Long? = null
    private var currentCourseId: Long? = null

    fun loadComposeData(examId: Long, courseId: Long) {
        currentExamId = examId
        currentCourseId = courseId
        refreshData()
    }

    private fun refreshData() {
        val examId = currentExamId ?: return
        val courseId = currentCourseId ?: return

        _uiState.value = ExamComposeUiState.Loading
        viewModelScope.launch {
            try {
                // 1. Load exam detail
                val exam = examRepository.getExamDetail(examId).getOrThrow()
                // 2. Load questions currently in exam
                val examQuestions = examRepository.getExamQuestions(examId).getOrDefault(emptyList())
                // 3. Load all questions belonging to this course
                val courseQuestions = questionRepository.getQuestionsByCourse(courseId).getOrDefault(emptyList())

                _uiState.value = ExamComposeUiState.Success(
                    exam = exam,
                    examQuestions = examQuestions,
                    courseQuestions = courseQuestions
                )
            } catch (e: Exception) {
                _uiState.value = ExamComposeUiState.Error(e.message ?: "加载组卷数据失败")
            }
        }
    }

    fun addQuestionToExam(questionId: Long, score: Int) {
        val examId = currentExamId ?: return
        _actionState.value = ExamActionState.Loading
        viewModelScope.launch {
            val currentState = _uiState.value as? ExamComposeUiState.Success ?: return@launch
            val nextSequence = (currentState.examQuestions.maxOfOrNull { it.orderNum } ?: 0) + 1
            
            examRepository.addQuestionToExam(
                examId = examId,
                request = ExamQuestionRequest(questionId = questionId, sequence = nextSequence, score = score)
            ).onSuccess {
                _actionState.value = ExamActionState.Success("已添加")
                refreshData() // Reload visually
            }.onFailure {
                _actionState.value = ExamActionState.Error(it.message ?: "添加失败")
            }
        }
    }

    fun removeQuestionFromExam(questionId: Long) {
        val examId = currentExamId ?: return
        _actionState.value = ExamActionState.Loading
        viewModelScope.launch {
            examRepository.removeQuestionFromExam(examId, questionId).onSuccess {
                _actionState.value = ExamActionState.Success("已移除")
                refreshData() // Reload visually
            }.onFailure {
                _actionState.value = ExamActionState.Error(it.message ?: "移除失败")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = ExamActionState.Idle
    }
}

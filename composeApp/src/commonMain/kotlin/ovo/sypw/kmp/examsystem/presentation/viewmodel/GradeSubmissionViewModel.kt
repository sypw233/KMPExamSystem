package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.AiGradingResponse
import ovo.sypw.kmp.examsystem.data.dto.SubmissionResponse
import ovo.sypw.kmp.examsystem.data.repository.AiGradingRepository
import ovo.sypw.kmp.examsystem.data.repository.SubmissionRepository

sealed interface SubmissionsUiState {
    data object Loading : SubmissionsUiState
    data class Success(val submissions: List<SubmissionResponse>) : SubmissionsUiState
    data class Error(val message: String) : SubmissionsUiState
}

sealed interface GradeActionState {
    data object Idle : GradeActionState
    data object Loading : GradeActionState
    data class Success(val message: String) : GradeActionState
    data class Error(val message: String) : GradeActionState
}

class GradeSubmissionViewModel(
    private val submissionRepository: SubmissionRepository,
    private val examRepository: ovo.sypw.kmp.examsystem.data.repository.ExamRepository,
    private val aiGradingRepository: AiGradingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SubmissionsUiState>(SubmissionsUiState.Loading)
    val uiState: StateFlow<SubmissionsUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<GradeActionState>(GradeActionState.Idle)
    val actionState: StateFlow<GradeActionState> = _actionState.asStateFlow()

    // Cache submission details and its questions
    private val _currentSubmission = MutableStateFlow<SubmissionResponse?>(null)
    val currentSubmission: StateFlow<SubmissionResponse?> = _currentSubmission.asStateFlow()

    private val _currentQuestions = MutableStateFlow<List<ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse>>(emptyList())
    val currentQuestions: StateFlow<List<ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse>> = _currentQuestions.asStateFlow()

    fun loadSubmissions(examId: Long) {
        _uiState.value = SubmissionsUiState.Loading
        viewModelScope.launch {
            submissionRepository.getExamSubmissions(examId).fold(
                onSuccess = { _uiState.value = SubmissionsUiState.Success(it) },
                onFailure = { _uiState.value = SubmissionsUiState.Error(it.message ?: "加载提交记录失败") }
            )
        }
    }

    fun loadSubmissionDetail(submissionId: Long) {
        viewModelScope.launch {
            submissionRepository.getSubmissionDetail(submissionId).fold(
                onSuccess = { sub ->
                    _currentSubmission.value = sub
                    // Load exam questions
                    examRepository.getExamQuestions(sub.examId).fold(
                        onSuccess = { _currentQuestions.value = it },
                        onFailure = { _currentQuestions.value = emptyList() }
                    )
                },
                onFailure = { _actionState.value = GradeActionState.Error("加载答卷失败: ${it.message}") }
            )
        }
    }

    fun submitGrades(submissionId: Long, grades: Map<Long, Int>) {
        _actionState.value = GradeActionState.Loading
        viewModelScope.launch {
            submissionRepository.gradeSubmission(submissionId, grades).fold(
                onSuccess = {
                    _actionState.value = GradeActionState.Success("批改完成")
                    _currentSubmission.value = it // Update current detail with graded info
                },
                onFailure = { _actionState.value = GradeActionState.Error("批改保存失败: ${it.message}") }
            )
        }
    }

    suspend fun requestAiGrade(questionId: Long, studentAnswer: String, maxScore: Int): Result<AiGradingResponse> {
        return aiGradingRepository.aiGrade(questionId, studentAnswer, maxScore)
    }

    fun resetActionState() {
        _actionState.value = GradeActionState.Idle
    }

    fun clearSubmissionDetail() {
        _currentSubmission.value = null
    }
}

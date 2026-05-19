package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.AiBatchGradingResponse
import ovo.sypw.kmp.examsystem.data.dto.AiGradingResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.ProctoringDataResponse
import ovo.sypw.kmp.examsystem.data.dto.SubmissionResponse
import ovo.sypw.kmp.examsystem.data.repository.AiGradingRepository
import ovo.sypw.kmp.examsystem.data.repository.ExamRepository
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

sealed interface ProctoringUiState {
    data object Idle : ProctoringUiState
    data object Loading : ProctoringUiState
    data class Success(val data: ProctoringDataResponse) : ProctoringUiState
    data class Error(val message: String) : ProctoringUiState
}

class GradeSubmissionViewModel(
    private val submissionRepository: SubmissionRepository,
    private val examRepository: ExamRepository,
    private val aiGradingRepository: AiGradingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SubmissionsUiState>(SubmissionsUiState.Loading)
    val uiState: StateFlow<SubmissionsUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<GradeActionState>(GradeActionState.Idle)
    val actionState: StateFlow<GradeActionState> = _actionState.asStateFlow()

    private val _currentSubmission = MutableStateFlow<SubmissionResponse?>(null)
    val currentSubmission: StateFlow<SubmissionResponse?> = _currentSubmission.asStateFlow()

    private val _currentQuestions = MutableStateFlow<List<ExamQuestionResponse>>(emptyList())
    val currentQuestions: StateFlow<List<ExamQuestionResponse>> = _currentQuestions.asStateFlow()

    private val _detailError = MutableStateFlow<String?>(null)
    val detailError: StateFlow<String?> = _detailError.asStateFlow()

    private val _proctoringState = MutableStateFlow<ProctoringUiState>(ProctoringUiState.Idle)
    val proctoringState: StateFlow<ProctoringUiState> = _proctoringState.asStateFlow()

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
        _currentSubmission.value = null
        _currentQuestions.value = emptyList()
        _detailError.value = null
        viewModelScope.launch {
            submissionRepository.getSubmissionDetail(submissionId).fold(
                onSuccess = { submission ->
                    _currentSubmission.value = submission
                    examRepository.getExamQuestions(submission.examId).fold(
                        onSuccess = { _currentQuestions.value = it },
                        onFailure = {
                            examRepository.getExamPaperQuestions(submission.examId).fold(
                                onSuccess = { paperQuestions -> _currentQuestions.value = paperQuestions },
                                onFailure = { _currentQuestions.value = emptyList() }
                            )
                        }
                    )
                },
                onFailure = { _detailError.value = "加载答卷失败: ${it.message}" }
            )
        }
    }

    fun submitGrades(submissionId: Long, grades: Map<Long, Int>) {
        if (_actionState.value is GradeActionState.Loading) return
        _actionState.value = GradeActionState.Loading
        viewModelScope.launch {
            submissionRepository.gradeSubmission(submissionId, grades).fold(
                onSuccess = {
                    _actionState.value = GradeActionState.Success("批改完成")
                    _currentSubmission.value = it
                },
                onFailure = { _actionState.value = GradeActionState.Error("批改保存失败: ${it.message}") }
            )
        }
    }

    suspend fun requestAiGrade(questionId: Long, studentAnswer: String, maxScore: Int): Result<AiGradingResponse> {
        return aiGradingRepository.aiGrade(questionId, studentAnswer, maxScore)
    }

    suspend fun requestBatchAiGrade(submissionId: Long): Result<AiBatchGradingResponse> {
        return aiGradingRepository.batchGrade(submissionId)
    }

    fun loadProctoringData(submissionId: Long) {
        _proctoringState.value = ProctoringUiState.Loading
        viewModelScope.launch {
            submissionRepository.getProctoringData(submissionId).fold(
                onSuccess = { _proctoringState.value = ProctoringUiState.Success(it) },
                onFailure = { _proctoringState.value = ProctoringUiState.Error(it.message ?: "加载监考记录失败") }
            )
        }
    }

    fun clearProctoringData() {
        _proctoringState.value = ProctoringUiState.Idle
    }

    fun resetActionState() {
        _actionState.value = GradeActionState.Idle
    }

    fun clearSubmissionDetail() {
        _currentSubmission.value = null
        _currentQuestions.value = emptyList()
        _detailError.value = null
    }
}

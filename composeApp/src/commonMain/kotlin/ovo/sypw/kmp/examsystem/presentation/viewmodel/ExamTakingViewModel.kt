package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.data.dto.SubmissionResponse
import ovo.sypw.kmp.examsystem.data.repository.ExamRepository
import ovo.sypw.kmp.examsystem.data.repository.SubmissionRepository

/**
 * 答题界面 UI 状态
 */
sealed interface ExamTakingUiState {
    data object Idle : ExamTakingUiState
    data object Loading : ExamTakingUiState
    data class Ready(
        val exam: ExamResponse,
        val questions: List<ExamQuestionResponse>,
        val submissionId: Long
    ) : ExamTakingUiState
    data class Submitted(val submission: SubmissionResponse) : ExamTakingUiState
    data class Error(val message: String) : ExamTakingUiState
}

/**
 * 考试答题 ViewModel
 * 管理：题目加载、答案状态、计时、提交
 */
class ExamTakingViewModel(
    private val examRepository: ExamRepository,
    private val submissionRepository: SubmissionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExamTakingUiState>(ExamTakingUiState.Idle)
    val uiState: StateFlow<ExamTakingUiState> = _uiState.asStateFlow()

    // 答案 Map: questionId -> answer(String)
    private val _answers = MutableStateFlow<Map<Long, String>>(emptyMap())
    val answers: StateFlow<Map<Long, String>> = _answers.asStateFlow()

    private var currentExamId: Long = -1
    private var currentSubmissionId: Long = -1

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    // 提交错误消息（不覆盖 uiState，让 UI 通过 Snackbar 显示，保留 Ready 状态供重试）
    private val _submitErrorMessage = MutableStateFlow<String?>(null)
    val submitErrorMessage: StateFlow<String?> = _submitErrorMessage.asStateFlow()

    /**
     * 进入考试：开始考试 + 加载试卷
     * 相同考试在加载或已就绪时直接忽略；若开始考试已成功但后续加载失败，重试时复用已有提交记录。
     */
    fun enterExam(examId: Long) {
        val current = _uiState.value
        if (currentExamId == examId &&
            current !is ExamTakingUiState.Idle &&
            current !is ExamTakingUiState.Error
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.value = ExamTakingUiState.Loading
            if (currentExamId != examId) {
                currentExamId = examId
                currentSubmissionId = -1
                _answers.value = emptyMap()
            }

            if (currentSubmissionId <= 0) {
                val startResult = submissionRepository.startExam(examId)
                val submission = startResult.getOrNull()
                if (submission == null) {
                    _uiState.value = ExamTakingUiState.Error(startResult.exceptionOrNull()?.message ?: "开始考试失败")
                    return@launch
                }
                currentSubmissionId = submission.id
            }

            // 加载考试详情
            val examResult = examRepository.getExamDetail(examId)
            val exam = examResult.getOrNull()
            if (exam == null) {
                _uiState.value = ExamTakingUiState.Error(
                    examResult.exceptionOrNull()?.message ?: "加载考试信息失败，请重试"
                )
                return@launch
            }

            // 使用学生试卷接口，确保答题端不会拿到标准答案和解析
            val questionsResult = examRepository.getExamPaperQuestions(examId)
            val questions = questionsResult.getOrNull()
            if (questions == null) {
                _uiState.value = ExamTakingUiState.Error(
                    questionsResult.exceptionOrNull()?.message ?: "加载试题失败，请重试"
                )
                return@launch
            }

            _answers.value = emptyMap()
            _uiState.value = ExamTakingUiState.Ready(exam, questions, currentSubmissionId)
        }
    }

    /**
     * 更新单道题的答案
     * @param questionId 题目ID
     * @param answer 答案字符串
     */
    fun updateAnswer(questionId: Long, answer: String) {
        _answers.value = _answers.value.toMutableMap().apply {
            if (answer.isBlank()) {
                remove(questionId)
            } else {
                put(questionId, answer)
            }
        }
    }

    /**
     * 多选题：切换选项选中状态
     * 选项约定为单字母（A/B/C/D），sorted() 保证相同选择产生相同字符串（如"A,B,C"）
     * @param questionId 题目ID
     * @param option 选项字母如"A"
     */
    fun toggleMultipleChoice(questionId: Long, option: String) {
        val currentAnswer = _answers.value[questionId] ?: ""
        val selectedOptions = if (currentAnswer.isBlank()) {
            mutableSetOf()
        } else {
            currentAnswer.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toMutableSet()
        }

        if (selectedOptions.contains(option)) {
            selectedOptions.remove(option)
        } else {
            selectedOptions.add(option)
        }
        val newAnswer = selectedOptions.sorted().joinToString(",")
        updateAnswer(questionId, newAnswer)
    }

    /**
     * 提交考试答案
     * 【修复 BUG-01】提交失败时不覆盖 uiState（保留 Ready），仅通过 submitErrorMessage 上报，
     * 让用户可以重试，避免进入 Error 状态后答案无法再次提交的僵局。
     */
    fun submitExam() {
        if (_isSubmitting.value) return
        // 确保在 Ready 状态才允许提交，防止 Error 状态下的意外调用
        if (_uiState.value !is ExamTakingUiState.Ready) return

        viewModelScope.launch {
            _isSubmitting.value = true
            _submitErrorMessage.value = null

            val answers = _answers.value.filterValues { it.isNotBlank() }
            val result = submissionRepository.submitExam(currentExamId, answers)
            result.onSuccess { submission ->
                _uiState.value = ExamTakingUiState.Submitted(submission)
            }.onFailure { e ->
                // 提交失败：保留 Ready 状态，用 submitErrorMessage 通知 UI 显示 Snackbar
                _submitErrorMessage.value = e.message ?: "提交失败，请重试"
            }
            _isSubmitting.value = false
        }
    }

    /** 清除提交错误消息（UI Snackbar 显示后调用） */
    fun clearSubmitError() {
        _submitErrorMessage.value = null
    }

    /**
     * 记录监考事件（切屏等）
     */
    fun recordProctoringEvent(eventType: String, detail: String? = null) {
        if (currentExamId <= 0) return
        viewModelScope.launch {
            submissionRepository.recordProctoringEvent(currentExamId, eventType, detail)
        }
    }

    /**
     * 重置状态（退出考试时调用）
     * 【修复 BUG-SEC-03】提交中禁止 reset，防止提交协程上下文被清空导致逻辑混乱
     */
    fun reset() {
        if (_isSubmitting.value) return
        _uiState.value = ExamTakingUiState.Idle
        _answers.value = emptyMap()
        _submitErrorMessage.value = null
        currentExamId = -1
        currentSubmissionId = -1
    }
}

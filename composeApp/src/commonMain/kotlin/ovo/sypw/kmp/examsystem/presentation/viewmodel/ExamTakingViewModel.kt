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

    // 答案 Map: questionId(String) -> answer(String)
    private val _answers = MutableStateFlow<Map<String, String>>(emptyMap())
    val answers: StateFlow<Map<String, String>> = _answers.asStateFlow()

    private var currentExamId: Long = -1
    private var currentSubmissionId: Long = -1

    /**
     * 进入考试：开始考试 + 加载题目
     */
    fun enterExam(examId: Long) {
        viewModelScope.launch {
            _uiState.value = ExamTakingUiState.Loading
            currentExamId = examId

            // 开始考试（创建答题记录）
            val startResult = submissionRepository.startExam(examId)
            startResult.onFailure { e ->
                _uiState.value = ExamTakingUiState.Error(e.message ?: "开始考试失败")
                return@launch
            }
            currentSubmissionId = startResult.getOrNull()!!.submissionId

            // 加载考试详情
            val examResult = examRepository.getExamDetail(examId)
            examResult.onFailure { e ->
                _uiState.value = ExamTakingUiState.Error(e.message ?: "加载考试信息失败")
                return@launch
            }
            val exam = examResult.getOrNull()!!

            // 加载题目列表
            val questionsResult = examRepository.getExamQuestions(examId)
            val questions = questionsResult.getOrDefault(emptyList())

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
            put(questionId.toString(), answer)
        }
    }

    /**
     * 多选题：切换选项选中状态
     * @param questionId 题目ID
     * @param option 选项字母如"A"
     */
    fun toggleMultipleChoice(questionId: Long, option: String) {
        val currentAnswer = _answers.value[questionId.toString()] ?: ""
        val selectedOptions = if (currentAnswer.isBlank()) mutableSetOf()
        else currentAnswer.split(",").toMutableSet()

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
     */
    fun submitExam() {
        viewModelScope.launch {
            val currentState = _uiState.value as? ExamTakingUiState.Ready ?: return@launch
            _uiState.value = ExamTakingUiState.Loading

            val result = submissionRepository.submitExam(currentExamId, _answers.value)
            result.onSuccess { submission ->
                _uiState.value = ExamTakingUiState.Submitted(submission)
            }.onFailure { e ->
                _uiState.value = ExamTakingUiState.Error(e.message ?: "提交失败")
            }
        }
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
     * 重置状态
     */
    fun reset() {
        _uiState.value = ExamTakingUiState.Idle
        _answers.value = emptyMap()
        currentExamId = -1
        currentSubmissionId = -1
    }
}

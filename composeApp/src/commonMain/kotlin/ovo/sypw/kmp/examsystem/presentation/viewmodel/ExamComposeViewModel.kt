package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.ComposeOptions
import ovo.sypw.kmp.examsystem.data.dto.ComposeRandomExamRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionBankResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.SectionRule
import ovo.sypw.kmp.examsystem.data.repository.ExamRepository
import ovo.sypw.kmp.examsystem.data.repository.QuestionBankRepository
import ovo.sypw.kmp.examsystem.data.repository.QuestionRepository

sealed interface ExamComposeUiState {
    data object Loading : ExamComposeUiState
    data class Success(
        val exam: ExamResponse,
        val examQuestions: List<ExamQuestionResponse>,
        val courseQuestions: List<QuestionResponse>,
        val myBanks: List<QuestionBankResponse> = emptyList()
    ) : ExamComposeUiState
    data class Error(val message: String) : ExamComposeUiState
}

/** 智能组卷配置状态 */
sealed interface RandomComposeState {
    data object Idle : RandomComposeState
    data object Loading : RandomComposeState
    data class Configuring(
        val banks: List<QuestionBankResponse>,
        val selectedBankId: Long? = null,
        val expectedTotalScore: Int? = null,
        val sections: List<SectionRule> = emptyList(),
        val shuffleQuestions: Boolean = true,
        val lenientMode: Boolean = false
    ) : RandomComposeState
    data class Success(val message: String) : RandomComposeState
    data class Error(val message: String) : RandomComposeState
}

class ExamComposeViewModel(
    private val examRepository: ExamRepository,
    private val questionRepository: QuestionRepository,
    private val questionBankRepository: QuestionBankRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExamComposeUiState>(ExamComposeUiState.Loading)
    val uiState: StateFlow<ExamComposeUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<ExamActionState>(ExamActionState.Idle)
    val actionState: StateFlow<ExamActionState> = _actionState.asStateFlow()

    private val _randomComposeState = MutableStateFlow<RandomComposeState>(RandomComposeState.Idle)
    val randomComposeState: StateFlow<RandomComposeState> = _randomComposeState.asStateFlow()

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
                // 3. Load my questions for exam composition
                val courseQuestions = questionRepository.loadMyQuestions().getOrDefault(emptyList())
                // 4. Load my banks for random compose
                val banks = questionBankRepository.loadMyBanks().getOrDefault(emptyList())

                _uiState.value = ExamComposeUiState.Success(
                    exam = exam,
                    examQuestions = examQuestions,
                    courseQuestions = courseQuestions,
                    myBanks = banks
                )
            } catch (e: Exception) {
                _uiState.value = ExamComposeUiState.Error(e.message ?: "加载组卷数据失败")
            }
        }
    }

    /**
     * 打开智能随机组卷配置面板
     */
    fun openRandomComposeConfig() {
        val currentState = _uiState.value as? ExamComposeUiState.Success ?: return
        _randomComposeState.value = RandomComposeState.Configuring(
            banks = currentState.myBanks,
            selectedBankId = currentState.myBanks.firstOrNull()?.id,
            expectedTotalScore = currentState.exam.totalScore,
            sections = emptyList(),
            shuffleQuestions = true,
            lenientMode = false
        )
    }

    /**
     * 更新智能组卷配置
     */
    fun updateRandomComposeConfig(config: RandomComposeState.Configuring) {
        _randomComposeState.value = config
    }

    /**
     * 执行智能随机组卷
     */
    fun composeRandomExam(
        bankId: Long,
        expectedTotalScore: Int? = null,
        sections: List<SectionRule>,
        shuffleQuestions: Boolean = true,
        lenientMode: Boolean = false
    ) {
        val examId = currentExamId ?: return
        if (_randomComposeState.value is RandomComposeState.Loading) return
        _randomComposeState.value = RandomComposeState.Loading
        viewModelScope.launch {
            val request = ComposeRandomExamRequest(
                bankId = bankId,
                expectedTotalScore = expectedTotalScore,
                sections = sections,
                options = ComposeOptions(shuffleQuestions = shuffleQuestions, lenientMode = lenientMode)
            )
            examRepository.composeRandomExam(examId, request)
                .onSuccess {
                    _randomComposeState.value = RandomComposeState.Success("智能组卷成功，已生成 ${it.questionCount} 题")
                    refreshData()
                }
                .onFailure {
                    _randomComposeState.value = RandomComposeState.Error(it.message ?: "智能组卷失败")
                }
        }
    }

    fun resetRandomComposeState() {
        _randomComposeState.value = RandomComposeState.Idle
    }

    fun addQuestionToExam(questionId: Long, score: Int) {
        val examId = currentExamId ?: return
        if (_actionState.value is ExamActionState.Loading) return
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
        if (_actionState.value is ExamActionState.Loading) return
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

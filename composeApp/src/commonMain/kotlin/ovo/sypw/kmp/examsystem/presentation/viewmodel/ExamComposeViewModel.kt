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
import ovo.sypw.kmp.examsystem.utils.Logger

sealed interface ExamComposeUiState {
    data object Loading : ExamComposeUiState
    data class Success(
        val exam: ExamResponse,
        val examQuestions: List<ExamQuestionResponse>,
        val bankQuestions: List<QuestionResponse> = emptyList(),
        val myBanks: List<QuestionBankResponse> = emptyList(),
        val selectedBankId: Long? = null,
        val bankQuestionsLoading: Boolean = false,
        val bankQuestionsError: String? = null
    ) : ExamComposeUiState
    data class Error(val message: String) : ExamComposeUiState
}

sealed interface RandomComposeState {
    data object Idle : RandomComposeState
    data object Loading : RandomComposeState
    data class Configuring(
        val banks: List<QuestionBankResponse>,
        val selectedBankId: Long? = null,
        val expectedTotalScore: Int? = null,
        val sections: List<SectionRule> = emptyList(),
        val shuffleQuestions: Boolean = true,
        val lenientMode: Boolean = false,
        val errorMessage: String? = null
    ) : RandomComposeState
    data class Success(val message: String) : RandomComposeState
    data class Error(val message: String) : RandomComposeState
}

class ExamComposeViewModel(
    private val examRepository: ExamRepository,
    private val questionBankRepository: QuestionBankRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExamComposeUiState>(ExamComposeUiState.Loading)
    val uiState: StateFlow<ExamComposeUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<ExamActionState>(ExamActionState.Idle)
    val actionState: StateFlow<ExamActionState> = _actionState.asStateFlow()

    private val _randomComposeState = MutableStateFlow<RandomComposeState>(RandomComposeState.Idle)
    val randomComposeState: StateFlow<RandomComposeState> = _randomComposeState.asStateFlow()

    private val _currentExamId = MutableStateFlow<Long?>(null)
    private val _currentCourseId = MutableStateFlow<Long?>(null)

    fun loadComposeData(examId: Long, courseId: Long) {
        _currentExamId.value = examId
        _currentCourseId.value = courseId
        refreshData()
    }

    private fun refreshData() {
        val examId = _currentExamId.value ?: return
        _currentCourseId.value ?: return
        val previousBankId = (_uiState.value as? ExamComposeUiState.Success)?.selectedBankId

        _uiState.value = ExamComposeUiState.Loading
        viewModelScope.launch {
            val exam = examRepository.getExamDetail(examId).getOrElse {
                _uiState.value = ExamComposeUiState.Error(it.message ?: "加载考试详情失败")
                return@launch
            }

            val examQuestions = examRepository.getExamQuestions(examId).getOrElse {
                Logger.w("ExamComposeViewModel", "加载考试题目失败: ${it.message}")
                emptyList()
            }.distinctBy { it.questionId }

            val banks = questionBankRepository.loadMyBanks().getOrElse {
                Logger.w("ExamComposeViewModel", "加载题库失败: ${it.message}")
                emptyList()
            }

            val selectedBankId = previousBankId?.takeIf { id -> banks.any { it.id == id } }
            val bankQuestions = selectedBankId?.let { bankId ->
                questionBankRepository.loadBankQuestions(bankId).getOrElse {
                    Logger.w("ExamComposeViewModel", "加载题库题目失败: ${it.message}")
                    emptyList()
                }.distinctBy { it.id }
            }.orEmpty()

            _uiState.value = ExamComposeUiState.Success(
                exam = exam,
                examQuestions = examQuestions,
                bankQuestions = bankQuestions,
                myBanks = banks,
                selectedBankId = selectedBankId
            )
        }
    }

    fun selectQuestionBank(bankId: Long) {
        val currentState = _uiState.value as? ExamComposeUiState.Success ?: return
        if (currentState.selectedBankId == bankId && currentState.bankQuestions.isNotEmpty()) return

        _uiState.value = currentState.copy(
            selectedBankId = bankId,
            bankQuestions = emptyList(),
            bankQuestionsLoading = true,
            bankQuestionsError = null
        )

        viewModelScope.launch {
            questionBankRepository.loadBankQuestions(bankId)
                .onSuccess { questions ->
                    val latestState = _uiState.value as? ExamComposeUiState.Success ?: return@onSuccess
                    if (latestState.selectedBankId == bankId) {
                        _uiState.value = latestState.copy(
                            bankQuestions = questions.distinctBy { it.id },
                            bankQuestionsLoading = false,
                            bankQuestionsError = null
                        )
                    }
                }
                .onFailure { error ->
                    val latestState = _uiState.value as? ExamComposeUiState.Success ?: return@onFailure
                    if (latestState.selectedBankId == bankId) {
                        _uiState.value = latestState.copy(
                            bankQuestions = emptyList(),
                            bankQuestionsLoading = false,
                            bankQuestionsError = error.message ?: "题库题目加载失败"
                        )
                    }
                }
        }
    }

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

    fun updateRandomComposeConfig(config: RandomComposeState.Configuring) {
        _randomComposeState.value = config
    }

    fun composeRandomExam(
        bankId: Long,
        expectedTotalScore: Int? = null,
        sections: List<SectionRule>,
        shuffleQuestions: Boolean = true,
        lenientMode: Boolean = false
    ) {
        val examId = _currentExamId.value ?: return
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
                .onFailure { e ->
                    val currentConfig = _randomComposeState.value
                    _randomComposeState.value = if (currentConfig is RandomComposeState.Configuring) {
                        currentConfig.copy(errorMessage = e.message ?: "智能组卷失败")
                    } else {
                        RandomComposeState.Error(e.message ?: "智能组卷失败")
                    }
                }
        }
    }

    fun resetRandomComposeState() {
        _randomComposeState.value = RandomComposeState.Idle
    }

    fun addQuestionToExam(questionId: Long, score: Int) {
        val examId = _currentExamId.value ?: return
        if (_actionState.value is ExamActionState.Loading) return

        _actionState.value = ExamActionState.Loading
        viewModelScope.launch {
            val currentState = _uiState.value as? ExamComposeUiState.Success ?: return@launch
            val nextSequence = (currentState.examQuestions.maxOfOrNull { it.orderNum } ?: 0) + 1

            examRepository.addQuestionToExam(
                examId = examId,
                request = ExamQuestionRequest(questionId = questionId, sequence = nextSequence, score = score)
            ).onSuccess { addedQuestion ->
                val latestState = _uiState.value as? ExamComposeUiState.Success
                if (latestState != null) {
                    val sourceQuestion = latestState.bankQuestions.firstOrNull { it.id == questionId }
                    val normalized = addedQuestion.copy(
                        question = addedQuestion.question ?: sourceQuestion,
                        score = if (addedQuestion.score > 0) addedQuestion.score else score,
                        orderNum = if (addedQuestion.orderNum > 0) addedQuestion.orderNum else nextSequence
                    )
                    _uiState.value = latestState.copy(
                        examQuestions = (latestState.examQuestions + normalized).distinctBy { it.questionId }
                    )
                }
                _actionState.value = ExamActionState.Success("已添加")
            }.onFailure {
                _actionState.value = ExamActionState.Error(it.message ?: "添加失败")
            }
        }
    }

    fun removeQuestionFromExam(questionId: Long) {
        val examId = _currentExamId.value ?: return
        if (_actionState.value is ExamActionState.Loading) return

        _actionState.value = ExamActionState.Loading
        viewModelScope.launch {
            examRepository.removeQuestionFromExam(examId, questionId).onSuccess {
                val latestState = _uiState.value as? ExamComposeUiState.Success
                if (latestState != null) {
                    _uiState.value = latestState.copy(
                        examQuestions = latestState.examQuestions.filterNot { it.questionId == questionId }
                    )
                }
                _actionState.value = ExamActionState.Success("已移除")
            }.onFailure {
                _actionState.value = ExamActionState.Error(it.message ?: "移除失败")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = ExamActionState.Idle
    }
}

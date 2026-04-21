package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.data.repository.QuestionRepository

/**
 * 题目管理 UI 状态
 */
sealed interface QuestionUiState {
    data object Loading : QuestionUiState
    data class Success(val questions: List<QuestionResponse>) : QuestionUiState
    data class Error(val message: String) : QuestionUiState
}

/**
 * 题目管理 ViewModel（教师/管理员）
 */
class QuestionViewModel(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuestionUiState>(QuestionUiState.Loading)
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    init {
        loadMyQuestions()
    }

    fun loadMyQuestions() {
        viewModelScope.launch {
            _uiState.value = QuestionUiState.Loading
            questionRepository.loadMyQuestions()
                .onSuccess { questions ->
                    val filtered = if (_selectedType.value != null)
                        questions.filter { it.type == _selectedType.value }
                    else questions
                    _uiState.value = QuestionUiState.Success(filtered)
                }
                .onFailure { e ->
                    _uiState.value = QuestionUiState.Error(e.message ?: "加载题目失败")
                }
        }
    }

    fun filterByType(type: String?) {
        _selectedType.value = type
        val allQuestions = (questionRepository.myQuestions.value)
        val filtered = if (type != null) allQuestions.filter { it.type == type } else allQuestions
        _uiState.value = QuestionUiState.Success(filtered)
    }

    fun deleteQuestion(questionId: Long) {
        viewModelScope.launch {
            questionRepository.deleteQuestion(questionId)
                .onSuccess { loadMyQuestions() }
                .onFailure { e ->
                    _uiState.value = QuestionUiState.Error(e.message ?: "删除失败")
                }
        }
    }
}

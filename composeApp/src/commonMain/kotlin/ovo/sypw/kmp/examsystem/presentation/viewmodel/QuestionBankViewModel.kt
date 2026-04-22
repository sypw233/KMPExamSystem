package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.QuestionBankRequest
import ovo.sypw.kmp.examsystem.data.dto.QuestionBankResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionRequest
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.data.repository.QuestionBankRepository
import ovo.sypw.kmp.examsystem.data.repository.QuestionRepository

sealed interface QuestionBankUiState {
    data object Loading : QuestionBankUiState
    data class Success(val banks: List<QuestionBankResponse>) : QuestionBankUiState
    data class Error(val message: String) : QuestionBankUiState
}

sealed interface QuestionBankActionState {
    data object Idle : QuestionBankActionState
    data object Loading : QuestionBankActionState
    data class Success(val message: String) : QuestionBankActionState
    data class Error(val message: String) : QuestionBankActionState
}

class QuestionBankViewModel(
    private val questionBankRepository: QuestionBankRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<QuestionBankUiState>(QuestionBankUiState.Loading)
    val uiState: StateFlow<QuestionBankUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<QuestionBankActionState>(QuestionBankActionState.Idle)
    val actionState: StateFlow<QuestionBankActionState> = _actionState.asStateFlow()

    private val _selectedBank = MutableStateFlow<QuestionBankResponse?>(null)
    val selectedBank: StateFlow<QuestionBankResponse?> = _selectedBank.asStateFlow()

    private val _bankQuestions = MutableStateFlow<List<QuestionResponse>>(emptyList())
    val bankQuestions: StateFlow<List<QuestionResponse>> = _bankQuestions.asStateFlow()

    private val _allQuestions = MutableStateFlow<List<QuestionResponse>>(emptyList())
    val allQuestions: StateFlow<List<QuestionResponse>> = _allQuestions.asStateFlow()

    init {
        refreshBanks()
        loadAllQuestions()
    }

    fun refreshBanks() {
        viewModelScope.launch {
            _uiState.value = QuestionBankUiState.Loading
            questionBankRepository.loadMyBanks()
                .onSuccess { _uiState.value = QuestionBankUiState.Success(it) }
                .onFailure { _uiState.value = QuestionBankUiState.Error(it.message ?: "Failed to load question banks") }
        }
    }

    fun createBank(name: String, description: String?) {
        if (name.isBlank()) return
        _actionState.value = QuestionBankActionState.Loading
        viewModelScope.launch {
            questionBankRepository.createBank(QuestionBankRequest(name.trim(), description?.takeIf { it.isNotBlank() }))
                .onSuccess {
                    _actionState.value = QuestionBankActionState.Success("Question bank created")
                    refreshBanks()
                }
                .onFailure { _actionState.value = QuestionBankActionState.Error(it.message ?: "Create failed") }
        }
    }

    fun updateBank(bankId: Long, name: String, description: String?) {
        if (name.isBlank()) return
        _actionState.value = QuestionBankActionState.Loading
        viewModelScope.launch {
            questionBankRepository.updateBank(bankId, QuestionBankRequest(name.trim(), description?.takeIf { it.isNotBlank() }))
                .onSuccess {
                    _actionState.value = QuestionBankActionState.Success("Question bank updated")
                    refreshBanks()
                }
                .onFailure { _actionState.value = QuestionBankActionState.Error(it.message ?: "Update failed") }
        }
    }

    fun deleteBank(bankId: Long) {
        _actionState.value = QuestionBankActionState.Loading
        viewModelScope.launch {
            questionBankRepository.deleteBank(bankId)
                .onSuccess {
                    if (_selectedBank.value?.id == bankId) {
                        _selectedBank.value = null
                        _bankQuestions.value = emptyList()
                    }
                    _actionState.value = QuestionBankActionState.Success("Question bank deleted")
                    refreshBanks()
                }
                .onFailure { _actionState.value = QuestionBankActionState.Error(it.message ?: "Delete failed") }
        }
    }

    fun selectBank(bank: QuestionBankResponse?) {
        _selectedBank.value = bank
        if (bank == null) {
            _bankQuestions.value = emptyList()
            return
        }
        loadBankQuestions(bank.id)
    }

    fun loadBankQuestions(bankId: Long) {
        viewModelScope.launch {
            questionBankRepository.loadBankQuestions(bankId)
                .onSuccess { _bankQuestions.value = it }
                .onFailure {
                    _bankQuestions.value = emptyList()
                    _actionState.value = QuestionBankActionState.Error(it.message ?: "Failed to load bank questions")
                }
        }
    }

    fun loadAllQuestions() {
        viewModelScope.launch {
            questionRepository.loadMyQuestions()
                .onSuccess { _allQuestions.value = it }
                .onFailure {
                    questionRepository.loadAllQuestions().onSuccess { _allQuestions.value = it }
                }
        }
    }

    fun addQuestionToBank(bankId: Long, questionId: Long) {
        _actionState.value = QuestionBankActionState.Loading
        viewModelScope.launch {
            questionBankRepository.addQuestionToBank(bankId, questionId)
                .onSuccess {
                    _actionState.value = QuestionBankActionState.Success("Question added")
                    loadBankQuestions(bankId)
                    refreshBanks()
                }
                .onFailure { _actionState.value = QuestionBankActionState.Error(it.message ?: "Add failed") }
        }
    }

    fun removeQuestionFromBank(bankId: Long, questionId: Long) {
        _actionState.value = QuestionBankActionState.Loading
        viewModelScope.launch {
            questionBankRepository.removeQuestionFromBank(bankId, questionId)
                .onSuccess {
                    _actionState.value = QuestionBankActionState.Success("Question removed")
                    loadBankQuestions(bankId)
                    refreshBanks()
                }
                .onFailure { _actionState.value = QuestionBankActionState.Error(it.message ?: "Remove failed") }
        }
    }

    fun createQuestion(request: QuestionRequest) {
        _actionState.value = QuestionBankActionState.Loading
        viewModelScope.launch {
            questionRepository.createQuestion(request)
                .onSuccess { response ->
                    _actionState.value = QuestionBankActionState.Success("题目创建成功")
                    loadAllQuestions()
                    _selectedBank.value?.let { bank ->
                        loadBankQuestions(bank.id)
                        refreshBanks()
                    }
                }
                .onFailure { _actionState.value = QuestionBankActionState.Error(it.message ?: "创建失败") }
        }
    }

    fun downloadTemplate() {
        _actionState.value = QuestionBankActionState.Success("模板下载功能开发中")
    }

    fun importQuestions() {
        _actionState.value = QuestionBankActionState.Success("题目导入功能开发中")
    }

    fun resetActionState() {
        _actionState.value = QuestionBankActionState.Idle
    }
}

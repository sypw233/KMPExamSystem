package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.ImportResultResponse
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
                .onFailure { _uiState.value = QuestionBankUiState.Error(it.message ?: "加载题库失败") }
        }
    }

    fun createBank(name: String, description: String?) {
        if (name.isBlank()) return
        if (_actionState.value is QuestionBankActionState.Loading) return
        _actionState.value = QuestionBankActionState.Loading
        viewModelScope.launch {
            questionBankRepository.createBank(QuestionBankRequest(name.trim(), description?.takeIf { it.isNotBlank() }))
                .onSuccess {
                    _actionState.value = QuestionBankActionState.Success("题库创建成功")
                    refreshBanks()
                }
                .onFailure { _actionState.value = QuestionBankActionState.Error(it.message ?: "创建失败") }
        }
    }

    fun updateBank(bankId: Long, name: String, description: String?) {
        if (name.isBlank()) return
        if (_actionState.value is QuestionBankActionState.Loading) return
        _actionState.value = QuestionBankActionState.Loading
        viewModelScope.launch {
            questionBankRepository.updateBank(bankId, QuestionBankRequest(name.trim(), description?.takeIf { it.isNotBlank() }))
                .onSuccess {
                    _actionState.value = QuestionBankActionState.Success("题库更新成功")
                    refreshBanks()
                }
                .onFailure { _actionState.value = QuestionBankActionState.Error(it.message ?: "更新失败") }
        }
    }

    fun deleteBank(bankId: Long) {
        if (_actionState.value is QuestionBankActionState.Loading) return
        _actionState.value = QuestionBankActionState.Loading
        viewModelScope.launch {
            questionBankRepository.deleteBank(bankId)
                .onSuccess {
                    if (_selectedBank.value?.id == bankId) {
                        _selectedBank.value = null
                        _bankQuestions.value = emptyList()
                    }
                    _actionState.value = QuestionBankActionState.Success("题库删除成功")
                    refreshBanks()
                }
                .onFailure { _actionState.value = QuestionBankActionState.Error(it.message ?: "删除失败") }
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
                    _actionState.value = QuestionBankActionState.Error(it.message ?: "加载题库题目失败")
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
        if (_actionState.value is QuestionBankActionState.Loading) return
        _actionState.value = QuestionBankActionState.Loading
        viewModelScope.launch {
            questionBankRepository.addQuestionToBank(bankId, questionId)
                .onSuccess {
                    _actionState.value = QuestionBankActionState.Success("题目添加成功")
                    loadBankQuestions(bankId)
                    refreshBanks()
                }
                .onFailure { _actionState.value = QuestionBankActionState.Error(it.message ?: "添加失败") }
        }
    }

    fun removeQuestionFromBank(bankId: Long, questionId: Long) {
        if (_actionState.value is QuestionBankActionState.Loading) return
        _actionState.value = QuestionBankActionState.Loading
        viewModelScope.launch {
            questionBankRepository.removeQuestionFromBank(bankId, questionId)
                .onSuccess {
                    _actionState.value = QuestionBankActionState.Success("题目移除成功")
                    loadBankQuestions(bankId)
                    refreshBanks()
                }
                .onFailure { _actionState.value = QuestionBankActionState.Error(it.message ?: "移除失败") }
        }
    }

    fun createQuestion(request: QuestionRequest) {
        if (_actionState.value is QuestionBankActionState.Loading) return
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

    fun updateQuestion(questionId: Long, request: QuestionRequest) {
        if (_actionState.value is QuestionBankActionState.Loading) return
        _actionState.value = QuestionBankActionState.Loading
        viewModelScope.launch {
            questionRepository.updateQuestion(questionId, request)
                .onSuccess {
                    _actionState.value = QuestionBankActionState.Success("题目更新成功")
                    loadAllQuestions()
                    _selectedBank.value?.let { bank ->
                        loadBankQuestions(bank.id)
                        refreshBanks()
                    }
                }
                .onFailure { _actionState.value = QuestionBankActionState.Error(it.message ?: "更新失败") }
        }
    }

    fun downloadTemplate(
        onSuccess: (ByteArray) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_actionState.value is QuestionBankActionState.Loading) return
        _actionState.value = QuestionBankActionState.Loading
        viewModelScope.launch {
            questionRepository.downloadTemplate()
                .onSuccess {
                    _actionState.value = QuestionBankActionState.Success("模板下载成功")
                    onSuccess(it)
                }
                .onFailure {
                    _actionState.value = QuestionBankActionState.Error(it.message ?: "模板下载失败")
                    onError(it.message ?: "模板下载失败")
                }
        }
    }

    fun importQuestions(
        bankId: Long,
        fileBytes: ByteArray,
        fileName: String,
        onSuccess: (ImportResultResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_actionState.value is QuestionBankActionState.Loading) return
        _actionState.value = QuestionBankActionState.Loading
        viewModelScope.launch {
            questionRepository.importQuestions(bankId, fileBytes, fileName)
                .onSuccess { result ->
                    val msg = buildString {
                        append("导入完成: 成功 ${result.successCount} 条")
                        if (result.failedCount > 0) append(", 失败 ${result.failedCount} 条")
                    }
                    _actionState.value = QuestionBankActionState.Success(msg)
                    loadAllQuestions()
                    loadBankQuestions(bankId)
                    refreshBanks()
                    onSuccess(result)
                }
                .onFailure {
                    _actionState.value = QuestionBankActionState.Error(it.message ?: "导入失败")
                    onError(it.message ?: "导入失败")
                }
        }
    }

    fun resetActionState() {
        _actionState.value = QuestionBankActionState.Idle
    }
}

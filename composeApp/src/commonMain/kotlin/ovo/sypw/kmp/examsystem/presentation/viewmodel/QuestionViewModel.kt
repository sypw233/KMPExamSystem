package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.dto.QuestionRequest
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.data.repository.QuestionRepository
import ovo.sypw.kmp.examsystem.presentation.navigation.UserRole

/** 题目管理 UI 状态 */
sealed interface QuestionUiState {
    data object Loading : QuestionUiState
    data class Success(val questions: List<QuestionResponse>) : QuestionUiState
    data class Error(val message: String) : QuestionUiState
}

/** 题目操作状态 */
sealed interface QuestionActionState {
    data object Idle : QuestionActionState
    data object Loading : QuestionActionState
    data class Success(val message: String) : QuestionActionState
    data class Error(val message: String) : QuestionActionState
}

/**
 * 题目管理 ViewModel（教师/管理员）
 * 管理员加载全部题目，教师只加载自己的
 */
class QuestionViewModel(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuestionUiState>(QuestionUiState.Loading)
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    private val _actionState = MutableStateFlow<QuestionActionState>(QuestionActionState.Idle)
    val actionState: StateFlow<QuestionActionState> = _actionState.asStateFlow()

    private var userRole: UserRole = UserRole.UNKNOWN
    // 完整题目列表（未按类型筛选前）
    private var cachedQuestions: List<QuestionResponse> = emptyList()

    /** 设置用户角色，管理员加载全量，其他加载我的 */
    fun setRole(role: UserRole) {
        userRole = role
        load()
    }

    /** 加载题目列表（依据角色决定接口） */
    fun load() {
        viewModelScope.launch {
            _uiState.value = QuestionUiState.Loading
            val result = if (userRole == UserRole.ADMIN)
                questionRepository.loadAllQuestions()
            else
                questionRepository.loadMyQuestions()
            result
                .onSuccess { questions ->
                    cachedQuestions = questions
                    applyFilter()
                }
                .onFailure { e ->
                    _uiState.value = QuestionUiState.Error(e.message ?: "加载题目失败")
                }
        }
    }

    /** 旧方法保留：教师加载我的题目 */
    fun loadMyQuestions() {
        viewModelScope.launch {
            _uiState.value = QuestionUiState.Loading
            questionRepository.loadMyQuestions()
                .onSuccess { questions ->
                    cachedQuestions = questions
                    applyFilter()
                }
                .onFailure { e ->
                    _uiState.value = QuestionUiState.Error(e.message ?: "加载题目失败")
                }
        }
    }

    /** 按类型筛选 */
    fun filterByType(type: String?) {
        _selectedType.value = type
        applyFilter()
    }

    private fun applyFilter() {
        val type = _selectedType.value
        val filtered = if (type != null) cachedQuestions.filter { it.type == type } else cachedQuestions
        _uiState.value = QuestionUiState.Success(filtered)
    }

    /** 创建题目 */
    fun createQuestion(request: QuestionRequest) {
        _actionState.value = QuestionActionState.Loading
        viewModelScope.launch {
            questionRepository.createQuestion(request).fold(
                onSuccess = {
                    _actionState.value = QuestionActionState.Success("题目创建成功")
                    load()
                },
                onFailure = { _actionState.value = QuestionActionState.Error(it.message ?: "创建失败") }
            )
        }
    }

    /** 更新题目 */
    fun updateQuestion(questionId: Long, request: QuestionRequest) {
        _actionState.value = QuestionActionState.Loading
        viewModelScope.launch {
            questionRepository.updateQuestion(questionId, request).fold(
                onSuccess = {
                    _actionState.value = QuestionActionState.Success("题目更新成功")
                    load()
                },
                onFailure = { _actionState.value = QuestionActionState.Error(it.message ?: "更新失败") }
            )
        }
    }

    /** 删除题目 */
    fun deleteQuestion(questionId: Long) {
        _actionState.value = QuestionActionState.Loading
        viewModelScope.launch {
            questionRepository.deleteQuestion(questionId).fold(
                onSuccess = {
                    _actionState.value = QuestionActionState.Success("题目已删除")
                    load()
                },
                onFailure = { _actionState.value = QuestionActionState.Error(it.message ?: "删除失败") }
            )
        }
    }

    fun resetActionState() { _actionState.value = QuestionActionState.Idle }
}

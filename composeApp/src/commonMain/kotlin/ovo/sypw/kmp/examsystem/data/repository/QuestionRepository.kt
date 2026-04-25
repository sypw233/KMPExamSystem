package ovo.sypw.kmp.examsystem.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ovo.sypw.kmp.examsystem.data.api.QuestionApi
import ovo.sypw.kmp.examsystem.data.dto.BatchDeleteRequest
import ovo.sypw.kmp.examsystem.data.dto.BatchDeleteResult
import ovo.sypw.kmp.examsystem.data.dto.ImportResultResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionRequest
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

/**
 * 题目管理仓库（完整版）
 */
class QuestionRepository(
    private val questionApi: QuestionApi,
    private val tokenStorage: TokenStorage
) {

    private val _myQuestions = MutableStateFlow<List<QuestionResponse>>(emptyList())
    val myQuestions: StateFlow<List<QuestionResponse>> = _myQuestions.asStateFlow()

    private val _allQuestions = MutableStateFlow<List<QuestionResponse>>(emptyList())
    val allQuestions: StateFlow<List<QuestionResponse>> = _allQuestions.asStateFlow()

    /** 加载所有题目（管理员，分页） */
    suspend fun loadAllQuestions(): Result<List<QuestionResponse>> = runWithToken { token ->
        val r = questionApi.getAllQuestions(token)
        if (r.code == 200 && r.data != null) { _allQuestions.value = r.data.content; r.data.content }
        else throw Exception(r.message)
    }

    /** 加载我创建的题目 */
    suspend fun loadMyQuestions(): Result<List<QuestionResponse>> = runWithToken { token ->
        val r = questionApi.getMyQuestions(token)
        if (r.code == 200 && r.data != null) { _myQuestions.value = r.data; r.data }
        else throw Exception(r.message)
    }

    /** 获取题目详情 */
    suspend fun getQuestionDetail(questionId: Long): Result<QuestionResponse> = runWithToken { token ->
        val r = questionApi.getQuestionDetail(token, questionId)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /** 创建题目 */
    suspend fun createQuestion(request: QuestionRequest): Result<QuestionResponse> = runWithToken { token ->
        val r = questionApi.createQuestion(token, request)
        if (r.code == 200 && r.data != null) {
            _myQuestions.value = _myQuestions.value + r.data
            r.data
        } else throw Exception(r.message)
    }

    /** 更新题目 */
    suspend fun updateQuestion(questionId: Long, request: QuestionRequest): Result<QuestionResponse> = runWithToken { token ->
        val r = questionApi.updateQuestion(token, questionId, request)
        if (r.code == 200 && r.data != null) {
            _myQuestions.value = _myQuestions.value.map { if (it.id == questionId) r.data else it }
            r.data
        } else throw Exception(r.message)
    }

    /** 删除题目 */
    suspend fun deleteQuestion(questionId: Long): Result<Unit> = runWithToken { token ->
        val r = questionApi.deleteQuestion(token, questionId)
        if (r.code == 200) {
            _myQuestions.value = _myQuestions.value.filter { it.id != questionId }
            Unit
        } else throw Exception(r.message)
    }

    /** 下载题目导入模板 */
    suspend fun downloadTemplate(): Result<ByteArray> = runWithToken { token ->
        val r = questionApi.downloadTemplate(token)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message ?: "下载失败")
    }

    /** 导入题目（Excel） */
    suspend fun importQuestions(bankId: Long, fileBytes: ByteArray, fileName: String): Result<ImportResultResponse> = runWithToken { token ->
        val r = questionApi.importQuestions(token, bankId, fileBytes, fileName)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message ?: "导入失败")
    }

    /** 批量删除题目 */
    suspend fun batchDeleteQuestions(ids: List<Long>): Result<BatchDeleteResult> = runWithToken { token ->
        val r = questionApi.batchDeleteQuestions(token, BatchDeleteRequest(ids))
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /** 按类型筛选题目 */
    suspend fun getQuestionsByType(type: String): Result<List<QuestionResponse>> = runWithToken { token ->
        val r = questionApi.getQuestionsByType(token, type)
        if (r.code == 200) r.data ?: emptyList() else throw Exception(r.message)
    }

    /** 按难度筛选题目 */
    suspend fun getQuestionsByDifficulty(difficulty: String): Result<List<QuestionResponse>> = runWithToken { token ->
        val r = questionApi.getQuestionsByDifficulty(token, difficulty)
        if (r.code == 200) r.data ?: emptyList() else throw Exception(r.message)
    }

    /** 按分类筛选题目 */
    suspend fun getQuestionsByCategory(category: String): Result<List<QuestionResponse>> = runWithToken { token ->
        val r = questionApi.getQuestionsByCategory(token, category)
        if (r.code == 200) r.data ?: emptyList() else throw Exception(r.message)
    }

    private suspend fun <T> runWithToken(block: suspend (String) -> T): Result<T> {
        return try {
            val token = tokenStorage.getAccessToken() ?: throw Exception("未登录")
            Result.success(block(token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

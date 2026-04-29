package ovo.sypw.kmp.examsystem.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ovo.sypw.kmp.examsystem.data.api.QuestionBankApi
import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.PageQuestionBankResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionBankRequest
import ovo.sypw.kmp.examsystem.data.dto.QuestionBankResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

/**
 * 题库管理仓库
 */
class QuestionBankRepository(
    private val questionBankApi: QuestionBankApi,
    private val tokenStorage: TokenStorage
) {
    private val _myBanks = MutableStateFlow<List<QuestionBankResponse>>(emptyList())
    val myBanks: StateFlow<List<QuestionBankResponse>> = _myBanks.asStateFlow()

    private val _bankQuestions = MutableStateFlow<List<QuestionResponse>>(emptyList())
    val bankQuestions: StateFlow<List<QuestionResponse>> = _bankQuestions.asStateFlow()

    suspend fun loadMyBanks(): Result<List<QuestionBankResponse>> = runWithToken { token ->
        fetchBankPages { page, size -> questionBankApi.getMyBanks(token, page, size) }.also {
            _myBanks.value = it
        }
    }

    /** 获取题库详情 */
    suspend fun getBankDetail(bankId: Long): Result<QuestionBankResponse> = runWithToken { token ->
        val r = questionBankApi.getBankDetail(token, bankId)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    suspend fun createBank(request: QuestionBankRequest): Result<QuestionBankResponse> = runWithToken { token ->
        val r = questionBankApi.createBank(token, request)
        if (r.code == 200 && r.data != null) { loadMyBanks(); r.data }
        else throw Exception(r.message)
    }

    suspend fun updateBank(bankId: Long, request: QuestionBankRequest): Result<QuestionBankResponse> = runWithToken { token ->
        val r = questionBankApi.updateBank(token, bankId, request)
        if (r.code == 200 && r.data != null) { loadMyBanks(); r.data }
        else throw Exception(r.message)
    }

    suspend fun deleteBank(bankId: Long): Result<Unit> = runWithToken { token ->
        val r = questionBankApi.deleteBank(token, bankId)
        if (r.code == 200) { _myBanks.value = _myBanks.value.filter { it.id != bankId }; Unit }
        else throw Exception(r.message)
    }

    suspend fun loadBankQuestions(bankId: Long): Result<List<QuestionResponse>> = runWithToken { token ->
        val r = questionBankApi.getBankQuestions(token, bankId)
        if (r.code == 200 && r.data != null) { _bankQuestions.value = r.data; r.data }
        else throw Exception(r.message)
    }

    suspend fun addQuestionToBank(bankId: Long, questionId: Long): Result<Unit> = runWithToken { token ->
        val r = questionBankApi.addQuestionToBank(token, bankId, questionId)
        if (r.code == 200) Unit else throw Exception(r.message)
    }

    suspend fun removeQuestionFromBank(bankId: Long, questionId: Long): Result<Unit> = runWithToken { token ->
        val r = questionBankApi.removeQuestionFromBank(token, bankId, questionId)
        if (r.code == 200) { _bankQuestions.value = _bankQuestions.value.filter { it.id != questionId }; Unit }
        else throw Exception(r.message)
    }

    private suspend fun fetchBankPages(
        requestPage: suspend (page: Int, size: Int) -> ApiResponse<PageQuestionBankResponse>
    ): List<QuestionBankResponse> {
        val banks = mutableListOf<QuestionBankResponse>()
        var page = 0
        val size = 100
        var hasNextPage: Boolean

        do {
            val response = requestPage(page, size)
            if (response.code != 200) throw Exception(response.message)
            val data = response.data ?: break
            banks += data.content
            page += 1
            hasNextPage = !data.last && page < data.totalPages
        } while (hasNextPage)

        return banks.distinctBy { it.id }
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

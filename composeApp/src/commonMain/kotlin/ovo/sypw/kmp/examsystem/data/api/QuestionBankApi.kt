package ovo.sypw.kmp.examsystem.data.api

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionBankRequest
import ovo.sypw.kmp.examsystem.data.dto.QuestionBankResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.parseData

/**
 * 题库管理 API 服务（教师/管理员，全部 8 个接口）
 */
class QuestionBankApi : BaseApiService() {

    companion object {
        private const val BANK_ENDPOINT = "/api/question-banks"
    }

    /** 获取所有题库 */
    suspend fun getAllBanks(token: String): ApiResponse<List<QuestionBankResponse>> {
        val result = getWithToken(endpoint = BANK_ENDPOINT, token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 获取题库详情 */
    suspend fun getBankDetail(token: String, bankId: Long): ApiResponse<QuestionBankResponse> {
        val result = getWithToken(endpoint = "$BANK_ENDPOINT/$bankId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 获取我的题库 */
    suspend fun getMyBanks(token: String): ApiResponse<List<QuestionBankResponse>> {
        val result = getWithToken(endpoint = "$BANK_ENDPOINT/my", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 创建题库 */
    suspend fun createBank(token: String, request: QuestionBankRequest): ApiResponse<QuestionBankResponse> {
        val result = postWithToken(endpoint = BANK_ENDPOINT, token = token, body = request)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 更新题库 */
    suspend fun updateBank(token: String, bankId: Long, request: QuestionBankRequest): ApiResponse<QuestionBankResponse> {
        val result = putWithToken(endpoint = "$BANK_ENDPOINT/$bankId", token = token, body = request)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 删除题库 */
    suspend fun deleteBank(token: String, bankId: Long): ApiResponse<Unit> {
        val result = deleteWithToken(endpoint = "$BANK_ENDPOINT/$bankId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, Unit)
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 获取题库中的所有题目 */
    suspend fun getBankQuestions(token: String, bankId: Long): ApiResponse<List<QuestionResponse>> {
        val result = getWithToken(endpoint = "$BANK_ENDPOINT/$bankId/questions", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 添加题目到题库 */
    suspend fun addQuestionToBank(token: String, bankId: Long, questionId: Long): ApiResponse<Unit> {
        val result = postWithToken(
            endpoint = "$BANK_ENDPOINT/$bankId/questions",
            token = token,
            body = mapOf("questionId" to questionId)
        )
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, Unit)
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 从题库移除题目 */
    suspend fun removeQuestionFromBank(token: String, bankId: Long, questionId: Long): ApiResponse<Unit> {
        val result = deleteWithToken(endpoint = "$BANK_ENDPOINT/$bankId/questions/$questionId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, Unit)
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 导出题库（返回下载 URL） */
    suspend fun exportBank(token: String, bankId: Long): ApiResponse<Unit> {
        val result = getWithToken(endpoint = "$BANK_ENDPOINT/$bankId/export", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, Unit)
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }
}

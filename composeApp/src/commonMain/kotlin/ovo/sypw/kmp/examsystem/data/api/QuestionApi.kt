package ovo.sypw.kmp.examsystem.data.api

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionRequest
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.parseData

/**
 * 题目管理 API 服务（教师/管理员）
 */
class QuestionApi : BaseApiService() {

    companion object {
        private const val QUESTION_ENDPOINT = "/api/questions"
    }

    /** 获取所有题目 */
    suspend fun getAllQuestions(token: String): ApiResponse<List<QuestionResponse>> {
        val result = getWithToken(endpoint = QUESTION_ENDPOINT, token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 获取我创建的题目 */
    suspend fun getMyQuestions(token: String): ApiResponse<List<QuestionResponse>> {
        val result = getWithToken(endpoint = "$QUESTION_ENDPOINT/my", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 获取题目详情 */
    suspend fun getQuestionDetail(token: String, questionId: Long): ApiResponse<QuestionResponse> {
        val result = getWithToken(endpoint = "$QUESTION_ENDPOINT/$questionId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 按类型筛选题目 */
    suspend fun getQuestionsByType(token: String, type: String): ApiResponse<List<QuestionResponse>> {
        val result = getWithToken(endpoint = "$QUESTION_ENDPOINT/type/$type", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 按难度筛选题目 (easy, medium, hard) */
    suspend fun getQuestionsByDifficulty(token: String, difficulty: String): ApiResponse<List<QuestionResponse>> {
        val result = getWithToken(endpoint = "$QUESTION_ENDPOINT/difficulty/$difficulty", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 按分类筛选题目 */
    suspend fun getQuestionsByCategory(token: String, category: String): ApiResponse<List<QuestionResponse>> {
        val result = getWithToken(endpoint = "$QUESTION_ENDPOINT/category/$category", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 创建题目 */
    suspend fun createQuestion(token: String, request: QuestionRequest): ApiResponse<QuestionResponse> {
        val result = postWithToken(endpoint = QUESTION_ENDPOINT, token = token, body = request)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 更新题目 */
    suspend fun updateQuestion(token: String, questionId: Long, request: QuestionRequest): ApiResponse<QuestionResponse> {
        val result = putWithToken(endpoint = "$QUESTION_ENDPOINT/$questionId", token = token, body = request)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 删除题目 */
    suspend fun deleteQuestion(token: String, questionId: Long): ApiResponse<Unit> {
        val result = deleteWithToken(endpoint = "$QUESTION_ENDPOINT/$questionId", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, Unit)
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 导入题目（Excel/CSV） */
    suspend fun importQuestions(token: String, bankId: Long): ApiResponse<Unit> {
        val result = postWithToken(
            endpoint = "$QUESTION_ENDPOINT/import",
            token = token,
            body = mapOf("bankId" to bankId)
        )
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, Unit)
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }
}

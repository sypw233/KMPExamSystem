package ovo.sypw.kmp.examsystem.data.repository

import ovo.sypw.kmp.examsystem.data.api.AiGradingApi
import ovo.sypw.kmp.examsystem.data.dto.AiBatchGradingRequest
import ovo.sypw.kmp.examsystem.data.dto.AiBatchGradingResponse
import ovo.sypw.kmp.examsystem.data.dto.AiConfigRequest
import ovo.sypw.kmp.examsystem.data.dto.AiConfigResponse
import ovo.sypw.kmp.examsystem.data.dto.AiGradingRequest
import ovo.sypw.kmp.examsystem.data.dto.AiGradingResponse
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

/**
 * AI 辅助判题仓库
 */
class AiGradingRepository(
    private val aiGradingApi: AiGradingApi,
    private val tokenStorage: TokenStorage
) {

    suspend fun getAiConfigs(): Result<List<AiConfigResponse>> {
        return runWithToken { token ->
            val r = aiGradingApi.getAiConfigs(token)
            if (r.code == 200 && r.data != null) r.data
            else throw Exception(r.message)
        }
    }

    suspend fun updateAiConfig(request: AiConfigRequest): Result<AiConfigResponse> {
        return runWithToken { token ->
            val r = aiGradingApi.updateAiConfig(token, request)
            if (r.code == 200 && r.data != null) r.data
            else throw Exception(r.message)
        }
    }

    suspend fun aiGrade(questionId: Long, studentAnswer: String, maxScore: Int): Result<AiGradingResponse> {
        return runWithToken { token ->
            val r = aiGradingApi.aiGrade(token, AiGradingRequest(questionId, studentAnswer, maxScore))
            if (r.code == 200 && r.data != null) r.data
            else throw Exception(r.message)
        }
    }

    suspend fun batchGrade(submissionId: Long, concurrency: Int? = null): Result<AiBatchGradingResponse> {
        return runWithToken { token ->
            val r = aiGradingApi.batchGrade(token, AiBatchGradingRequest(submissionId, concurrency))
            if (r.code == 200 && r.data != null) r.data
            else throw Exception(r.message)
        }
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

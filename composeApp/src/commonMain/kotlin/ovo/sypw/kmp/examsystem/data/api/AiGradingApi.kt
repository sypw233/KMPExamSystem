package ovo.sypw.kmp.examsystem.data.api

import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.AiBatchGradingRequest
import ovo.sypw.kmp.examsystem.data.dto.AiBatchGradingResponse
import ovo.sypw.kmp.examsystem.data.dto.AiConfigRequest
import ovo.sypw.kmp.examsystem.data.dto.AiConfigResponse
import ovo.sypw.kmp.examsystem.data.dto.AiGradingRequest
import ovo.sypw.kmp.examsystem.data.dto.AiGradingResponse
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.parseData

/**
 * AI 辅助判题 API 服务（全部 3 个接口）
 */
class AiGradingApi : BaseApiService() {

    companion object {
        private const val AI_ENDPOINT = "/api/ai-grading"
    }

    /** 获取 AI 配置列表 */
    suspend fun getAiConfigs(token: String): ApiResponse<List<AiConfigResponse>> {
        val result = getWithToken(endpoint = "$AI_ENDPOINT/config", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** 更新 AI 配置（管理员） */
    suspend fun updateAiConfig(token: String, request: AiConfigRequest): ApiResponse<AiConfigResponse> {
        val result = putWithToken(endpoint = "$AI_ENDPOINT/config", token = token, body = request)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /** AI 辅助判题 */
    suspend fun aiGrade(token: String, request: AiGradingRequest): ApiResponse<AiGradingResponse> {
        val result = postWithToken(endpoint = "$AI_ENDPOINT/grade", token = token, body = request)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * AI 批量评分（对一次提交中的所有主观题进行批量评分）
     * @param token 访问令牌
     * @param request AI批量评分请求
     * @return AI批量评分响应
     */
    suspend fun batchGrade(token: String, request: AiBatchGradingRequest): ApiResponse<AiBatchGradingResponse> {
        val result = postWithToken(
            endpoint = "$AI_ENDPOINT/grade-submission",
            token = token,
            body = request
        )
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }
}

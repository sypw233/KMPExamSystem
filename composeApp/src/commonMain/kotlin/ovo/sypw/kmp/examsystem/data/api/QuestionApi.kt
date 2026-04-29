package ovo.sypw.kmp.examsystem.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.BatchDeleteRequest
import ovo.sypw.kmp.examsystem.data.dto.BatchDeleteResult
import ovo.sypw.kmp.examsystem.data.dto.ImportResultResponse
import ovo.sypw.kmp.examsystem.data.dto.PageQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionRequest
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.SaResult
import ovo.sypw.kmp.examsystem.data.dto.result.parseData

/**
 * 题目管理 API 服务（教师/管理员）
 * @param httpClient 共享的HTTP客户端实例
 */
class QuestionApi(httpClient: HttpClient) : BaseApiService(httpClient) {

    companion object {
        private const val QUESTION_ENDPOINT = "/api/questions"
    }

    /** 查询题目列表（分页，支持筛选） */
    suspend fun getAllQuestions(
        token: String,
        page: Int = 0,
        size: Int = 20,
        type: String? = null,
        difficulty: String? = null,
        category: String? = null
    ): ApiResponse<PageQuestionResponse> {
        val params = buildMap<String, Any> {
            put("page", page)
            put("size", size)
            type?.let { put("type", it) }
            difficulty?.let { put("difficulty", it) }
            category?.let { put("category", it) }
        }
        val result = getWithToken(endpoint = QUESTION_ENDPOINT, token = token, parameters = params)
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

    /**
     * 导入题目（multipart/form-data）
     * @param token 访问令牌
     * @param bankId 题库ID（query参数）
     * @param fileBytes Excel文件内容
     * @param fileName 文件名
     */
    suspend fun importQuestions(token: String, bankId: Long, fileBytes: ByteArray, fileName: String): ApiResponse<ImportResultResponse> {
        return try {
            val response = httpClient.post(HttpClientConfig.getApiUrl("/api/question-import-export/import?bankId=$bankId")) {
                header(io.ktor.http.HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    io.ktor.client.request.forms.MultiPartFormDataContent(
                        io.ktor.client.request.forms.formData {
                            append("file", fileBytes, io.ktor.http.Headers.build {
                                append(io.ktor.http.HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                append(io.ktor.http.HttpHeaders.ContentType, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                            })
                        }
                    )
                )
            }
            if (response.status == io.ktor.http.HttpStatusCode.OK) {
                val saResult = response.body<SaResult>()
                ApiResponse(saResult.code, saResult.msg, saResult.parseData())
            } else {
                ApiResponse(response.status.value, "导入失败", null)
            }
        } catch (e: Exception) {
            ApiResponse(500, e.message ?: "导入异常", null)
        }
    }

    /**
     * 批量删除题目
     * @param token 访问令牌
     * @param request 批量删除请求（ID列表）
     * @return 批量删除结果
     */
    suspend fun batchDeleteQuestions(token: String, request: BatchDeleteRequest): ApiResponse<BatchDeleteResult> {
        val result = postWithToken(
            endpoint = "$QUESTION_ENDPOINT/batch-delete",
            token = token,
            body = request
        )
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * 下载题目导入模板
     * @param token 访问令牌
     * @return 模板文件字节数组
     */
    suspend fun downloadTemplate(token: String): ApiResponse<ByteArray> {
        return try {
            val response = httpClient.get(HttpClientConfig.getApiUrl("/api/question-import-export/template")) {
                header(io.ktor.http.HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status == io.ktor.http.HttpStatusCode.OK) {
                val bytes = response.body<ByteArray>()
                ApiResponse(200, "下载成功", bytes)
            } else {
                ApiResponse(response.status.value, "下载失败", null)
            }
        } catch (e: Exception) {
            ApiResponse(500, e.message ?: "下载异常", null)
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

    /** 按难度筛选题目 */
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
}

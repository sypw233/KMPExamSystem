package ovo.sypw.kmp.examsystem.data.api

import io.ktor.client.plugins.timeout
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import ovo.sypw.kmp.examsystem.data.dto.ApiTestRequest
import ovo.sypw.kmp.examsystem.data.dto.ApiTestResponse
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.utils.Logger
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * API测试服务类
 * 负责执行各种HTTP请求测试
 */
class ApiTestService : BaseApiService() {

    /**
     * 执行API测试请求
     * @param request API测试请求参数
     * @return 测试结果Flow
     */
    @OptIn(ExperimentalTime::class)
    fun executeApiTest(request: ApiTestRequest): Flow<NetworkResult<ApiTestResponse>> = flow {
        emit(NetworkResult.Loading)

        try {

            val startTime = Clock.System.now().toEpochMilliseconds()

            Logger.d("ApiTestService", "开始执行API测试: ${request.method} ${request.url}")

            val response = when (request.method.uppercase()) {
                "GET" -> executeGetRequest(request)
                "POST" -> executePostRequest(request)
                "PUT" -> executePutRequest(request)
                "DELETE" -> executeDeleteRequest(request)
                else -> throw IllegalArgumentException("不支持的HTTP方法: ${request.method}")
            }

            val endTime = Clock.System.now().toEpochMilliseconds()
            val responseTime = endTime - startTime

            val responseBody = response.bodyAsText()
            val responseHeaders = response.headers.entries().associate {
                it.key to it.value.joinToString(", ")
            }

            val apiTestResponse = ApiTestResponse(
                statusCode = response.status.value,
                statusText = response.status.description,
                headers = responseHeaders,
                body = responseBody,
                responseTime = responseTime,
                isSuccess = response.status.value in 200..299
            )

            Logger.d(
                "ApiTestService",
                "API测试完成，状态码: ${response.status.value}, 响应时间: ${responseTime}ms"
            )
            emit(NetworkResult.Success(apiTestResponse))

        } catch (e: Exception) {
            Logger.e("ApiTestService", "API测试失败", e)

            val errorResponse = ApiTestResponse(
                statusCode = 0,
                statusText = "请求失败",
                headers = emptyMap(),
                body = e.message ?: "未知错误",
                responseTime = 0,
                isSuccess = false
            )

            emit(NetworkResult.Error(e, e.message ?: "未知错误"))
        }
    }

    /**
     * 执行GET请求
     */
    private suspend fun executeGetRequest(request: ApiTestRequest): HttpResponse {
        return httpClient.get {
            url(request.url)

            // 设置请求头
            request.headers.forEach { (key, value) ->
                if (key.isNotBlank() && value.isNotBlank()) {
                    header(key, value)
                }
            }

            // 设置查询参数
            request.queryParams.forEach { (key, value) ->
                if (key.isNotBlank() && value.isNotBlank()) {
                    parameter(key, value)
                }
            }

            // 设置超时
            timeout {
                requestTimeoutMillis = request.timeout
            }
        }
    }

    /**
     * 执行POST请求
     */
    private suspend fun executePostRequest(request: ApiTestRequest): HttpResponse {
        return httpClient.post {
            url(request.url)

            // 设置请求头
            request.headers.forEach { (key, value) ->
                if (key.isNotBlank() && value.isNotBlank()) {
                    header(key, value)
                }
            }

            // 设置查询参数
            request.queryParams.forEach { (key, value) ->
                if (key.isNotBlank() && value.isNotBlank()) {
                    parameter(key, value)
                }
            }

            // 设置请求体
            if (request.body.isNotBlank()) {
                contentType(ContentType.Application.Json)
                setBody(request.body)
            }

            // 设置超时
            timeout {
                requestTimeoutMillis = request.timeout
            }
        }
    }

    /**
     * 执行PUT请求
     */
    private suspend fun executePutRequest(request: ApiTestRequest): HttpResponse {
        return httpClient.put {
            url(request.url)

            // 设置请求头
            request.headers.forEach { (key, value) ->
                if (key.isNotBlank() && value.isNotBlank()) {
                    header(key, value)
                }
            }

            // 设置查询参数
            request.queryParams.forEach { (key, value) ->
                if (key.isNotBlank() && value.isNotBlank()) {
                    parameter(key, value)
                }
            }

            // 设置请求体
            if (request.body.isNotBlank()) {
                contentType(ContentType.Application.Json)
                setBody(request.body)
            }

            // 设置超时
            timeout {
                requestTimeoutMillis = request.timeout
            }
        }
    }

    /**
     * 执行DELETE请求
     */
    private suspend fun executeDeleteRequest(request: ApiTestRequest): HttpResponse {
        return httpClient.delete {
            url(request.url)

            // 设置请求头
            request.headers.forEach { (key, value) ->
                if (key.isNotBlank() && value.isNotBlank()) {
                    header(key, value)
                }
            }

            // 设置查询参数
            request.queryParams.forEach { (key, value) ->
                if (key.isNotBlank() && value.isNotBlank()) {
                    parameter(key, value)
                }
            }

            // 设置超时
            timeout {
                requestTimeoutMillis = request.timeout
            }
        }
    }

    /**
     * 验证URL格式
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            val regex = Regex(
                "^(https?://)" + // 协议
                        "([\\w.-]+)" + // 域名
                        "(:[0-9]+)?" + // 端口（可选）
                        "(/.*)?$" // 路径（可选）
            )
            regex.matches(url)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取支持的HTTP方法列表
     */
    fun getSupportedMethods(): List<String> {
        return listOf(
            HttpMethod.Get.value,
            HttpMethod.Post.value,
            HttpMethod.Put.value,
            HttpMethod.Delete.value
        )
    }
}
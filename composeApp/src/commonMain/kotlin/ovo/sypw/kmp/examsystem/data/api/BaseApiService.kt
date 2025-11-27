package ovo.sypw.kmp.examsystem.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.SaResult
import ovo.sypw.kmp.examsystem.utils.Logger

/**
 * 基础API服务类
 * 提供通用的网络请求方法
 */
abstract class BaseApiService {

    /**
     * HTTP客户端实例
     */
    protected val httpClient: HttpClient by lazy {
        HttpClientConfig.createHttpClient()
    }

    /**
     * 执行GET请求
     * @param endpoint API端点
     * @param parameters 请求参数
     * @return 网络请求结果
     */
    protected suspend fun get(
        endpoint: String,
        parameters: Map<String, Any> = emptyMap()
    ): NetworkResult<SaResult> {
//        Logger.d("BaseApiService", "API请求URL: ${HttpClientConfig.getApiUrl(endpoint)}")
        return safeApiCall {
            httpClient.get(HttpClientConfig.getApiUrl(endpoint)) {
                parameters.forEach { (key, value) ->
                    parameter(key, value)
                }
            }
        }
    }

    /**
     * 执行带Token的GET请求
     * @param endpoint API端点
     * @param token 认证令牌
     * @param parameters 请求参数
     * @return 网络请求结果
     */
    protected suspend fun getWithToken(
        endpoint: String,
        token: String,
        parameters: Map<String, Any> = emptyMap()
    ): NetworkResult<SaResult> {
        Logger.d("BaseApiService", "API请求URL: ${HttpClientConfig.getApiUrl(endpoint)}")
        return safeApiCall {
            httpClient.get(HttpClientConfig.getApiUrl(endpoint)) {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameters.forEach { (key, value) ->
                    parameter(key, value)
                }
            }
        }
    }

    /**
     * 执行带Token的文件下载GET请求
     * @param endpoint API端点
     * @param token 认证令牌
     * @param parameters 请求参数
     * @return 文件数据结果
     */
    protected suspend fun getFileWithToken(
        endpoint: String,
        token: String,
        parameters: Map<String, Any> = emptyMap()
    ): NetworkResult<ByteArray> {
        Logger.d("BaseApiService", "文件下载请求URL: ${HttpClientConfig.getApiUrl(endpoint)}")
        return safeFileApiCall {
            httpClient.get(HttpClientConfig.getApiUrl(endpoint)) {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameters.forEach { (key, value) ->
                    parameter(key, value)
                }
            }
        }
    }

    /**
     * 执行POST请求
     * @param endpoint API端点
     * @param body 请求体
     * @param parameters 请求参数
     * @return 网络请求结果
     */
    protected suspend fun post(
        endpoint: String,
        body: Any? = null,
        parameters: Map<String, Any> = emptyMap()
    ): NetworkResult<SaResult> {
        return safeApiCall {
            httpClient.post(HttpClientConfig.getApiUrl(endpoint)) {
                contentType(ContentType.Application.Json)
                parameters.forEach { (key, value) ->
                    parameter(key, value)
                }
                body?.let { setBody(it) }
            }
        }
    }


    /**
     * 执行带Token的POST请求
     * @param endpoint API端点
     * @param token 认证令牌
     * @param body 请求体
     * @param parameters 请求参数
     * @return 网络请求结果
     */
    protected suspend fun postWithToken(
        endpoint: String,
        token: String,
        body: Any? = null,
        parameters: Map<String, Any> = emptyMap()
    ): NetworkResult<SaResult> {
        return safeApiCall {
            httpClient.post(HttpClientConfig.getApiUrl(endpoint)) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                parameters.forEach { (key, value) ->
                    parameter(key, value)
                }
                body?.let { setBody(it) }
            }
        }
    }

    /**
     * 执行PUT请求
     * @param endpoint API端点
     * @param body 请求体
     * @param parameters 请求参数
     * @return 网络请求结果
     */
    protected suspend fun put(
        endpoint: String,
        body: Any? = null,
        parameters: Map<String, Any> = emptyMap()
    ): NetworkResult<SaResult> {
        return safeApiCall {
            httpClient.put(HttpClientConfig.getApiUrl(endpoint)) {
                contentType(ContentType.Application.Json)
                parameters.forEach { (key, value) ->
                    parameter(key, value)
                }
                body?.let { setBody(it) }
            }
        }
    }

    /**
     * 执行带Token的PUT请求
     * @param endpoint API端点
     * @param token 认证令牌
     * @param body 请求体
     * @param parameters 请求参数
     * @return 网络请求结果
     */
    protected suspend fun putWithToken(
        endpoint: String,
        token: String,
        body: Any? = null,
        parameters: Map<String, Any> = emptyMap()
    ): NetworkResult<SaResult> {
        return safeApiCall {
            httpClient.put(HttpClientConfig.getApiUrl(endpoint)) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                parameters.forEach { (key, value) ->
                    parameter(key, value)
                }
                body?.let { setBody(it) }
            }
        }
    }

    /**
     * 执行DELETE请求
     * @param endpoint API端点
     * @param parameters 请求参数
     * @return 网络请求结果
     */
    protected suspend fun delete(
        endpoint: String,
        parameters: Map<String, Any> = emptyMap()
    ): NetworkResult<SaResult> {
        return safeApiCall {
            httpClient.delete(HttpClientConfig.getApiUrl(endpoint)) {
                parameters.forEach { (key, value) ->
                    parameter(key, value)
                }
            }
        }
    }

    /**
     * 执行带Token的DELETE请求
     * @param endpoint API端点
     * @param token 认证令牌
     * @param parameters 请求参数
     * @return 网络请求结果
     */
    protected suspend fun deleteWithToken(
        endpoint: String,
        token: String,
        parameters: Map<String, Any> = emptyMap()
    ): NetworkResult<SaResult> {
        return safeApiCall {
            httpClient.delete(HttpClientConfig.getApiUrl(endpoint)) {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameters.forEach { (key, value) ->
                    parameter(key, value)
                }
            }
        }
    }

    /**
     * 安全的文件下载API调用方法
     * 统一处理文件下载请求异常和响应解析
     * @param apiCall API调用函数
     * @return 文件数据结果
     */
    protected suspend inline fun safeFileApiCall(
        crossinline apiCall: suspend () -> HttpResponse
    ): NetworkResult<ByteArray> {
        return try {
            val response = apiCall()

            when (response.status) {
                HttpStatusCode.OK -> {
                    try {
                        val fileData = response.body<ByteArray>()
                        Logger.d("BaseApiService", "文件下载成功，大小: ${fileData.size} bytes")
                        NetworkResult.Success(fileData)
                    } catch (e: Exception) {
                        Logger.e("BaseApiService", "文件数据读取失败: ${e.message}")
                        NetworkResult.Error(
                            e,
                            "文件数据读取失败: ${e.message}"
                        )
                    }
                }

                HttpStatusCode.Unauthorized -> {
                    NetworkResult.Error(
                        Exception("Unauthorized"),
                        "认证失败，请重新登录"
                    )
                }

                HttpStatusCode.Forbidden -> {
                    NetworkResult.Error(
                        Exception("Forbidden"),
                        "权限不足"
                    )
                }

                HttpStatusCode.NotFound -> {
                    NetworkResult.Error(
                        Exception("Not Found"),
                        "请求的资源不存在"
                    )
                }

                HttpStatusCode.InternalServerError -> {
                    NetworkResult.Error(
                        Exception("Internal Server Error"),
                        "服务器内部错误"
                    )
                }

                else -> {
                    Logger.e("BaseApiService", "文件下载失败，状态码: ${response.status}")
                    NetworkResult.Error(
                        Exception("HTTP ${response.status}"),
                        "文件下载失败，状态码: ${response.status}"
                    )
                }
            }
        } catch (e: Exception) {
            Logger.e("BaseApiService", "文件下载网络请求异常: ${e.message}")
            NetworkResult.Error(e, "网络请求失败: ${e.message}")
        }
    }

    /**
     * 安全的API调用方法
     * 统一处理网络请求异常和响应解析
     * @param apiCall API调用函数
     * @return 网络请求结果
     */
    protected suspend inline fun safeApiCall(
        crossinline apiCall: suspend () -> HttpResponse
    ): NetworkResult<SaResult> {
        return try {
            val response = apiCall()

            when (response.status) {
                HttpStatusCode.OK -> {
                    try {
                        // 先获取原始响应文本用于调试
//                        val responseText = response.bodyAsText()
//                        Logger.d("BaseApiService", "服务器原始响应: $responseText")

                        val saResult = response.body<SaResult>()
//                        Logger.d("BaseApiService", "解析后的SaResult: $saResult")
                        NetworkResult.Success(saResult)
                    } catch (e: Exception) {
                        Logger.e("BaseApiService", "响应解析失败: ${e.message}")
                        NetworkResult.Error(
                            e,
                            "响应格式解析失败: ${e.message}"
                        )
                    }
                }

                HttpStatusCode.Unauthorized -> {
                    NetworkResult.Error(
                        Exception("Unauthorized"),
                        "认证失败，请重新登录"
                    )
                }

                HttpStatusCode.Forbidden -> {
                    NetworkResult.Error(
                        Exception("Forbidden"),
                        "权限不足"
                    )
                }

                HttpStatusCode.NotFound -> {
                    NetworkResult.Error(
                        Exception("Not Found"),
                        "请求的资源不存在"
                    )
                }

                HttpStatusCode.InternalServerError -> {
                    NetworkResult.Error(
                        Exception("Internal Server Error"),
                        "服务器内部错误"
                    )
                }

                else -> {
                    NetworkResult.Error(
                        Exception("HTTP ${response.status.value}"),
                        "请求失败: ${response.status.description}"
                    )
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                e,
                e.message ?: "网络请求失败"
            )
        }
    }

    /**
     * 关闭HTTP客户端
     */
    fun close() {
        httpClient.close()
    }
}
package ovo.sypw.kmp.examsystem.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import ovo.sypw.kmp.examsystem.data.dto.ApiResponse
import ovo.sypw.kmp.examsystem.data.dto.FileUploadResponse
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.data.dto.result.SaResult
import ovo.sypw.kmp.examsystem.data.dto.result.parseData

/**
 * 文件管理 API 服务（全部 4 个接口）
 * @param httpClient 共享的HTTP客户端实例
 */
class FileApi(httpClient: HttpClient) : BaseApiService(httpClient) {

    companion object {
        private const val FILE_ENDPOINT = "/api/files"
    }

    /**
     * 上传图片（multipart/form-data）
     */
    suspend fun uploadImage(token: String, imageBytes: ByteArray, fileName: String): ApiResponse<FileUploadResponse> {
        return try {
            val response = httpClient.post(HttpClientConfig.getApiUrl("$FILE_ENDPOINT/image")) {
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    MultiPartFormDataContent(formData {
                        append("file", imageBytes, Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            append(HttpHeaders.ContentType, "image/*")
                        })
                    })
                )
            }
            if (response.status == HttpStatusCode.OK) {
                val saResult = response.body<SaResult>()
                ApiResponse(saResult.code, saResult.msg, saResult.parseData())
            } else {
                ApiResponse(response.status.value, "上传失败", null)
            }
        } catch (e: Exception) {
            ApiResponse(500, e.message ?: "上传异常", null)
        }
    }

    /**
     * 上传文档（multipart/form-data）
     */
    suspend fun uploadDocument(token: String, docBytes: ByteArray, fileName: String): ApiResponse<FileUploadResponse> {
        return try {
            val response = httpClient.post(HttpClientConfig.getApiUrl("$FILE_ENDPOINT/document")) {
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    MultiPartFormDataContent(formData {
                        append("file", docBytes, Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            append(HttpHeaders.ContentType, "application/octet-stream")
                        })
                    })
                )
            }
            if (response.status == HttpStatusCode.OK) {
                val saResult = response.body<SaResult>()
                ApiResponse(saResult.code, saResult.msg, saResult.parseData())
            } else {
                ApiResponse(response.status.value, "上传失败", null)
            }
        } catch (e: Exception) {
            ApiResponse(500, e.message ?: "上传异常", null)
        }
    }

    /**
     * 获取文件 URL
     */
    suspend fun getFileUrl(token: String, fileKey: String): ApiResponse<String> {
        val result = getWithToken(endpoint = "$FILE_ENDPOINT/url/$fileKey", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, result.data.parseData())
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }

    /**
     * 删除文件
     */
    suspend fun deleteFile(token: String, fileKey: String): ApiResponse<Unit> {
        val result = deleteWithToken(endpoint = "$FILE_ENDPOINT/$fileKey", token = token)
        return when (result) {
            is NetworkResult.Success -> ApiResponse(result.data.code, result.data.msg, Unit)
            is NetworkResult.Error -> ApiResponse(500, result.message, null)
            else -> ApiResponse(500, "未知状态", null)
        }
    }
}

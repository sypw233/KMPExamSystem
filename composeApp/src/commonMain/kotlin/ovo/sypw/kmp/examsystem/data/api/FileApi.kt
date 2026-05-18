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
    suspend fun uploadImage(
        token: String,
        imageBytes: ByteArray,
        fileName: String,
        category: String = "temp"
    ): ApiResponse<FileUploadResponse> {
        return try {
            val contentType = detectImageContentType(imageBytes) ?: resolveImageContentType(fileName)
            val uploadFileName = normalizeImageFileName(fileName, contentType)
            val response = httpClient.post(HttpClientConfig.getApiUrl("$FILE_ENDPOINT/image")) {
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    MultiPartFormDataContent(formData {
                        append("category", category)
                        append("file", imageBytes, Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=\"${uploadFileName.replace("\"", "")}\"")
                            append(HttpHeaders.ContentType, contentType)
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

    private fun resolveImageContentType(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "svg" -> "image/svg+xml"
            else -> "application/octet-stream"
        }
    }

    private fun detectImageContentType(bytes: ByteArray): String? {
        if (bytes.size >= 12) {
            val isJpeg = bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()
            val isPng = bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()
            val isGif = bytes[0] == 'G'.code.toByte() && bytes[1] == 'I'.code.toByte() && bytes[2] == 'F'.code.toByte()
            val isBmp = bytes[0] == 'B'.code.toByte() && bytes[1] == 'M'.code.toByte()
            val isWebp = bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte() && bytes[2] == 'F'.code.toByte() &&
                bytes[3] == 'F'.code.toByte() && bytes[8] == 'W'.code.toByte() && bytes[9] == 'E'.code.toByte() &&
                bytes[10] == 'B'.code.toByte() && bytes[11] == 'P'.code.toByte()
            when {
                isJpeg -> return "image/jpeg"
                isPng -> return "image/png"
                isGif -> return "image/gif"
                isBmp -> return "image/bmp"
                isWebp -> return "image/webp"
            }
        }
        val head = bytes.take(256).map { it.toInt().toChar() }.joinToString("").trimStart()
        return if (head.startsWith("<svg", ignoreCase = true) || head.contains("<svg", ignoreCase = true)) {
            "image/svg+xml"
        } else {
            null
        }
    }

    private fun normalizeImageFileName(fileName: String, contentType: String): String {
        val cleanName = fileName.substringAfterLast('/').substringAfterLast('\\').takeIf { it.isNotBlank() && it != "image/*" }
            ?: "avatar"
        val expectedExtension = when (contentType) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            "image/bmp" -> "bmp"
            "image/svg+xml" -> "svg"
            else -> null
        } ?: return cleanName
        val currentExtension = cleanName.substringAfterLast('.', "").lowercase()
        return if (currentExtension in setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg")) {
            cleanName
        } else {
            "$cleanName.$expectedExtension"
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

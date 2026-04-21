package ovo.sypw.kmp.examsystem.data.repository

import ovo.sypw.kmp.examsystem.data.api.FileApi
import ovo.sypw.kmp.examsystem.data.dto.FileUploadResponse
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

/**
 * 文件管理仓库
 */
class FileRepository(
    private val fileApi: FileApi,
    private val tokenStorage: TokenStorage
) {

    suspend fun uploadImage(imageBytes: ByteArray, fileName: String): Result<FileUploadResponse> {
        return runWithToken { token ->
            val r = fileApi.uploadImage(token, imageBytes, fileName)
            if (r.code == 200 && r.data != null) r.data
            else throw Exception(r.message ?: "上传失败")
        }
    }

    suspend fun uploadDocument(docBytes: ByteArray, fileName: String): Result<FileUploadResponse> {
        return runWithToken { token ->
            val r = fileApi.uploadDocument(token, docBytes, fileName)
            if (r.code == 200 && r.data != null) r.data
            else throw Exception(r.message ?: "上传失败")
        }
    }

    suspend fun getFileUrl(fileKey: String): Result<String> {
        return runWithToken { token ->
            val r = fileApi.getFileUrl(token, fileKey)
            if (r.code == 200 && r.data != null) r.data
            else throw Exception(r.message)
        }
    }

    suspend fun deleteFile(fileKey: String): Result<Unit> {
        return runWithToken { token ->
            val r = fileApi.deleteFile(token, fileKey)
            if (r.code == 200) Unit
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

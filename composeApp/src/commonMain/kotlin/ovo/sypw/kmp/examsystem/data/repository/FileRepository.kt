package ovo.sypw.kmp.examsystem.data.repository

import ovo.sypw.kmp.examsystem.data.api.FileApi
import ovo.sypw.kmp.examsystem.data.dto.FileUploadResponse
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

/**
 * 鏂囦欢绠＄悊浠撳簱
 */
class FileRepository(
    private val fileApi: FileApi,
    tokenStorage: TokenStorage
) : BaseRepository(tokenStorage) {

    suspend fun uploadImage(
        imageBytes: ByteArray,
        fileName: String,
        category: String = "temp"
    ): Result<FileUploadResponse> {
        return runWithToken { token ->
            val r = fileApi.uploadImage(token, imageBytes, fileName, category)
            if (r.code == 200 && r.data != null) r.data
            else throw Exception(r.message)
        }
    }

    suspend fun uploadDocument(docBytes: ByteArray, fileName: String): Result<FileUploadResponse> {
        return runWithToken { token ->
            val r = fileApi.uploadDocument(token, docBytes, fileName)
            if (r.code == 200 && r.data != null) r.data
            else throw Exception(r.message)
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
}

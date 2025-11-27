package ovo.sypw.kmp.examsystem.utils.file

import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.PlatformFile

/**
 * 跨平台文件工具类接口
 * 使用FileKit库实现跨平台文件操作功能
 */
interface FileUtils {
    /**
     * 检查当前平台是否支持文件选择
     * @return 是否支持文件选择
     */
    fun isFileSelectionSupported(): Boolean

    /**
     * 选择图片文件
     * @return 选择的图片文件，如果取消选择则返回null
     */
    suspend fun selectImage(): PlatformFile?

    /**
     * 选择单个文件
     * @return 选择的文件，如果取消选择则返回null
     */
    suspend fun selectFile(): PlatformFile?


    /**
     * 保存文件
     * @param data 文件数据
     * @param fileName 建议的文件名
     * @param extension 文件扩展名
     * @return 保存的文件，如果取消保存则返回null
     */
    suspend fun saveFile(data: ByteArray, fileName: String, extension: String): PlatformFile?

    /**
     * 从PlatformFile读取字节数组
     * @param file 平台文件对象
     * @return 文件的字节数组
     */
    suspend fun readBytes(file: PlatformFile): ByteArray

    /**
     * 将字节数组转换为ImageBitmap
     * @param bytes 图片字节数组
     * @return ImageBitmap对象
     */
    fun bytesToImageBitmap(bytes: ByteArray): ImageBitmap?

    /**
     * 从PlatformFile转换为ImageBitmap
     * @param file 图片文件
     * @return ImageBitmap对象
     */
    suspend fun fileToImageBitmap(file: PlatformFile): ImageBitmap?

    // 兼容性方法，保持向后兼容
    /**
     * 选择图片文件并返回字节数组（兼容性方法）
     * @return 图片的字节数组，如果取消选择则返回null
     */
    suspend fun selectImageBytes(): ByteArray? {
        val file = selectImage()
        return file?.let { readBytes(it) }
    }

    /**
     * 选择文件并返回字节数组（兼容性方法）
     * @return 文件的字节数组，如果取消选择则返回null
     */
    suspend fun selectFileBytes(): ByteArray? {
        val file = selectFile()
        return file?.let { readBytes(it) }
    }

    /**
     * 保存文件（兼容性方法）
     * @param data 文件数据
     * @param fileName 文件名
     * @param mimeType MIME类型
     * @return 是否保存成功
     */
    suspend fun saveFileCompat(data: ByteArray, fileName: String, mimeType: String): Boolean {
        val extension = when {
            mimeType.contains("excel") || mimeType.contains("spreadsheet") -> "xlsx"
            mimeType.contains("pdf") -> "pdf"
            mimeType.contains("image/png") -> "png"
            mimeType.contains("image/jpeg") -> "jpg"
            else -> "txt"
        }
        val file = saveFile(data, fileName, extension)
        return file != null
    }
}

/**
 * 获取平台特定的FileUtils实例
 */
expect fun createFileUtils(): FileUtils


/**
 * 支持的图片格式
 */
object SupportedImageFormats {
    val extensions = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    val mimeTypes = listOf(
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/bmp",
        "image/webp"
    )
}

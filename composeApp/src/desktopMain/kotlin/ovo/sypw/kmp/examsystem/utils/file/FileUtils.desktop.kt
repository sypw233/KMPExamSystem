package ovo.sypw.kmp.examsystem.utils.file

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import org.jetbrains.skia.Image

/**
 * Desktop平台的FileUtils实现
 * 使用FileKit库提供跨平台文件操作功能
 */
class DesktopFileUtils : FileUtils {

    /**
     * 检查当前平台是否支持文件选择
     * Desktop平台始终支持文件选择
     * @return 始终返回true
     */
    override fun isFileSelectionSupported(): Boolean = true

    /**
     * 选择图片文件
     * 使用FileKit的图片选择器，支持常见的图片格式
     * @return 选择的图片文件，取消选择时返回null
     */
    override suspend fun selectImage(): PlatformFile? {
        return try {
            FileKit.openFilePicker(
                type = FileKitType.Image,
                mode = FileKitMode.Single
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 选择单个文件
     * 支持选择任意类型的文件
     * @return 选择的文件，取消选择时返回null
     */
    override suspend fun selectFile(): PlatformFile? {
        return try {
            FileKit.openFilePicker(
                type = FileKitType.File(),
                mode = FileKitMode.Single
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    /**
     * 保存文件
     * 使用FileKit的文件保存功能
     * @param data 文件数据
     * @param fileName 建议的文件名
     * @param extension 文件扩展名
     * @return 保存的文件，取消保存时返回null
     */
    override suspend fun saveFile(
        data: ByteArray,
        fileName: String,
        extension: String
    ): PlatformFile? {
        return try {
            val file = FileKit.openFileSaver(
                suggestedName = fileName,
                extension = extension
            )
            file?.write(data)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 从PlatformFile读取字节数组
     * @param file 平台文件对象
     * @return 文件的字节数组
     */
    override suspend fun readBytes(file: PlatformFile): ByteArray {
        return try {
            file.readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(0)
        }
    }

    /**
     * 将字节数组转换为ImageBitmap
     * 使用Skia进行图片解码，适用于Desktop平台
     * @param bytes 图片字节数组
     * @return ImageBitmap对象，解码失败时返回null
     */
    override fun bytesToImageBitmap(bytes: ByteArray): ImageBitmap? {
        return try {
            val skiaImage = Image.makeFromEncoded(bytes)
            skiaImage.toComposeImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 从PlatformFile转换为ImageBitmap
     * 先读取文件字节数组，然后转换为ImageBitmap
     * @param file 图片文件
     * @return ImageBitmap对象，转换失败时返回null
     */
    override suspend fun fileToImageBitmap(file: PlatformFile): ImageBitmap? {
        return try {
            val bytes = readBytes(file)
            bytesToImageBitmap(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * 创建Desktop平台的FileUtils实例
 * @return FileUtils实例
 */
actual fun createFileUtils(): FileUtils {
    return DesktopFileUtils()
}


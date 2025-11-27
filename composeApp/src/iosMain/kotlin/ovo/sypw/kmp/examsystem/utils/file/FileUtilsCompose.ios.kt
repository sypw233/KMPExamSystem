package ovo.sypw.kmp.examsystem.utils.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * iOS平台的FileUtilsCompose实现
 * 在Composable中获取FileUtils实例
 */
@Composable
actual fun rememberFileUtils(): FileUtils {
    return remember {
        createFileUtils()
    }
}
package ovo.sypw.kmp.examsystem.utils.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android平台的Compose集成实现
 * 提供在Composable中获取FileUtils实例的功能
 */

/**
 * 在Composable中记住FileUtils实例
 * Android平台实现，使用LocalContext获取当前上下文
 * @return FileUtils实例，当context变化时会重新创建
 */
@Composable
actual fun rememberFileUtils(): FileUtils {
    val context = LocalContext.current
    return remember(context) {
        createFileUtils(context)
    }
}
package ovo.sypw.kmp.examsystem

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.di.appModule
import ovo.sypw.kmp.examsystem.utils.file.FileUtils
import ovo.sypw.kmp.examsystem.utils.file.createFileUtils

/**
 * iOS 平台的 Koin 应用初始化
 */
@Composable
actual fun PlatformKoinApplication(content: @Composable () -> Unit) {
    KoinApplication(
        application = {
            modules(appModule)
            modules(module {
                single<FileUtils> {
                    createFileUtils()
                }
            })
        }
    ) {
        content()
    }
}
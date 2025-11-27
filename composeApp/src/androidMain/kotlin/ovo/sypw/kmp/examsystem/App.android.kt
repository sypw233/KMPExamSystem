package ovo.sypw.kmp.examsystem

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.di.appModule
import ovo.sypw.kmp.examsystem.utils.file.FileUtils
import ovo.sypw.kmp.examsystem.utils.file.createFileUtils

/**
 * Android 平台的 Koin 应用初始化
 * 自动注入 Android Context
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
actual fun PlatformKoinApplication(content: @Composable () -> Unit) {
    val context = LocalContext.current
    Log.d("MAIN", "PlatformKoinApplication: START")

    KoinApplication(
        application = {
            androidContext(context)
            modules(appModule)
            modules(module {
                single<Context> { context }
                single<FileUtils> {
                    createFileUtils(get<Context>())
                }
            })
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.systemBars,
            content = { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    content()
                }


            }
        )
    }

}
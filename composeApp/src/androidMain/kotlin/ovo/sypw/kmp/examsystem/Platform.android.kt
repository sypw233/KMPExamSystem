package ovo.sypw.kmp.examsystem

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun updateSystemBarStyle(isDark: Boolean) {
    // Android 通过 WindowInsetsControllerCompat 或 edgeToEdge 处理系统栏样式
}
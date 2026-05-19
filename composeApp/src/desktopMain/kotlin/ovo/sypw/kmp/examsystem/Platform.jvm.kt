package ovo.sypw.kmp.examsystem

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun updateSystemBarStyle(isDark: Boolean) {
    // Desktop 平台无状态栏样式概念
}
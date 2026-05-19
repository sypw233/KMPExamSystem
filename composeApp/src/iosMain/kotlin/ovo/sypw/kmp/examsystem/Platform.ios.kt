package ovo.sypw.kmp.examsystem

import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun updateSystemBarStyle(isDark: Boolean) {
    // iOS 状态栏样式由宿主 UIViewController/SwiftUI 配置
}

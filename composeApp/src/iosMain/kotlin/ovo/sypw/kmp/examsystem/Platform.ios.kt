package ovo.sypw.kmp.examsystem

import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIStatusBarStyle
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun updateSystemBarStyle(isDark: Boolean) {
    val style = if (isDark) UIStatusBarStyleLightContent else UIStatusBarStyleDarkContent
    UIApplication.sharedApplication.statusBarStyle = style
}
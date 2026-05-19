package ovo.sypw.kmp.examsystem

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

/**
 * 更新系统栏样式以匹配当前主题
 * - iOS: 切换状态栏文字颜色（light/dark content）
 * - Android: Edge-to-Edge 场景下更新系统栏图标颜色
 * - Desktop: no-op
 */
expect fun updateSystemBarStyle(isDark: Boolean)
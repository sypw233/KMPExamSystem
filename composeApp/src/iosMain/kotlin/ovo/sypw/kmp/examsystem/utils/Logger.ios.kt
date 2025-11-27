package ovo.sypw.kmp.examsystem.utils

import platform.Foundation.NSLog

/**
 * iOS平台的日志实现
 * 使用NSLog进行日志输出
 */
actual fun log(level: Logger.Level, tag: String, message: String, throwable: Throwable?) {
    val levelStr = when (level) {
        Logger.Level.DEBUG -> "[DEBUG]"
        Logger.Level.INFO -> "[INFO]"
        Logger.Level.WARN -> "[WARN]"
        Logger.Level.ERROR -> "[ERROR]"
    }

    val logMessage = if (throwable != null) {
        "$levelStr [$tag] $message\nException: ${throwable.message}\n${throwable.stackTraceToString()}"
    } else {
        "$levelStr [$tag] $message"
    }

    NSLog(logMessage)
}
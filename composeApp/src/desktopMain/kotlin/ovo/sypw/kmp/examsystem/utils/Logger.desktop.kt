package ovo.sypw.kmp.examsystem.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Desktop平台的日志实现
 * 使用标准输出println进行日志输出
 */
actual fun log(level: Logger.Level, tag: String, message: String, throwable: Throwable?) {
    val timestamp =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))

    val levelStr = when (level) {
        Logger.Level.DEBUG -> "[DEBUG]"
        Logger.Level.INFO -> "[INFO ]"
        Logger.Level.WARN -> "[WARN ]"
        Logger.Level.ERROR -> "[ERROR]"
    }

    val logMessage = if (throwable != null) {
        "$timestamp $levelStr [$tag] $message\nException: ${throwable.message}\n${throwable.stackTraceToString()}"
    } else {
        "$timestamp $levelStr [$tag] $message"
    }

    // 根据日志级别选择输出流
    when (level) {
        Logger.Level.ERROR -> System.err.println(logMessage)
        else -> println(logMessage)
    }
}
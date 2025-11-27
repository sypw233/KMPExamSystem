package ovo.sypw.kmp.examsystem.utils

import android.util.Log

/**
 * Android平台的日志实现
 * 使用Android的Log类进行日志输出
 */
actual fun log(level: Logger.Level, tag: String, message: String, throwable: Throwable?) {
    val logMessage = if (throwable != null) {
        "$message\n${Log.getStackTraceString(throwable)}"
    } else {
        message
    }

    when (level) {
        Logger.Level.DEBUG -> Log.d(tag, logMessage)
        Logger.Level.INFO -> Log.i(tag, logMessage)
        Logger.Level.WARN -> Log.w(tag, logMessage)
        Logger.Level.ERROR -> Log.e(tag, logMessage)
    }
}
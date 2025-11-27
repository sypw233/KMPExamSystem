package ovo.sypw.kmp.examsystem.utils

/**
 * 跨平台日志工具类
 * 使用expect/actual机制实现不同平台的日志输出
 */
object Logger {

    /**
     * 日志级别枚举
     */
    enum class Level {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    /**
     * 调试日志
     * @param tag 标签
     * @param message 消息
     * @param throwable 异常（可选）
     */
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.DEBUG, tag, message, throwable)
    }

    /**
     * 信息日志
     * @param tag 标签
     * @param message 消息
     * @param throwable 异常（可选）
     */
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.INFO, tag, message, throwable)
    }

    /**
     * 警告日志
     * @param tag 标签
     * @param message 消息
     * @param throwable 异常（可选）
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.WARN, tag, message, throwable)
    }

    /**
     * 错误日志
     * @param tag 标签
     * @param message 消息
     * @param throwable 异常（可选）
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.ERROR, tag, message, throwable)
    }

    /**
     * 简化的调试日志（使用默认标签）
     * @param message 消息
     */
    fun d(message: String) {
        d("KMP", message)
    }

    /**
     * 简化的信息日志（使用默认标签）
     * @param message 消息
     */
    fun i(message: String) {
        i("KMP", message)
    }

    /**
     * 简化的警告日志（使用默认标签）
     * @param message 消息
     */
    fun w(message: String) {
        w("KMP", message)
    }

    /**
     * 简化的错误日志（使用默认标签）
     * @param message 消息
     */
    fun e(message: String) {
        e("KMP", message)
    }

    /**
     * 打印对象信息（调试用）
     * @param tag 标签
     * @param obj 对象
     */
    fun printObject(tag: String, obj: Any?) {
        d(tag, "Object: $obj")
    }

    /**
     * 打印对象信息（使用默认标签）
     * @param obj 对象
     */
    fun printObject(obj: Any?) {
        printObject("KMP", obj)
    }
}

/**
 * 平台特定的日志实现
 * @param level 日志级别
 * @param tag 标签
 * @param message 消息
 * @param throwable 异常（可选）
 */
expect fun log(level: Logger.Level, tag: String, message: String, throwable: Throwable? = null)

/**
 * 扩展函数：为任何对象添加日志功能
 */
fun Any.logd(message: String) {
    Logger.d(this::class.simpleName ?: "Unknown", message)
}

fun Any.logi(message: String) {
    Logger.i(this::class.simpleName ?: "Unknown", message)
}

fun Any.logw(message: String) {
    Logger.w(this::class.simpleName ?: "Unknown", message)
}

fun Any.loge(message: String, throwable: Throwable? = null) {
    Logger.e(this::class.simpleName ?: "Unknown", message, throwable)
}
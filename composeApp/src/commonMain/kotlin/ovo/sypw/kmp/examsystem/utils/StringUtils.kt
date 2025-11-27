package ovo.sypw.kmp.examsystem.utils

import ovo.sypw.kmp.examsystem.utils.StringUtils.format
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 字符串工具类
 * 提供字符串格式化和处理相关的扩展函数
 */
object StringUtils {

    /**
     * 字符串格式化扩展函数，类似于Kotlin原生的String.format
     * 支持多种格式化占位符和可变参数
     *
     * @param format 格式化字符串，支持以下占位符：
     *   - %s: 字符串
     *   - %d: 整数
     *   - %f: 浮点数
     *   - %.1f: 保留1位小数的浮点数
     *   - %.2f: 保留2位小数的浮点数
     * @param args 可变参数列表
     * @return 格式化后的字符串
     */
    fun String.Companion.format(format: String, vararg args: Any?): String {
        var result = format
        var argIndex = 0

        // 处理格式化占位符
        val regex = Regex("%(\\.[0-9]+)?[sdfl]")
        result = regex.replace(result) { matchResult ->
            if (argIndex >= args.size) {
                matchResult.value // 如果参数不够，保持原样
            } else {
                val arg = args[argIndex++]
                when {
                    matchResult.value == "%s" -> arg?.toString() ?: "null"
                    matchResult.value == "%d" -> {
                        when (arg) {
                            is Number -> arg.toLong().toString()
                            else -> arg?.toString() ?: "0"
                        }
                    }

                    matchResult.value == "%f" -> {
                        when (arg) {
                            is Number -> arg.toDouble().toString()
                            else -> "0.0"
                        }
                    }

                    matchResult.value.matches(Regex("%.([0-9]+)f")) -> {
                        val decimals =
                            matchResult.value.substring(2, matchResult.value.length - 1).toInt()
                        when (arg) {
                            is Number -> {
                                val value = arg.toDouble()
                                val multiplier = 10.0.pow(decimals.toDouble())
                                val rounded = round(value * multiplier) / multiplier
                                // 手动格式化小数位数
                                val intPart = rounded.toLong()
                                val fracPart = ((rounded - intPart) * multiplier).toLong()
                                "$intPart.${fracPart.toString().padStart(decimals, '0')}"
                            }

                            else -> "0.${'0'.toString().repeat(decimals)}"
                        }
                    }

                    else -> matchResult.value
                }
            }
        }

        return result
    }

    /**
     * 简化的浮点数格式化函数（保持向后兼容）
     * @param format 格式化字符串
     * @param percentage 浮点数值
     * @return 格式化后的字符串
     */
    fun String.Companion.formatFloat(format: String, percentage: Float): String {
        return format(format, percentage)
    }

    /**
     * 字符串是否为空或仅包含空白字符
     * @return 如果字符串为空或仅包含空白字符则返回true
     */
    fun String?.isNullOrBlank(): Boolean {
        return this == null || this.isBlank()
    }

    /**
     * 安全的字符串截取
     * @param maxLength 最大长度
     * @param suffix 超出长度时的后缀，默认为"..."
     * @return 截取后的字符串
     */
    fun String.truncate(maxLength: Int, suffix: String = "..."): String {
        return if (this.length <= maxLength) {
            this
        } else {
            this.substring(0, maxLength - suffix.length) + suffix
        }
    }

    /**
     * 首字母大写
     * @return 首字母大写的字符串
     */
    fun String.capitalize(): String {
        return if (this.isEmpty()) {
            this
        } else {
            this.first().uppercaseChar() + this.drop(1)
        }
    }


    /**
     * 格式化文件大小显示
     * @param sizeInBytes 文件大小（字节）
     * @return 格式化后的大小字符串
     */
    fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups =
            (log10(sizeInBytes.toDouble()) / log10(1024.0)).toInt().coerceAtMost(units.size - 1)

        val size = sizeInBytes / 1024.0.pow(digitGroups.toDouble())
        return String.format("%.1f %s", size, units[digitGroups])
    }

    /**
     * 格式化日期时间显示
     * @param dateTimeString ISO格式的日期时间字符串
     * @return 格式化后的日期时间字符串
     */
    fun formatDateTime(dateTimeString: String): String {
        return try {
            // 如果是ISO格式，转换为更友好的显示格式
            if (dateTimeString.contains('T')) {
                val parts = dateTimeString.split('T')
                val datePart = parts[0]
                val timePart = parts.getOrNull(1)?.substringBefore('.') ?: "00:00:00"
                "$datePart $timePart"
            } else {
                dateTimeString
            }
        } catch (e: Exception) {
            dateTimeString
        }
    }

    /**
     * 格式化相对时间显示（如：2小时前、3天前）
     * @param dateTimeString ISO格式的日期时间字符串
     * @return 相对时间字符串
     */
    @OptIn(ExperimentalTime::class)
    fun formatRelativeTime(dateTimeString: String): String {
        return try {
            Clock.System.now().toEpochMilliseconds()
            // 简单的时间解析，实际项目中建议使用kotlinx-datetime
            if (dateTimeString.contains('T')) {
                dateTimeString.replace('T', ' ').substringBefore('.')
            } else {
                dateTimeString
            }

            // 这里返回格式化的时间，实际实现需要根据具体需求调整
            formatDateTime(dateTimeString)
        } catch (e: Exception) {
            dateTimeString
        }
    }


}
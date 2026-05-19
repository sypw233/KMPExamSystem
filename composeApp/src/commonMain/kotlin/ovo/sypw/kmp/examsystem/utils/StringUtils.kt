package ovo.sypw.kmp.examsystem.utils

import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 字符串工具类
 * 提供字符串格式化和处理相关的扩展函数
 */
object StringUtils {

    /**
     * 字符串格式化扩展函数
     * 支持多种格式化占位符和可变参数, 适用于KMP跨平台环境
     *
     * @param format 格式化字符串, 支持以下占位符：
     *   - %s: 字符串
     *   - %d/%02d/%04d: 整数
     *   - %f: 浮点数
     *   - %.1f: 保留1位小数的浮点数
     *   - %.2f: 保留2位小数的浮点数
     * @param args 可变参数列表
     * @return 格式化后的字符串
     */
    fun String.Companion.format(format: String, vararg args: Any?): String {
        var result = format
        var argIndex = 0

        val regex = Regex("%(0\\d+)?(\\.[0-9]+)?[sdfl]")
        result = regex.replace(result) { matchResult ->
            if (argIndex >= args.size) {
                matchResult.value
            } else {
                val arg = args[argIndex++]
                val placeholder = matchResult.value
                when {
                    placeholder == "%s" -> arg?.toString() ?: "null"
                    placeholder.endsWith("d") -> {
                        val value = when (arg) {
                            is Number -> arg.toLong().toString()
                            else -> arg?.toString() ?: "0"
                        }
                        val width = placeholder
                            .removePrefix("%")
                            .removeSuffix("d")
                            .takeIf { it.startsWith('0') }
                            ?.drop(1)
                            ?.toIntOrNull()
                        if (width != null) value.padStart(width, '0') else value
                    }

                    placeholder == "%f" -> {
                        when (arg) {
                            is Number -> arg.toDouble().toString()
                            else -> "0.0"
                        }
                    }

                    placeholder.matches(Regex("%.([0-9]+)f")) -> {
                        val decimals =
                            placeholder.substring(2, placeholder.length - 1).toInt()
                        when (arg) {
                            is Number -> {
                                val value = arg.toDouble()
                                val multiplier = 10.0.pow(decimals.toDouble())
                                val rounded = round(value * multiplier) / multiplier
                                val intPart = rounded.toLong()
                                if (decimals == 0) return@replace intPart.toString()
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

    fun String.format(vararg args: Any?): String {
        return String.Companion.format(this, *args)
    }

    /**
     * 安全的字符串截取
     * @param maxLength 最大长度
     * @param suffix 超出长度时的后缀, 默认为"..."
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
        return String.Companion.format("%.1f %s", size, units[digitGroups])
    }

    /**
     * 格式化日期时间显示
     * @param dateTimeString ISO格式的日期时间字符串
     * @return 格式化后的日期时间字符串
     */
    fun formatDateTime(dateTimeString: String): String {
        return try {
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

    @OptIn(ExperimentalTime::class)
    fun isFutureDateTime(dateTimeString: String?): Boolean {
        val targetMs = parseDateTimeToEpochMs(dateTimeString) ?: return false
        return targetMs > Clock.System.now().toEpochMilliseconds()
    }

    fun parseDateTimeToEpochMs(dateTimeString: String?): Long? {
        val raw = dateTimeString?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return try {
            val normalized = raw
                .substringBefore('.')
                .replace(' ', 'T')
            LocalDateTime.parse(normalized)
                .toInstant(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
        } catch (e: Exception) {
            parseIsoToEpochMs(raw)
        }
    }

    /**
     * 格式化相对时间显示（如：2小时前、3天前、刚刚）
     * 将ISO格式的时间戳转换为用户友好的相对时间描述
     * @param dateTimeString ISO格式的日期时间字符串
     * @return 相对时间字符串, 如"刚刚"/"5分钟前"/"3小时前"/"昨天"/"3天前"/"2024-01-15"
     */
    @OptIn(ExperimentalTime::class)
    fun formatRelativeTime(dateTimeString: String): String {
        return try {
            if (dateTimeString.isBlank()) return dateTimeString

            val now = Clock.System.now().toEpochMilliseconds()
            val targetMs = parseIsoToEpochMs(dateTimeString) ?: return formatDateTime(dateTimeString)
            val diffMs = now - targetMs

            when {
                diffMs < 0 -> formatDateTime(dateTimeString)
                diffMs < 60_000L -> "刚刚"
                diffMs < 3_600_000L -> "${diffMs / 60_000L}分钟前"
                diffMs < 86_400_000L -> "${diffMs / 3_600_000L}小时前"
                diffMs < 172_800_000L -> "昨天"
                diffMs < 604_800_000L -> "${diffMs / 86_400_000L}天前"
                else -> formatDateTime(dateTimeString).substringBefore(' ')
            }
        } catch (e: Exception) {
            formatDateTime(dateTimeString)
        }
    }

    /**
     * 将ISO格式日期时间字符串解析为毫秒时间戳
     * 支持 "2024-01-15T10:30:00" 和 "2024-01-15T10:30:00.000+08:00" 等格式
     * @param isoString ISO格式日期时间字符串
     * @return 毫秒时间戳, 解析失败返回null
     */
    private fun parseIsoToEpochMs(isoString: String): Long? {
        return try {
            val cleanStr = isoString.substringBefore('.').replace('T', ' ').trim()
            val parts = cleanStr.split(' ')
            if (parts.size < 2) return null

            val dateParts = parts[0].split('-')
            val timeParts = parts[1].split(':')
            if (dateParts.size < 3 || timeParts.size < 3) return null

            val year = dateParts[0].toInt()
            val month = dateParts[1].toInt()
            val day = dateParts[2].toInt()
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            val second = timeParts[2].toInt()

            // 简化的纪元毫秒计算（不考虑闰秒, 适用于近似时间比较）
            val totalDays = daysFromEpoch(year, month, day)
            totalDays * 86_400_000L + hour * 3_600_000L + minute * 60_000L + second * 1000L
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 计算从1970-01-01到指定日期的天数
     */
    private fun daysFromEpoch(year: Int, month: Int, day: Int): Long {
        var totalDays = 0L
        for (y in 1970 until year) {
            totalDays += if (isLeapYear(y)) 366 else 365
        }
        val monthDays = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        for (m in 1 until month) {
            totalDays += if (m == 2 && isLeapYear(year)) 29 else monthDays[m]
        }
        return totalDays + day - 1
    }

    private fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)


}

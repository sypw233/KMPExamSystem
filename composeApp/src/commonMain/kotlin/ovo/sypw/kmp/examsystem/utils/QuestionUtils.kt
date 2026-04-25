package ovo.sypw.kmp.examsystem.utils

import kotlinx.serialization.json.Json

/**
 * 题目相关工具函数
 * 集中管理题目类型标签、选项解析等共享逻辑
 */
object QuestionUtils {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    /** 题目类型 → 中文标签 */
    fun questionTypeLabel(type: String): String = when (type) {
        "single" -> "单选"
        "multiple" -> "多选"
        "true_false" -> "判断"
        "fill_blank" -> "填空"
        "short_answer" -> "简答"
        else -> type
    }

    /** 题目类型选项列表（用于下拉菜单） */
    val questionTypeOptions = listOf(
        "single" to "单选",
        "multiple" to "多选",
        "true_false" to "判断",
        "fill_blank" to "填空",
        "short_answer" to "简答"
    )

    /** 难度选项列表（用于下拉菜单） */
    val difficultyOptions = listOf(
        "easy" to "简单",
        "medium" to "中等",
        "hard" to "困难"
    )

    /**
     * 解析选项 JSON 字符串
     * @param optionsJson JSON 格式的选项字符串
     * @return 选项文本列表（去除 A./B./C./D. 前缀）
     */
    fun parseOptionsJson(optionsJson: String?): List<String> {
        if (optionsJson.isNullOrBlank()) return listOf("", "", "", "")
        return try {
            val list = jsonParser.decodeFromString<List<String>>(optionsJson)
            list.map {
                it.removePrefix("A. ")
                    .removePrefix("B. ")
                    .removePrefix("C. ")
                    .removePrefix("D. ")
                    .removePrefix("E. ")
                    .trim()
            }
        } catch (_: Exception) {
            listOf("", "", "", "")
        }
    }

    /**
     * 构建选项 JSON 字符串
     * @param list 选项文本列表
     * @return JSON 格式字符串（带 A./B./C./D. 前缀）
     */
    fun buildOptionsJson(list: List<String>): String {
        val valid = list.mapIndexedNotNull { index, text ->
            val trimmed = text.trim()
            if (trimmed.isNotBlank()) {
                val letter = ('A' + index).toString()
                "\"$letter. $trimmed\""
            } else null
        }
        return "[${valid.joinToString(",")}]"
    }
}

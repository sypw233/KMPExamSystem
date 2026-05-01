package ovo.sypw.kmp.examsystem.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ovo.sypw.kmp.examsystem.data.dto.QuestionDifficulty
import ovo.sypw.kmp.examsystem.data.dto.QuestionType

/**
 * 题目相关工具函数
 * 集中管理题目类型标签、选项解析等共享逻辑
 */
object QuestionUtils {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    /** 题目类型枚举 → 中文标签 */
    fun questionTypeLabel(type: QuestionType): String = when (type) {
        QuestionType.SINGLE -> "单选题"
        QuestionType.MULTIPLE -> "多选题"
        QuestionType.TRUE_FALSE -> "判断题"
        QuestionType.FILL_BLANK -> "填空题"
        QuestionType.SHORT_ANSWER -> "简答题"
    }

    /** 题目类型字符串 → 中文标签 (向后兼容) */
    fun questionTypeLabel(type: String): String = questionTypeLabel(QuestionType.fromValue(type))

    /** 题目类型选项列表（用于下拉菜单）, value -> label */
    val questionTypeOptions: List<Pair<String, String>> = QuestionType.entries.map {
        it.value to questionTypeLabel(it)
    }

    /** 难度枚举 → 中文标签 */
    fun difficultyLabel(difficulty: QuestionDifficulty): String = when (difficulty) {
        QuestionDifficulty.EASY -> "简单"
        QuestionDifficulty.MEDIUM -> "中等"
        QuestionDifficulty.HARD -> "困难"
    }

    /** 难度选项列表（用于下拉菜单）, value -> label */
    val difficultyOptions: List<Pair<String, String>> = QuestionDifficulty.entries.map {
        it.value to difficultyLabel(it)
    }

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
                "$letter. $trimmed"
            } else null
        }
        return if (valid.isEmpty()) "" else jsonParser.encodeToString(valid)
    }
}

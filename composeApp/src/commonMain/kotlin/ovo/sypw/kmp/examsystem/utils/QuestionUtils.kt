package ovo.sypw.kmp.examsystem.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ovo.sypw.kmp.examsystem.data.dto.QuestionDifficulty
import ovo.sypw.kmp.examsystem.data.dto.QuestionType

object QuestionUtils {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    fun questionTypeLabel(type: QuestionType): String = when (type) {
        QuestionType.SINGLE -> "单选题"
        QuestionType.MULTIPLE -> "多选题"
        QuestionType.TRUE_FALSE -> "判断题"
        QuestionType.FILL_BLANK -> "填空题"
        QuestionType.SHORT_ANSWER -> "简答题"
    }

    fun questionTypeLabel(type: String): String = questionTypeLabel(QuestionType.fromValue(type))

    val questionTypeOptions: List<Pair<String, String>> = QuestionType.entries.map {
        it.value to questionTypeLabel(it)
    }

    fun difficultyLabel(difficulty: QuestionDifficulty): String = when (difficulty) {
        QuestionDifficulty.EASY -> "简单"
        QuestionDifficulty.MEDIUM -> "中等"
        QuestionDifficulty.HARD -> "困难"
    }

    val difficultyOptions: List<Pair<String, String>> = QuestionDifficulty.entries.map {
        it.value to difficultyLabel(it)
    }

    fun parseOptionsJson(optionsJson: String?): List<String> {
        if (optionsJson.isNullOrBlank()) return listOf("", "", "", "")
        return try {
            jsonParser.decodeFromString<List<String>>(optionsJson).map {
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

    fun buildOptionsJson(list: List<String>): String {
        val valid = list.mapIndexedNotNull { index, text ->
            val trimmed = text.trim()
            if (trimmed.isNotBlank()) {
                val letter = ('A' + index).toString()
                "$letter. $trimmed"
            } else {
                null
            }
        }
        return if (valid.isEmpty()) "" else jsonParser.encodeToString(valid)
    }
}

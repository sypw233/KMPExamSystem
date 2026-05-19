package ovo.sypw.kmp.examsystem.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
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
            when (val element = jsonParser.decodeFromString<JsonElement>(optionsJson)) {
                is JsonArray -> element.map { it.asOptionText() }
                is JsonObject -> listOf("A", "B", "C", "D", "E")
                    .mapNotNull { key -> element[key]?.asOptionText() }
                else -> listOf("", "", "", "")
            }.ifEmpty { listOf("", "", "", "") }
        } catch (_: Exception) {
            listOf("", "", "", "")
        }
    }

    fun buildOptionsJson(list: List<String>): String {
        val valid = buildJsonObject {
            list.forEachIndexed { index, text ->
                val trimmed = text.trim()
                if (trimmed.isNotBlank()) {
                    put(('A' + index).toString(), JsonPrimitive(trimmed))
                }
            }
        }
        return if (valid.isEmpty()) "" else valid.toString()
    }

    private fun JsonElement.asOptionText(): String {
        return when (this) {
            is JsonPrimitive -> content
                .removePrefix("A. ")
                .removePrefix("B. ")
                .removePrefix("C. ")
                .removePrefix("D. ")
                .removePrefix("E. ")
                .trim()
            else -> toString().trim()
        }
    }
}

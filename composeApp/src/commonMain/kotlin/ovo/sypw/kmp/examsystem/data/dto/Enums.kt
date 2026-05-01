package ovo.sypw.kmp.examsystem.data.dto

/**
 * 考试状态枚举
 * 与后端 status 字段一一对应, 消除魔法数字
 */
enum class ExamStatus(val code: Int) {
    DRAFT(0),
    PUBLISHED(1),
    ENDED(2);

    companion object {
        fun fromCode(code: Int): ExamStatus = entries.firstOrNull { it.code == code } ?: DRAFT
    }
}

/**
 * 提交状态枚举
 */
enum class SubmissionStatus(val code: Int) {
    IN_PROGRESS(0),
    SUBMITTED(1),
    GRADED(2);

    companion object {
        fun fromCode(code: Int): SubmissionStatus = entries.firstOrNull { it.code == code } ?: IN_PROGRESS
    }
}

/**
 * 用户启用状态枚举
 */
enum class UserEnabledStatus(val code: Int) {
    DISABLED(0),
    ENABLED(1);

    companion object {
        fun fromCode(code: Int): UserEnabledStatus = entries.firstOrNull { it.code == code } ?: DISABLED
    }
}

/**
 * 题目类型枚举
 */
enum class QuestionType(val value: String) {
    SINGLE("single"),
    MULTIPLE("multiple"),
    TRUE_FALSE("true_false"),
    FILL_BLANK("fill_blank"),
    SHORT_ANSWER("short_answer");

    companion object {
        fun fromValue(value: String): QuestionType = entries.firstOrNull { it.value == value } ?: SHORT_ANSWER
    }
}

/**
 * 题目难度枚举
 */
enum class QuestionDifficulty(val value: String) {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    companion object {
        fun fromValue(value: String): QuestionDifficulty = entries.firstOrNull { it.value == value } ?: MEDIUM
    }
}

// -- 便捷扩展属性, 通过 Int 值直接访问枚举 --

val ExamResponse.examStatus: ExamStatus get() = ExamStatus.fromCode(status)
val SubmissionResponse.submissionStatus: SubmissionStatus get() = SubmissionStatus.fromCode(status)
val UserResponse.enabledStatus: UserEnabledStatus get() = UserEnabledStatus.fromCode(status)
val QuestionResponse.questionType: QuestionType get() = QuestionType.fromValue(type)
val QuestionResponse.questionDifficulty: QuestionDifficulty? get() = difficulty?.let { QuestionDifficulty.fromValue(it) }

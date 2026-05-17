package ovo.sypw.kmp.examsystem.presentation.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("日间模式"),
    DARK("夜间模式")
}

enum class ThemeAccentMode(val label: String) {
    SYSTEM("莫奈取色"),
    CUSTOM("自定义")
}

enum class ThemeAccent(val label: String) {
    BLUE("海蓝"),
    GREEN("松绿"),
    ROSE("玫红")
}

enum class ExamDisplayMode(val label: String) {
    SINGLE_QUESTION("单页单题"),
    LIST("列表展示")
}

enum class FontScaleLevel(val label: String, val scale: Float) {
    SMALL("小", 0.92f),
    STANDARD("标准", 1.0f),
    LARGE("大", 1.12f),
    EXTRA_LARGE("特大", 1.24f)
}

data class AppSettingsState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val accentMode: ThemeAccentMode = ThemeAccentMode.SYSTEM,
    val accent: ThemeAccent = ThemeAccent.BLUE,
    val examDisplayMode: ExamDisplayMode = ExamDisplayMode.LIST,
    val fontScaleLevel: FontScaleLevel = FontScaleLevel.STANDARD
)

object AppSettingsStore {
    private val _settings = MutableStateFlow(AppSettingsState())
    val settings: StateFlow<AppSettingsState> = _settings.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _settings.update { it.copy(themeMode = mode) }
    }

    fun setAccentMode(mode: ThemeAccentMode) {
        _settings.update { it.copy(accentMode = mode) }
    }

    fun setAccent(accent: ThemeAccent) {
        _settings.update { it.copy(accent = accent, accentMode = ThemeAccentMode.CUSTOM) }
    }

    fun setExamDisplayMode(mode: ExamDisplayMode) {
        _settings.update { it.copy(examDisplayMode = mode) }
    }

    fun setFontScale(level: FontScaleLevel) {
        _settings.update { it.copy(fontScaleLevel = level) }
    }
}

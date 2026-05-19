package ovo.sypw.kmp.examsystem.presentation.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.data.storage.LocalStorage

enum class AppThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色模式"),
    DARK("深色模式")
}

enum class ThemeAccentMode(val label: String) {
    SYSTEM("跟随系统"),
    CUSTOM("手动颜色")
}

enum class ThemeAccent(val label: String, val seedHex: String) {
    BLUE("海湾蓝", "#006D77"),
    GREEN("松石绿", "#2E6B35"),
    ROSE("玫瑰粉", "#9B4057"),
    AMBER("琥珀橙", "#9A5E00"),
    TEAL("青玉色", "#006A60"),
    VIOLET("鸢尾紫", "#6B4EA2"),
    SLATE("雾灰蓝", "#4C607A"),
    CORAL("珊瑚橙", "#A44A3F")
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
    val useCustomAccentColor: Boolean = false,
    val customAccentHex: String = ThemeAccent.BLUE.seedHex,
    val examDisplayMode: ExamDisplayMode = ExamDisplayMode.LIST,
    val fontScaleLevel: FontScaleLevel = FontScaleLevel.STANDARD,
    val confirmBeforeSubmit: Boolean = true,
    val timerWarningEnabled: Boolean = true,
    val autoSaveAnswers: Boolean = true,
    val compactListMode: Boolean = false
) {
    val resolvedAccentHex: String
        get() = if (useCustomAccentColor) customAccentHex else accent.seedHex
}

object AppSettingsStore {
    private const val KEY_THEME_MODE = "app_settings_theme_mode"
    private const val KEY_ACCENT_MODE = "app_settings_accent_mode"
    private const val KEY_ACCENT = "app_settings_accent"
    private const val KEY_USE_CUSTOM_ACCENT = "app_settings_use_custom_accent"
    private const val KEY_CUSTOM_ACCENT_HEX = "app_settings_custom_accent_hex"
    private const val KEY_EXAM_DISPLAY_MODE = "app_settings_exam_display_mode"
    private const val KEY_FONT_SCALE = "app_settings_font_scale"
    private const val KEY_CONFIRM_BEFORE_SUBMIT = "app_settings_confirm_before_submit"
    private const val KEY_TIMER_WARNING = "app_settings_timer_warning"
    private const val KEY_AUTO_SAVE_ANSWERS = "app_settings_auto_save_answers"
    private const val KEY_COMPACT_LIST_MODE = "app_settings_compact_list_mode"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _settings = MutableStateFlow(AppSettingsState())
    private var localStorage: LocalStorage? = null
    private var initialized = false

    val settings: StateFlow<AppSettingsState> = _settings.asStateFlow()

    suspend fun initialize(storage: LocalStorage) {
        if (initialized && localStorage === storage) return
        localStorage = storage
        _settings.value = AppSettingsState(
            themeMode = storage.getString(KEY_THEME_MODE).toAppThemeMode(),
            accentMode = storage.getString(KEY_ACCENT_MODE).toThemeAccentMode(),
            accent = storage.getString(KEY_ACCENT).toThemeAccent(),
            useCustomAccentColor = storage.getBoolean(KEY_USE_CUSTOM_ACCENT, false),
            customAccentHex = storage.getString(KEY_CUSTOM_ACCENT_HEX)?.normalizeColorHex()
                ?: ThemeAccent.BLUE.seedHex,
            examDisplayMode = storage.getString(KEY_EXAM_DISPLAY_MODE).toExamDisplayMode(),
            fontScaleLevel = storage.getString(KEY_FONT_SCALE).toFontScaleLevel(),
            confirmBeforeSubmit = storage.getBoolean(KEY_CONFIRM_BEFORE_SUBMIT, true),
            timerWarningEnabled = storage.getBoolean(KEY_TIMER_WARNING, true),
            autoSaveAnswers = storage.getBoolean(KEY_AUTO_SAVE_ANSWERS, true),
            compactListMode = storage.getBoolean(KEY_COMPACT_LIST_MODE, false)
        )
        initialized = true
    }

    fun setThemeMode(mode: AppThemeMode) {
        updateSettings { it.copy(themeMode = mode) }
    }

    fun setFollowSystemTheme(enabled: Boolean) {
        updateSettings { it.copy(themeMode = if (enabled) AppThemeMode.SYSTEM else AppThemeMode.LIGHT) }
    }

    fun setDarkThemeEnabled(enabled: Boolean) {
        updateSettings { it.copy(themeMode = if (enabled) AppThemeMode.DARK else AppThemeMode.LIGHT) }
    }

    fun setAccentMode(mode: ThemeAccentMode) {
        updateSettings { it.copy(accentMode = mode) }
    }

    fun setAccent(accent: ThemeAccent) {
        updateSettings {
            it.copy(
                accent = accent,
                accentMode = ThemeAccentMode.CUSTOM,
                useCustomAccentColor = false
            )
        }
    }

    fun setCustomAccentColor(hex: String) {
        val normalized = hex.normalizeColorHex() ?: return
        updateSettings {
            it.copy(
                accentMode = ThemeAccentMode.CUSTOM,
                useCustomAccentColor = true,
                customAccentHex = normalized
            )
        }
    }

    fun setExamDisplayMode(mode: ExamDisplayMode) {
        updateSettings { it.copy(examDisplayMode = mode) }
    }

    fun setSingleQuestionMode(enabled: Boolean) {
        updateSettings { it.copy(examDisplayMode = if (enabled) ExamDisplayMode.SINGLE_QUESTION else ExamDisplayMode.LIST) }
    }

    fun setFontScale(level: FontScaleLevel) {
        updateSettings { it.copy(fontScaleLevel = level) }
    }

    fun setConfirmBeforeSubmit(enabled: Boolean) {
        updateSettings { it.copy(confirmBeforeSubmit = enabled) }
    }

    fun setTimerWarningEnabled(enabled: Boolean) {
        updateSettings { it.copy(timerWarningEnabled = enabled) }
    }

    fun setAutoSaveAnswers(enabled: Boolean) {
        updateSettings { it.copy(autoSaveAnswers = enabled) }
    }

    fun setCompactListMode(enabled: Boolean) {
        updateSettings { it.copy(compactListMode = enabled) }
    }

    private fun updateSettings(transform: (AppSettingsState) -> AppSettingsState) {
        _settings.update(transform)
        persistSettings()
    }

    private fun persistSettings() {
        val storage = localStorage ?: return
        val current = _settings.value
        scope.launch {
            storage.saveString(KEY_THEME_MODE, current.themeMode.name)
            storage.saveString(KEY_ACCENT_MODE, current.accentMode.name)
            storage.saveString(KEY_ACCENT, current.accent.name)
            storage.saveBoolean(KEY_USE_CUSTOM_ACCENT, current.useCustomAccentColor)
            storage.saveString(KEY_CUSTOM_ACCENT_HEX, current.customAccentHex)
            storage.saveString(KEY_EXAM_DISPLAY_MODE, current.examDisplayMode.name)
            storage.saveString(KEY_FONT_SCALE, current.fontScaleLevel.name)
            storage.saveBoolean(KEY_CONFIRM_BEFORE_SUBMIT, current.confirmBeforeSubmit)
            storage.saveBoolean(KEY_TIMER_WARNING, current.timerWarningEnabled)
            storage.saveBoolean(KEY_AUTO_SAVE_ANSWERS, current.autoSaveAnswers)
            storage.saveBoolean(KEY_COMPACT_LIST_MODE, current.compactListMode)
        }
    }
}

private fun String.normalizeColorHex(): String? {
    val raw = trim().removePrefix("#").uppercase()
    if (raw.length != 6 || raw.any { it !in "0123456789ABCDEF" }) return null
    return "#$raw"
}

private fun String?.toAppThemeMode(): AppThemeMode =
    AppThemeMode.entries.firstOrNull { it.name == this } ?: AppThemeMode.SYSTEM

private fun String?.toThemeAccentMode(): ThemeAccentMode =
    ThemeAccentMode.entries.firstOrNull { it.name == this } ?: ThemeAccentMode.SYSTEM

private fun String?.toThemeAccent(): ThemeAccent =
    ThemeAccent.entries.firstOrNull { it.name == this } ?: ThemeAccent.BLUE

private fun String?.toExamDisplayMode(): ExamDisplayMode =
    ExamDisplayMode.entries.firstOrNull { it.name == this } ?: ExamDisplayMode.LIST

private fun String?.toFontScaleLevel(): FontScaleLevel =
    FontScaleLevel.entries.firstOrNull { it.name == this } ?: FontScaleLevel.STANDARD

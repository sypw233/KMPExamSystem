package ovo.sypw.kmp.examsystem.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import ovo.sypw.kmp.examsystem.presentation.settings.ThemeAccent
import ovo.sypw.kmp.examsystem.presentation.settings.ThemeAccentMode

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    surfaceDim = md_theme_light_surfaceDim,
    surfaceBright = md_theme_light_surfaceBright,
    surfaceContainerLowest = md_theme_light_surfaceContainerLowest,
    surfaceContainerLow = md_theme_light_surfaceContainerLow,
    surfaceContainer = md_theme_light_surfaceContainer,
    surfaceContainerHigh = md_theme_light_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_light_surfaceContainerHighest,
    scrim = md_theme_light_scrim,
)


private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    outlineVariant = md_theme_dark_outlineVariant,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    surfaceDim = md_theme_dark_surfaceDim,
    surfaceBright = md_theme_dark_surfaceBright,
    surfaceContainerLowest = md_theme_dark_surfaceContainerLowest,
    surfaceContainerLow = md_theme_dark_surfaceContainerLow,
    surfaceContainer = md_theme_dark_surfaceContainer,
    surfaceContainerHigh = md_theme_dark_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_dark_surfaceContainerHighest,
    scrim = md_theme_dark_scrim,
)

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    accentMode: ThemeAccentMode = ThemeAccentMode.SYSTEM,
    accent: ThemeAccent = ThemeAccent.BLUE,
    fontScale: Float = 1f,
    content: @Composable() () -> Unit
) {
    val baseColors = if (!useDarkTheme) {
        LightColors
    } else {
        DarkColors
    }
    val colors = if (accentMode == ThemeAccentMode.CUSTOM) {
        baseColors.withAccent(accent, useDarkTheme)
    } else {
        baseColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography.scaled(fontScale),
        shapes = AppShapes,
        content = content
    )
}

private fun ColorScheme.withAccent(accent: ThemeAccent, dark: Boolean): ColorScheme {
    val palette = when (accent) {
        ThemeAccent.BLUE -> if (dark) AccentPalette(
            primary = Color(0xFF8BD3DD),
            onPrimary = Color(0xFF00363D),
            primaryContainer = Color(0xFF004F58),
            onPrimaryContainer = Color(0xFFA8EFF8)
        ) else AccentPalette(
            primary = Color(0xFF006D77),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFBCEBEF),
            onPrimaryContainer = Color(0xFF001F23)
        )
        ThemeAccent.GREEN -> if (dark) AccentPalette(
            primary = Color(0xFFAAD8AA),
            onPrimary = Color(0xFF143A19),
            primaryContainer = Color(0xFF2B522E),
            onPrimaryContainer = Color(0xFFC5F4C5)
        ) else AccentPalette(
            primary = Color(0xFF2E6B35),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFC9EBCB),
            onPrimaryContainer = Color(0xFF09210D)
        )
        ThemeAccent.ROSE -> if (dark) AccentPalette(
            primary = Color(0xFFFFB1C4),
            onPrimary = Color(0xFF5E112B),
            primaryContainer = Color(0xFF7C2941),
            onPrimaryContainer = Color(0xFFFFD9E2)
        ) else AccentPalette(
            primary = Color(0xFF9B4057),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFD9E2),
            onPrimaryContainer = Color(0xFF3F0017)
        )
    }
    return copy(
        primary = palette.primary,
        onPrimary = palette.onPrimary,
        primaryContainer = palette.primaryContainer,
        onPrimaryContainer = palette.onPrimaryContainer,
        secondary = palette.primary,
        secondaryContainer = palette.primaryContainer,
        surfaceTint = palette.primary,
        inversePrimary = palette.primary
    )
}

private data class AccentPalette(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color
)

private fun Typography.scaled(scale: Float): Typography =
    if (scale == 1f) this else copy(
        displayLarge = displayLarge.scaled(scale),
        displayMedium = displayMedium.scaled(scale),
        displaySmall = displaySmall.scaled(scale),
        headlineLarge = headlineLarge.scaled(scale),
        headlineMedium = headlineMedium.scaled(scale),
        headlineSmall = headlineSmall.scaled(scale),
        titleLarge = titleLarge.scaled(scale),
        titleMedium = titleMedium.scaled(scale),
        titleSmall = titleSmall.scaled(scale),
        bodyLarge = bodyLarge.scaled(scale),
        bodyMedium = bodyMedium.scaled(scale),
        bodySmall = bodySmall.scaled(scale),
        labelLarge = labelLarge.scaled(scale),
        labelMedium = labelMedium.scaled(scale),
        labelSmall = labelSmall.scaled(scale)
    )

private fun TextStyle.scaled(scale: Float): TextStyle =
    copy(
        fontSize = fontSize * scale,
        lineHeight = lineHeight * scale
    )

/**
 * 语义颜色扩展属性
 * 提供 info/warning/success 颜色令牌, 支持深色主题自适应
 * 通过 surface 与 onSurface 的亮度比判断当前主题, 兼容 Dynamic Color
 */
private val ColorScheme.isDarkScheme: Boolean
    @Composable
    get() = surface.luminance() < onSurface.luminance()

val ColorScheme.info: Color
    @Composable
    get() = if (isDarkScheme) md_theme_dark_info else md_theme_light_info

val ColorScheme.warning: Color
    @Composable
    get() = if (isDarkScheme) md_theme_dark_warning else md_theme_light_warning

val ColorScheme.success: Color
    @Composable
    get() = if (isDarkScheme) md_theme_dark_success else md_theme_light_success

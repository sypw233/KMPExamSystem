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
import kotlin.math.max
import kotlin.math.min
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
    customAccentHex: String? = null,
    fontScale: Float = 1f,
    content: @Composable() () -> Unit
) {
    val baseColors = if (!useDarkTheme) {
        LightColors
    } else {
        DarkColors
    }
    val colors = if (accentMode == ThemeAccentMode.CUSTOM) {
        baseColors.withAccent(
            accent = accent,
            dark = useDarkTheme,
            customAccent = customAccentHex?.toColorOrNull()
        )
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

private fun ColorScheme.withAccent(
    accent: ThemeAccent,
    dark: Boolean,
    customAccent: Color? = null
): ColorScheme {
    val palette = customAccent?.let { seed ->
        seed.toAccentPalette(dark)
    } ?: when (accent) {
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
        ThemeAccent.AMBER -> if (dark) AccentPalette(
            primary = Color(0xFFFFB95B),
            onPrimary = Color(0xFF4F2500),
            primaryContainer = Color(0xFF713800),
            onPrimaryContainer = Color(0xFFFFDDB6)
        ) else AccentPalette(
            primary = Color(0xFF9A5E00),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFDDB6),
            onPrimaryContainer = Color(0xFF311B00)
        )
        ThemeAccent.TEAL -> if (dark) AccentPalette(
            primary = Color(0xFF4FDAC6),
            onPrimary = Color(0xFF003730),
            primaryContainer = Color(0xFF005047),
            onPrimaryContainer = Color(0xFF6EF7E2)
        ) else AccentPalette(
            primary = Color(0xFF006A60),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF74F8E3),
            onPrimaryContainer = Color(0xFF00201C)
        )
        ThemeAccent.VIOLET -> if (dark) AccentPalette(
            primary = Color(0xFFD3BCFF),
            onPrimary = Color(0xFF3C1E71),
            primaryContainer = Color(0xFF53358A),
            onPrimaryContainer = Color(0xFFEBDDFF)
        ) else AccentPalette(
            primary = Color(0xFF6B4EA2),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFEBDDFF),
            onPrimaryContainer = Color(0xFF250A58)
        )
        ThemeAccent.SLATE -> if (dark) AccentPalette(
            primary = Color(0xFFB5C8E7),
            onPrimary = Color(0xFF1D3148),
            primaryContainer = Color(0xFF344960),
            onPrimaryContainer = Color(0xFFD4E4FF)
        ) else AccentPalette(
            primary = Color(0xFF4C607A),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD4E4FF),
            onPrimaryContainer = Color(0xFF061C33)
        )
        ThemeAccent.CORAL -> if (dark) AccentPalette(
            primary = Color(0xFFFFB4A8),
            onPrimary = Color(0xFF61140C),
            primaryContainer = Color(0xFF7F2C21),
            onPrimaryContainer = Color(0xFFFFDAD4)
        ) else AccentPalette(
            primary = Color(0xFFA44A3F),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFDAD4),
            onPrimaryContainer = Color(0xFF410002)
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

private fun String.toColorOrNull(): Color? {
    val normalized = trim().removePrefix("#")
    if (normalized.length != 6) return null
    val value = normalized.toLongOrNull(16) ?: return null
    return Color(0xFF000000 or value)
}

private fun Color.toAccentPalette(dark: Boolean): AccentPalette {
    val primary = if (dark) mixWith(Color.White, 0.35f) else this
    val primaryContainer = if (dark) mixWith(Color.Black, 0.45f) else mixWith(Color.White, 0.78f)
    return AccentPalette(
        primary = primary,
        onPrimary = primary.bestForeground(),
        primaryContainer = primaryContainer,
        onPrimaryContainer = primaryContainer.bestForeground()
    )
}

private fun Color.mixWith(other: Color, ratio: Float): Color {
    val clamped = ratio.coerceIn(0f, 1f)
    return Color(
        red = red * (1f - clamped) + other.red * clamped,
        green = green * (1f - clamped) + other.green * clamped,
        blue = blue * (1f - clamped) + other.blue * clamped,
        alpha = 1f
    )
}

private fun Color.bestForeground(): Color {
    val contrastOnBlack = contrastRatio(this, Color.Black)
    val contrastOnWhite = contrastRatio(this, Color.White)
    return if (contrastOnBlack >= contrastOnWhite) Color.Black else Color.White
}

private fun contrastRatio(background: Color, foreground: Color): Float {
    val lighter = max(background.luminance(), foreground.luminance())
    val darker = min(background.luminance(), foreground.luminance())
    return ((lighter + 0.05f) / (darker + 0.05f))
}

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

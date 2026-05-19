package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import ovo.sypw.kmp.examsystem.presentation.settings.AppSettingsStore
import ovo.sypw.kmp.examsystem.presentation.settings.AppThemeMode
import ovo.sypw.kmp.examsystem.presentation.settings.ExamDisplayMode
import ovo.sypw.kmp.examsystem.presentation.settings.FontScaleLevel
import ovo.sypw.kmp.examsystem.presentation.settings.ThemeAccent
import ovo.sypw.kmp.examsystem.presentation.settings.ThemeAccentMode

@Composable
fun SettingsPageContent(modifier: Modifier = Modifier) {
    val settings by AppSettingsStore.settings.collectAsState()
    val uriHandler = LocalUriHandler.current
    var customColorInput by remember(settings.customAccentHex) { mutableStateOf(settings.customAccentHex) }
    val customColorPreview = remember(customColorInput) { customColorInput.toColorOrNull() }
    val fontLevels = FontScaleLevel.entries
    val fontIndex = fontLevels.indexOf(settings.fontScaleLevel).coerceAtLeast(0)

    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection(
                title = "外观",
                description = "控制明暗模式、系统取色和手动主题色。"
            ) {
                SettingsSwitchRow(
                    title = "跟随系统",
                    description = "开启后根据系统深色/浅色模式自动切换。",
                    icon = Icons.Default.AutoMode,
                    checked = settings.themeMode == AppThemeMode.SYSTEM,
                    onCheckedChange = AppSettingsStore::setFollowSystemTheme
                )
                SettingsSwitchRow(
                    title = "深色模式",
                    description = if (settings.themeMode == AppThemeMode.SYSTEM) "跟随系统时由系统决定。" else "关闭后使用浅色模式。",
                    icon = Icons.Default.DarkMode,
                    checked = settings.themeMode == AppThemeMode.DARK,
                    enabled = settings.themeMode != AppThemeMode.SYSTEM,
                    onCheckedChange = AppSettingsStore::setDarkThemeEnabled
                )
                SettingsSwitchRow(
                    title = "手动主题色",
                    description = if (settings.accentMode == ThemeAccentMode.CUSTOM) "使用下方预设或自定义颜色。" else "使用系统或默认主题色。",
                    icon = Icons.Default.Palette,
                    checked = settings.accentMode == ThemeAccentMode.CUSTOM,
                    onCheckedChange = {
                        AppSettingsStore.setAccentMode(if (it) ThemeAccentMode.CUSTOM else ThemeAccentMode.SYSTEM)
                    }
                )

                AnimatedVisibility(visible = settings.accentMode == ThemeAccentMode.CUSTOM) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("预设配色", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ThemeAccent.entries.forEach { accent ->
                                ColorPresetButton(
                                    accent = accent,
                                    selected = !settings.useCustomAccentColor && settings.accent == accent,
                                    onClick = { AppSettingsStore.setAccent(accent) }
                                )
                            }
                        }

                        Text("自定义颜色", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customColorInput,
                                onValueChange = { customColorInput = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("HEX") },
                                supportingText = { Text("例如 #6750A4") },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(customColorPreview ?: MaterialTheme.colorScheme.outlineVariant)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    )
                                },
                                isError = customColorInput.isNotBlank() && customColorPreview == null
                            )
                            Button(
                                onClick = {
                                    AppSettingsStore.setCustomAccentColor(customColorInput)
                                    customColorInput = customColorInput.normalizeColorHex() ?: settings.customAccentHex
                                },
                                enabled = customColorPreview != null
                            ) {
                                Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("应用")
                            }
                        }
                    }
                }
            }
        }

        item {
            SettingsSection(
                title = "考试",
                description = "调整答题展示、提交确认和考试提醒。"
            ) {
                SettingsSwitchRow(
                    title = "单页单题",
                    description = if (settings.examDisplayMode == ExamDisplayMode.SINGLE_QUESTION) "当前逐题展示。" else "关闭后使用列表展示。",
                    icon = Icons.Default.ViewAgenda,
                    checked = settings.examDisplayMode == ExamDisplayMode.SINGLE_QUESTION,
                    onCheckedChange = AppSettingsStore::setSingleQuestionMode
                )
                SettingsSwitchRow(
                    title = "提交前确认",
                    description = "减少误触提交试卷的风险。",
                    icon = Icons.Default.Check,
                    checked = settings.confirmBeforeSubmit,
                    onCheckedChange = AppSettingsStore::setConfirmBeforeSubmit
                )
                SettingsSwitchRow(
                    title = "考试时间提醒",
                    description = "临近结束时保留明显提醒。",
                    icon = Icons.Default.Schedule,
                    checked = settings.timerWarningEnabled,
                    onCheckedChange = AppSettingsStore::setTimerWarningEnabled
                )
                SettingsSwitchRow(
                    title = "答案自动暂存",
                    description = "页面切换或网络波动时尽量保留本地答题状态。",
                    icon = Icons.Default.Save,
                    checked = settings.autoSaveAnswers,
                    onCheckedChange = AppSettingsStore::setAutoSaveAnswers
                )
            }
        }

        item {
            SettingsSection(
                title = "阅读与布局",
                description = "字号越大，部分密集页面可能增加换行和滚动距离。"
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("字体大小", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                        Text(
                            "${settings.fontScaleLevel.label} ${(settings.fontScaleLevel.scale * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Slider(
                    value = fontIndex.toFloat(),
                    onValueChange = { value ->
                        val nextIndex = value.roundToInt().coerceIn(0, fontLevels.lastIndex)
                        AppSettingsStore.setFontScale(fontLevels[nextIndex])
                    },
                    valueRange = 0f..fontLevels.lastIndex.toFloat(),
                    steps = (fontLevels.size - 2).coerceAtLeast(0)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    fontLevels.forEach {
                        Text(it.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                SettingsSwitchRow(
                    title = "紧凑列表",
                    description = "列表类页面减少卡片留白，提高信息密度。",
                    icon = Icons.Default.ViewCompact,
                    checked = settings.compactListMode,
                    onCheckedChange = AppSettingsStore::setCompactListMode
                )
            }
        }

        item {
            SettingsSection(
                title = "更多",
                description = "通知、外部入口和辅助功能。"
            ) {
                SettingsSwitchRow(
                    title = "通知角标",
                    description = "保留通知入口的未读提示。",
                    icon = Icons.Default.NotificationsActive,
                    checked = true,
                    enabled = false,
                    onCheckedChange = {}
                )
                SettingsActionRow(
                    title = "下载原神",
                    description = "打开 miHoYo 官方页面。",
                    icon = Icons.Default.Download,
                    onClick = { uriHandler.openUri("https://ys.mihoyo.com/") }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (checked && enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (checked && enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        onClick = { if (enabled) onCheckedChange(!checked) },
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = if (checked && enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (checked && enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = if (enabled) onCheckedChange else null, enabled = enabled)
        }
    }
}

@Composable
private fun ColorPresetButton(
    accent: ThemeAccent,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = accent.seedHex.toColorOrNull() ?: MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = color.bestForegroundColor(), modifier = Modifier.size(16.dp))
                }
            }
            Text(accent.label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun String.normalizeColorHex(): String? {
    val raw = trim().removePrefix("#").uppercase()
    if (raw.length != 6 || raw.any { it !in "0123456789ABCDEF" }) return null
    return "#$raw"
}

private fun String.toColorOrNull(): Color? {
    val normalized = normalizeColorHex() ?: return null
    return Color(0xFF000000 or normalized.removePrefix("#").toLong(16))
}

private fun Color.bestForegroundColor(): Color =
    if (luminance() > 0.45f) Color.Black else Color.White

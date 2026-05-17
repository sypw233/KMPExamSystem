package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ovo.sypw.kmp.examsystem.data.dto.UserInfo
import ovo.sypw.kmp.examsystem.presentation.components.common.adaptiveDialogModifier
import ovo.sypw.kmp.examsystem.presentation.components.common.adaptiveDialogProperties
import ovo.sypw.kmp.examsystem.presentation.settings.AppSettingsStore
import ovo.sypw.kmp.examsystem.presentation.settings.AppThemeMode
import ovo.sypw.kmp.examsystem.presentation.settings.ExamDisplayMode
import ovo.sypw.kmp.examsystem.presentation.settings.FontScaleLevel
import ovo.sypw.kmp.examsystem.presentation.settings.ThemeAccent
import ovo.sypw.kmp.examsystem.presentation.settings.ThemeAccentMode
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig

@Composable
fun EditProfileDialog(
    user: UserInfo,
    onDismiss: () -> Unit,
    onConfirm: (realName: String, email: String?, avatarUrl: String?) -> Unit,
    onOpenChangePassword: () -> Unit,
    onUploadAvatar: (onSuccess: (String) -> Unit, onError: (String) -> Unit) -> Unit
) {
    val config = LocalResponsiveConfig.current
    var realName by remember { mutableStateOf(user.realName.orEmpty()) }
    var email by remember { mutableStateOf(user.email.orEmpty()) }
    var avatarUrl by remember { mutableStateOf(user.avatar) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        realName.trim(),
                        email.trim().ifBlank { null },
                        avatarUrl
                    )
                },
                enabled = realName.isNotBlank() && !isUploading
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = adaptiveDialogModifier(),
        properties = adaptiveDialogProperties(),
        title = { Text("编辑资料") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(config.verticalSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(88.dp), contentAlignment = Alignment.Center) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "头像",
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(88.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = realName.take(1).uppercase().ifBlank { user.username.take(1).uppercase() },
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        uploadError = null
                        isUploading = true
                        onUploadAvatar(
                            { url ->
                                avatarUrl = url
                                isUploading = false
                            },
                            { error ->
                                uploadError = error
                                isUploading = false
                            }
                        )
                    },
                    enabled = !isUploading
                ) {
                    Text(if (isUploading) "上传中..." else "更换头像")
                }

                uploadError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = realName,
                    onValueChange = { realName = it },
                    label = { Text("姓名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedButton(
                    onClick = onOpenChangePassword,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("修改密码")
                }
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (oldPassword: String, newPassword: String) -> Unit
) {
    var oldPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    val isValid = oldPwd.isNotBlank() && newPwd.length >= 6 && newPwd == confirmPwd

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm(oldPwd.trim(), newPwd.trim()) },
                enabled = isValid
            ) {
                Text("确认修改")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = adaptiveDialogModifier(),
        properties = adaptiveDialogProperties(),
        title = { Text("修改密码") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = oldPwd,
                    onValueChange = { oldPwd = it },
                    label = { Text("当前密码") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newPwd,
                    onValueChange = { newPwd = it },
                    label = { Text("新密码") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = newPwd.isNotBlank() && newPwd.length < 6
                )
                OutlinedTextField(
                    value = confirmPwd,
                    onValueChange = { confirmPwd = it },
                    label = { Text("确认新密码") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = confirmPwd.isNotBlank() && confirmPwd != newPwd
                )
                Text(
                    text = "新密码至少 6 位。修改后请使用新密码重新登录。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
fun AppSettingsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("完成")
            }
        },
        modifier = adaptiveDialogModifier(),
        properties = adaptiveDialogProperties(),
        title = { Text("设置") },
        text = {
            AppSettingsContent()
        }
    )
}

@Composable
fun AppSettingsContent(modifier: Modifier = Modifier) {
    val settings by AppSettingsStore.settings.collectAsState()
    val uriHandler = LocalUriHandler.current
    var customColorInput by remember(settings.customAccentHex) { mutableStateOf(settings.customAccentHex) }
    val customColorPreview = remember(customColorInput) { customColorInput.toColorOrNull() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsGroup(
            title = "主题设置",
            description = "支持亮暗主题、系统配色与自定义主题色。"
        ) {
            OptionGrid(
                options = AppThemeMode.values().toList(),
                selected = settings.themeMode,
                icon = {
                    when (it) {
                        AppThemeMode.SYSTEM -> Icons.Default.AutoMode
                        AppThemeMode.LIGHT -> Icons.Default.Brightness7
                        AppThemeMode.DARK -> Icons.Default.DarkMode
                    }
                },
                title = { it.label },
                subtitle = {
                    when (it) {
                        AppThemeMode.SYSTEM -> "自动跟随系统"
                        AppThemeMode.LIGHT -> "固定浅色外观"
                        AppThemeMode.DARK -> "固定深色外观"
                    }
                },
                onSelect = AppSettingsStore::setThemeMode
            )

            SettingsToggleRow(
                title = "主题色来源",
                description = if (settings.accentMode == ThemeAccentMode.SYSTEM) "当前使用系统默认配色" else "当前使用手动主题色",
                selected = settings.accentMode == ThemeAccentMode.CUSTOM,
                selectedLabel = "手动"
            ) {
                AppSettingsStore.setAccentMode(
                    if (settings.accentMode == ThemeAccentMode.SYSTEM) ThemeAccentMode.CUSTOM else ThemeAccentMode.SYSTEM
                )
            }

            if (settings.accentMode == ThemeAccentMode.CUSTOM) {
                SettingsSubsection("预设配色")
                ThemeAccentPicker(
                    accents = ThemeAccent.values().toList(),
                    selected = settings.accent,
                    activeHex = if (settings.useCustomAccentColor) settings.customAccentHex else settings.accent.seedHex,
                    onSelect = AppSettingsStore::setAccent
                )

                SettingsSubsection("自定义颜色")
                OutlinedTextField(
                    value = customColorInput,
                    onValueChange = { customColorInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("HEX 颜色") },
                    supportingText = { Text("请输入 #RRGGBB，例如 #6750A4。") },
                    leadingIcon = {
                        Surface(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(customColorPreview ?: MaterialTheme.colorScheme.outlineVariant),
                            shape = CircleShape
                        ) {}
                    },
                    trailingIcon = {
                        if (settings.useCustomAccentColor) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    isError = customColorInput.isNotBlank() && customColorPreview == null
                )
                OutlinedButton(
                    onClick = {
                        AppSettingsStore.setCustomAccentColor(customColorInput)
                        customColorInput = customColorInput.normalizeColorHex() ?: settings.customAccentHex
                    },
                    enabled = customColorPreview != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ColorLens, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("应用自定义颜色")
                }
            }
        }

        SettingsGroup(
            title = "考试设置",
            description = "控制考试页默认展示方式。"
        ) {
            OptionGrid(
                options = ExamDisplayMode.values().toList(),
                selected = settings.examDisplayMode,
                icon = {
                    when (it) {
                        ExamDisplayMode.SINGLE_QUESTION -> Icons.Default.ViewAgenda
                        ExamDisplayMode.LIST -> Icons.Default.GridView
                    }
                },
                title = { it.label },
                subtitle = {
                    when (it) {
                        ExamDisplayMode.SINGLE_QUESTION -> "逐题展示，更聚焦"
                        ExamDisplayMode.LIST -> "列表展示，便于总览"
                    }
                },
                onSelect = AppSettingsStore::setExamDisplayMode
            )
        }

        SettingsGroup(
            title = "字体大小",
            description = "字号越大，部分密集页面可能会增加换行和滚动距离。"
        ) {
            OptionGrid(
                options = FontScaleLevel.values().toList(),
                selected = settings.fontScaleLevel,
                icon = { Icons.Default.FormatSize },
                title = { it.label },
                subtitle = { "${(it.scale * 100).toInt()}%" },
                onSelect = AppSettingsStore::setFontScale
            )
        }

        SettingsGroup(
            title = "更多",
            description = "外部入口与附加功能。"
        ) {
            SettingsActionCard(
                title = "下载原神",
                description = "打开官网页面。",
                icon = Icons.Default.Download,
                onClick = { uriHandler.openUri("https://ys.mihoyo.com/") }
            )
        }
    }
}

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("关闭")
            }
        },
        modifier = adaptiveDialogModifier(),
        properties = adaptiveDialogProperties(),
        title = { Text("帮助中心") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HelpItem("考试", "查看考试安排、答题进度和阅卷流程。")
                HelpItem("成绩", "学生可以在这里查看考试历史和成绩详情。")
                HelpItem("通知", "系统公告、考试提醒和成绩发布都会显示在通知中心。")
                HelpItem("题库", "教师可按题型、难度维护题库内容。")
                HelpItem("个人资料", "资料、头像和密码分开修改，减少误操作。")
            }
        }
    )
}

@Composable
fun HelpItem(title: String, desc: String) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
private fun SettingsSubsection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    selected: Boolean,
    selectedLabel: String,
    onClick: () -> Unit
) {
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = if (selected) selectedLabel else "系统",
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun <T> OptionGrid(
    options: List<T>,
    selected: T,
    icon: (T) -> ImageVector,
    title: (T) -> String,
    subtitle: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { item ->
            val isSelected = item == selected
            Surface(
                onClick = { onSelect(item) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon(item),
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = title(item),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle(item),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeAccentPicker(
    accents: List<ThemeAccent>,
    selected: ThemeAccent,
    activeHex: String,
    onSelect: (ThemeAccent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        accents.forEach { accent ->
            val isSelected = accent == selected && activeHex == accent.seedHex
            val swatchColor = accent.seedHex.toColorOrNull() ?: MaterialTheme.colorScheme.primary
            Surface(
                onClick = { onSelect(accent) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = swatchColor.bestForegroundColor(),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(text = accent.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    if (isSelected) {
                        Text("当前", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsActionCard(
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
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

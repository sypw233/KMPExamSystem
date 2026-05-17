package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSection("主题设置")
        ChoiceRow(
            title = "显示模式",
            items = AppThemeMode.values().toList(),
            selected = settings.themeMode,
            label = { it.label },
            onSelect = AppSettingsStore::setThemeMode
        )
        ChoiceRow(
            title = "主题色",
            items = ThemeAccentMode.values().toList(),
            selected = settings.accentMode,
            label = { it.label },
            onSelect = AppSettingsStore::setAccentMode
        )
        if (settings.accentMode == ThemeAccentMode.CUSTOM) {
            ChoiceRow(
                title = "自定义色",
                items = ThemeAccent.values().toList(),
                selected = settings.accent,
                label = { it.label },
                onSelect = AppSettingsStore::setAccent
            )
        }

        SettingsSection("考试设置")
        ChoiceRow(
            title = "答题展示",
            items = ExamDisplayMode.values().toList(),
            selected = settings.examDisplayMode,
            label = { it.label },
            onSelect = AppSettingsStore::setExamDisplayMode
        )

        SettingsSection("字体大小")
        Text(
            text = "较大的字号可能导致部分密集页面出现换行或滚动增多。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ChoiceRow(
            title = "字号",
            items = FontScaleLevel.values().toList(),
            selected = settings.fontScaleLevel,
            label = { it.label },
            onSelect = AppSettingsStore::setFontScale
        )

        SettingsSection("更多")
        OutlinedButton(
            onClick = { uriHandler.openUri("https://ys.mihoyo.com/") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("下载原神")
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
                HelpItem("通知", "系统公告、考试提醒和成绩发布都会出现在通知中心。")
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
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun <T> ChoiceRow(
    title: String,
    items: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items.forEach { item ->
                ChoiceChip(
                    text = label(item),
                    selected = item == selected,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelect(item) }
                )
            }
        }
    }
}

@Composable
private fun ChoiceChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

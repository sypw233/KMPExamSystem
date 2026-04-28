package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ovo.sypw.kmp.examsystem.data.dto.UserInfo
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig

@Composable
fun EditProfileDialog(
    user: UserInfo,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String?) -> Unit,
    onUploadAvatar: (onSuccess: (String) -> Unit, onError: (String) -> Unit) -> Unit
) {
    val config = LocalResponsiveConfig.current
    var realName by remember { mutableStateOf(user.realName.orEmpty()) }
    var email by remember { mutableStateOf(user.email.orEmpty()) }
    var avatarUrl by remember { mutableStateOf(user.avatar) }
    var isUploading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑个人资料") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 头像
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "头像",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = realName.take(1).uppercase()
                                        .ifBlank { user.username.take(1).uppercase() },
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        isUploading = true
                        onUploadAvatar(
                            { url ->
                                avatarUrl = url
                                isUploading = false
                            },
                            { error ->
                                isUploading = false
                            }
                        )
                    },
                    enabled = !isUploading
                ) {
                    Text(if (isUploading) "上传中..." else "更换头像")
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
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(realName.trim(), email.trim().ifBlank { null }, avatarUrl) },
                enabled = realName.isNotBlank() && !isUploading
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (oldPwd: String, newPwd: String) -> Unit
) {
    val config = LocalResponsiveConfig.current
    var oldPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    val valid = oldPwd.isNotBlank() && newPwd.length >= 6 && newPwd == confirmPwd

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改密码") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = oldPwd,
                    onValueChange = { oldPwd = it },
                    label = { Text("旧密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newPwd,
                    onValueChange = { newPwd = it },
                    label = { Text("新密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = confirmPwd,
                    onValueChange = { confirmPwd = it },
                    label = { Text("确认新密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(oldPwd, newPwd) }, enabled = valid) { Text("更新") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    val config = LocalResponsiveConfig.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("帮助中心") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HelpItem("考试管理", "教师可以创建、发布和管理考试，设置考试时间和题目。")
                HelpItem("成绩查看", "学生可以在考试结束后查看自己的成绩和答题详情。")
                HelpItem("通知中心", "系统会推送考试安排、成绩发布等重要通知。")
                HelpItem("题库管理", "教师可以维护题库，支持分类、难度和类型筛选。")
                HelpItem("个人资料", "在编辑资料中可以修改昵称、邮箱和头像。")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("知道了") }
        }
    )
}

@Composable
fun HelpItem(title: String, desc: String) {
    val config = LocalResponsiveConfig.current
    Column {
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

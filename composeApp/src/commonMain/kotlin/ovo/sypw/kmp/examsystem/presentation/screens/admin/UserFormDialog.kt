package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.UserCreateRequest
import ovo.sypw.kmp.examsystem.data.dto.UserResponse
import ovo.sypw.kmp.examsystem.data.dto.UserUpdateRequest
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserDialog(
    onConfirm: (UserCreateRequest) -> Unit,
    onDismiss: () -> Unit
) {
    val config = LocalResponsiveConfig.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var realName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("student") }
    var roleExpanded by remember { mutableStateOf(false) }

    val isValid = username.length >= 3 && password.length >= 6 && role.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建用户") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码 * (≥6位)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = realName,
                    onValueChange = { realName = it },
                    label = { Text("真实姓名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = roleDisplayName(role),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("角色 *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        listOf("student" to "学生", "teacher" to "教师", "admin" to "管理员").forEach { (v, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { role = v; roleExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(UserCreateRequest(
                        username = username.trim(),
                        password = password,
                        realName = realName.takeIf { it.isNotBlank() },
                        email = email.takeIf { it.isNotBlank() },
                        role = role
                    ))
                },
                enabled = isValid
            ) { Text("创建") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(
    user: UserResponse,
    onConfirm: (UserUpdateRequest) -> Unit,
    onDismiss: () -> Unit
) {
    val config = LocalResponsiveConfig.current
    var realName by remember { mutableStateOf(user.realName ?: "") }
    var email by remember { mutableStateOf(user.email ?: "") }
    var role by remember { mutableStateOf(user.role) }
    var roleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑用户：${user.username}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = realName,
                    onValueChange = { realName = it },
                    label = { Text("真实姓名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = roleDisplayName(role),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("角色") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        listOf("student" to "学生", "teacher" to "教师", "admin" to "管理员").forEach { (v, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { role = v; roleExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(UserUpdateRequest(
                    realName = realName.takeIf { it.isNotBlank() },
                    email = email.takeIf { it.isNotBlank() },
                    role = role
                ))
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

fun roleDisplayName(role: String) = when (role.lowercase()) {
    "admin"   -> "管理员"
    "teacher" -> "教师"
    else      -> "学生"
}

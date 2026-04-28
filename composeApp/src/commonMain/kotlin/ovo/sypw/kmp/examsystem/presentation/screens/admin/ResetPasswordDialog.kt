package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig

@Composable
fun ResetPasswordDialog(
    username: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val config = LocalResponsiveConfig.current
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val isValid = newPassword.length >= 6 && newPassword == confirmPassword

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重置密码：$username") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("新密码 (≥6位)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("确认密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                            Text("两次密码不一致", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(newPassword) }, enabled = isValid) { Text("重置") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

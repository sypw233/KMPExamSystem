package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import ovo.sypw.kmp.examsystem.data.dto.UserResponse
import ovo.sypw.kmp.examsystem.presentation.viewmodel.UserManageViewModel

@Composable
internal fun UserManageDialogHost(
    viewModel: UserManageViewModel,
    showCreateDialog: Boolean,
    showEditDialog: UserResponse?,
    showDeleteConfirm: UserResponse?,
    showResetPwdDialog: UserResponse?,
    showBatchDeleteConfirm: Boolean,
    selectedIds: Set<Long>,
    onDismissCreate: () -> Unit,
    onDismissEdit: () -> Unit,
    onDismissDelete: () -> Unit,
    onDismissResetPassword: () -> Unit,
    onDismissBatchDelete: () -> Unit,
    onBatchDeleteFinished: () -> Unit
) {
    if (showCreateDialog) {
        CreateUserDialog(
            onConfirm = { req ->
                viewModel.createUser(req)
                onDismissCreate()
            },
            onDismiss = onDismissCreate
        )
    }

    showEditDialog?.let { user ->
        EditUserDialog(
            user = user,
            onConfirm = { req ->
                viewModel.updateUser(user.id, req)
                onDismissEdit()
            },
            onDismiss = onDismissEdit
        )
    }

    showDeleteConfirm?.let { user ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("删除用户") },
            text = { Text("确定要删除用户「${user.realName ?: user.username}」吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUser(user.id)
                        onDismissDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) { Text("取消") }
            }
        )
    }

    showResetPwdDialog?.let { user ->
        ResetPasswordDialog(
            username = user.realName ?: user.username,
            onConfirm = { pwd ->
                viewModel.resetPassword(user.id, pwd)
                onDismissResetPassword()
            },
            onDismiss = onDismissResetPassword
        )
    }

    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = onDismissBatchDelete,
            title = { Text("批量删除用户") },
            text = { Text("确定要删除选中的 ${selectedIds.size} 位用户吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.batchDeleteUsers(selectedIds.toList())
                        onBatchDeleteFinished()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = onDismissBatchDelete) { Text("取消") } }
        )
    }
}

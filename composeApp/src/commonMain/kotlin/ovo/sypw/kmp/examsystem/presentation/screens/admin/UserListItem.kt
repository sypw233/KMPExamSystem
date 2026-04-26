package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.UserResponse
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@Composable
fun UserCard(
    user: UserResponse,
    isBatchMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onResetPassword: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val config = LocalResponsiveConfig.current
    val isEnabled = user.status == 1
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = config.cardPadding),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                isEnabled -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(config.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isBatchMode) {
                Checkbox(checked = isSelected, onCheckedChange = { onToggleSelect() })
                Spacer(modifier = Modifier.width(8.dp))
            }
            // 用户信息
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user.realName ?: user.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RoleBadge(role = user.role)
                    if (!isEnabled) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                            Text("禁用", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
                Text(
                    "@${user.username}  ${user.email ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 操作按钮
            if (!isBatchMode) {
                Row {
                    IconButton(onClick = onToggleStatus) {
                        Icon(
                            if (isEnabled) Icons.Default.Block else Icons.Default.CheckCircle,
                            contentDescription = if (isEnabled) "禁用" else "启用",
                            tint = if (isEnabled) MaterialTheme.colorScheme.error
                                   else MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onResetPassword) {
                        Icon(Icons.Default.LockReset, contentDescription = "重置密码")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Person, contentDescription = "编辑")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "删除",
                             tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun RoleBadge(role: String) {
    val (label, color) = when (role.lowercase()) {
        "admin"   -> "管理员" to MaterialTheme.colorScheme.tertiary
        "teacher" -> "教师" to MaterialTheme.colorScheme.secondary
        else      -> "学生" to MaterialTheme.colorScheme.primary
    }
    Badge(containerColor = color.copy(alpha = 0.15f)) {
        Text(label, color = color, style = MaterialTheme.typography.labelSmall)
    }
}

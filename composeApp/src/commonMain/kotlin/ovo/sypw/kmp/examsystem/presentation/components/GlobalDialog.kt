package ovo.sypw.kmp.examsystem.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.domain.DialogConfig
import ovo.sypw.kmp.examsystem.domain.DialogType

/**
 * 全局弹窗组件
 * @param config 弹窗配置，null 表示不显示弹窗
 * @param onDismiss 关闭弹窗回调
 */
@Composable
fun GlobalDialog(
    config: DialogConfig?,
    onDismiss: () -> Unit
) {
    if (config != null) {
        AlertDialog(
            onDismissRequest = {
                if (config.dismissOnClickOutside) {
                    config.onCancel?.invoke()
                    onDismiss()
                }
            },
            icon = {
                Icon(
                    imageVector = getDialogIcon(config.type),
                    contentDescription = null,
                    tint = getDialogColor(config.type),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = config.title,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = config.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        config.onConfirm?.invoke()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getDialogColor(config.type)
                    )
                ) {
                    Text(config.confirmText)
                }
            },
            dismissButton = if (config.cancelText != null) {
                {
                    TextButton(
                        onClick = {
                            config.onCancel?.invoke()
                            onDismiss()
                        }
                    ) {
                        Text(config.cancelText)
                    }
                }
            } else null,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}

/**
 * 根据弹窗类型获取图标
 */
private fun getDialogIcon(type: DialogType): ImageVector {
    return when (type) {
        DialogType.INFO -> Icons.Default.Info
        DialogType.WARNING -> Icons.Default.Warning
        DialogType.ERROR -> Icons.Default.Error
        DialogType.SUCCESS -> Icons.Default.CheckCircle
        DialogType.CONFIRM -> Icons.AutoMirrored.Filled.HelpOutline
    }
}

/**
 * 根据弹窗类型获取颜色
 */
@Composable
private fun getDialogColor(type: DialogType): Color {
    return when (type) {
        DialogType.INFO -> Color(0xFF2196F3)      // 蓝色
        DialogType.WARNING -> Color(0xFFFFA726)   // 橙色
        DialogType.ERROR -> Color(0xFFF44336)     // 红色
        DialogType.SUCCESS -> Color(0xFF4CAF50)   // 绿色
        DialogType.CONFIRM -> Color(0xFF9C27B0)   // 紫色
    }
}

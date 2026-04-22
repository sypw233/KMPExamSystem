package ovo.sypw.kmp.examsystem.examples

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.utils.DialogManager

/**
 * 全局弹窗使用示例
 * 演示如何使用 DialogManager 显示各种类型的弹窗
 */
@Composable
fun DialogExampleScreen(
    dialogManager: DialogManager = koinInject()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "全局弹窗示例",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // 信息弹窗
        Button(
            onClick = {
                dialogManager.showInfo(
                    title = "提示",
                    message = "这是一条信息弹窗"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("显示信息弹窗")
        }
        
        // 警告弹窗
        Button(
            onClick = {
                dialogManager.showWarning(
                    title = "警告",
                    message = "请注意此操作可能会有风险"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("显示警告弹窗")
        }
        
        // 错误弹窗
        Button(
            onClick = {
                dialogManager.showError(
                    message = "操作失败，请重试"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("显示错误弹窗")
        }
        
        // 成功弹窗
        Button(
            onClick = {
                dialogManager.showSuccess(
                    message = "操作成功！"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("显示成功弹窗")
        }
        
        // 确认弹窗
        Button(
            onClick = {
                dialogManager.showConfirm(
                    title = "确认删除",
                    message = "确定要删除这条记录吗？此操作不可撤销。",
                    onConfirm = {
                        // 执行删除操作
                        dialogManager.showSuccess(message = "删除成功")
                    },
                    onCancel = {
                        // 取消删除
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("显示确认弹窗")
        }
        
        // 多个弹窗（队列测试）
        Button(
            onClick = {
                dialogManager.showInfo(title = "第一个", message = "这是第一个弹窗")
                dialogManager.showWarning(title = "第二个", message = "这是第二个弹窗")
                dialogManager.showSuccess(title = "第三个", message = "这是第三个弹窗")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("测试弹窗队列（连续显示3个）")
        }
    }
}

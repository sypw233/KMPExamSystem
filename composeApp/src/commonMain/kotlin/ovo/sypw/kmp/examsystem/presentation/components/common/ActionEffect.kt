package ovo.sypw.kmp.examsystem.presentation.components.common

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshotFlow

/**
 * 操作状态反馈效果
 * 监听 sealed interface 类型的操作状态, 在 Success/Error 时显示 Snackbar 并自动重置
 * 使用 snapshotFlow 替代 LaunchedEffect(sealedClass), 避免 resetState 时的无用触发
 *
 * @param actionState 操作状态 (collectAsState 的结果)
 * @param snackbarHostState Snackbar 宿主
 * @param isSuccess 判断是否为成功状态
 * @param isError 判断是否为错误状态
 * @param getMessage 从状态中提取消息
 * @param onConsumed 状态被消费后的回调 (通常用于 resetState)
 * @param onSuccess 可选的成功额外回调
 */
@Composable
fun <T> ActionEffect(
    actionState: State<T>,
    snackbarHostState: SnackbarHostState,
    isSuccess: (T) -> Boolean,
    isError: (T) -> Boolean,
    getMessage: (T) -> String,
    onConsumed: () -> Unit,
    onSuccess: (() -> Unit)? = null
) {
    LaunchedEffect(Unit) {
        snapshotFlow { actionState.value }.collect { state ->
            when {
                isSuccess(state) -> {
                    snackbarHostState.showSnackbar(getMessage(state))
                    onConsumed()
                    onSuccess?.invoke()
                }
                isError(state) -> {
                    snackbarHostState.showSnackbar(getMessage(state))
                    onConsumed()
                }
            }
        }
    }
}

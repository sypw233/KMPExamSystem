package ovo.sypw.kmp.examsystem.presentation.components.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 通用 UI 状态接口
 * ViewModel 可直接使用此接口或通过扩展函数适配
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Error(val message: String) : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
}

/**
 * 通用屏幕状态处理器
 * 封装 Loading / Error / Success(含空状态) 的切换逻辑
 *
 * 用法示例:
 * ```
 * val uiState by viewModel.uiState.collectAsState()
 * ScreenStateHandler(
 *     uiState = uiState,
 *     onRetry = { viewModel.loadData() },
 *     emptyCheck = { it.isEmpty() },
 *     emptyContent = { EmptyState(icon = Icons.Default.Inbox, title = "暂无数据") },
 *     successContent = { data -> MyContent(data) }
 * )
 * ```
 *
 * @param uiState 当前 UI 状态，支持 UiState<T>、Loading/Error/Success 三态模式
 * @param onRetry 重试回调，null 时不显示重试按钮
 * @param emptyCheck 判断成功数据是否为空的 lambda，默认任何数据都不为空
 * @param emptyContent 空状态展示内容
 * @param successContent 成功状态展示内容
 * @param loadingContent 自定义加载内容，默认使用全屏加载
 * @param modifier 修饰符
 */
@Composable
fun <T> ScreenStateHandler(
    uiState: UiState<T>,
    onRetry: (() -> Unit)? = null,
    emptyCheck: (T) -> Boolean = { false },
    emptyContent: @Composable () -> Unit = {
        EmptyState(
            icon = Icons.Default.Inbox,
            title = "暂无数据"
        )
    },
    successContent: @Composable (T) -> Unit,
    loadingContent: @Composable () -> Unit = {
        LoadingContent(
            size = LoadingSize.FULL,
            message = "加载中..."
        )
    },
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is UiState.Loading -> loadingContent()
        is UiState.Error -> ErrorContent(
            message = uiState.message,
            onRetry = onRetry,
            modifier = modifier
        )
        is UiState.Success -> {
            if (emptyCheck(uiState.data)) {
                emptyContent()
            } else {
                successContent(uiState.data)
            }
        }
    }
}

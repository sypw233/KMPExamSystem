package ovo.sypw.kmp.examsystem.presentation.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 加载尺寸模式
 * - FULL: 全屏居中加载，适用于页面级加载
 * - SMALL: 小尺寸加载指示器，适用于内联加载
 */
enum class LoadingSize {
    FULL,
    SMALL
}

/**
 * 通用加载组件
 *
 * @param modifier 修饰符
 * @param size 加载尺寸模式，默认全屏模式
 * @param message 可选的加载提示文本，null 时不显示文字
 */
@Composable
fun LoadingContent(
    modifier: Modifier = Modifier,
    size: LoadingSize = LoadingSize.FULL,
    message: String? = null
) {
    when (size) {
        LoadingSize.FULL -> FullScreenLoading(modifier = modifier, message = message)
        LoadingSize.SMALL -> SmallLoading(modifier = modifier)
    }
}

@Composable
private fun FullScreenLoading(
    modifier: Modifier = Modifier,
    message: String?
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            if (message != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SmallLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.dp
        )
    }
}

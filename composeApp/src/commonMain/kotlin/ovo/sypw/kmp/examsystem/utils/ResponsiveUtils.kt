package ovo.sypw.kmp.examsystem.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 响应式设计工具类
 * 定义不同屏幕尺寸的断点和布局配置
 */
object ResponsiveUtils {

    /**
     * 屏幕尺寸类型枚举
     */
    enum class ScreenSize {
        COMPACT,    // 紧凑型 (手机竖屏)
        MEDIUM,     // 中等型 (手机横屏/小平板)
        EXPANDED    // 扩展型 (大平板/桌面)
    }

    /**
     * Material Design 3 推荐的断点
     */
    object Breakpoints {
        val COMPACT_MAX = 600.dp
        val MEDIUM_MAX = 840.dp
    }

    /**
     * 根据屏幕宽度获取屏幕尺寸类型
     */
    fun getScreenSize(screenWidth: Dp): ScreenSize {
        return when {
            screenWidth < Breakpoints.COMPACT_MAX -> ScreenSize.COMPACT
            screenWidth < Breakpoints.MEDIUM_MAX -> ScreenSize.MEDIUM
            else -> ScreenSize.EXPANDED
        }
    }

    /**
     * 响应式内边距配置
     */
    object Padding {
        fun getScreenPadding(screenSize: ScreenSize): Dp {
            return when (screenSize) {
                ScreenSize.COMPACT -> 16.dp
                ScreenSize.MEDIUM -> 24.dp
                ScreenSize.EXPANDED -> 32.dp
            }
        }

        fun getContentPadding(screenSize: ScreenSize): Dp {
            return when (screenSize) {
                ScreenSize.COMPACT -> 12.dp
                ScreenSize.MEDIUM -> 16.dp
                ScreenSize.EXPANDED -> 20.dp
            }
        }

        fun getCardPadding(screenSize: ScreenSize): Dp {
            return when (screenSize) {
                ScreenSize.COMPACT -> 16.dp
                ScreenSize.MEDIUM -> 20.dp
                ScreenSize.EXPANDED -> 24.dp
            }
        }
    }

    /**
     * 响应式间距配置
     */
    object Spacing {
        fun getVerticalSpacing(screenSize: ScreenSize): Dp {
            return when (screenSize) {
                ScreenSize.COMPACT -> 12.dp
                ScreenSize.MEDIUM -> 16.dp
                ScreenSize.EXPANDED -> 20.dp
            }
        }

        fun getHorizontalSpacing(screenSize: ScreenSize): Dp {
            return when (screenSize) {
                ScreenSize.COMPACT -> 8.dp
                ScreenSize.MEDIUM -> 12.dp
                ScreenSize.EXPANDED -> 16.dp
            }
        }
    }

    /**
     * 响应式列数配置
     */
    object Grid {
        fun getColumnCount(screenSize: ScreenSize): Int {
            return when (screenSize) {
                ScreenSize.COMPACT -> 1
                ScreenSize.MEDIUM -> 2
                ScreenSize.EXPANDED -> 3
            }
        }

        fun getMaxCardWidth(screenSize: ScreenSize): Dp {
            return when (screenSize) {
                ScreenSize.COMPACT -> Dp.Unspecified
                ScreenSize.MEDIUM -> 400.dp
                ScreenSize.EXPANDED -> 350.dp
            }
        }
    }

    /**
     * 响应式按钮配置
     */
    object Button {
        fun getButtonHeight(screenSize: ScreenSize): Dp {
            return when (screenSize) {
                ScreenSize.COMPACT -> 48.dp
                ScreenSize.MEDIUM -> 52.dp
                ScreenSize.EXPANDED -> 56.dp
            }
        }

        fun shouldUseFullWidthButtons(screenSize: ScreenSize): Boolean {
            return screenSize == ScreenSize.COMPACT
        }
    }

    /**
     * 响应式Tab配置
     */
    object Tab {
        fun shouldUseScrollableTabs(screenSize: ScreenSize): Boolean {
            return screenSize == ScreenSize.COMPACT
        }

        fun getTabMinWidth(screenSize: ScreenSize): Dp {
            return when (screenSize) {
                ScreenSize.COMPACT -> 90.dp
                ScreenSize.MEDIUM -> 120.dp
                ScreenSize.EXPANDED -> 160.dp
            }
        }
    }
}

/**
 * 响应式布局配置数据类
 */
data class ResponsiveLayoutConfig(
    val screenSize: ResponsiveUtils.ScreenSize,
    val screenSizeOrigin: Dp,
    val screenPadding: Dp,
    val contentPadding: Dp,
    val cardPadding: Dp,
    val verticalSpacing: Dp,
    val horizontalSpacing: Dp,
    val columnCount: Int,
    val useFullWidthButtons: Boolean,
    val useScrollableTabs: Boolean
)

/**
 * 获取当前屏幕的响应式布局配置
 */
@Composable
fun getResponsiveLayoutConfig(screenWidth: Dp): ResponsiveLayoutConfig {
    val screenSize = ResponsiveUtils.getScreenSize(screenWidth)

    return ResponsiveLayoutConfig(
        screenSize = screenSize,
        screenSizeOrigin = screenWidth,
        screenPadding = ResponsiveUtils.Padding.getScreenPadding(screenSize),
        contentPadding = ResponsiveUtils.Padding.getContentPadding(screenSize),
        cardPadding = ResponsiveUtils.Padding.getCardPadding(screenSize),
        verticalSpacing = ResponsiveUtils.Spacing.getVerticalSpacing(screenSize),
        horizontalSpacing = ResponsiveUtils.Spacing.getHorizontalSpacing(screenSize),
        columnCount = ResponsiveUtils.Grid.getColumnCount(screenSize),
        useFullWidthButtons = ResponsiveUtils.Button.shouldUseFullWidthButtons(screenSize),
        useScrollableTabs = ResponsiveUtils.Tab.shouldUseScrollableTabs(screenSize)
    )
}

/**
 * CompositionLocal 提供的响应式布局配置
 * 在 App.kt 的 MainContent 中设置，所有屏幕可直接访问
 */
val LocalResponsiveConfig = compositionLocalOf<ResponsiveLayoutConfig> {
    error("LocalResponsiveConfig not provided. Wrap your content with CompositionLocalProvider.")
}

/**
 * 响应式屏幕内容容器
 * 统一处理：居中对齐、最大宽度限制、响应式 padding、垂直滚动
 *
 * @param modifier 外层修饰符
 * @param maxWidth 内容最大宽度，null 表示使用响应式默认值
 * @param horizontalAlignment 子项水平对齐方式
 * @param verticalArrangement 子项垂直排列方式
 * @param content 内容块
 */
@Composable
fun ResponsiveContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    val config = LocalResponsiveConfig.current
    val effectiveMaxWidth = maxWidth ?: when (config.screenSize) {
        ResponsiveUtils.ScreenSize.COMPACT -> Dp.Unspecified
        ResponsiveUtils.ScreenSize.MEDIUM -> 960.dp
        ResponsiveUtils.ScreenSize.EXPANDED -> 1200.dp
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(config.screenPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (effectiveMaxWidth != Dp.Unspecified) {
                        Modifier.widthIn(max = effectiveMaxWidth)
                    } else Modifier
                )
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement
        ) {
            content()
        }
    }
}

/**
 * 窄版响应式容器（适合登录、表单等窄内容）
 */
@Composable
fun ResponsiveNarrowContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val config = LocalResponsiveConfig.current
    val maxWidth = when (config.screenSize) {
        ResponsiveUtils.ScreenSize.COMPACT -> Dp.Unspecified
        ResponsiveUtils.ScreenSize.MEDIUM -> 520.dp
        ResponsiveUtils.ScreenSize.EXPANDED -> 480.dp
    }

    ResponsiveContent(
        modifier = modifier,
        maxWidth = maxWidth,
        content = content
    )
}
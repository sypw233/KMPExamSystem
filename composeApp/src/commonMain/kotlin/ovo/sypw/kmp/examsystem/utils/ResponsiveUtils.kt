package ovo.sypw.kmp.examsystem.utils

import androidx.compose.runtime.Composable
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
        cardPadding = ResponsiveUtils.Padding.getContentPadding(screenSize),
        verticalSpacing = ResponsiveUtils.Spacing.getVerticalSpacing(screenSize),
        horizontalSpacing = ResponsiveUtils.Spacing.getHorizontalSpacing(screenSize),
        columnCount = ResponsiveUtils.Grid.getColumnCount(screenSize),
        useFullWidthButtons = ResponsiveUtils.Button.shouldUseFullWidthButtons(screenSize),
        useScrollableTabs = ResponsiveUtils.Tab.shouldUseScrollableTabs(screenSize)
    )
}
package ovo.sypw.kmp.examsystem.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
        val MEDIUM_MAX = 900.dp    // 增加到900dp，让更多设备使用桌面布局
        val EXPANDED_LARGE = 1200.dp  // 大屏桌面断点
    }

    /**
     * 统一内容最大宽度常量
     * 避免各屏幕随意使用不同数值
     */
    object MaxWidths {
        val FULL = 1480.dp      // 全宽内容（仪表盘、管理页）
        val STANDARD = 1120.dp  // 标准内容（列表页）
        val NARROW = 880.dp     // 窄内容（考试、通知）
        val FORM = 480.dp       // 表单内容（登录、注册、设置）
        val EXAM_TAKING = 800.dp     // 考试答题页
        val SYSTEM_SETTINGS = 960.dp // 系统设置页（增加宽度）
        val EXAM_COMPOSE = 900.dp    // 组卷页
        val PROFILE_FORM = 420.dp    // 个人资料表单
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
     * 判断是否为宽屏桌面（>1200dp）
     */
    fun isLargeDesktop(screenWidth: Dp): Boolean {
        return screenWidth >= Breakpoints.EXPANDED_LARGE
    }

    /**
     * 响应式内边距配置
     */
    object Padding {
        fun getScreenPadding(screenSize: ScreenSize): Dp {
            return when (screenSize) {
                ScreenSize.COMPACT -> 16.dp
                ScreenSize.MEDIUM -> 24.dp
                ScreenSize.EXPANDED -> 24.dp
            }
        }
        fun getContentPadding(screenSize: ScreenSize): Dp {
            return when (screenSize) {
                ScreenSize.COMPACT -> 12.dp
                ScreenSize.MEDIUM -> 16.dp
                ScreenSize.EXPANDED -> 16.dp
            }
        }
        fun getCardPadding(screenSize: ScreenSize): Dp {
            return when (screenSize) {
                ScreenSize.COMPACT -> 16.dp
                ScreenSize.MEDIUM -> 20.dp
                ScreenSize.EXPANDED -> 20.dp
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
                ScreenSize.EXPANDED -> 16.dp
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

        /**
         * 根据可用宽度动态计算列数（比固定断点更灵活）
         */
        fun getAdaptiveColumnCount(
            availableWidth: Dp,
            minItemWidth: Dp = 320.dp,
            maxColumns: Int = Int.MAX_VALUE
        ): Int {
            val widthBasedColumns = (availableWidth / minItemWidth).toInt().coerceAtLeast(1)
            return widthBasedColumns.coerceAtMost(maxColumns.coerceAtLeast(1))
        }

        fun getMaxColumnCount(screenSize: ScreenSize): Int {
            return when (screenSize) {
                ScreenSize.COMPACT -> 1
                ScreenSize.MEDIUM -> 2
                ScreenSize.EXPANDED -> 4
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
) {
    /**
     * 是否为桌面端（扩展型屏幕）
     */
    val isDesktop: Boolean
        get() = screenSize == ResponsiveUtils.ScreenSize.EXPANDED

    /**
     * 是否使用桌面端管理系统布局
     * 桌面端使用标题栏、筛选工具条、主从布局等
     */
    val useDesktopAdminLayout: Boolean
        get() = isDesktop

    /**
     * 桌面端主从布局权重配置
     */
    val desktopMasterWeight: Float
        get() = 1f

    val desktopDetailWeight: Float
        get() = 1.4f
}

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
        ResponsiveUtils.ScreenSize.MEDIUM -> ResponsiveUtils.MaxWidths.STANDARD
        ResponsiveUtils.ScreenSize.EXPANDED -> ResponsiveUtils.MaxWidths.FULL
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(config.screenPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (effectiveMaxWidth != Dp.Unspecified) {
                        Modifier.widthIn(max = effectiveMaxWidth)
                    } else Modifier
                )
                .fillMaxWidth()
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
        ResponsiveUtils.ScreenSize.MEDIUM -> 560.dp
        ResponsiveUtils.ScreenSize.EXPANDED -> ResponsiveUtils.MaxWidths.FORM
    }

    ResponsiveContent(
        modifier = modifier,
        maxWidth = maxWidth,
        content = content
    )
}

/**
 * 桌面端双栏布局（主从/列表+详情）
 * 在桌面端显示左右两栏，在移动端显示单栏
 *
 * @param master 左侧/主栏内容
 * @param detail 右侧/详情栏内容
 * @param masterWeight 主栏权重
 * @param detailWeight 详情栏权重
 * @param modifier 修饰符
 */
@Composable
fun DesktopTwoPaneLayout(
    master: @Composable () -> Unit,
    detail: @Composable () -> Unit,
    masterWeight: Float = 1f,
    detailWeight: Float = 1.4f,
    modifier: Modifier = Modifier
) {
    val config = LocalResponsiveConfig.current
    if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) {
        Row(
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing * 2)
        ) {
            Box(modifier = Modifier.weight(masterWeight)) { master() }
            Box(modifier = Modifier.weight(detailWeight)) { detail() }
        }
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            master()
        }
    }
}

/**
 * 桌面端数据表格行
 * 在桌面端显示为横向排列的表格行，在移动端保持纵向卡片
 *
 * @param modifier 修饰符
 * @param columns 列内容，每个 Pair 为 (权重, @Composable 内容)
 */
@Composable
fun DesktopDataTableRow(
    modifier: Modifier = Modifier,
    vararg columns: Pair<Float, @Composable () -> Unit>
) {
    val config = LocalResponsiveConfig.current
    val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED

    if (isDesktop) {
        Row(
            modifier = modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(config.horizontalSpacing)
        ) {
            columns.forEach { (weight, content) ->
                Box(modifier = Modifier.weight(weight)) { content() }
            }
        }
    }
}



/**
 * 响应式懒加载网格
 * 根据容器宽度自动调整列数，并按屏幕类型限制最大列数
 *
 * @param items 数据列表
 * @param key 项的唯一键
 * @param modifier 修饰符
 * @param contentPadding 内容内边距
 * @param verticalArrangement 垂直间距
 * @param horizontalArrangement 水平间距（多列时有效）
 * @param columnCountOverride 指定列数，null 时使用全局响应式列数
 * @param minItemWidth 自适应列宽下限
 * @param itemContent 单项内容渲染
 */
@Composable
fun <T> ResponsiveLazyVerticalGrid(
    items: List<T>,
    key: ((T) -> Any)? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
    columnCountOverride: Int? = null,
    minItemWidth: Dp = 320.dp,
    itemContent: @Composable (T) -> Unit
) {
    val config = LocalResponsiveConfig.current

    BoxWithConstraints(modifier = modifier) {
        val columns = (
            columnCountOverride
                ?: ResponsiveUtils.Grid.getAdaptiveColumnCount(
                    availableWidth = maxWidth,
                    minItemWidth = minItemWidth,
                    maxColumns = ResponsiveUtils.Grid.getMaxColumnCount(config.screenSize)
                )
            ).coerceAtLeast(1)

        if (columns == 1 || items.isEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement
            ) {
                items(items, key = key) { itemContent(it) }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement
            ) {
                items(items.chunked(columns).withIndex().toList(), key = { it.index }) { (_, rowItems) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = horizontalArrangement
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                itemContent(item)
                            }
                        }
                        repeat(columns - rowItems.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

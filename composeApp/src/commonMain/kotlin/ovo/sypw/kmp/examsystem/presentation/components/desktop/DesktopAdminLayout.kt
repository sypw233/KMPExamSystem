package ovo.sypw.kmp.examsystem.presentation.components.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.presentation.components.management.ManagementPageHeader
import ovo.sypw.kmp.examsystem.presentation.components.management.ManagementPanel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

/**
 * 桌面端管理系统布局容器
 * 提供标题栏 + 内容区域的标准布局结构
 *
 * @param title 页面标题
 * @param subtitle 页面副标题
 * @param modifier 修饰符
 * @param actions 标题栏操作按钮
 * @param content 内容区域
 */
@Composable
fun DesktopAdminLayout(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val config = LocalResponsiveConfig.current
    val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED

    if (isDesktop) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(config.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ManagementPageHeader(
                title = title,
                subtitle = subtitle,
                actions = actions
            )
            ManagementPanel(
                modifier = Modifier.fillMaxSize()
            ) {
                content()
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            content = content
        )
    }
}

/**
 * 桌面端主从布局容器
 * 左侧列表/表格区域 + 右侧详情/表单区域
 *
 * @param title 页面标题
 * @param subtitle 页面副标题
 * @param master 左侧主内容
 * @param detail 右侧详情内容
 * @param modifier 修饰符
 * @param actions 标题栏操作按钮
 * @param masterWeight 左侧权重
 * @param detailWeight 右侧权重
 * @param showDetail 是否显示右侧详情面板
 */
@Composable
fun DesktopAdminMasterDetailLayout(
    title: String,
    subtitle: String,
    master: @Composable () -> Unit,
    detail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    masterWeight: Float = 1f,
    detailWeight: Float = 1.4f,
    showDetail: Boolean = true
) {
    val config = LocalResponsiveConfig.current
    val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED

    if (isDesktop) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(config.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ManagementPageHeader(
                title = title,
                subtitle = subtitle,
                actions = actions
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ManagementPanel(
                    modifier = Modifier.weight(masterWeight)
                ) {
                    master()
                }
                if (showDetail) {
                    ManagementPanel(
                        modifier = Modifier.weight(detailWeight)
                    ) {
                        detail()
                    }
                }
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            master()
        }
    }
}

/**
 * 桌面端全宽布局容器
 * 用于仪表板等需要全宽展示的页面
 *
 * @param title 页面标题
 * @param subtitle 页面副标题
 * @param modifier 修饰符
 * @param actions 标题栏操作按钮
 * @param content 内容区域
 */
@Composable
fun DesktopAdminFullWidthLayout(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val config = LocalResponsiveConfig.current
    val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED

    if (isDesktop) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(config.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ManagementPageHeader(
                title = title,
                subtitle = subtitle,
                actions = actions
            )
            content()
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            content = content
        )
    }
}

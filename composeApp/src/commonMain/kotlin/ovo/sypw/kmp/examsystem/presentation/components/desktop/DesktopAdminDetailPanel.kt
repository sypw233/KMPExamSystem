package ovo.sypw.kmp.examsystem.presentation.components.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 桌面端详情面板组件
 * 支持详情展示和表单编辑
 *
 * @param title 面板标题
 * @param modifier 修饰符
 * @param showEditButton 是否显示编辑按钮
 * @param onEditClick 编辑按钮点击事件
 * @param showCloseButton 是否显示关闭按钮
 * @param onCloseClick 关闭按钮点击事件
 * @param actions 额外操作按钮
 * @param content 面板内容
 */
@Composable
fun DesktopAdminDetailPanel(
    title: String,
    modifier: Modifier = Modifier,
    showEditButton: Boolean = false,
    onEditClick: (() -> Unit)? = null,
    showCloseButton: Boolean = false,
    onCloseClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 面板头部
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actions()

                    if (showEditButton && onEditClick != null) {
                        FilledTonalButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "编辑",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("编辑")
                        }
                    }

                    if (showCloseButton && onCloseClick != null) {
                        IconButton(onClick = onCloseClick) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭"
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // 面板内容
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }
}

/**
 * 桌面端表单面板组件
 * 用于编辑表单
 *
 * @param title 面板标题
 * @param modifier 修饰符
 * @param showCloseButton 是否显示关闭按钮
 * @param onCloseClick 关闭按钮点击事件
 * @param submitText 提交按钮文本
 * @param onSubmit 提交按钮点击事件
 * @param submitEnabled 提交按钮是否可用
 * @param cancelText 取消按钮文本
 * @param onCancel 取消按钮点击事件
 * @param content 表单内容
 */
@Composable
fun DesktopAdminFormPanel(
    title: String,
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = false,
    onCloseClick: (() -> Unit)? = null,
    submitText: String = "保存",
    onSubmit: () -> Unit,
    submitEnabled: Boolean = true,
    cancelText: String = "取消",
    onCancel: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 面板头部
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (showCloseButton && onCloseClick != null) {
                    IconButton(onClick = onCloseClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }
            }

            HorizontalDivider()

            // 表单内容
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )

            HorizontalDivider()

            // 操作按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
            ) {
                if (onCancel != null) {
                    OutlinedButton(onClick = onCancel) {
                        Text(cancelText)
                    }
                }

                Button(
                    onClick = onSubmit,
                    enabled = submitEnabled
                ) {
                    Text(submitText)
                }
            }
        }
    }
}

/**
 * 桌面端可展开/收起详情面板
 *
 * @param title 面板标题
 * @param expanded 是否展开
 * @param onExpandedChange 展开状态变化回调
 * @param modifier 修饰符
 * @param width 面板宽度
 * @param content 面板内容
 */
@Composable
fun DesktopAdminCollapsiblePanel(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 400.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandHorizontally(expandFrom = Alignment.End),
        exit = shrinkHorizontally(shrinkTowards = Alignment.End),
        modifier = modifier
    ) {
        DesktopAdminDetailPanel(
            title = title,
            modifier = Modifier.width(width),
            showCloseButton = true,
            onCloseClick = { onExpandedChange(false) },
            content = content
        )
    }
}

/**
 * 详情项组件
 * 用于展示标签-值对
 *
 * @param label 标签
 * @param value 值
 * @param modifier 修饰符
 */
@Composable
fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 详情项组件（带自定义内容）
 *
 * @param label 标签
 * @param modifier 修饰符
 * @param content 内容
 */
@Composable
fun DetailItemContent(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

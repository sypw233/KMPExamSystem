package ovo.sypw.kmp.examsystem.presentation.components.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.IndeterminateCheckBox
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 桌面端表格组件
 * 支持列定义、排序、行选择和虚拟滚动
 *
 * @param columns 列定义列表
 * @param data 数据列表
 * @param modifier 修饰符
 * @param selectedIds 选中的ID集合
 * @param onSelectionChange 选择变化回调
 * @param sortColumn 当前排序列
 * @param sortAscending 是否升序排序
 * @param onSort 排序变化回调
 * @param onRowClick 行点击回调
 * @param rowKey 行唯一键获取函数
 * @param emptyContent 空状态内容
 */
@Composable
fun <T> DesktopAdminTable(
    columns: List<TableColumn<T>>,
    data: List<T>,
    modifier: Modifier = Modifier,
    selectedIds: Set<Any>? = null,
    onSelectionChange: ((Set<Any>) -> Unit)? = null,
    sortColumn: TableColumn<T>? = null,
    sortAscending: Boolean = true,
    onSort: ((TableColumn<T>) -> Unit)? = null,
    onRowClick: ((T) -> Unit)? = null,
    rowKey: (T) -> Any = { it.hashCode() },
    emptyContent: @Composable () -> Unit = { Text("暂无数据") }
) {
    val listState = rememberLazyListState()
    val hasSelection = selectedIds != null && onSelectionChange != null

    // 全选状态
    val allSelected by remember(data, selectedIds) {
        derivedStateOf {
            data.isNotEmpty() && selectedIds?.containsAll(data.map(rowKey)) == true
        }
    }
    val partiallySelected by remember(data, selectedIds) {
        derivedStateOf {
            selectedIds?.isNotEmpty() == true && !allSelected
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 表头
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 全选框
                if (hasSelection) {
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { checked ->
                            if (checked) {
                                onSelectionChange(data.map(rowKey).toSet())
                            } else {
                                onSelectionChange(emptySet())
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                // 列标题
                columns.forEach { column ->
                    Row(
                        modifier = Modifier
                            .weight(column.weight)
                            .then(
                                if (onSort != null && column.sortable) {
                                    Modifier.clickable { onSort(column) }
                                } else Modifier
                            )
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = when (column.alignment) {
                            TextAlign.Center -> Arrangement.Center
                            TextAlign.End -> Arrangement.End
                            else -> Arrangement.Start
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = column.title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // 排序指示器
                        if (onSort != null && column.sortable && sortColumn == column) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = if (sortAscending) "升序" else "降序",
                                modifier = Modifier.padding(start = 4.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // 数据行
            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    emptyContent()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = listState
                ) {
                    items(
                        items = data,
                        key = rowKey
                    ) { item ->
                        val itemId = rowKey(item)
                        val isSelected = selectedIds?.contains(itemId) == true

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                    } else {
                                        Color.Transparent
                                    }
                                )
                                .then(
                                    if (onRowClick != null) {
                                        Modifier.clickable { onRowClick(item) }
                                    } else Modifier
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 选择框
                            if (hasSelection) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        val newSelected = if (checked) {
                                            selectedIds + itemId
                                        } else {
                                            selectedIds - itemId
                                        }
                                        onSelectionChange(newSelected)
                                    },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }

                            // 单元格内容
                            columns.forEach { column ->
                                Box(
                                    modifier = Modifier
                                        .weight(column.weight)
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = when (column.alignment) {
                                        TextAlign.Center -> Alignment.Center
                                        TextAlign.End -> Alignment.CenterEnd
                                        else -> Alignment.CenterStart
                                    }
                                ) {
                                    column.content(item)
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 表格列定义
 *
 * @param title 列标题
 * @param weight 列权重
 * @param sortable 是否可排序
 * @param alignment 文本对齐方式
 * @param content 单元格内容渲染
 */
data class TableColumn<T>(
    val title: String,
    val weight: Float = 1f,
    val sortable: Boolean = false,
    val alignment: TextAlign = TextAlign.Start,
    val content: @Composable (T) -> Unit
)

/**
 * 简化版桌面端表格
 * 不支持选择和排序，仅用于数据展示
 *
 * @param columns 列定义列表
 * @param data 数据列表
 * @param modifier 修饰符
 * @param rowKey 行唯一键获取函数
 * @param onRowClick 行点击回调
 * @param emptyContent 空状态内容
 */
@Composable
fun <T> DesktopAdminSimpleTable(
    columns: List<SimpleTableColumn<T>>,
    data: List<T>,
    modifier: Modifier = Modifier,
    rowKey: (T) -> Any = { it.hashCode() },
    onRowClick: ((T) -> Unit)? = null,
    emptyContent: @Composable () -> Unit = { Text("暂无数据") }
) {
    val listState = rememberLazyListState()

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 表头
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                columns.forEach { column ->
                    Text(
                        text = column.title,
                        modifier = Modifier
                            .weight(column.weight)
                            .padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = column.alignment,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider()

            // 数据行
            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    emptyContent()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = listState
                ) {
                    items(
                        items = data,
                        key = rowKey
                    ) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (onRowClick != null) {
                                        Modifier.clickable { onRowClick(item) }
                                    } else Modifier
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            columns.forEach { column ->
                                Box(
                                    modifier = Modifier
                                        .weight(column.weight)
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = when (column.alignment) {
                                        TextAlign.Center -> Alignment.Center
                                        TextAlign.End -> Alignment.CenterEnd
                                        else -> Alignment.CenterStart
                                    }
                                ) {
                                    column.content(item)
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 简化版表格列定义
 */
data class SimpleTableColumn<T>(
    val title: String,
    val weight: Float = 1f,
    val alignment: TextAlign = TextAlign.Start,
    val content: @Composable (T) -> Unit
)

package ovo.sypw.kmp.examsystem.presentation.components.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 桌面端筛选工具条组件
 * 支持搜索框、筛选条件和批量操作
 *
 * @param searchQuery 搜索关键词
 * @param onSearchQueryChange 搜索关键词变化回调
 * @param onSearch 搜索执行回调
 * @param onReset 重置筛选回调
 * @param modifier 修饰符
 * @param searchPlaceholder 搜索框占位文本
 * @param filters 筛选条件列表
 * @param batchActions 批量操作按钮区域
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesktopAdminFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    searchPlaceholder: String = "搜索...",
    filters: List<FilterItem>? = null,
    batchActions: @Composable () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 搜索行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 搜索框
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(searchPlaceholder) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "清除"
                                )
                            }
                        }
                    },
                    singleLine = true
                )

                // 搜索按钮
                androidx.compose.material3.Button(onClick = onSearch) {
                    Text("搜索")
                }

                // 重置按钮
                TextButton(onClick = onReset) {
                    Text("重置")
                }
            }

            // 筛选条件
            if (!filters.isNullOrEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        FilterChip(
                            selected = filter.selected,
                            onClick = filter.onClick,
                            label = { Text(filter.label) }
                        )
                    }
                }
            }

            // 批量操作
            if (batchActions != {}) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    batchActions()
                }
            }
        }
    }
}

/**
 * 筛选条件项
 */
data class FilterItem(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit
)

/**
 * 简化版桌面端筛选工具条
 * 仅包含搜索框和搜索/重置按钮
 *
 * @param searchQuery 搜索关键词
 * @param onSearchQueryChange 搜索关键词变化回调
 * @param onSearch 搜索执行回调
 * @param onReset 重置筛选回调
 * @param modifier 修饰符
 * @param searchPlaceholder 搜索框占位文本
 */
@Composable
fun DesktopAdminSimpleFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    searchPlaceholder: String = "搜索..."
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(searchPlaceholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "清除"
                            )
                        }
                    }
                },
                singleLine = true
            )

            androidx.compose.material3.Button(onClick = onSearch) {
                Text("搜索")
            }

            TextButton(onClick = onReset) {
                Text("重置")
            }
        }
    }
}

/**
 * 桌面端内联筛选工具条
 * 用于嵌入在表格头部的紧凑型筛选
 *
 * @param searchQuery 搜索关键词
 * @param onSearchQueryChange 搜索关键词变化回调
 * @param modifier 修饰符
 * @param searchPlaceholder 搜索框占位文本
 * @param actions 额外操作按钮
 */
@Composable
fun DesktopAdminInlineFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    searchPlaceholder: String = "搜索...",
    actions: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(searchPlaceholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "清除"
                        )
                    }
                }
            },
            singleLine = true
        )

        actions()
    }
}

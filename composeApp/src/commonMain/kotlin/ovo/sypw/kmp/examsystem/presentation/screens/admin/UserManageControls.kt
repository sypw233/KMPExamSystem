package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.data.dto.UserQueryParams
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

internal fun UserQueryParams.withUserStatusFilter(status: Int?): UserQueryParams {
    return copy(status = status, page = 0)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FilterBar(params: UserQueryParams, onParamsChange: (UserQueryParams) -> Unit) {
    var keyword by remember(params.keyword) { mutableStateOf(params.keyword ?: "") }
    val config = LocalResponsiveConfig.current

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) {
                        Modifier.widthIn(max = ResponsiveUtils.MaxWidths.FULL)
                    } else {
                        Modifier
                    }
                )
                .fillMaxWidth()
                .padding(horizontal = config.screenPadding, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索用户名/姓名/邮箱") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (keyword.isNotEmpty()) {
                        IconButton(onClick = {
                            keyword = ""
                            onParamsChange(params.copy(keyword = null, page = 0))
                        }) { Icon(Icons.Default.Close, null) }
                    }
                },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(null to "全部", "student" to "学生", "teacher" to "教师", "admin" to "管理员").forEach { (role, label) ->
                    FilterChip(
                        selected = params.role == role,
                        onClick = { onParamsChange(params.copy(role = role, page = 0)) },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(null to "全部状态", 1 to "启用", 0 to "禁用").forEach { (status, label) ->
                        FilterChip(
                            selected = params.status == status,
                            onClick = { onParamsChange(params.withUserStatusFilter(status)) },
                            label = { Text(label) }
                        )
                    }
                }
                Button(
                    onClick = { onParamsChange(params.copy(keyword = keyword.takeIf { it.isNotBlank() }, page = 0)) }
                ) { Text("搜索") }
            }
        }
    }
}

@Composable
internal fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    hasFirst: Boolean,
    hasLast: Boolean,
    onPageChange: (Int) -> Unit
) {
    val config = LocalResponsiveConfig.current
    val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = if (isDesktop) Arrangement.End else Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { onPageChange(currentPage - 1) }, enabled = hasFirst) {
            Text("上一页")
        }
        Text(
            "${currentPage + 1} / $totalPages",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        TextButton(onClick = { onPageChange(currentPage + 1) }, enabled = hasLast) {
            Text("下一页")
        }
    }
}

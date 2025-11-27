package ovo.sypw.kmp.examsystem.presentation.screens.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.ApiTestResponse
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ApiTestViewModel

/**
 * API测试界面
 * 提供API请求测试功能
 */
@Composable
fun ApiTestScreen() {
    val viewModel: ApiTestViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val currentTestResult by viewModel.currentTestResult.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "API测试工具",
                style = MaterialTheme.typography.headlineMedium
            )

            // 请求表单
            ApiRequestForm(
                uiState = uiState,
                onUrlChange = viewModel::updateUrl,
                onMethodChange = viewModel::updateMethod,
                onHeadersChange = { headers ->
                    viewModel.updateHeaders(parseHeaders(headers))
                },
                onBodyChange = viewModel::updateBody,
                onQueryParamsChange = { params ->
                    viewModel.updateQueryParams(parseQueryParams(params))
                },
                onTimeoutChange = { timeout ->
                    viewModel.updateTimeout(timeout.toLongOrNull() ?: 30000L)
                },
                onExecuteTest = viewModel::executeTest
            )

            // 测试结果
            currentTestResult?.let { result ->
                ApiResponseDisplay(
                    response = result,
                    onClear = viewModel::clearResult
                )
            }

            // 错误信息
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = viewModel::clearError) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "清除错误",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * API请求表单
 */
@Composable
private fun ApiRequestForm(
    uiState: ovo.sypw.kmp.examsystem.presentation.viewmodel.ApiTestUiState,
    onUrlChange: (String) -> Unit,
    onMethodChange: (String) -> Unit,
    onHeadersChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onQueryParamsChange: (String) -> Unit,
    onTimeoutChange: (String) -> Unit,
    onExecuteTest: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // URL输入
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // HTTP方法选择
                Box {
                    Button(
                        onClick = { expanded = true },
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text(uiState.method)
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        methods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    onMethodChange(method)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // URL输入框
                OutlinedTextField(
                    value = uiState.url,
                    onValueChange = onUrlChange,
                    label = { Text("请求URL") },
                    placeholder = { Text("https://api.example.com/endpoint") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 标签页
            val tabs = listOf("Headers", "Body", "Query Params", "Settings")
            var selectedTab by remember { mutableStateOf(0) }

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 标签页内容
            when (selectedTab) {
                0 -> {
                    // Headers
                    OutlinedTextField(
                        value = formatHeaders(uiState.headers),
                        onValueChange = onHeadersChange,
                        label = { Text("请求头") },
                        placeholder = { Text("Content-Type: application/json\nAuthorization: Bearer token") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )
                }

                1 -> {
                    // Body
                    OutlinedTextField(
                        value = uiState.body,
                        onValueChange = onBodyChange,
                        label = { Text("请求体") },
                        placeholder = { Text("{\n  \"key\": \"value\"\n}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )
                }

                2 -> {
                    // Query Params
                    OutlinedTextField(
                        value = formatQueryParams(uiState.queryParams),
                        onValueChange = onQueryParamsChange,
                        label = { Text("查询参数") },
                        placeholder = { Text("page=1\nsize=10") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )
                }

                3 -> {
                    // Settings
                    OutlinedTextField(
                        value = uiState.timeout.toString(),
                        onValueChange = onTimeoutChange,
                        label = { Text("超时时间 (毫秒)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 执行按钮
            Button(
                onClick = onExecuteTest,
                enabled = !uiState.isLoading && uiState.url.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("执行中...")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("发送请求")
                }
            }
        }
    }
}

/**
 * API响应显示
 */
@Composable
private fun ApiResponseDisplay(
    response: ApiTestResponse,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "响应结果",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "清除结果")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 状态信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "状态: ${response.statusCode} ${response.statusText}",
                    color = if (response.isSuccess)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
                Text(
                    text = "${response.responseTime}ms",
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 响应内容标签页
            val tabs = listOf("Body", "Headers")
            var selectedTab by remember { mutableStateOf(0) }

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 响应内容
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        Text(
                            text = response.body.ifEmpty { "(空响应体)" },
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    1 -> {
                        Text(
                            text = response.headers.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                                .ifEmpty { "(无响应头)" },
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * 解析请求头字符串
 */
private fun parseHeaders(headersText: String): Map<String, String> {
    if (headersText.isBlank()) return emptyMap()

    return try {
        headersText.lines()
            .filter { it.isNotBlank() && it.contains(":") }
            .associate { line ->
                val parts = line.split(":", limit = 2)
                parts[0].trim() to parts[1].trim()
            }
    } catch (e: Exception) {
        emptyMap()
    }
}

/**
 * 解析查询参数字符串
 */
private fun parseQueryParams(queryParamsText: String): Map<String, String> {
    if (queryParamsText.isBlank()) return emptyMap()

    return try {
        queryParamsText.lines()
            .filter { it.isNotBlank() && it.contains("=") }
            .associate { line ->
                val parts = line.split("=", limit = 2)
                parts[0].trim() to parts[1].trim()
            }
    } catch (e: Exception) {
        emptyMap()
    }
}

/**
 * 格式化请求头为字符串
 */
private fun formatHeaders(headers: Map<String, String>): String {
    return headers.entries.joinToString("\n") { "${it.key}: ${it.value}" }
}

/**
 * 格式化查询参数为字符串
 */
private fun formatQueryParams(queryParams: Map<String, String>): String {
    return queryParams.entries.joinToString("\n") { "${it.key}=${it.value}" }
}
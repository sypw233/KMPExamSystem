package ovo.sypw.kmp.examsystem.presentation.screens.test

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ovo.sypw.kmp.examsystem.utils.file.rememberFileUtils

/**
 * 图片测试界面
 * 用于测试跨平台图片选择和显示功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageTestScreen(
    modifier: Modifier = Modifier
) {
    val fileUtils = rememberFileUtils()
    val scope = rememberCoroutineScope()

    var selectedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Text(
            text = "图片测试工具",
            style = MaterialTheme.typography.headlineMedium
        )

        // 平台支持信息
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "平台信息",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "文件选择支持: ${if (fileUtils.isFileSelectionSupported()) "✅ 支持" else "❌ 不支持"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 选择图片按钮
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    try {
                        val bytes = fileUtils.selectImageBytes()
                        if (bytes != null) {
                            selectedImageBytes = bytes
                            val bitmap = fileUtils.bytesToImageBitmap(bytes)
                            selectedImageBitmap = bitmap
                            if (bitmap == null) {
                                errorMessage = "无法解析选择的图片文件"
                            }
                        } else {
                            errorMessage = "未选择图片或选择被取消"
                        }
                    } catch (e: Exception) {
                        errorMessage = "选择图片时发生错误: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading && fileUtils.isFileSelectionSupported(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("选择中...")
            } else {
                Text("选择图片")
            }
        }

        // 错误信息显示
        errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // 图片信息显示
        selectedImageBytes?.let { bytes ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "图片信息",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "文件大小: ${bytes.size} 字节",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "解析状态: ${if (selectedImageBitmap != null) "✅ 成功" else "❌ 失败"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    selectedImageBitmap?.let { bitmap ->
                        Text(
                            text = "图片尺寸: ${bitmap.width} × ${bitmap.height}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // 图片预览
        selectedImageBitmap?.let { bitmap ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "图片预览",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Image(
                        bitmap = bitmap,
                        contentDescription = "选择的图片",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // 清除按钮
        if (selectedImageBitmap != null || selectedImageBytes != null) {
            OutlinedButton(
                onClick = {
                    selectedImageBitmap = null
                    selectedImageBytes = null
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("清除图片")
            }
        }
    }
}
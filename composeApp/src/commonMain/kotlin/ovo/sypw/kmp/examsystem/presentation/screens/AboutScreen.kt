package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.AppInfo
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val config = LocalResponsiveConfig.current
    val uriHandler = LocalUriHandler.current
    val httpClient: HttpClient = koinInject()
    val scope = rememberCoroutineScope()
    var updateMessage by remember { mutableStateOf("尚未检查更新") }
    var checking by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(config.screenPadding),
            verticalArrangement = Arrangement.spacedBy(config.verticalSpacing)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(config.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("在线考试系统", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    InfoRow("VersionCode", AppInfo.versionCode.toString())
                    InfoRow("VersionName", AppInfo.versionName)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(config.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("项目仓库", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    RepoButton("客户端 GitHub", AppInfo.frontendRepo) { uriHandler.openUri(AppInfo.frontendRepo) }
                    RepoButton("后端 GitHub", AppInfo.backendRepo) { uriHandler.openUri(AppInfo.backendRepo) }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(config.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("更新检查", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(updateMessage, style = MaterialTheme.typography.bodyMedium)
                    Button(
                        enabled = !checking,
                        onClick = {
                            checking = true
                            updateMessage = "正在检查 GitHub Release..."
                            scope.launch {
                                updateMessage = runCatching {
                                    val text = httpClient
                                        .get("https://api.github.com/repos/sypw233/KMPExamSystem/releases/latest")
                                        .body<String>()
                                    val tag = Json.parseToJsonElement(text).jsonObject["tag_name"]?.jsonPrimitive?.content
                                    if (tag.isNullOrBlank()) {
                                        "未获取到最新 Release 信息"
                                    } else if (tag == AppInfo.versionName || tag == "v${AppInfo.versionName}") {
                                        "当前已是最新版本：$tag"
                                    } else {
                                        "发现最新 Release：$tag"
                                    }
                                }.getOrElse { "检查失败：${it.message ?: "网络异常"}" }
                                checking = false
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (checking) "检查中" else "检查更新")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RepoButton(text: String, url: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text, modifier = Modifier.weight(1f))
        Icon(Icons.Default.OpenInNew, contentDescription = url)
    }
}

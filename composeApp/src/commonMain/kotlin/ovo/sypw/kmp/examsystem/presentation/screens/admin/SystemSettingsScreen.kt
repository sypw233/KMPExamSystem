package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.dto.AiConfigResponse
import ovo.sypw.kmp.examsystem.presentation.components.management.ManagementPageHeader
import ovo.sypw.kmp.examsystem.presentation.components.management.ManagementPanel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.SystemSettingsActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.SystemSettingsUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.SystemSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemSettingsScreen(onBack: (() -> Unit)? = null) {
    val viewModel: SystemSettingsViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val config = LocalResponsiveConfig.current
    val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED

    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is SystemSettingsActionState.Success -> {
                snackbar.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            is SystemSettingsActionState.Error -> {
                snackbar.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            if (!isDesktop) {
                TopAppBar(
                    title = { Text("AI 配置管理") },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "返回"
                                )
                            }
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .then(if (isDesktop) Modifier.padding(config.screenPadding) else Modifier),
            verticalArrangement = if (isDesktop) Arrangement.spacedBy(16.dp) else Arrangement.Top
        ) {
            if (isDesktop) {
                ManagementPageHeader(
                    title = "系统设置",
                    subtitle = "集中维护 AI 服务配置和系统级参数，桌面端采用后台设置列表形式。"
                ) {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "刷新")
                    }
                }
            }

            ManagementPanel(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
            when (val state = uiState) {
                is SystemSettingsUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                is SystemSettingsUiState.Error -> Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { viewModel.refresh() }) {
                        Text("重试")
                    }
                }
                is SystemSettingsUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .then(if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = ResponsiveUtils.MaxWidths.SYSTEM_SETTINGS) else Modifier)
                            .fillMaxSize()
                            .padding(config.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.configs, key = { it.configKey }) { config ->
                            ConfigItem(
                                config = config,
                                onSave = { key, value -> viewModel.saveConfig(key, value) }
                            )
                        }
                    }
                }
            }
                }
            }
        }
    }
}

@Composable
private fun ConfigItem(
    config: AiConfigResponse,
    onSave: (String, String) -> Unit
) {
    var value by remember(config.configKey) { mutableStateOf(config.configValue) }
    val screenConfig = LocalResponsiveConfig.current
    val isDesktop = screenConfig.screenSize == ResponsiveUtils.ScreenSize.EXPANDED

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = MaterialTheme.shapes.large
    ) {
        if (isDesktop) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(0.9f)) {
                    Text(
                        text = config.configKey,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    config.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.weight(1.2f),
                    singleLine = value.length <= 60,
                    minLines = if (value.length > 60) 3 else 1
                )
                FilledTonalButton(
                    onClick = { onSave(config.configKey, value) },
                    enabled = value.isNotBlank()
                ) {
                    Text("保存")
                }
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = config.configKey,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                config.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = if (value.length > 60) 3 else 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = { onSave(config.configKey, value) },
                        enabled = value.isNotBlank()
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

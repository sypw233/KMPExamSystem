package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.presentation.components.management.ManagementPageHeader
import ovo.sypw.kmp.examsystem.presentation.components.management.ManagementPanel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.AiCustomProvider
import ovo.sypw.kmp.examsystem.presentation.viewmodel.AiModelMode
import ovo.sypw.kmp.examsystem.presentation.viewmodel.AiSettingsForm
import ovo.sypw.kmp.examsystem.presentation.viewmodel.DEFAULT_KIMI_PRESET
import ovo.sypw.kmp.examsystem.presentation.viewmodel.SystemSettingsActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.SystemSettingsUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.SystemSettingsViewModel
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemSettingsScreen(onBack: (() -> Unit)? = null) {
    val viewModel: SystemSettingsViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val config = LocalResponsiveConfig.current
    val isDesktop = config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED

    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is SystemSettingsActionState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            is SystemSettingsActionState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            if (!isDesktop) {
                TopAppBar(
                    title = { Text("AI 配置") },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .then(if (isDesktop) Modifier.padding(config.screenPadding) else Modifier)
        ) {
            if (isDesktop) {
                ManagementPageHeader(
                    title = "AI 配置",
                    subtitle = "默认模型固定为 Kimi，自定义模式支持预置兼容服务或手动 Base URL。"
                ) {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            }

            ManagementPanel(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is SystemSettingsUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is SystemSettingsUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(state.message, color = MaterialTheme.colorScheme.error)
                                Button(onClick = { viewModel.refresh() }) {
                                    Text("重试")
                                }
                            }
                        }
                    }

                    is SystemSettingsUiState.Success -> {
                        SystemSettingsForm(
                            initialForm = state.form,
                            isSaving = actionState is SystemSettingsActionState.Loading,
                            onSave = viewModel::saveSettings,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) {
                                        Modifier.widthIn(max = ResponsiveUtils.MaxWidths.SYSTEM_SETTINGS)
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemSettingsForm(
    initialForm: AiSettingsForm,
    isSaving: Boolean,
    onSave: (AiSettingsForm) -> Unit,
    modifier: Modifier = Modifier
) {
    var form by remember(initialForm) { mutableStateOf(initialForm) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSectionCard(
            title = "模型来源",
            description = "默认模型固定为 Kimi，不开放展示基础参数；切到自定义模型后，才允许选择兼容服务或手动填写 Base URL。"
        ) {
            ModeCards(selected = form.mode, onSelect = { form = form.copy(mode = it) })

            if (form.mode == AiModelMode.DEFAULT) {
                FixedPresetCard()
            } else {
                ProviderCards(
                    selected = form.customProvider,
                    onSelect = {
                        form = form.copy(
                            customProvider = it,
                            customBaseUrl = if (it == AiCustomProvider.CUSTOM_URL) form.customBaseUrl else "",
                            customModelName = form.customModelName.ifBlank { it.placeholderModel }
                        )
                    }
                )

                if (form.customProvider == AiCustomProvider.CUSTOM_URL) {
                    OutlinedTextField(
                        value = form.customBaseUrl,
                        onValueChange = { form = form.copy(customBaseUrl = it) },
                        label = { Text("Base URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    ReadOnlyValueRow("Base URL", form.customProvider.baseUrl.orEmpty())
                }

                OutlinedTextField(
                    value = form.customModelName,
                    onValueChange = { form = form.copy(customModelName = it) },
                    label = { Text("Model Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = form.customApiKey,
                    onValueChange = { form = form.copy(customApiKey = it) },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        SettingsSectionCard(
            title = "评分参数",
            description = "系统提示词直接影响主观题判题口径；其余参数决定输出长度和稳定性。"
        ) {
            OutlinedTextField(
                value = form.systemPrompt,
                onValueChange = { form = form.copy(systemPrompt = it) },
                label = { Text("系统提示词") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 6
            )
            OutlinedTextField(
                value = form.temperature,
                onValueChange = { form = form.copy(temperature = it) },
                label = { Text("Temperature") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = form.maxTokens,
                onValueChange = { form = form.copy(maxTokens = it) },
                label = { Text("Max Tokens") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = form.batchConcurrency,
                onValueChange = { form = form.copy(batchConcurrency = it) },
                label = { Text("并发数") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { form = initialForm }, enabled = !isSaving) {
                Text("重置")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onSave(form) }, enabled = !isSaving && form.isValid()) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("保存配置")
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun ModeCards(
    selected: AiModelMode,
    onSelect: (AiModelMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AiModelMode.values().forEach { mode ->
            ActionCard(
                title = mode.label,
                description = if (mode == AiModelMode.DEFAULT) "固定 Kimi，不显示接口细节" else "支持兼容服务和自定义 URL",
                icon = if (mode == AiModelMode.DEFAULT) Icons.Default.AutoAwesome else Icons.Default.Code,
                selected = mode == selected,
                onClick = { onSelect(mode) }
            )
        }
    }
}

@Composable
private fun FixedPresetCard() {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("系统默认 Kimi 模型", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "后端固定维护，无需填写 Base URL、Model Name 或 API Key",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = DEFAULT_KIMI_PRESET.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ProviderCards(
    selected: AiCustomProvider,
    onSelect: (AiCustomProvider) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AiCustomProvider.values().forEach { provider ->
            ActionCard(
                title = provider.label,
                description = if (provider == AiCustomProvider.CUSTOM_URL) "手动填写接口地址" else provider.placeholderModel,
                icon = when (provider) {
                    AiCustomProvider.DEEPSEEK -> Icons.Default.Psychology
                    AiCustomProvider.CUSTOM_URL -> Icons.Default.Code
                    else -> Icons.Default.AutoAwesome
                },
                selected = provider == selected,
                onClick = { onSelect(provider) }
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ReadOnlyValueRow(title: String, value: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun AiSettingsForm.isValid(): Boolean {
    if (systemPrompt.isBlank() || temperature.isBlank() || maxTokens.isBlank() || batchConcurrency.isBlank()) return false
    return if (mode == AiModelMode.CUSTOM) {
        customModelName.isNotBlank() &&
            customApiKey.isNotBlank() &&
            effectiveCustomBaseUrl.isNotBlank()
    } else {
        true
    }
}

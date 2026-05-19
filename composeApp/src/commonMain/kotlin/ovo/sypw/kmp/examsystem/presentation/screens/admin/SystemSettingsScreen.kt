package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import ovo.sypw.kmp.examsystem.presentation.viewmodel.AiCustomProvider
import ovo.sypw.kmp.examsystem.presentation.viewmodel.AiModelMode
import ovo.sypw.kmp.examsystem.presentation.viewmodel.AiSettingsForm
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
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(config.screenPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is SystemSettingsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 48.dp))
                }
                is SystemSettingsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = ResponsiveUtils.MaxWidths.NARROW)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = viewModel::refresh) { Text("重试") }
                    }
                }
                is SystemSettingsUiState.Success -> {
                    SystemSettingsForm(
                        initialForm = state.form,
                        isSaving = actionState is SystemSettingsActionState.Loading,
                        onSave = viewModel::saveSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = ResponsiveUtils.MaxWidths.SYSTEM_SETTINGS)
                    )
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
    var showPromptEditor by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSectionCard(title = "模型来源", description = null) {
                ModeCards(
                    selected = form.mode,
                    onSelect = {
                        form = form.copy(
                            mode = it,
                            customProvider = if (it == AiModelMode.CUSTOM) AiCustomProvider.CUSTOM_URL else form.customProvider
                        )
                    }
                )

                AnimatedVisibility(visible = form.mode == AiModelMode.CUSTOM) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "兼容 OpenAI Compatible 格式",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = form.customBaseUrl.ifBlank { form.customProvider.baseUrl.orEmpty() },
                            onValueChange = { form = form.copy(customBaseUrl = it, customProvider = AiCustomProvider.CUSTOM_URL) },
                            label = { Text("Base URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = form.customModelName,
                            onValueChange = { form = form.copy(customModelName = it) },
                            label = { Text("模型名称") },
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
            }
        }

        item {
            SettingsSectionCard(
                title = "评分参数",
                description = "系统提示词影响主观题判题口径；数值参数控制输出长度和稳定性。"
            ) {
                PromptEditorCard(
                    prompt = form.systemPrompt,
                    expanded = showPromptEditor,
                    onExpandedChange = { showPromptEditor = it },
                    onPromptChange = { form = form.copy(systemPrompt = it) }
                )
                OutlinedTextField(
                    value = form.temperature,
                    onValueChange = { form = form.copy(temperature = it) },
                    label = { Text("温度") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = form.maxTokens,
                    onValueChange = { form = form.copy(maxTokens = it) },
                    label = { Text("最大输出 Token") },
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
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
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
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("保存配置")
                }
            }
        }
    }
}

@Composable
private fun PromptEditorCard(
    prompt: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPromptChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("系统提示词", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = prompt.lineSequence().firstOrNull()?.take(60).orEmpty().ifBlank { "未配置" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedButton(onClick = { onExpandedChange(!expanded) }) {
                    Text(if (expanded) "收起" else "编辑")
                }
            }

            AnimatedVisibility(visible = expanded) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    label = { Text("系统提示词") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    maxLines = 8
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    description: String?,
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
            if (!description.isNullOrBlank()) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
        listOf(AiModelMode.DEFAULT, AiModelMode.CUSTOM).forEach { mode ->
            ActionCard(
                title = if (mode == AiModelMode.DEFAULT) "默认模型" else "自定义模型",
                description = if (mode == AiModelMode.DEFAULT) {
                    "采用针对智能判题微调的QWEN3模型"
                } else {
                    "兼容 OpenAI Compatible 格式"
                },
                icon = if (mode == AiModelMode.DEFAULT) Icons.Default.AutoAwesome else Icons.Default.Code,
                selected = mode == selected,
                onClick = { onSelect(mode) }
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

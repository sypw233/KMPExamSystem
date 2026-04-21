package ovo.sypw.kmp.examsystem.presentation.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.presentation.viewmodel.SystemSettingsActionState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.SystemSettingsUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.SystemSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemSettingsScreen() {
    val viewModel: SystemSettingsViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var provider by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is SystemSettingsUiState.Success && state.configs.isNotEmpty()) {
            val config = state.configs.first()
            provider = config.provider
            model = config.model
            baseUrl = config.baseUrl.orEmpty()
        }
    }

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
        topBar = { TopAppBar(title = { Text("System Settings") }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (uiState) {
                is SystemSettingsUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                is SystemSettingsUiState.Error -> Text(
                    text = (uiState as SystemSettingsUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp)
                )
                is SystemSettingsUiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().widthIn(max = 760.dp).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = provider,
                            onValueChange = { provider = it },
                            label = { Text("Provider") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = model,
                            onValueChange = { model = it },
                            label = { Text("Model") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("API Key") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = baseUrl,
                            onValueChange = { baseUrl = it },
                            label = { Text("Base URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            label = { Text("Prompt template") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { viewModel.saveConfig(provider, apiKey, model, baseUrl, prompt) },
                            enabled = provider.isNotBlank() && model.isNotBlank() && apiKey.isNotBlank()
                        ) {
                            Text("Save AI config")
                        }
                    }
                }
            }
        }
    }
}

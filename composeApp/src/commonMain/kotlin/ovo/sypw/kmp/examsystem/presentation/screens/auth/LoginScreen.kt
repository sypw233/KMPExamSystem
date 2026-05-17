package ovo.sypw.kmp.examsystem.presentation.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.presentation.viewmodel.LoginUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.LoginViewModel
import ovo.sypw.kmp.examsystem.utils.DialogManager
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = koinInject(),
    dialogManager: DialogManager = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var usernameInteracted by remember { mutableStateOf(false) }
    var passwordInteracted by remember { mutableStateOf(false) }
    val config = LocalResponsiveConfig.current

    LaunchedEffect(Unit) {
        snapshotFlow { uiState }.collect { state ->
            when (state) {
                is LoginUiState.Success -> {
                    onLoginSuccess()
                    viewModel.resetState()
                }

                is LoginUiState.Error -> {
                    dialogManager.showError(title = "登录失败", message = state.message)
                    viewModel.resetState()
                }

                else -> Unit
            }
        }
    }

    val quickAccounts = remember {
        listOf(
            Triple("管理员", "admin", "123456"),
            Triple("教师", "teacher1", "123456"),
            Triple("学生", "student", "123456")
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 1040.dp)
                        .fillMaxWidth()
                        .padding(config.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AuthHeroPanel(modifier = Modifier.weight(1f))
                    LoginFormCard(
                        modifier = Modifier.weight(0.82f),
                        username = username,
                        password = password,
                        passwordVisible = passwordVisible,
                        usernameInteracted = usernameInteracted,
                        passwordInteracted = passwordInteracted,
                        uiState = uiState,
                        quickAccounts = quickAccounts,
                        onUsernameChange = viewModel::updateUsername,
                        onPasswordChange = viewModel::updatePassword,
                        onUsernameInteracted = { usernameInteracted = true },
                        onPasswordInteracted = { passwordInteracted = true },
                        onPasswordVisibleChange = { passwordVisible = it },
                        onQuickFill = { quickUsername, quickPassword ->
                            viewModel.updateUsername(quickUsername)
                            viewModel.updatePassword(quickPassword)
                            usernameInteracted = true
                            passwordInteracted = true
                        },
                        onLogin = {
                            usernameInteracted = true
                            passwordInteracted = true
                            viewModel.login()
                        },
                        onNavigateToRegister = onNavigateToRegister
                    )
                }
            } else {
                LoginFormCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(config.screenPadding),
                    username = username,
                    password = password,
                    passwordVisible = passwordVisible,
                    usernameInteracted = usernameInteracted,
                    passwordInteracted = passwordInteracted,
                    uiState = uiState,
                    quickAccounts = quickAccounts,
                    onUsernameChange = viewModel::updateUsername,
                    onPasswordChange = viewModel::updatePassword,
                    onUsernameInteracted = { usernameInteracted = true },
                    onPasswordInteracted = { passwordInteracted = true },
                    onPasswordVisibleChange = { passwordVisible = it },
                    onQuickFill = { quickUsername, quickPassword ->
                        viewModel.updateUsername(quickUsername)
                        viewModel.updatePassword(quickPassword)
                        usernameInteracted = true
                        passwordInteracted = true
                    },
                    onLogin = {
                        usernameInteracted = true
                        passwordInteracted = true
                        viewModel.login()
                    },
                    onNavigateToRegister = onNavigateToRegister
                )
            }
        }
    }
}

@Composable
private fun AuthHeroPanel(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.heightIn(min = 520.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "在线考试系统",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "支持一键填充测试账号，便于快速验证管理员、教师和学生端流程。",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                )
            }
        }
    }
}

@Composable
private fun LoginFormCard(
    modifier: Modifier,
    username: String,
    password: String,
    passwordVisible: Boolean,
    usernameInteracted: Boolean,
    passwordInteracted: Boolean,
    uiState: LoginUiState,
    quickAccounts: List<Triple<String, String, String>>,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onUsernameInteracted: () -> Unit,
    onPasswordInteracted: () -> Unit,
    onPasswordVisibleChange: (Boolean) -> Unit,
    onQuickFill: (String, String) -> Unit,
    onLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.widthIn(max = ResponsiveUtils.MaxWidths.FORM),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "欢迎登录",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "请输入账号密码继续使用",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickAccounts.forEach { (label, quickUsername, quickPassword) ->
                    SuggestionChip(
                        onClick = { onQuickFill(quickUsername, quickPassword) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("用户名") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onUsernameInteracted() },
                singleLine = true,
                enabled = uiState !is LoginUiState.Loading,
                isError = usernameInteracted && username.isBlank(),
                supportingText = if (usernameInteracted && username.isBlank()) {
                    { Text("请输入用户名") }
                } else {
                    null
                },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("密码") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) onPasswordInteracted() },
                singleLine = true,
                enabled = uiState !is LoginUiState.Loading,
                isError = passwordInteracted && password.isBlank(),
                supportingText = if (passwordInteracted && password.isBlank()) {
                    { Text("请输入密码") }
                } else {
                    null
                },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onLogin() }),
                trailingIcon = {
                    FilledIconButton(onClick = { onPasswordVisibleChange(!passwordVisible) }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState !is LoginUiState.Loading,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                if (uiState is LoginUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("登录", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text("注册账号", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

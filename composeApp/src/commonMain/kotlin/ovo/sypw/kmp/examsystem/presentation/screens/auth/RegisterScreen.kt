package ovo.sypw.kmp.examsystem.presentation.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.presentation.viewmodel.RegisterUiState
import ovo.sypw.kmp.examsystem.presentation.viewmodel.RegisterViewModel
import ovo.sypw.kmp.examsystem.utils.DialogManager

/**
 * 注册界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = koinInject(),
    dialogManager: DialogManager = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val realName by viewModel.realName.collectAsState()
    val email by viewModel.email.collectAsState()
    val role by viewModel.role.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var usernameInteracted by remember { mutableStateOf(false) }
    var passwordInteracted by remember { mutableStateOf(false) }
    var confirmPasswordInteracted by remember { mutableStateOf(false) }
    var realNameInteracted by remember { mutableStateOf(false) }
    var emailInteracted by remember { mutableStateOf(false) }
    val config = LocalResponsiveConfig.current

    // 监听注册成功和错误
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is RegisterUiState.Success -> {
                dialogManager.showSuccess(
                    title = "注册成功",
                    message = "欢迎加入在线考试系统！",
                    onConfirm = {
                        onRegisterSuccess()
                        viewModel.resetState()
                    }
                )
            }
            is RegisterUiState.Error -> {
                // 根据错误类型显示不同的弹窗
                if (state.message.contains("已存在") || state.message.contains("格式")) {
                    dialogManager.showWarning(
                        title = "注意",
                        message = state.message
                    )
                } else {
                    dialogManager.showError(
                        title = "注册失败",
                        message = state.message
                    )
                }
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // 注册卡片容器 - 限制最大宽度适配桌面端
            Card(
                modifier = Modifier
                    .then(if (config.screenSize == ResponsiveUtils.ScreenSize.EXPANDED) Modifier.widthIn(max = ResponsiveUtils.MaxWidths.FORM) else Modifier)
                    .fillMaxWidth()
                    .padding(config.screenPadding)
                    .verticalScroll(rememberScrollState()),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "创建新账号",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "填写以下信息完成注册",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )

                    // 用户名
                    OutlinedTextField(
                        value = username,
                        onValueChange = { viewModel.updateUsername(it) },
                        label = { Text("用户名") },
                        modifier = Modifier.fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) usernameInteracted = true },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        isError = usernameInteracted && username.length < 3,
                        supportingText = if (usernameInteracted && username.length < 3) {
                            { Text("用户名至少需要3个字符") }
                        } else null,
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 密码
                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.updatePassword(it) },
                        label = { Text("密码") },
                        modifier = Modifier.fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) passwordInteracted = true },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        isError = passwordInteracted && password.length < 6,
                        supportingText = if (passwordInteracted && password.length < 6) {
                            { Text("密码至少需要6个字符") }
                        } else null,
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 确认密码
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("确认密码") },
                        modifier = Modifier.fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) confirmPasswordInteracted = true },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        isError = confirmPasswordInteracted && confirmPassword != password,
                        supportingText = if (confirmPasswordInteracted && confirmPassword != password) {
                            { Text("两次输入的密码不一致") }
                        } else null,
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "隐藏密码" else "显示密码"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 真实姓名
                    OutlinedTextField(
                        value = realName,
                        onValueChange = { viewModel.updateRealName(it) },
                        label = { Text("真实姓名") },
                        modifier = Modifier.fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) realNameInteracted = true },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        isError = realNameInteracted && realName.isBlank(),
                        supportingText = if (realNameInteracted && realName.isBlank()) {
                            { Text("请输入真实姓名") }
                        } else null,
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 邮箱
                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.updateEmail(it) },
                        label = { Text("邮箱（选填）") },
                        modifier = Modifier.fillMaxWidth()
                            .onFocusChanged { if (!it.isFocused) emailInteracted = true },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        isError = emailInteracted && email.isNotBlank() && !email.contains("@"),
                        supportingText = if (emailInteracted && email.isNotBlank() && !email.contains("@")) {
                            { Text("请输入有效的邮箱地址") }
                        } else null,
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.register() }
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 角色选择
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "选择角色",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FilterChip(
                                selected = role == "student",
                                onClick = { viewModel.updateRole("student") },
                                label = { Text("学生") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = if (role == "student") {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
        
                            FilterChip(
                                selected = role == "teacher",
                                onClick = { viewModel.updateRole("teacher") },
                                label = { Text("教师") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = if (role == "teacher") {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 注册按钮
                    Button(
                        onClick = {
                            usernameInteracted = true
                            passwordInteracted = true
                            confirmPasswordInteracted = true
                            realNameInteracted = true
                            emailInteracted = true
                            viewModel.register()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = uiState !is RegisterUiState.Loading,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (uiState is RegisterUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                "注册",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 登录链接
                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            "已有账号？立即登录",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

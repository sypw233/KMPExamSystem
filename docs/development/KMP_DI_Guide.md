# KMP 依赖注入指南

## 项目使用的库

本项目使用以下依赖注入和 ViewModel 库：
- **Koin** - 依赖注入框架
- **kmp-viewmodel** (`com.hoc081098.kmp.viewmodel`) - KMP ViewModel 库

## 正确的依赖注入模式

### 1. ViewModel 定义

在 `commonMain` 中定义 ViewModel 时，使用 `kmp-viewmodel` 库：

```kotlin
package ovo.sypw.kmp.examsystem.presentation.viewmodel

import com.hoc081098.kmp.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun login() {
        viewModelScope.launch {
            // 业务逻辑
        }
    }
}
```

**要点：**
- ✅ 继承 `com.hoc081098.kmp.viewmodel.ViewModel`
- ✅ 使用 `viewModelScope` 启动协程
- ✅ 构造函数注入依赖

### 2. Koin 模块配置

**在 Koin 模块中，ViewModel 使用 `single` 而不是 `viewModel`：**

```kotlin
package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.presentation.viewmodel.LoginViewModel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.RegisterViewModel

val authModule = module {
    // API
    single { AuthApi() }
    
    // Repository
    single { AuthRepository(get(), get()) }
    
    // ViewModel - 使用 single，不使用 viewModel DSL
    single { LoginViewModel(get()) }
    single { RegisterViewModel(get()) }
}
```

**要点：**
- ✅ ViewModel 使用 `single { }` 定义
- ❌ **不要使用** `viewModel { }` DSL
- ✅ 依赖通过 [get()](file:///f:/androidProject/KMPExamSystem/composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/data/api/BaseApiService.kt#34-53) 自动注入

### 3. Composable 中使用 ViewModel

**在 Compose 界面中使用 `koinInject()` 获取 ViewModel：**

```kotlin
package ovo.sypw.kmp.examsystem.presentation.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val username by viewModel.username.collectAsState()
    
    // UI 代码...
}
```

**要点：**
- ✅ 使用 `koinInject()` 获取 ViewModel
- ❌ **不要使用** `koinViewModel()`
- ✅ 使用 `collectAsState()` 观察 StateFlow

### 4. 完整的依赖链示例

#### Koin 模块结构

```kotlin
// StorageModule.kt - 存储层
val storageModule = module {
    single<LocalStorage> { createLocalStorage() }
    single<TokenStorage> { TokenStorageImpl(get()) }
}

// AuthModule.kt - 认证模块
val authModule = module {
    single { AuthApi() }
    single { AuthRepository(get(), get()) }
    single { LoginViewModel(get()) }
    single { RegisterViewModel(get()) }
}

// AppModule.kt - 主模块
val appModule = module {
    includes(storageModule)
    includes(authModule)
    includes(viewModelModule)
    includes(apiTestModule)
}
```

#### 依赖注入流程

```
LoginScreen
    ↓ koinInject()
LoginViewModel(authRepository)
    ↓ get()
AuthRepository(authApi, tokenStorage)
    ↓ get()
AuthApi() + TokenStorageImpl(localStorage)
    ↓ get()
LocalStorage (平台特定实现)
```

## 常见错误和解决方案

### ❌ 错误 1: 使用错误的 ViewModel 库

```kotlin
// 错误
import androidx.lifecycle.ViewModel
import org.rickclephas.kmp.observableviewmodel.ViewModel
```

```kotlin
// 正确
import com.hoc081098.kmp.viewmodel.ViewModel
```

### ❌ 错误 2: 在 Koin 中使用 viewModel DSL

```kotlin
// 错误
val authModule = module {
    viewModel { LoginViewModel(get()) }  // 不支持
}
```

```kotlin
// 正确
val authModule = module {
    single { LoginViewModel(get()) }
}
```

### ❌ 错误 3: 在 Compose 中使用 koinViewModel()

```kotlin
// 错误
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel()  // 不支持
)
```

```kotlin
// 正确
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinInject()
)
```

### ❌ 错误 4: LocalStorage 实例化

```kotlin
// 错误 - expect class 没有构造函数
val storageModule = module {
    single { LocalStorage() }  // 编译错误
}
```

```kotlin
// 正确 - 使用平台特定的工厂函数
expect fun createLocalStorage(): LocalStorage

val storageModule = module {
    single<LocalStorage> { createLocalStorage() }
}
```

## 最佳实践

### 1. 模块化组织

- 按功能模块划分 Koin 模块
- 每个模块只包含相关的依赖
- 使用 `includes()` 组合模块

### 2. 依赖作用域

- `single` - 单例，整个应用共享一个实例
- `factory` - 每次请求创建新实例
- ViewModel 通常使用 `single`

### 3. 依赖注入顺序

1. 基础设施层（Storage, Network）
2. API 层
3. Repository 层
4. ViewModel 层

### 4. 测试友好

```kotlin
// 在测试中可以轻松替换依赖
val testModule = module {
    single<AuthRepository> { MockAuthRepository() }
}
```

## 总结

**KMP 项目依赖注入核心原则：**

1. ✅ ViewModel 继承 `com.hoc081098.kmp.viewmodel.ViewModel`
2. ✅ Koin 模块中 ViewModel 使用 `single { }`
3. ✅ Composable 中使用 `koinInject()` 获取实例
4. ✅ expect/actual 类使用工厂函数实例化
5. ✅ 保持模块化和清晰的依赖层次

遵循这些模式可以确保代码在所有 KMP 平台（Android、iOS、Desktop）上正常工作。

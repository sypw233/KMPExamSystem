# KMP Exam System - AI 项目开发规范 (AI Project Specification)

本文档旨在为后续参与开发的 AI Agent 提供统一的项目规范、架构指导和代码标准。请在执行任务前仔细阅读现有规则。

## 1. 核心原则 (Core Principles)

*   **语言**: 所有文档、代码注释、UI 文本必须使用 **中文 (Simplified Chinese)**。
*   **注释风格**: 方法内部注释不使用序号，仅使用简洁的语句。
*   **标点符号**: 代码和注释中全部使用 **半角符号** (如 `,` `.` `(` `)` 等)，严禁使用全角符号。
*   **平台支持**: 项目基于 Kotlin Multiplatform (KMP)，主要目标平台为 **Android**, **iOS**, **Desktop (JVM)** 和 **Web (Wasm)**。代码必须保持跨平台兼容性。

## 2. 技术栈 (Tech Stack)

*   **核心语言**: Kotlin
*   **UI 框架**: Compose Multiplatform (Material Design 3)
*   **依赖注入**: Koin
*   **网络请求**: Ktor Client
*   **异步处理**: Kotlin Coroutines & Flow
*   **架构模式**: MVVM (Model-View-ViewModel) + Clean Architecture (Data/Domain/Presentation)

## 3. 项目架构 (Project Architecture)

代码位于 `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/` 目录下，分层如下：

### 3.1 目录结构
*   **`data/`**: 数据层
    *   `api/`: Ktor API 服务定义 (e.g., `AuthApi`)。
    *   `dto/`: 数据传输对象 (Request/Response)，使用 `@Serializable`。
    *   `repository/`: 仓库实现，负责协调 API 和本地存储 (e.g., `AuthRepository`)。
    *   `storage/`: 本地数据存储 (e.g., `TokenStorage`)，通常使用 Settings 或键值对存储。
*   **`domain/`**: 领域层
    *   定义业务逻辑状态 (e.g., `AuthState`) 和 纯 Kotlin 业务模型。
*   **`presentation/`**: 表现层
    *   `components/`: 通用 UI 组件 (e.g., `GlobalDialog`)。
    *   `screens/`: 具体的业务页面 (e.g., `LoginScreen`, `HomeScreen`)。
    *   `viewmodel/`: 视图模型，继承自 `ViewModel`，管理 UI 状态。
    *   `navigation/`: 导航管理逻辑 (`NavigationManager`)。
    *   `theme/`: UI 主题定义 (`Color.kt`, `Theme.kt`, `Type.kt`)。
*   **`di/`**: Koin 模块定义 (`AppModule`, `AuthModule`)。
*   **`utils/`**: 工具类 (e.g., `Logger`, `DialogManager`)。

## 4. UI 设计规范 (UI Design Guidelines)

遵循 **UI Design System (`ui_design_system.md`)**：

*   **设计语言**: Material Design 3 (Material You)。
*   **主题色**: **Light Blue** (深浅色模式适配)。
    *   Primary: `#006495` (Light), `#8DCDFF` (Dark)
*   **桌面端适配 (Desktop Optimization)**:
    *   **最大宽度限制**: 在大屏设备上，内容区域应限制最大宽度，防止 UI 过度拉伸。
    *   **实现方式**: 使用 `Box(contentAlignment = Alignment.Center)` 包裹内容，并对内容容器应用 `Modifier.widthIn(max = 400.dp)` (登录/注册) 或 `max = 600.dp/800.dp` (内容页)。
    *   **禁止全屏拉伸**: 输入框、卡片等元素不应在大屏上占满整个屏幕宽度。
*   **组件使用**:
    *   使用 `MaterialTheme.colorScheme` 获取颜色。
    *   使用 `MaterialTheme.typography` 获取字体样式。
    *   不显示品牌 Logo 图片，仅使用文字标题。

## 5. 编码规范 (Coding Standards)

### 5.1 依赖注入 (DI)
使用 Koin 进行依赖注入。
*   **ViewModel 定义**: 继承自 `com.hoc081098.kmp.viewmodel.ViewModel`。
*   **Module 定义**: 使用 `single { }` 定义 ViewModel，**不要**使用 `viewModel { }` DSL (KMP 环境下可能有兼容性问题)。
    ```kotlin
    val appModule = module {
        single { AuthRepository(get()) }
        single { LoginViewModel(get()) } // 正确
    }
    ```
*   **注入方式**: 在 Composable 中使用 `koinInject()` 获取实例。
    ```kotlin
    val viewModel: LoginViewModel = koinInject() // 正确
    // val viewModel: LoginViewModel = koinViewModel() // 避免使用
    ```

### 5.2 网络请求
*   **API 定义**: 位于 `data/api/`，直接使用 Ktor 的 `HttpClient`。
*   **异常处理**: 必须捕获网络异常并在 Repository 层转换为 Result 或特定的 Error State。
*   **基础 URL**: 默认开发环境 `http://localhost:8080` (注意 Android 模拟器需使用 `10.0.2.2`)。

### 5.3 状态管理
*   **StateFlow**: ViewModel 使用 `MutableStateFlow` 暴露 UI 状态。
*   **UI State**: 定义 Sealed Interface (e.g., `LoginUiState`) 包含 `Idle`, `Loading`, `Success`, `Error` 状态。
*   **收集状态**: Composable中使用 `collectAsState()` 收集状态。

## 6. 开发工作流 (Development Workflow)

当添加新功能时，请遵循以下步骤：
1.  **定义 DTO**: 在 `data/dto` 中创建 Request/Response 数据类。
2.  **实现 API**: 在 `data/api` 中添加网络请求方法。
3.  **实现 Repository**: 在 `data/repository` 中封装业务逻辑。
4.  **实现 ViewModel**: 在 `presentation/viewmodel` 中管理状态和调用 Repository。
5.  **注册 DI**: 在 `di/AppModule.kt` 或特定模块中注册上述类。
6.  **实现 UI**: 在 `presentation/screens` 中创建 Composable，应用 **UI 设计规范** 和 **桌面端适配**。
7.  **配置导航**: 在 `presentation/navigation` 中添加路由。

## 7. 重要文件索引 (File Index)

*   `App.kt`: 应用入口，包含 `AppTheme` 和全局弹窗容器。
*   `ui_design_system.md`: 详细 UI 设计规范。
*   `KMP_DI_Guide.md`: 详细依赖注入指南。
*   `data/api/BaseApiService.kt`: Ktor HTTP Client 配置单例。

此规范为动态文档，根据项目演进可随时更新。

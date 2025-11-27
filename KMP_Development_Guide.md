# KMP 客户端开发文档

本文档旨在指导开发者进行 KMP (Kotlin Multiplatform) 客户端的开发，特别是如何与现有的考试系统后端进行集成。

## 1. 项目概览

本项目是一个基于 Kotlin Multiplatform (KMP) 和 Compose Multiplatform 的跨平台考试系统客户端，支持 Android、iOS 和 Desktop 平台。

### 技术栈
-   **语言**: Kotlin
-   **UI 框架**: Compose Multiplatform
-   **网络请求**: Ktor Client
-   **依赖注入**: Koin
-   **异步编程**: Kotlin Coroutines
-   **图片加载**: Coil
-   **导航**: 自定义导航 / Voyager (视具体实现而定)

## 2. 项目结构

核心代码位于 `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem`，主要包含以下模块：

-   **data**: 数据层，负责网络请求、本地存储和数据转换。
    -   `api`: 定义 Ktor API 服务。
    -   `dto`: 数据传输对象 (Data Transfer Objects)，与后端接口对应。
    -   `repository`: 仓库层，屏蔽数据来源。
-   **domain**: 领域层，包含业务逻辑和用例 (UseCase)。
-   **presentation**: 表现层，包含 UI 界面和 ViewModel。
    -   `screens`: 各个功能页面。
    -   `viewmodel`: 页面状态管理。
-   **di**: Koin 依赖注入模块配置。

## 3. 环境搭建与运行

### 前置要求
-   JDK 17+
-   Android Studio (最新版)
-   Xcode (仅 iOS 开发需要)

### 运行命令
-   **Android**: `./gradlew :composeApp:assembleDebug` 或在 Android Studio 中运行 `composeApp`。
-   **Desktop**: `./gradlew :composeApp:run`。
-   **iOS**: 在 Xcode 中打开 `iosApp/iosApp.xcodeproj` 运行。

## 4. 后端集成指南

### 4.1 配置

API 基础地址通常在 `HttpClientConfig.kt` 或类似的配置文件中定义：

```kotlin
// 示例配置
const val BASE_URL = "http://localhost:8080/api" // 本地调试
// const val BASE_URL = "https://api.example.com/api" // 生产环境
```

### 4.2 数据模型 (DTO)

客户端的 DTO 需要与后端保持一致。参考 `backDto` 目录下的定义。

#### 通用响应结构
```kotlin
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null
)
```

#### 关键 DTO 示例

**登录请求 (LoginRequest)**
```kotlin
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)
```

**考试信息 (ExamResponse)**
```kotlin
@Serializable
data class ExamResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val courseId: Long,
    val courseName: String,
    val startTime: String, // ISO 8601 格式
    val endTime: String,
    val duration: Int?,
    val totalScore: Int,
    val status: Int, // 0:草稿, 1:已发布, 2:已结束
    val strictMode: Boolean,
    val allowedPlatforms: String? // "desktop", "mobile", "both"
)
```

### 4.3 API 服务实现

使用 Ktor 实现 API 调用。

```kotlin
class AuthApi(private val client: HttpClient) {
    suspend fun login(request: LoginRequest): ApiResponse<AuthResponse> {
        return client.post("/auth/login") {
            setBody(request)
        }.body()
    }
}
```

### 4.4 认证流程

1.  **登录**: 用户输入账号密码，调用 `login` 接口。
2.  **保存 Token**: 登录成功后，将 `accessToken` 和 `refreshToken` 保存到本地存储 (如 DataStore 或 Settings)。
3.  **请求拦截**: 在 Ktor 的 `install(Auth)` 或自定义拦截器中，为每个请求添加 `Authorization: Bearer <token>` 头。
4.  **Token 刷新**: 监听 401 错误，自动调用刷新接口更新 Token。

## 5. 开发流程建议

1.  **定义 DTO**: 根据后端接口文档 (`frontend_integration_guide.md`) 和 `backDto`，在 `commonMain` 中创建对应的 Kotlin `data class`，并添加 `@Serializable` 注解。
2.  **创建 API**: 在 `data/api` 中添加新的 API 接口方法。
3.  **实现 Repository**: 在 `data/repository` 中封装 API 调用，处理异常和数据转换。
4.  **编写 ViewModel**: 创建 ViewModel，调用 Repository 获取数据，并暴露 `StateFlow` 给 UI。
5.  **构建 UI**: 使用 Compose Multiplatform 编写界面，观察 ViewModel 的状态并渲染。

## 6. 注意事项

-   **时间处理**: 后端使用 `LocalDateTime`，客户端建议使用 `kotlinx-datetime` 库处理 ISO 8601 字符串。
-   **平台限制**: 注意 `ExamResponse` 中的 `allowedPlatforms` 字段，客户端应根据当前运行平台判断是否允许进入考试。
-   **防作弊**: 桌面端可检测窗口焦点丢失 (切屏)，移动端需相应处理后台运行情况。

# KMP 跨平台项目模板

一个功能完整的 Kotlin Multiplatform (KMP) 跨平台项目模板，支持 Android、iOS 和 Desktop 平台，集成了现代化的开发工具链和最佳实践。

## 🚀 功能特性

### 📱 跨平台支持
- **Android** - 原生 Android 应用
- **iOS** - 原生 iOS 应用 
- **Desktop** - JVM 桌面应用 (Windows/macOS/Linux)

### 🛠️ 核心功能
- ✅ **网络请求** - 基于 Ktor 的 HTTP 客户端，支持 JSON 序列化
- ✅ **文件操作** - 跨平台文件选择、保存和下载功能
- ✅ **图片加载** - 使用 Coil 实现跨平台图片加载
- ✅ **导航系统** - 多页面导航，包含底部导航栏
- ✅ **状态管理** - 基于 KMP ViewModel 的响应式状态管理
- ✅ **依赖注入** - 使用 Koin 实现跨平台依赖注入
- ✅ **本地存储** - Token 存储和跨平台数据持久化
- ✅ **响应式布局** - 自适应不同屏幕尺寸的 UI 设计

### 🧪 测试功能页面
- **API 测试页面** - 测试网络请求功能
- **文件操作页面** - 测试文件选择和下载功能  
- **图片测试页面** - 测试图片加载和显示功能

## 🚀 自动化构建

项目集成了 GitHub Actions 自动构建工具，支持多平台自动编译和发布：

### 🔧 构建平台
- **Android** - 自动构建并签名 APK 文件
- **Windows** - 生成 MSI 安装包和可执行文件
- **macOS** - 生成 DMG 安装包
- **Linux** - 生成 DEB 安装包

### ⚙️ 配置要求

在使用 GitHub Actions 自动构建前，需要在 GitHub 仓库的 Settings > Secrets and variables > Actions 中配置以下密钥：

#### Android 签名配置
```
ANDROID_KEYSTORE_BASE64    # Android 密钥库文件的 Base64 编码
ANDROID_KEY_ALIAS          # 密钥别名
ANDROID_KEYSTORE_PASSWORD  # 密钥库密码
ANDROID_KEY_PASSWORD       # 密钥密码
```

#### 生成 Android 密钥库
```bash
# 生成密钥库文件
keytool -genkey -v -keystore release-key.keystore -alias your-key-alias -keyalg RSA -keysize 2048 -validity 10000

# 转换为 Base64 编码
base64 -i release-key.keystore | pbcopy  # macOS
base64 -w 0 release-key.keystore         # Linux
```

### 🏗️ 触发构建

自动构建在以下情况触发：
- **自动触发**: 推送带有 `v*` 格式的 tag（如 `v1.0.0`）
- **手动触发**: 在 GitHub Actions 页面手动运行工作流

```bash
# 创建并推送版本标签
git tag v1.0.0
git push origin v1.0.0
```

### 📦 构建产物

构建完成后，会自动创建 GitHub Release 并上传以下文件：
- `app-release-signed.apk` - Android 应用
- `*.msi` - Windows 安装包
- `*.dmg` - macOS 安装包  
- `*.deb` - Linux 安装包
- Windows 可执行文件目录

## 🧪 测试

项目包含多个测试页面用于验证功能：

- **首页** - 项目概览和导航
- **API 测试** - 测试各种 HTTP 请求方法
- **文件测试** - 测试文件选择、保存和下载
- **图片测试** - 测试图片加载和显示

## 🏗️ 技术栈

### 核心框架
- **Kotlin Multiplatform** - 跨平台开发框架
- **Compose Multiplatform** - 声明式 UI 框架
- **Kotlin Coroutines** - 异步编程

### 网络层
- **Ktor Client**  - 跨平台 HTTP 客户端
  - Android: OkHttp 引擎
  - iOS: Darwin 引擎  
  - Desktop: OkHttp 引擎
- **Kotlinx Serialization** - JSON 序列化

### UI 组件
- **Material 3** - Material Design 3 组件
- **Material Icons Extended** - 扩展图标库
- **Coil**  - 图片加载库

### 架构组件
- **KMP ViewModel**  - 跨平台 ViewModel
- **Koin**  - 依赖注入框架
- **FileKit**  - 跨平台文件操作库

### 工具库
- **Kotlinx DateTime** - 跨平台日期时间处理
- **Hot Reload** - 开发时热重载支持

## 📁 项目结构

```
BProject-main/
├── composeApp/                    # 主应用模块
│   ├── src/
│   │   ├── commonMain/            # 共享代码
│   │   │   └── kotlin/ovo/sypw/kmp/template/
│   │   │       ├── data/          # 数据层
│   │   │       │   ├── api/       # API 服务
│   │   │       │   ├── dto/       # 数据传输对象
│   │   │       │   └── storage/   # 本地存储
│   │   │       ├── di/            # 依赖注入模块
│   │   │       ├── domain/        # 业务逻辑层
│   │   │       ├── presentation/  # 表现层
│   │   │       │   ├── navigation/# 导航组件
│   │   │       │   ├── screens/   # 页面组件
│   │   │       │   └── viewmodel/ # ViewModel
│   │   │       └── utils/         # 工具类
│   │   ├── androidMain/           # Android 平台特定代码
│   │   ├── iosMain/               # iOS 平台特定代码
│   │   └── desktopMain/           # Desktop 平台特定代码
│   └── build.gradle.kts
├── iosApp/                        # iOS 应用
├── gradle/                        # Gradle 配置
└── build.gradle.kts
```

## 🔧 平台特定配置

### Android 平台
- ✅ **网络权限** - 已添加 `INTERNET` 权限
- ✅ **网络安全配置** - 支持 HTTP 请求（开发调试用）
- ✅ **Token 存储** - 基于 SharedPreferences 实现

### iOS 平台  
- ✅ **HTTP 请求支持** - 配置 `NSAllowsArbitraryLoads` 允许 HTTP 请求
- ✅ **Token 存储** - 基于 UserDefaults 实现

### Desktop 平台
- ✅ **JVM 优化** - 针对桌面环境的性能优化
- ✅ **Token 存储** - 基于文件系统实现

## 🚀 快速开始

### 环境要求
- **JDK 21** 或更高版本
- **Android Studio** 最新版本
- **Xcode** 15+ (仅 iOS 开发需要)
- **Kotlin** 2.1.20+

### 克隆项目
```bash
git clone <repository-url>
cd BProject-main
```

### 运行项目

#### Android
```bash
./gradlew :composeApp:assembleDebug
```

#### iOS
```bash
./gradlew :composeApp:iosSimulatorArm64Test
```
或在 Xcode 中打开 `iosApp/iosApp.xcodeproj`

#### Desktop
```bash
./gradlew :composeApp:run
```

## 📖 使用指南

### 网络请求
项目集成了完整的网络请求解决方案：

```kotlin
// 在 ApiTestScreen 中查看完整示例
class ApiTestService(private val httpClient: HttpClient) {
    suspend fun testGet(): ApiResponse<String> {
        // 网络请求实现
    }
}
```

### 文件操作
使用 FileKit 实现跨平台文件操作：

```kotlin
// 在 FileTestScreen 中查看完整示例
val fileUtils = rememberFileUtils()
val selectedFile = fileUtils.selectFile()
```

### 依赖注入
使用 Koin 进行依赖管理：

```kotlin
// 在各个 Module 文件中查看配置
val appModule = module {
    single<ApiTestService> { ApiTestService(get()) }
}
```

## 🔧 自定义配置

### 修改 API 基础 URL
在 `HttpClientConfig.kt` 中修改：
```kotlin
const val BASE_URL = "https://your-api-domain.com"
```

### 添加新的页面
1. 在 `presentation/screens/` 下创建新的 Screen 组件
2. 在 `NavigationItem.kt` 中添加导航项
3. 在相应的 Module 中注册 ViewModel

### 平台特定实现
使用 `expect/actual` 机制添加平台特定功能：

```kotlin
// commonMain
expect class PlatformSpecificClass

// androidMain  
actual class PlatformSpecificClass {
    // Android 实现
}

// iosMain
actual class PlatformSpecificClass {
    // iOS 实现  
}
```
## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🔗 相关链接

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Ktor](https://ktor.io/)
- [Koin](https://insert-koin.io/)
- [FileKit](https://github.com/vinceglb/FileKit)
- [Coil](https://coil-kt.github.io/coil/)

## 🤖 AI 辅助开发
已创建详细的 **AI 项目开发规范**，供后续 AI Agent 参考：
> **[AI_PROJECT_SPEC.md](AI_PROJECT_SPEC.md)**
>
> 包含核心原则、架构规范、UI 设计指南、编码标准和开发工作流。

---
---

**注意**: 这是一个开发模板项目，包含了 KMP 跨平台开发的最佳实践和常用功能实现。可以基于此模板快速开始你的跨平台项目开发。
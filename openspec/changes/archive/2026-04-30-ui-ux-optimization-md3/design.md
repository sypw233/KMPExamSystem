## Context

KMP 考试系统是一个 Kotlin Multiplatform 项目，目标平台包括 Android、iOS 和 Desktop (JVM)。当前项目使用 Compose Multiplatform 构建 UI，已有以下基础设施：

- **主题系统**: Color.kt 定义了完整的 M3 颜色方案（33 light + 29 dark tokens），但缺少 Typography.kt 和 Shapes.kt
- **响应式布局**: ResponsiveUtils.kt 提供了断点系统（COMPACT/MEDIUM/EXPANDED）和布局容器
- **导航系统**: 自定义状态导航（NavigationManager），非 Jetpack Navigation
- **屏幕数量**: 28 个独立屏幕/组件
- **状态管理**: ViewModel 暴露 sealed interface 状态（Loading/Error/Success）

**约束条件**:
- 必须保持 KMP 跨平台兼容性（不能使用 Android-only API）
- 必须保持现有响应式布局架构
- 必须保持现有导航系统架构（不迁移到 Jetpack Navigation）
- 所有 UI 字符使用简体中文

## Goals / Non-Goals

**Goals:**
- 完善 M3 主题系统（Typography + Shapes + 扩展 Color 令牌）
- 提取可复用 UI 组件统一 Loading/Error/Empty 状态显示
- 添加屏幕切换动画提升视觉连续性
- 添加 BackHandler 支持改善 Android 返回导航体验
- 为表单添加实时验证反馈
- 统一响应式宽度常量和间距令牌
- 为列表添加下拉刷新功能

**Non-Goals:**
- 不迁移到 Jetpack Navigation Compose 或 Decompose
- 不引入新的外部依赖
- 不重构现有 ViewModel 状态管理模式
- 不修改业务逻辑或 API 层
- 不添加 Dynamic Color（Material You）支持
- 不实现完整的深链接系统

## Decisions

### Decision 1: Typography 实现方式

**选择**: 创建 Typography.kt，使用系统默认字体但自定义类型比例

**理由**:
- KMP 跨平台字体加载复杂（需要 expect/actual 机制）
- M3 默认字体（Roboto）在各平台表现良好
- 优先保证一致性，后续可单独优化字体

**替代方案**:
- 使用 Google Fonts Compose 库 → 增加依赖，KMP 兼容性未知
- 使用 Noto Sans CJK → 需要额外字体文件，增大包体积

### Decision 2: 可复用组件架构

**选择**: 创建独立的 `presentation/components/common/` 目录，放置 4 个通用组件

**组件设计**:
```
LoadingContent - 接收 size 参数，支持全屏/内联两种模式
ErrorContent   - 接收 message + onRetry 回调，统一图标+消息+重试按钮布局
EmptyState     - 接收 icon + title + subtitle + action，支持自定义
ScreenStateHandler - 封装 when(uiState) 切换，自动渲染对应状态
```

**理由**:
- 集中管理，避免重复代码
- 统一视觉风格，便于后续调整
- 接口简单，易于各屏幕集成

### Decision 3: 动画实现策略

**选择**: 使用 `AnimatedContent` + `fadeIn/fadeOut` 实现屏幕切换

**理由**:
- `AnimatedContent` 是 Compose Multiplatform 原生支持的 API
- 淡入淡出是最通用的过渡效果，不会引起视觉干扰
- 实现简单，只需包装 `NavigationScreen` 的 `when` 块

**替代方案**:
- `slideInHorizontally` → 可能与响应式布局冲突
- 共享元素过渡 → 实现复杂，需要修改每个屏幕

### Decision 4: BackHandler 集成策略

**选择**: 在 3 个关键位置添加 BackHandler

1. **App.kt MainContent** - 返回上一个导航标签（需要在 NavigationManager 添加历史栈）
2. **ExamTakingScreen** - 显示退出确认对话框
3. **ProfileScreen** - 返回 Profile 主界面（清除 currentSubScreen）

**理由**:
- 最小化修改，解决最关键的返回导航问题
- 不改变现有导航架构

**替代方案**:
- 重构 NavigationManager 为完整的历史栈 → 改动范围大，风险高

### Decision 5: 表单验证反馈

**选择**: 使用 M3 的 `isError` + `supportingText` 实现内联验证

**实现**:
- 在 ViewModel 中添加 `validationErrors: Map<String, String>` 状态
- 在 `OutlinedTextField` 中绑定 `isError` 和 `supportingText`
- 仅在用户交互后显示错误（避免初始状态就显示红色）

**理由**:
- M3 原生支持，无需自定义组件
- 提供即时反馈，改善用户体验

### Decision 6: 响应式常量统一

**选择**: 扩展 `ResponsiveUtils.MaxWidths` 添加缺失常量

**新增常量**:
```kotlin
EXAM_TAKING = 800.dp
SYSTEM_SETTINGS = 760.dp
EXAM_COMPOSE = 900.dp
PROFILE_FORM = 420.dp
```

**修改的屏幕**:
- LoginScreen: 400.dp → FORM
- RegisterScreen: 400.dp → FORM
- ExamTakingScreen: 800.dp → EXAM_TAKING
- SystemSettingsScreen: 760.dp → SYSTEM_SETTINGS
- ExamComposeScreen: 900.dp → EXAM_COMPOSE
- ProfileMainScreen: 420.dp → PROFILE_FORM

## Risks / Trade-offs

**Risk 1: 动画可能影响性能**
- Mitigation: 使用简单的 fadeIn/fadeOut，避免复杂动画
- 监控: 测试低端设备上的帧率

**Risk 2: BackHandler 可能与系统返回冲突**
- Mitigation: 仅在关键位置添加，确保行为符合用户预期
- 测试: Android 真机测试返回行为

**Risk 3: 可复用组件可能导致过度抽象**
- Mitigation: 保持组件接口简单，仅封装最常用的功能
- 原则: 如果某屏幕需要特殊处理，允许绕过通用组件

**Risk 4: 骨架屏实现复杂度**
- Mitigation: 初期仅为核心列表（课程、考试、通知）添加简单骨架屏
- 权衡: 骨架屏 vs 简单加载指示器的开发成本

## Open Questions

1. **骨架屏实现方式**: 使用 Compose Canvas 手绘 vs 简单的灰色占位 Box？
   - 建议: 初期使用灰色 Box 占位，后续可升级为 Canvas 动画

2. **中等屏幕导航**: 600-840dp 范围是否需要 NavigationRail？
   - 建议: 暂不实现，保持与 COMPACT 一致的底部导航

3. **TopAppBar 滚动行为**: 是否为所有可滚动屏幕添加？
   - 建议: 仅为主要列表屏幕（课程、考试、通知、用户管理）添加

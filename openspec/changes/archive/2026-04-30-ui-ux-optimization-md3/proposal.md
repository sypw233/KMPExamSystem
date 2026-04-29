## Why

当前考试系统虽然已实现基本的 Material Design 3 颜色方案和响应式布局框架，但存在多个 UI/UX 问题影响用户体验：主题系统不完整（缺少自定义 Typography 和 Shapes）、28 个屏幕中存在 6 处硬编码颜色绕过主题系统、加载/错误/空状态模式不一致（23+ 处内联实现）、无屏幕切换动画、无返回导航支持（零 BackHandler 使用）、表单无实时验证反馈、列表无下拉刷新。这些问题导致视觉不一致、交互生硬、用户体验碎片化。

## What Changes

**主题系统增强**
- 添加自定义 Typography 定义（中文字体优化、完整的 M3 类型比例）
- 添加自定义 Shapes 定义（统一的圆角令牌）
- 扩展 Color.kt 添加缺失的 M3 表面容器层级令牌
- 消除 6 处硬编码颜色，统一使用主题语义令牌

**可复用 UI 组件提取**
- 创建 `LoadingContent` 组件替代 23+ 处内联加载指示器
- 创建 `ErrorContent` 组件统一错误显示模式（图标 + 消息 + 重试）
- 创建 `EmptyState` 组件统一空状态显示（图标 + 标题 + 可选操作）
- 创建 `ScreenStateHandler` 封装 Loading/Error/Success 状态切换

**导航与交互优化**
- 为 `NavigationScreen` 添加 `AnimatedContent` 屏幕切换动画
- 添加 `BackHandler` 支持（主导航、考试模式、子屏幕）
- 为表单添加实时验证反馈（`OutlinedTextField` 的 `isError` + `supportingText`）
- 为列表屏幕添加 `PullToRefreshBox` 下拉刷新

**响应式布局完善**
- 将 6 处魔法宽度值（`widthIn(max = N.dp)`）统一到 `ResponsiveUtils.MaxWidths` 常量
- 修复 `ExamListLayout` 等文件中的硬编码间距值
- 为中等屏幕（600-840dp）添加 NavigationRail 中间态

**状态反馈改进**
- 添加骨架屏/Shimmer 加载状态替代纯 CircularProgressIndicator
- 统一 TopAppBar 滚动行为（`enterAlwaysScrollBehavior`）
- 标准化 Snackbar 使用模式

## Capabilities

### New Capabilities
- `md3-theme-enhancement`: 完整的 M3 主题系统（Typography + Shapes + 扩展 Color 令牌）
- `reusable-ui-components`: 可复用 UI 组件库（LoadingContent、ErrorContent、EmptyState、ScreenStateHandler）
- `navigation-ux`: 导航 UX 优化（动画、BackHandler、返回栈）
- `form-validation`: 表单实时验证反馈系统
- `responsive-layout-polish`: 响应式布局完善（统一宽度常量、间距令牌、中等屏幕适配）

### Modified Capabilities
（无现有 spec 需要修改）

## Impact

**受影响的代码**
- `presentation/theme/` - 新增 Typography.kt、Shapes.kt，修改 Color.kt、Theme.kt
- `presentation/components/` - 新增 4 个可复用组件
- `presentation/navigation/` - 修改 NavigationScreen.kt、NavigationBar.kt、NavigationManager.kt
- `presentation/screens/` - 所有 28 个屏幕文件需要更新（使用新组件、修复硬编码值）
- `utils/ResponsiveUtils.kt` - 扩展 MaxWidths 常量

**依赖变化**
- 无新外部依赖（使用 Compose Multiplatform 内置动画和 Material 3 组件）

**API 变化**
- `NavigationManager` 需要添加 `popBack()` 方法和历史栈
- 所有屏幕的 Loading/Error/Empty 状态处理将统一为新组件接口

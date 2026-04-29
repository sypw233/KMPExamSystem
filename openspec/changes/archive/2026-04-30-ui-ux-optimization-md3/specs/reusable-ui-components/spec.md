## ADDED Requirements

### Requirement: LoadingContent 组件
系统 SHALL 提供 `LoadingContent` 可复用组件，支持全屏居中和内联两种显示模式。

#### Scenario: 全屏加载状态
- **WHEN** 屏幕处于加载状态
- **THEN** LoadingContent SHALL 在屏幕中央显示 CircularProgressIndicator

#### Scenario: 内联加载状态
- **WHEN** 列表正在加载更多数据
- **THEN** LoadingContent(size = SMALL) SHALL 在列表底部显示小型加载指示器

#### Scenario: 自定义加载提示
- **WHEN** 需要显示加载提示文本
- **THEN** LoadingContent SHALL 支持可选的 message 参数显示提示文本

### Requirement: ErrorContent 组件
系统 SHALL 提供 `ErrorContent` 可复用组件，统一错误显示模式（图标 + 消息 + 重试按钮）。

#### Scenario: 错误状态显示
- **WHEN** 数据加载失败
- **THEN** ErrorContent SHALL 显示错误图标、错误消息和重试按钮

#### Scenario: 无重试操作
- **WHEN** 错误不可重试（如权限错误）
- **THEN** ErrorContent(onRetry = null) SHALL 仅显示错误图标和消息

#### Scenario: 自定义错误消息
- **WHEN** 需要显示特定错误消息
- **THEN** ErrorContent SHALL 使用传入的 message 参数而非默认错误文本

### Requirement: EmptyState 组件
系统 SHALL 提供 `EmptyState` 可复用组件，统一空状态显示（图标 + 标题 + 可选副标题 + 可选操作）。

#### Scenario: 空列表显示
- **WHEN** 列表数据为空
- **THEN** EmptyState SHALL 显示空状态图标和"暂无数据"标题

#### Scenario: 带操作的空状态
- **WHEN** 空状态有可执行操作（如创建新课程）
- **THEN** EmptyState SHALL 显示操作按钮

#### Scenario: 自定义空状态内容
- **WHEN** 需要自定义空状态提示
- **THEN** EmptyState SHALL 支持自定义 icon、title、subtitle 参数

### Requirement: ScreenStateHandler 组件
系统 SHALL 提供 `ScreenStateHandler` 可复用组件，封装 Loading/Error/Success 状态切换逻辑。

#### Scenario: 加载状态处理
- **WHEN** UI 状态为 Loading
- **THEN** ScreenStateHandler SHALL 渲染 LoadingContent

#### Scenario: 错误状态处理
- **WHEN** UI 状态为 Error
- **THEN** ScreenStateHandler SHALL 渲染 ErrorContent 并传递 onRetry 回调

#### Scenario: 成功状态处理
- **WHEN** UI 状态为 Success
- **THEN** ScreenStateHandler SHALL 渲染传入的 successContent

#### Scenario: 空数据处理
- **WHEN** UI 状态为 Success 但数据为空列表
- **THEN** ScreenStateHandler SHALL 渲染 EmptyState

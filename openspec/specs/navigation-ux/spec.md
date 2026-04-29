## ADDED Requirements

### Requirement: 屏幕切换动画
系统 SHALL 为 NavigationScreen 的路由切换添加 `AnimatedContent` 动画，使用 fadeIn/fadeOut 过渡效果。

#### Scenario: 导航到新屏幕
- **WHEN** 用户点击导航项切换屏幕
- **THEN** 新屏幕 SHALL 以淡入动画显示，旧屏幕以淡出动画消失

#### Scenario: 动画性能
- **WHEN** 屏幕切换动画播放
- **THEN** 动画 SHALL 使用 300ms 时长，不影响应用响应性

### Requirement: BackHandler 主导航支持
系统 SHALL 在 App.kt 的 MainContent 中添加 BackHandler，支持返回上一个导航标签。

#### Scenario: Android 返回键导航
- **WHEN** 用户在非首页标签按 Android 返回键
- **THEN** 应用 SHALL 返回上一个访问的导航标签

#### Scenario: 首页返回行为
- **WHEN** 用户在首页标签按 Android 返回键
- **THEN** 应用 SHALL 正常退出（系统默认行为）

### Requirement: BackHandler 考试模式
系统 SHALL 在 ExamTakingScreen 中添加 BackHandler，防止意外退出考试。

#### Scenario: 考试中按返回键
- **WHEN** 用户在考试过程中按 Android 返回键
- **THEN** 系统 SHALL 显示退出确认对话框

#### Scenario: 确认退出考试
- **WHEN** 用户在退出确认对话框中点击"确认退出"
- **THEN** 系统 SHALL 退出考试模式并返回考试列表

#### Scenario: 取消退出考试
- **WHEN** 用户在退出确认对话框中点击"继续考试"
- **THEN** 系统 SHALL 关闭对话框并继续考试

### Requirement: BackHandler 子屏幕支持
系统 SHALL 在 ProfileScreen 的子屏幕中添加 BackHandler，支持返回 Profile 主界面。

#### Scenario: 子屏幕返回
- **WHEN** 用户在成绩历史、通知列表等子屏幕按返回键
- **THEN** 系统 SHALL 返回 Profile 主界面

### Requirement: NavigationManager 历史栈
系统 SHALL 在 NavigationManager 中添加导航历史栈，支持 `popBack()` 操作。

#### Scenario: 记录导航历史
- **WHEN** 用户导航到新标签
- **THEN** NavigationManager SHALL 将当前标签压入历史栈

#### Scenario: 返回上一个标签
- **WHEN** 调用 popBack()
- **THEN** NavigationManager SHALL 弹出栈顶标签并导航到该标签

#### Scenario: 空栈返回
- **WHEN** 历史栈为空时调用 popBack()
- **THEN** NavigationManager SHALL 不执行任何操作

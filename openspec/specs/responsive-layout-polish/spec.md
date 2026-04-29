## ADDED Requirements

### Requirement: 统一响应式宽度常量
系统 SHALL 将所有硬编码的 `widthIn(max = N.dp)` 值统一到 `ResponsiveUtils.MaxWidths` 常量。

#### Scenario: LoginScreen 使用 FORM 常量
- **WHEN** LoginScreen 渲染登录表单
- **THEN** 表单容器 SHALL 使用 `ResponsiveUtils.MaxWidths.FORM` (480dp) 而非硬编码 400dp

#### Scenario: RegisterScreen 使用 FORM 常量
- **WHEN** RegisterScreen 渲染注册表单
- **THEN** 表单容器 SHALL 使用 `ResponsiveUtils.MaxWidths.FORM` (480dp) 而非硬编码 400dp

#### Scenario: ExamTakingScreen 使用 EXAM_TAKING 常量
- **WHEN** ExamTakingScreen 渲染考试界面
- **THEN** 内容容器 SHALL 使用 `ResponsiveUtils.MaxWidths.EXAM_TAKING` (800dp) 而非硬编码 800dp

#### Scenario: SystemSettingsScreen 使用 SYSTEM_SETTINGS 常量
- **WHEN** SystemSettingsScreen 渲染设置界面
- **THEN** 内容容器 SHALL 使用 `ResponsiveUtils.MaxWidths.SYSTEM_SETTINGS` (760dp) 而非硬编码 760dp

#### Scenario: ExamComposeScreen 使用 EXAM_COMPOSE 常量
- **WHEN** ExamComposeScreen 渲染组卷界面
- **THEN** 内容容器 SHALL 使用 `ResponsiveUtils.MaxWidths.EXAM_COMPOSE` (900dp) 而非硬编码 900dp

#### Scenario: ProfileMainScreen 使用 PROFILE_FORM 常量
- **WHEN** ProfileMainScreen 渲染个人资料表单
- **THEN** 表单容器 SHALL 使用 `ResponsiveUtils.MaxWidths.PROFILE_FORM` (420dp) 而非硬编码 420dp

### Requirement: 统一间距令牌
系统 SHALL 将硬编码的间距值（8dp, 12dp, 16dp 等）统一使用 `ResponsiveUtils.Spacing` 令牌。

#### Scenario: ExamListLayout 使用配置间距
- **WHEN** ExamListLayout 渲染考试列表
- **THEN** 列表间距 SHALL 使用 `config.verticalSpacing` 而非硬编码 12dp

#### Scenario: GradeSubmissionScreen 使用配置间距
- **WHEN** GradeSubmissionScreen 渲染评分界面
- **THEN** 卡片内间距 SHALL 使用 `config.cardPadding` 而非硬编码 16dp

### Requirement: PullToRefresh 下拉刷新
系统 SHALL 为主要列表屏幕添加 `PullToRefreshBox` 下拉刷新功能。

#### Scenario: ExamsScreen 下拉刷新
- **WHEN** 用户在考试列表下拉
- **THEN** 系统 SHALL 触发数据刷新并显示加载指示器

#### Scenario: NotificationScreen 下拉刷新
- **WHEN** 用户在通知列表下拉
- **THEN** 系统 SHALL 触发通知刷新并显示加载指示器

#### Scenario: CoursesScreen 下拉刷新
- **WHEN** 用户在课程列表下拉
- **THEN** 系统 SHALL 触发课程数据刷新并显示加载指示器

### Requirement: TopAppBar 滚动行为
系统 SHALL 为主要列表屏幕的 TopAppBar 添加滚动行为（enterAlwaysScrollBehavior）。

#### Scenario: 列表滚动时 TopAppBar 收起
- **WHEN** 用户向下滚动课程列表
- **THEN** TopAppBar SHALL 收起以显示更多内容

#### Scenario: 列表回滚时 TopAppBar 展开
- **WHEN** 用户向上滚动课程列表
- **THEN** TopAppBar SHALL 展开显示完整标题

## 1. 主题系统增强

- [x] 1.1 创建 `presentation/theme/Typography.kt`，定义完整的 M3 Typography（15 个类型比例级别）
- [x] 1.2 创建 `presentation/theme/Shapes.kt`，定义 small/medium/large/extraLarge 圆角半径
- [x] 1.3 修改 `presentation/theme/Color.kt`，添加缺失的 M3 表面容器层级令牌（surfaceContainerLowest 到 surfaceContainerHighest）和 outlineVariant/scrim
- [x] 1.4 修改 `presentation/theme/Theme.kt`，将自定义 Typography 和 Shapes 传递给 MaterialTheme
- [x] 1.5 修改 `presentation/components/GlobalDialog.kt`，将 5 个硬编码颜色替换为主题语义颜色
- [x] 1.6 修改 `presentation/screens/student/GradeDetailScreen.kt`，将硬编码绿色替换为主题成功语义颜色

## 2. 可复用 UI 组件

- [x] 2.1 创建 `presentation/components/common/LoadingContent.kt`，支持全屏和内联两种模式
- [x] 2.2 创建 `presentation/components/common/ErrorContent.kt`，统一错误显示（图标 + 消息 + 重试按钮）
- [x] 2.3 创建 `presentation/components/common/EmptyState.kt`，统一空状态显示（图标 + 标题 + 可选操作）
- [x] 2.4 创建 `presentation/components/common/ScreenStateHandler.kt`，封装 Loading/Error/Success 状态切换
- [x] 2.5 更新 `presentation/screens/DashboardScreen.kt`，使用 ScreenStateHandler 替换内联状态处理
- [x] 2.6 更新 `presentation/screens/ExamsScreen.kt`，使用 ScreenStateHandler 替换内联状态处理
- [x] 2.7 更新 `presentation/screens/CourseManageScreen.kt`，使用 ScreenStateHandler 替换内联状态处理
- [x] 2.8 更新 `presentation/screens/NotificationScreen.kt`，使用 ScreenStateHandler 替换内联状态处理
- [x] 2.9 更新 `presentation/screens/admin/AdminDashboardScreen.kt`，使用 ScreenStateHandler 替换内联状态处理
- [x] 2.10 更新 `presentation/screens/admin/UserManageScreen.kt`，使用 ScreenStateHandler 替换内联状态处理
- [x] 2.11 更新 `presentation/screens/teacher/TeacherExamManageScreen.kt`，使用 ScreenStateHandler 替换内联状态处理

## 3. 导航与交互优化

- [x] 3.1 修改 `presentation/navigation/NavigationManager.kt`，添加历史栈和 popBack() 方法
- [x] 3.2 修改 `presentation/navigation/NavigationScreen.kt`，用 AnimatedContent 包装路由切换
- [x] 3.3 修改 `App.kt`，在 MainContent 中添加 BackHandler 支持返回上一个导航标签
- [x] 3.4 修改 `presentation/screens/ExamTakingScreen.kt`，添加 BackHandler 防止意外退出考试
- [x] 3.5 修改 `presentation/screens/ProfileScreen.kt`，为子屏幕添加 BackHandler 支持返回

## 4. 表单验证反馈

- [x] 4.1 修改 `presentation/screens/auth/LoginScreen.kt`，添加用户名和密码的实时验证反馈
- [x] 4.2 修改 `presentation/screens/auth/RegisterScreen.kt`，添加注册表单的实时验证反馈
- [x] 4.3 修改 `presentation/screens/CourseDialogs.kt`，为 CourseFormDialog 添加表单验证

## 5. 响应式布局完善

- [x] 5.1 修改 `utils/ResponsiveUtils.kt`，在 MaxWidths 中添加 EXAM_TAKING、SYSTEM_SETTINGS、EXAM_COMPOSE、PROFILE_FORM 常量
- [x] 5.2 修改 `presentation/screens/auth/LoginScreen.kt`，将 400dp 替换为 MaxWidths.FORM
- [x] 5.3 修改 `presentation/screens/auth/RegisterScreen.kt`，将 400dp 替换为 MaxWidths.FORM
- [x] 5.4 修改 `presentation/screens/ExamTakingScreen.kt`，将 800dp 替换为 MaxWidths.EXAM_TAKING
- [x] 5.5 修改 `presentation/screens/admin/SystemSettingsScreen.kt`，将 760dp 替换为 MaxWidths.SYSTEM_SETTINGS
- [x] 5.6 修改 `presentation/screens/teacher/ExamComposeScreen.kt`，将 900dp 替换为 MaxWidths.EXAM_COMPOSE
- [x] 5.7 修改 `presentation/screens/ProfileMainScreen.kt`，将 420dp 替换为 MaxWidths.PROFILE_FORM
- [x] 5.8 修改 `presentation/screens/ExamListLayout.kt`，将硬编码间距替换为 config.verticalSpacing 和 config.cardPadding
- [x] 5.9 修改 `presentation/screens/teacher/GradeSubmissionScreen.kt`，将硬编码 16dp 替换为 config.cardPadding

## 6. 列表交互增强

- [x] 6.1 修改 `presentation/screens/ExamsScreen.kt`，添加 PullToRefreshBox 下拉刷新
- [x] 6.2 修改 `presentation/screens/NotificationScreen.kt`，添加 PullToRefreshBox 下拉刷新
- [x] 6.3 修改 `presentation/screens/StudentCourseScreen.kt`，添加 PullToRefreshBox 下拉刷新
- [x] 6.4 修改 `presentation/screens/CourseManageScreen.kt`，添加 PullToRefreshBox 下拉刷新
- [x] 6.5 修改 `presentation/screens/ExamsScreen.kt`，为 TopAppBar 添加 enterAlwaysScrollBehavior
- [x] 6.6 修改 `presentation/screens/CourseManageScreen.kt`，为 TopAppBar 添加 enterAlwaysScrollBehavior
- [x] 6.7 修改 `presentation/screens/NotificationScreen.kt`，为 TopAppBar 添加 enterAlwaysScrollBehavior
- [x] 6.8 修改 `presentation/screens/admin/UserManageScreen.kt`，为 TopAppBar 添加 enterAlwaysScrollBehavior

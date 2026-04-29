## ADDED Requirements

### Requirement: Typography 定义
系统 SHALL 提供完整的 M3 Typography 定义，包含 15 个类型比例级别（display/headline/title/body/label，各 large/medium/small）。

#### Scenario: 主题应用 Typography
- **WHEN** 应用启动并应用 AppTheme
- **THEN** MaterialTheme.typography SHALL 使用自定义 Typography 定义而非 M3 默认值

#### Scenario: 中文文本显示
- **WHEN** 屏幕显示中文文本
- **THEN** 文本 SHALL 使用优化的行高和字间距以确保可读性

### Requirement: Shapes 定义
系统 SHALL 提供自定义 Shapes 定义，定义 small/medium/large/extraLarge 的圆角半径。

#### Scenario: 组件使用主题 Shapes
- **WHEN** Card/Button/Dialog 等组件渲染
- **THEN** 组件 SHALL 使用 MaterialTheme.shapes 中定义的圆角值

#### Scenario: 圆角一致性
- **WHEN** 多个屏幕显示相同类型的组件
- **THEN** 相同类型组件 SHALL 使用相同的圆角半径

### Requirement: 扩展 Color 令牌
系统 SHALL 在 Color.kt 中添加缺失的 M3 表面容器层级令牌（surfaceContainerLowest 到 surfaceContainerHighest）和 outlineVariant/scrim。

#### Scenario: 深色主题表面层级
- **WHEN** 用户切换到深色主题
- **THEN** 表面容器 SHALL 使用正确的层级颜色以区分内容区域

#### Scenario: outlineVariant 使用
- **WHEN** 组件使用 outlineVariant 颜色
- **THEN** 颜色 SHALL 从主题获取而非使用 M3 默认值

### Requirement: 消除硬编码颜色
系统 SHALL 消除所有硬编码颜色值（GlobalDialog 的 5 个颜色、GradeDetailScreen 的绿色），统一使用主题语义令牌。

#### Scenario: GlobalDialog 主题适配
- **WHEN** GlobalDialog 显示不同类型的对话框（Info/Warning/Error/Success/Confirm）
- **THEN** 对话框图标颜色 SHALL 使用 MaterialTheme.colorScheme 中的语义颜色

#### Scenario: GradeDetailScreen 主题适配
- **WHEN** GradeDetailScreen 显示正确答案标记
- **THEN** 绿色标记 SHALL 使用主题中的成功语义颜色

#### Scenario: 深色主题兼容
- **WHEN** 用户在深色主题下查看对话框或成绩详情
- **THEN** 所有颜色 SHALL 自动适配深色主题

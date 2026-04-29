## ADDED Requirements

### Requirement: 表单字段验证状态
系统 SHALL 为 OutlinedTextField 添加实时验证反馈，使用 M3 的 isError 和 supportingText 属性。

#### Scenario: 验证错误显示
- **WHEN** 用户输入无效内容（如空用户名、密码过短）
- **THEN** 对应字段 SHALL 显示红色边框（isError = true）和错误提示文本（supportingText）

#### Scenario: 验证通过显示
- **WHEN** 用户输入有效内容
- **THEN** 字段 SHALL 恢复正常状态，不显示错误提示

#### Scenario: 交互后验证
- **WHEN** 用户首次加载表单
- **THEN** 字段 SHALL 不显示验证错误（避免初始状态就显示红色）

### Requirement: LoginScreen 表单验证
LoginScreen SHALL 实现用户名和密码的实时验证反馈。

#### Scenario: 空用户名验证
- **WHEN** 用户清空用户名字段并移开焦点
- **THEN** 用户名字段 SHALL 显示"请输入用户名"错误提示

#### Scenario: 空密码验证
- **WHEN** 用户清空密码字段并移开焦点
- **THEN** 密码字段 SHALL 显示"请输入密码"错误提示

### Requirement: RegisterScreen 表单验证
RegisterScreen SHALL 实现注册表单的实时验证反馈。

#### Scenario: 用户名长度验证
- **WHEN** 用户输入少于 3 个字符的用户名
- **THEN** 用户名字段 SHALL 显示"用户名至少 3 个字符"错误提示

#### Scenario: 密码强度验证
- **WHEN** 用户输入少于 6 个字符的密码
- **THEN** 密码字段 SHALL 显示"密码至少 6 个字符"错误提示

#### Scenario: 密码确认验证
- **WHEN** 用户输入的确认密码与密码不一致
- **THEN** 确认密码字段 SHALL 显示"两次密码不一致"错误提示

### Requirement: CourseFormDialog 表单验证
CourseFormDialog SHALL 实现课程表单的实时验证反馈。

#### Scenario: 课程名称为空验证
- **WHEN** 用户清空课程名称字段
- **THEN** 课程名称字段 SHALL 显示"请输入课程名称"错误提示

#### Scenario: 课程描述过长验证
- **WHEN** 用户输入超过 500 字符的课程描述
- **THEN** 课程描述字段 SHALL 显示"描述不超过 500 字符"错误提示

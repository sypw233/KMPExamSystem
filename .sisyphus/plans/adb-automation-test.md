# KMP考试系统 ADB自动化测试流程

## TL;DR

> **快速摘要**: 为KMP考试系统设计并实施一套完整的ADB自动化测试流程，覆盖学生、教师、管理员三个角色的所有功能模块，使用ADB MCP工具进行UI自动化测试，所有测试问题实时记录到文档。
> 
> **交付物**:
> - 自动化测试脚本/流程
> - 测试报告文档 (`.sisyphus/evidence/test-report.md`)
> - 测试截图目录 (`.sisyphus/evidence/screenshots/`)
> - 错误日志汇总
> 
> **预估工作量**: Medium
> **并行执行**: YES - 多个测试模块可并行
> **关键路径**: 环境准备 → 认证测试 → 角色功能测试 → 边界测试 → 报告生成

---

## Context

### 原始需求
用户需要一套针对KMP考试系统的AI自动化测试流程，要求：
- 全程自动测试，无需人工干预
- 使用ADB MCP工具进行Android UI自动化
- 测试问题实时输出到文档
- 完整测试覆盖整个项目

### 项目分析结果
- **技术栈**: Kotlin Multiplatform + Compose Multiplatform + Material Design 3
- **角色系统**: 学生(STUDENT)、教师(TEACHER)、管理员(ADMIN)
- **功能模块**: 认证、课程、考试、题目、用户管理、统计、通知、文件
- **API端点**: 71个RESTful API
- **导航**: 底部导航(移动端) / 侧边导航(桌面端)

### 已确认信息
- **后端服务器**: localhost:8080 (已运行)
- **测试账号**:
  - admin/123456 (管理员)
  - student1/123456 (学生)
  - teacher1/123456 (教师)
- **角色覆盖**: 所有角色
- **报告详细度**: 详细报告 (截图+日志+错误堆栈)

---

## Work Objectives

### 核心目标
设计并实施一套完整的ADB自动化测试流程，验证KMP考试系统的所有功能模块。

### 具体交付物
1. 测试流程定义文档
2. 测试用例矩阵
3. 自动化测试执行
4. 测试报告 (含截图、日志、错误记录)

### 完成标准
- [ ] 所有角色的登录功能验证通过
- [ ] 学生功能模块测试完成 (课程、考试、成绩)
- [ ] 教师功能模块测试完成 (课程管理、题目管理、考试管理)
- [ ] 管理员功能模块测试完成 (用户管理、系统设置)
- [ ] 边界测试完成 (异常输入、权限验证)
- [ ] 测试报告生成，包含所有失败用例详情

### 必须包含
- 使用ADB MCP工具进行UI自动化
- 实时错误记录到文档
- 截图记录关键步骤
- 完整的角色功能覆盖

### 必须不包含
- 性能测试 (不在本次范围)
- iOS/Desktop平台测试
- 后端API单元测试

---

## Verification Strategy

### 测试决策
- **基础设施**: 使用ADB MCP工具
- **测试类型**: UI自动化测试
- **报告格式**: Markdown文档

### QA策略
每个测试场景必须包含：
- 测试步骤描述
- 预期结果
- 实际结果
- 截图证据
- 错误日志 (如有)

---

## Execution Strategy

### 并行执行波次

```
Wave 1 (环境准备):
├── Task 1: 检查ADB设备连接 [quick]
├── Task 2: 验证应用已安装 [quick]
└── Task 3: 验证后端服务器可访问 [quick]

Wave 2 (认证测试):
├── Task 4: 管理员登录测试 [quick]
├── Task 5: 教师登录测试 [quick]
└── Task 6: 学生登录测试 [quick]

Wave 3 (学生功能测试 - 依赖Wave 2):
├── Task 7: 学生首页功能测试 [unspecified-high]
├── Task 8: 学生课程功能测试 [unspecified-high]
├── Task 9: 学生考试功能测试 [unspecified-high]
└── Task 10: 学生个人中心测试 [unspecified-high]

Wave 4 (教师功能测试 - 依赖Wave 2):
├── Task 11: 教师课程管理测试 [unspecified-high]
├── Task 12: 教师题目管理测试 [unspecified-high]
├── Task 13: 教师考试管理测试 [unspecified-high]
└── Task 14: 教师成绩批改测试 [unspecified-high]

Wave 5 (管理员功能测试 - 依赖Wave 2):
├── Task 15: 管理员用户管理测试 [unspecified-high]
├── Task 16: 管理员系统设置测试 [unspecified-high]
└── Task 17: 管理员数据统计测试 [unspecified-high]

Wave 6 (边界测试 - 依赖Wave 3-5):
├── Task 18: 异常输入测试 [unspecified-high]
├── Task 19: 权限验证测试 [unspecified-high]
└── Task 20: 网络错误处理测试 [unspecified-high]

Wave FINAL (报告生成 - 依赖所有任务):
└── Task F1: 生成完整测试报告 [writing]
```

### 依赖矩阵

| Task | 依赖 | 被依赖 |
|------|------|--------|
| 1-3 | - | 4-20 |
| 4-6 | 1-3 | 7-20 |
| 7-10 | 4-6 | 18-20 |
| 11-14 | 4-6 | 18-20 |
| 15-17 | 4-6 | 18-20 |
| 18-20 | 7-17 | F1 |
| F1 | 18-20 | - |

---

## TODOs

- [x] 1. 检查ADB设备连接

  **What to do**:
  - 使用 `android_adb_list_devices` 检查设备连接状态
  - 验证设备已授权调试
  - 记录设备信息 (型号、Android版本)

  **Must NOT do**:
  - 不要修改设备设置
  - 不要安装额外应用

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的设备检查操作

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3)
  - **Blocks**: Tasks 4-20
  - **Blocked By**: None

  **References**:
  - ADB工具文档: `android_adb_list_devices`

  **Acceptance Criteria**:
  - [ ] 设备列表返回至少一个设备
  - [ ] 设备状态为 "device" (已授权)

  **QA Scenarios**:

  ```
  Scenario: 检查ADB设备连接
    Tool: Bash (ADB)
    Preconditions: Android设备已连接USB并开启调试
    Steps:
      1. 调用 android_adb_list_devices
      2. 验证返回结果包含设备ID
      3. 验证设备状态为 "device"
    Expected Result: 返回至少一个设备，状态为device
    Failure Indicators: 无设备返回或设备状态为unauthorized
    Evidence: .sisyphus/evidence/task-1-device-check.txt
  ```

  **Commit**: NO

---

- [x] 2. 验证应用已安装

  **What to do**:
  - 使用 `android_adb_list_installed_packages` 检查应用是否已安装
  - 如果未安装，提示用户安装
  - 记录应用版本信息

  **Must NOT do**:
  - 不要自动安装应用 (避免覆盖用户数据)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的包检查操作

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3)
  - **Blocks**: Tasks 4-20
  - **Blocked By**: None

  **References**:
  - ADB工具文档: `android_adb_list_installed_packages`

  **Acceptance Criteria**:
  - [ ] 应用包名存在于已安装列表中

  **QA Scenarios**:

  ```
  Scenario: 验证应用已安装
    Tool: Bash (ADB)
    Preconditions: 应用已编译并安装到设备
    Steps:
      1. 调用 android_adb_list_installed_packages
      2. 在返回列表中搜索应用包名
      3. 记录应用版本信息
    Expected Result: 应用包名存在于列表中
    Failure Indicators: 应用包名不在列表中
    Evidence: .sisyphus/evidence/task-2-app-check.txt
  ```

  **Commit**: NO

---

- [x] 3. 验证后端服务器可访问

  **What to do**:
  - 使用 `android_adb_ping` 测试服务器连通性
  - 验证API端点响应
  - 记录服务器状态

  **Must NOT do**:
  - 不要修改服务器配置

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的网络连通性检查

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2)
  - **Blocks**: Tasks 4-20
  - **Blocked By**: None

  **References**:
  - ADB工具文档: `android_adb_ping`
  - API文档: `API_ENDPOINTS.md`

  **Acceptance Criteria**:
  - [ ] Ping服务器成功
  - [ ] API端点可访问

  **QA Scenarios**:

  ```
  Scenario: 验证后端服务器可访问
    Tool: Bash (ADB)
    Preconditions: 后端服务器已启动
    Steps:
      1. 调用 android_adb_ping host="10.0.2.2" count=4
      2. 验证ping成功 (无超时)
      3. 记录响应时间
    Expected Result: Ping成功，无丢包
    Failure Indicators: Ping超时或100%丢包
    Evidence: .sisyphus/evidence/task-3-server-check.txt
  ```

  **Commit**: NO

---

- [x] 4. 管理员登录测试

  **What to do**:
  - 启动应用
  - 输入管理员账号密码 (admin/123456)
  - 点击登录按钮
  - 验证登录成功 (跳转到管理员首页)
  - 截图记录

  **Must NOT do**:
  - 不要修改账号信息
  - 不要跳过验证步骤

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的登录流程测试

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6)
  - **Blocks**: Tasks 7-20
  - **Blocked By**: Tasks 1-3

  **References**:
  - 登录页面: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/auth/LoginScreen.kt`
  - 导航逻辑: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/navigation/NavigationScreen.kt`

  **Acceptance Criteria**:
  - [ ] 应用启动成功
  - [ ] 登录表单可交互
  - [ ] 登录成功后跳转到管理员首页
  - [ ] 截图保存成功

  **QA Scenarios**:

  ```
  Scenario: 管理员登录成功
    Tool: ADB
    Preconditions: 应用已安装，后端服务器可访问
    Steps:
      1. 调用 android_adb_start_app 启动应用
      2. 等待登录页面加载 (2秒)
      3. 调用 android_adb_dump_ui_hierarchy 获取UI结构
      4. 找到用户名输入框并输入 "admin"
      5. 找到密码输入框并输入 "123456"
      6. 点击登录按钮
      7. 等待页面跳转 (3秒)
      8. 调用 android_adb_take_screenshot 截图
      9. 调用 android_adb_check_element_exists text="首页" 验证跳转
    Expected Result: 登录成功，显示管理员首页
    Failure Indicators: 停留在登录页面或显示错误信息
    Evidence: .sisyphus/evidence/task-4-admin-login.png

  Scenario: 管理员登录失败 (错误密码)
    Tool: ADB
    Preconditions: 应用已安装，后端服务器可访问
    Steps:
      1. 调用 android_adb_start_app 启动应用
      2. 等待登录页面加载 (2秒)
      3. 输入用户名 "admin"
      4. 输入错误密码 "wrongpassword"
      5. 点击登录按钮
      6. 等待错误提示 (2秒)
      7. 调用 android_adb_take_screenshot 截图
      8. 调用 android_adb_check_element_exists text="登录失败" 验证错误提示
    Expected Result: 显示登录失败错误提示
    Failure Indicators: 无错误提示或跳转到其他页面
    Evidence: .sisyphus/evidence/task-4-admin-login-fail.png
  ```

  **Commit**: NO

---

- [ ] 5. 教师登录测试

  **What to do**:
  - 启动应用
  - 输入教师账号密码 (teacher1/123456)
  - 点击登录按钮
  - 验证登录成功 (跳转到教师首页)
  - 截图记录

  **Must NOT do**:
  - 不要修改账号信息

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的登录流程测试

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 6)
  - **Blocks**: Tasks 7-20
  - **Blocked By**: Tasks 1-3

  **References**:
  - 登录页面: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/auth/LoginScreen.kt`

  **Acceptance Criteria**:
  - [ ] 教师登录成功
  - [ ] 跳转到教师首页

  **QA Scenarios**:

  ```
  Scenario: 教师登录成功
    Tool: ADB
    Preconditions: 应用已安装，后端服务器可访问
    Steps:
      1. 调用 android_adb_start_app 启动应用
      2. 等待登录页面加载 (2秒)
      3. 输入用户名 "teacher1"
      4. 输入密码 "123456"
      5. 点击登录按钮
      6. 等待页面跳转 (3秒)
      7. 调用 android_adb_take_screenshot 截图
      8. 验证跳转成功
    Expected Result: 登录成功，显示教师首页
    Failure Indicators: 停留在登录页面或显示错误信息
    Evidence: .sisyphus/evidence/task-5-teacher-login.png
  ```

  **Commit**: NO

---

- [ ] 6. 学生登录测试

  **What to do**:
  - 启动应用
  - 输入学生账号密码 (student1/123456)
  - 点击登录按钮
  - 验证登录成功 (跳转到学生首页)
  - 截图记录

  **Must NOT do**:
  - 不要修改账号信息

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的登录流程测试

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 5)
  - **Blocks**: Tasks 7-20
  - **Blocked By**: Tasks 1-3

  **References**:
  - 登录页面: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/auth/LoginScreen.kt`

  **Acceptance Criteria**:
  - [ ] 学生登录成功
  - [ ] 跳转到学生首页

  **QA Scenarios**:

  ```
  Scenario: 学生登录成功
    Tool: ADB
    Preconditions: 应用已安装，后端服务器可访问
    Steps:
      1. 调用 android_adb_start_app 启动应用
      2. 等待登录页面加载 (2秒)
      3. 输入用户名 "student1"
      4. 输入密码 "123456"
      5. 点击登录按钮
      6. 等待页面跳转 (3秒)
      7. 调用 android_adb_take_screenshot 截图
      8. 验证跳转成功
    Expected Result: 登录成功，显示学生首页
    Failure Indicators: 停留在登录页面或显示错误信息
    Evidence: .sisyphus/evidence/task-6-student-login.png
  ```

  **Commit**: NO

---

- [ ] 7. 学生首页功能测试

  **What to do**:
  - 以学生身份登录
  - 验证首页显示通知列表
  - 验证首页显示即将考试列表
  - 测试通知点击跳转
  - 截图记录

  **Must NOT do**:
  - 不要修改通知状态

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要验证多个UI组件和交互

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 8, 9, 10)
  - **Blocks**: Tasks 18-20
  - **Blocked By**: Tasks 4-6

  **References**:
  - 首页: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/DashboardScreen.kt`
  - 通知组件: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/NotificationComponents.kt`

  **Acceptance Criteria**:
  - [ ] 首页加载成功
  - [ ] 通知列表显示
  - [ ] 考试列表显示

  **QA Scenarios**:

  ```
  Scenario: 学生首页加载
    Tool: ADB
    Preconditions: 学生已登录
    Steps:
      1. 验证当前页面为首页
      2. 调用 android_adb_check_element_exists text="通知"
      3. 调用 android_adb_check_element_exists text="即将开始的考试"
      4. 调用 android_adb_take_screenshot 截图
    Expected Result: 首页显示通知和考试列表
    Failure Indicators: 页面空白或缺少关键组件
    Evidence: .sisyphus/evidence/task-7-student-home.png
  ```

  **Commit**: NO

---

- [ ] 8. 学生课程功能测试

  **What to do**:
  - 以学生身份登录
  - 导航到课程页面
  - 验证课程列表显示
  - 测试课程详情查看
  - 测试选课功能 (如有可用课程)
  - 截图记录

  **Must NOT do**:
  - 不要重复选课

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要验证列表显示和交互功能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 7, 9, 10)
  - **Blocks**: Tasks 18-20
  - **Blocked By**: Tasks 4-6

  **References**:
  - 课程页面: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/CoursesScreen.kt`
  - 学生课程卡片: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/StudentCourseCard.kt`

  **Acceptance Criteria**:
  - [ ] 课程列表加载成功
  - [ ] 课程卡片显示完整信息
  - [ ] 课程详情可查看

  **QA Scenarios**:

  ```
  Scenario: 学生课程列表
    Tool: ADB
    Preconditions: 学生已登录
    Steps:
      1. 点击底部导航 "课程"
      2. 等待课程列表加载 (3秒)
      3. 调用 android_adb_dump_ui_hierarchy 验证列表
      4. 调用 android_adb_take_screenshot 截图
      5. 如有课程，点击第一个课程查看详情
      6. 验证课程详情页面显示
    Expected Result: 课程列表显示，详情可查看
    Failure Indicators: 列表为空或详情页加载失败
    Evidence: .sisyphus/evidence/task-8-student-courses.png
  ```

  **Commit**: NO

---

- [ ] 9. 学生考试功能测试

  **What to do**:
  - 以学生身份登录
  - 导航到考试页面
  - 验证考试列表显示
  - 测试开始考试流程 (如有可用考试)
  - 验证考试答题界面
  - 截图记录

  **Must NOT do**:
  - 不要实际提交考试答案 (避免影响数据)

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 考试流程复杂，需要多步骤验证

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 7, 8, 10)
  - **Blocks**: Tasks 18-20
  - **Blocked By**: Tasks 4-6

  **References**:
  - 考试列表: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/ExamsScreen.kt`
  - 考试答题: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/ExamTakingScreen.kt`

  **Acceptance Criteria**:
  - [ ] 考试列表加载成功
  - [ ] 考试信息显示完整
  - [ ] 开始考试流程可触发

  **QA Scenarios**:

  ```
  Scenario: 学生考试列表
    Tool: ADB
    Preconditions: 学生已登录
    Steps:
      1. 点击底部导航 "考试"
      2. 等待考试列表加载 (3秒)
      3. 调用 android_adb_dump_ui_hierarchy 验证列表
      4. 调用 android_adb_take_screenshot 截图
      5. 如有可参加考试，点击 "开始考试"
      6. 验证考试说明弹窗显示
      7. 取消考试 (不实际开始)
    Expected Result: 考试列表显示，开始考试流程可触发
    Failure Indicators: 列表为空或开始考试按钮不可用
    Evidence: .sisyphus/evidence/task-9-student-exams.png
  ```

  **Commit**: NO

---

- [ ] 10. 学生个人中心测试

  **What to do**:
  - 以学生身份登录
  - 导航到个人中心
  - 验证用户信息显示
  - 测试成绩历史查看
  - 测试退出登录
  - 截图记录

  **Must NOT do**:
  - 不要修改用户信息

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要验证多个子页面和功能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 7, 8, 9)
  - **Blocks**: Tasks 18-20
  - **Blocked By**: Tasks 4-6

  **References**:
  - 个人中心: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/ProfileScreen.kt`
  - 成绩历史: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/GradeHistoryScreen.kt`

  **Acceptance Criteria**:
  - [ ] 个人中心加载成功
  - [ ] 用户信息显示正确
  - [ ] 成绩历史可查看

  **QA Scenarios**:

  ```
  Scenario: 学生个人中心
    Tool: ADB
    Preconditions: 学生已登录
    Steps:
      1. 点击底部导航 "我的"
      2. 等待个人中心加载 (2秒)
      3. 调用 android_adb_check_element_exists text="student1"
      4. 调用 android_adb_take_screenshot 截图
      5. 点击 "成绩历史"
      6. 验证成绩历史页面显示
      7. 返回个人中心
    Expected Result: 个人中心显示用户信息，成绩历史可查看
    Failure Indicators: 用户信息不显示或成绩历史加载失败
    Evidence: .sisyphus/evidence/task-10-student-profile.png
  ```

  **Commit**: NO

---

- [ ] 11. 教师课程管理测试

  **What to do**:
  - 以教师身份登录
  - 导航到课程管理页面
  - 验证课程列表显示
  - 测试创建课程功能 (填写表单但取消提交)
  - 测试课程编辑功能 (查看但不修改)
  - 截图记录

  **Must NOT do**:
  - 不要实际创建或修改课程 (避免污染数据)

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要验证CRUD操作界面

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 12, 13, 14)
  - **Blocks**: Tasks 18-20
  - **Blocked By**: Tasks 4-6

  **References**:
  - 课程管理: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/CoursesScreen.kt`

  **Acceptance Criteria**:
  - [ ] 课程管理页面加载成功
  - [ ] 创建课程表单可打开
  - [ ] 课程列表显示

  **QA Scenarios**:

  ```
  Scenario: 教师课程管理
    Tool: ADB
    Preconditions: 教师已登录
    Steps:
      1. 点击底部导航 "课程管理"
      2. 等待课程列表加载 (3秒)
      3. 调用 android_adb_take_screenshot 截图
      4. 点击 "添加课程" 按钮
      5. 验证创建课程表单显示
      6. 输入课程名称 "测试课程"
      7. 输入课程描述 "测试描述"
      8. 点击返回键取消 (不提交)
      9. 验证返回课程列表
    Expected Result: 课程管理功能正常，表单可打开和取消
    Failure Indicators: 列表加载失败或表单无法打开
    Evidence: .sisyphus/evidence/task-11-teacher-courses.png
  ```

  **Commit**: NO

---

- [ ] 12. 教师题目管理测试

  **What to do**:
  - 以教师身份登录
  - 导航到题目管理页面
  - 验证题目列表显示
  - 测试创建题目功能 (填写表单但取消提交)
  - 测试题目筛选功能
  - 截图记录

  **Must NOT do**:
  - 不要实际创建题目

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要验证复杂表单和筛选功能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 11, 13, 14)
  - **Blocks**: Tasks 18-20
  - **Blocked By**: Tasks 4-6

  **References**:
  - 题目管理: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/admin/QuestionBankScreen.kt`

  **Acceptance Criteria**:
  - [ ] 题目管理页面加载成功
  - [ ] 题目列表显示
  - [ ] 创建题目表单可打开

  **QA Scenarios**:

  ```
  Scenario: 教师题目管理
    Tool: ADB
    Preconditions: 教师已登录
    Steps:
      1. 点击底部导航 "题目管理"
      2. 等待题目列表加载 (3秒)
      3. 调用 android_adb_take_screenshot 截图
      4. 点击 "添加题目" 按钮
      5. 验证创建题目表单显示
      6. 测试题目类型选择 (单选/多选/判断)
      7. 点击返回键取消 (不提交)
    Expected Result: 题目管理功能正常，表单可打开
    Failure Indicators: 列表加载失败或表单无法打开
    Evidence: .sisyphus/evidence/task-12-teacher-questions.png
  ```

  **Commit**: NO

---

- [ ] 13. 教师考试管理测试

  **What to do**:
  - 以教师身份登录
  - 导航到考试管理页面
  - 验证考试列表显示
  - 测试创建考试功能 (填写表单但取消提交)
  - 测试考试状态筛选
  - 截图记录

  **Must NOT do**:
  - 不要实际创建考试

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要验证复杂表单和状态管理

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 11, 12, 14)
  - **Blocks**: Tasks 18-20
  - **Blocked By**: Tasks 4-6

  **References**:
  - 考试管理: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/teacher/TeacherExamManageScreen.kt`

  **Acceptance Criteria**:
  - [ ] 考试管理页面加载成功
  - [ ] 考试列表显示
  - [ ] 创建考试表单可打开

  **QA Scenarios**:

  ```
  Scenario: 教师考试管理
    Tool: ADB
    Preconditions: 教师已登录
    Steps:
      1. 点击底部导航 "考试管理"
      2. 等待考试列表加载 (3秒)
      3. 调用 android_adb_take_screenshot 截图
      4. 点击 "创建考试" 按钮
      5. 验证创建考试表单显示
      6. 测试考试状态筛选 (草稿/已发布/已结束)
      7. 点击返回键取消 (不提交)
    Expected Result: 考试管理功能正常，表单可打开
    Failure Indicators: 列表加载失败或表单无法打开
    Evidence: .sisyphus/evidence/task-13-teacher-exams.png
  ```

  **Commit**: NO

---

- [ ] 14. 教师成绩批改测试

  **What to do**:
  - 以教师身份登录
  - 导航到成绩批改页面 (通过考试管理)
  - 验证提交列表显示
  - 测试批改界面 (查看但不修改)
  - 截图记录

  **Must NOT do**:
  - 不要实际修改成绩

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要验证批改流程和界面

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 11, 12, 13)
  - **Blocks**: Tasks 18-20
  - **Blocked By**: Tasks 4-6

  **References**:
  - 成绩批改: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/teacher/GradeSubmissionScreen.kt`
  - 考试提交: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/teacher/ExamSubmissions.kt`

  **Acceptance Criteria**:
  - [ ] 成绩批改页面可访问
  - [ ] 提交列表显示
  - [ ] 批改界面可打开

  **QA Scenarios**:

  ```
  Scenario: 教师成绩批改
    Tool: ADB
    Preconditions: 教师已登录，存在已提交的考试
    Steps:
      1. 导航到考试管理页面
      2. 选择一个已发布的考试
      3. 点击 "查看提交"
      4. 验证提交列表显示
      5. 点击一个提交记录
      6. 验证批改界面显示
      7. 调用 android_adb_take_screenshot 截图
    Expected Result: 成绩批改功能可访问，界面正常
    Failure Indicators: 提交列表为空或批改界面无法打开
    Evidence: .sisyphus/evidence/task-14-teacher-grading.png
  ```

  **Commit**: NO

---

- [x] 15. 管理员用户管理测试

  **What to do**:
  - 以管理员身份登录
  - 导航到用户管理页面
  - 验证用户列表显示
  - 测试用户搜索功能
  - 测试用户角色筛选
  - 测试创建用户功能 (填写表单但取消提交)
  - 截图记录

  **Must NOT do**:
  - 不要实际创建或修改用户

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要验证复杂CRUD和筛选功能

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 5 (with Tasks 16, 17)
  - **Blocks**: Tasks 18-20
  - **Blocked By**: Tasks 4-6

  **References**:
  - 用户管理: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/admin/UserManageScreen.kt`

  **Acceptance Criteria**:
  - [ ] 用户管理页面加载成功
  - [ ] 用户列表显示
  - [ ] 搜索和筛选功能可用

  **QA Scenarios**:

  ```
  Scenario: 管理员用户管理
    Tool: ADB
    Preconditions: 管理员已登录
    Steps:
      1. 点击底部导航 "用户管理"
      2. 等待用户列表加载 (3秒)
      3. 调用 android_adb_take_screenshot 截图
      4. 测试搜索功能：输入 "student" 搜索
      5. 验证搜索结果过滤
      6. 测试角色筛选：选择 "教师" 角色
      7. 验证列表更新
      8. 点击 "添加用户" 按钮
      9. 验证创建用户表单显示
      10. 点击返回键取消
    Expected Result: 用户管理功能正常，搜索和筛选可用
    Failure Indicators: 列表加载失败或搜索无效
    Evidence: .sisyphus/evidence/task-15-admin-users.png
  ```

  **Commit**: NO

---

- [x] 16. 管理员系统设置测试 (部分完成 - 无法通过坐标点击打开更多菜单)

  **What to do**:
  - 以管理员身份登录
  - 导航到系统设置页面
  - 验证设置选项显示
  - 测试AI配置查看
  - 截图记录

  **Must NOT do**:
  - 不要修改系统设置

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要验证设置界面和配置显示

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 5 (with Tasks 15, 17)
  - **Blocks**: Tasks 18-20
  - **Blocked By**: Tasks 4-6

  **References**:
  - 系统设置: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/admin/SystemSettingsScreen.kt`

  **Acceptance Criteria**:
  - [ ] 系统设置页面加载成功
  - [ ] 设置选项显示完整
  - [ ] AI配置可查看

  **QA Scenarios**:

  ```
  Scenario: 管理员系统设置
    Tool: ADB
    Preconditions: 管理员已登录
    Steps:
      1. 点击底部导航 "系统"
      2. 等待系统设置加载 (2秒)
      3. 调用 android_adb_take_screenshot 截图
      4. 验证AI配置区域显示
      5. 测试各个设置选项的显示
    Expected Result: 系统设置页面正常，配置可查看
    Failure Indicators: 页面加载失败或配置不显示
    Evidence: .sisyphus/evidence/task-16-admin-settings.png
  ```

  **Commit**: NO

---

- [x] 17. 管理员数据统计测试

  **What to do**:
  - 以管理员身份登录
  - 验证管理员首页数据统计显示
  - 测试统计图表加载
  - 截图记录

  **Must NOT do**:
  - 不要修改统计数据

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要验证数据可视化组件

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 5 (with Tasks 15, 16)
  - **Blocks**: Tasks 18-20
  - **Blocked By**: Tasks 4-6

  **References**:
  - 管理员首页: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/admin/AdminDashboardScreen.kt`

  **Acceptance Criteria**:
  - [ ] 管理员首页加载成功
  - [ ] 统计数据显示
  - [ ] 图表组件渲染正常

  **QA Scenarios**:

  ```
  Scenario: 管理员数据统计
    Tool: ADB
    Preconditions: 管理员已登录
    Steps:
      1. 验证当前页面为管理员首页
      2. 调用 android_adb_check_element_exists text="系统概览"
      3. 验证用户统计显示
      4. 验证课程统计显示
      5. 验证考试统计显示
      6. 调用 android_adb_take_screenshot 截图
    Expected Result: 管理员首页显示完整统计数据
    Failure Indicators: 统计数据不显示或图表加载失败
    Evidence: .sisyphus/evidence/task-17-admin-stats.png
  ```

  **Commit**: NO

---

- [ ] 18. 异常输入测试

  **What to do**:
  - 测试登录表单的异常输入
  - 测试空输入提交
  - 测试超长输入
  - 测试特殊字符输入
  - 截图记录错误提示

  **Must NOT do**:
  - 不要测试SQL注入等安全漏洞

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要验证表单验证和错误处理

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 6 (with Tasks 19, 20)
  - **Blocks**: Task F1
  - **Blocked By**: Tasks 7-17

  **References**:
  - 登录页面: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/auth/LoginScreen.kt`

  **Acceptance Criteria**:
  - [ ] 空输入显示错误提示
  - [ ] 超长输入被正确处理
  - [ ] 特殊字符不导致崩溃

  **QA Scenarios**:

  ```
  Scenario: 登录表单空输入验证
    Tool: ADB
    Preconditions: 应用在登录页面
    Steps:
      1. 不输入任何内容
      2. 点击登录按钮
      3. 验证显示 "请输入用户名" 错误提示
      4. 输入用户名，不输入密码
      5. 点击登录按钮
      6. 验证显示 "请输入密码" 错误提示
      7. 调用 android_adb_take_screenshot 截图
    Expected Result: 表单验证正确，显示相应错误提示
    Failure Indicators: 无错误提示或提示信息错误
    Evidence: .sisyphus/evidence/task-18-empty-input.png

  Scenario: 登录表单超长输入
    Tool: ADB
    Preconditions: 应用在登录页面
    Steps:
      1. 输入超长用户名 (1000字符)
      2. 输入超长密码 (1000字符)
      3. 点击登录按钮
      4. 验证应用不崩溃
      5. 验证显示合理的错误提示
      6. 调用 android_adb_take_screenshot 截图
    Expected Result: 应用不崩溃，显示错误提示
    Failure Indicators: 应用崩溃或ANR
    Evidence: .sisyphus/evidence/task-18-long-input.png
  ```

  **Commit**: NO

---

- [ ] 19. 权限验证测试

  **What to do**:
  - 测试学生访问管理员页面
  - 测试教师访问管理员页面
  - 测试未登录访问受保护页面
  - 截图记录权限处理

  **Must NOT do**:
  - 不要修改权限配置

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要验证角色权限控制

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 6 (with Tasks 18, 20)
  - **Blocks**: Task F1
  - **Blocked By**: Tasks 7-17

  **References**:
  - 导航逻辑: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/navigation/NavigationScreen.kt`
  - 路由定义: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/navigation/NavigationItem.kt`

  **Acceptance Criteria**:
  - [ ] 学生无法访问管理员页面
  - [ ] 教师无法访问管理员专属功能
  - [ ] 权限错误被正确处理

  **QA Scenarios**:

  ```
  Scenario: 学生权限验证
    Tool: ADB
    Preconditions: 学生已登录
    Steps:
      1. 验证底部导航不显示 "用户管理"
      2. 验证底部导航不显示 "系统"
      3. 验证底部导航显示 "课程" (非 "课程管理")
      4. 调用 android_adb_take_screenshot 截图
    Expected Result: 学生只能看到学生权限的导航项
    Failure Indicators: 显示了管理员专属的导航项
    Evidence: .sisyphus/evidence/task-19-student-permissions.png

  Scenario: 教师权限验证
    Tool: ADB
    Preconditions: 教师已登录
    Steps:
      1. 验证底部导航显示 "课程管理"
      2. 验证底部导航显示 "题目管理"
      3. 验证底部导航显示 "考试管理"
      4. 验证底部导航不显示 "用户管理"
      5. 验证底部导航不显示 "系统"
      6. 调用 android_adb_take_screenshot 截图
    Expected Result: 教师只能看到教师权限的导航项
    Failure Indicators: 显示了管理员专属的导航项
    Evidence: .sisyphus/evidence/task-19-teacher-permissions.png
  ```

  **Commit**: NO

---

- [ ] 20. 网络错误处理测试

  **What to do**:
  - 测试网络断开时的应用行为
  - 测试服务器无响应时的处理
  - 验证错误提示显示
  - 截图记录错误处理

  **Must NOT do**:
  - 不要长时间断开网络 (影响其他测试)

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要验证网络异常处理

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 6 (with Tasks 18, 19)
  - **Blocks**: Task F1
  - **Blocked By**: Tasks 7-17

  **References**:
  - HTTP配置: `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/data/api/HttpClientConfig.kt`

  **Acceptance Criteria**:
  - [ ] 网络断开时显示错误提示
  - [ ] 应用不崩溃
  - [ ] 网络恢复后可正常使用

  **QA Scenarios**:

  ```
  Scenario: 网络断开处理
    Tool: ADB
    Preconditions: 应用已登录，网络正常
    Steps:
      1. 调用 android_adb_toggle_wifi enable=false 关闭WiFi
      2. 等待2秒
      3. 尝试刷新页面或执行网络操作
      4. 验证显示网络错误提示
      5. 调用 android_adb_take_screenshot 截图
      6. 调用 android_adb_toggle_wifi enable=true 恢复WiFi
      7. 等待3秒
      8. 验证应用恢复正常
    Expected Result: 网络断开时显示错误提示，恢复后正常工作
    Failure Indicators: 应用崩溃或无错误提示
    Evidence: .sisyphus/evidence/task-20-network-error.png
  ```

  **Commit**: NO

---

- [ ] F1. 生成完整测试报告

  **What to do**:
  - 汇总所有测试结果
  - 整理错误记录
  - 生成测试报告文档
  - 包含截图引用和错误详情

  **Must NOT do**:
  - 不要删除测试证据文件

  **Recommended Agent Profile**:
  - **Category**: `writing`
    - Reason: 需要生成文档报告

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave FINAL
  - **Blocks**: None
  - **Blocked By**: Tasks 1-20

  **References**:
  - 测试证据目录: `.sisyphus/evidence/`

  **Acceptance Criteria**:
  - [ ] 测试报告生成成功
  - [ ] 包含所有测试用例结果
  - [ ] 包含错误详情和截图引用

  **QA Scenarios**:

  ```
  Scenario: 生成测试报告
    Tool: Bash
    Preconditions: 所有测试任务完成
    Steps:
      1. 收集 .sisyphus/evidence/ 目录下所有截图
      2. 汇总测试结果
      3. 生成测试报告文档
      4. 验证报告格式正确
    Expected Result: 测试报告生成成功
    Failure Indicators: 报告生成失败或格式错误
    Evidence: .sisyphus/evidence/test-report.md
  ```

  **Commit**: NO

---

## Final Verification Wave

- [ ] F1. **测试报告完整性检查**
  - 验证所有测试用例都有结果记录
  - 验证所有失败用例都有错误详情
  - 验证截图文件存在

---

## Commit Strategy

- **测试报告**: `docs(test): ADB自动化测试报告` - test-report.md

---

## Success Criteria

### 验证命令
```bash
# 检查测试报告是否存在
ls -la .sisyphus/evidence/test-report.md

# 检查截图目录
ls -la .sisyphus/evidence/screenshots/
```

### 最终检查清单
- [ ] 所有角色登录测试通过
- [ ] 学生功能测试完成
- [ ] 教师功能测试完成
- [ ] 管理员功能测试完成
- [ ] 边界测试完成
- [ ] 测试报告生成
- [ ] 所有错误记录到文档

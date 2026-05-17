# 客户端页面触发路径与层级梳理

本文档基于当前 `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem` 代码梳理跨平台客户端的页面入口、角色导航、二级页面、Dialog 与主要触发路径。后端不直接提供页面，但文末补充了主要页面对应的后端接口域。

## 1. 应用入口与全局层级

入口链路：

```text
App()
└─ PlatformKoinApplication
   └─ MainAppContent()
      ├─ AuthState.Loading -> 全屏加载
      ├─ AuthState.Unauthenticated / Error
      │  ├─ LoginScreen
      │  └─ RegisterScreen
      └─ AuthState.Authenticated
         └─ AuthenticatedContent
            ├─ ExamTakingScreen  考试模式，全屏隐藏主导航
            └─ 主导航布局
               ├─ MobileLayout   COMPACT，底部导航栏
               ├─ RailLayout     MEDIUM，侧边 NavigationRail
               └─ DesktopLayout  EXPANDED，PermanentNavigationDrawer
```

全局弹窗：

- `GlobalDialog`：应用级 Dialog，由 `DialogManager` 控制，覆盖在所有页面之上。
- 主导航返回：`NavigationManager.navigationHistory` 非空时，系统返回键触发 `popBack()`。
- 考试模式：`NavigationManager.enterExamMode(examId)` 后进入 `ExamTakingScreen`，主导航点击被禁止。

## 2. 登录注册层

```text
未认证状态
├─ LoginScreen
│  ├─ 输入账号/密码 -> 登录
│  ├─ 一键填充测试账号：管理员/教师/学生
│  ├─ 显示/隐藏密码
│  └─ 切换注册 -> RegisterScreen
└─ RegisterScreen
   ├─ 输入用户名、密码、确认密码、姓名、邮箱等
   ├─ 注册成功 -> 返回 LoginScreen
   └─ 切换登录 -> LoginScreen
```

## 3. 主路由表

主路由由 `NavigationScreen(route, navigationManager)` 分发：

| Route | 页面 | 角色规则 |
|---|---|---|
| `home` | 管理员：`AdminDashboardScreen`；教师/学生：`DashboardScreen` | 全角色 |
| `courses` | 管理员/教师：`CourseManageScreen`；学生：`StudentCourseScreen` | 全角色，按角色分流 |
| `exams` | 管理员/教师：`TeacherExamManageScreen`；学生：`ExamsScreen` | 全角色，按角色分流 |
| `users` | `UserManageScreen` | 管理员 |
| `profile` | `ProfileScreen` | 全角色 |
| `question_banks` | `QuestionBankScreen` | 教师、管理员 |
| `system_settings` | `SystemSettingsScreen` | 管理员；非管理员回退首页 |

## 4. 角色导航结构

### 学生

```text
底部/侧边导航
├─ 首页 home -> DashboardScreen
├─ 课程 courses -> StudentCourseScreen
├─ 考试 exams -> ExamsScreen
└─ 我的 profile -> ProfileScreen
```

### 教师

```text
底部/侧边导航
├─ 首页 home -> DashboardScreen
├─ 课程管理 courses -> CourseManageScreen
├─ 考试管理 exams -> TeacherExamManageScreen
├─ 题库管理 question_banks -> QuestionBankScreen
└─ 我的 profile -> ProfileScreen
```

### 管理员

移动端主导航最多展示 4 个主项，超出项进入“更多”底部抽屉：

```text
底部/侧边导航
├─ 首页 home -> AdminDashboardScreen
├─ 用户管理 users -> UserManageScreen
├─ 课程管理 courses -> CourseManageScreen
├─ 我的 profile -> ProfileScreen
└─ 更多
   ├─ 考试管理 exams -> TeacherExamManageScreen
   ├─ 题库管理 question_banks -> QuestionBankScreen
   └─ 系统设置 system_settings -> SystemSettingsScreen
```

桌面端使用永久抽屉，管理员所有导航项直接展示。

## 5. 首页层级

### 管理员首页

```text
home -> AdminDashboardScreen
├─ 管理仪表盘统计卡片
│  ├─ 用户
│  ├─ 学生
│  ├─ 教师
│  ├─ 管理员
│  ├─ 课程
│  ├─ 考试
│  ├─ 题目
│  └─ 提交
└─ 课程通过率概览
```

当前管理员首页主要是数据展示页，不在该页面内直接触发主路由跳转。

### 教师/学生首页

```text
home -> DashboardScreen
├─ DashboardHeader 用户卡片/问候语
├─ DashboardNotifications 系统通知卡片
│  ├─ 通知堆叠态
│  ├─ 点击展开/收起
│  └─ 通知项点击/标记已读相关操作
├─ DashboardExams 考试概览
│  └─ 查看考试 -> exams
└─ 课程入口/课程概览
   └─ 查看课程 -> courses
```

## 6. 课程页面层级

### 学生课程中心

```text
courses -> StudentCourseScreen
├─ 顶栏
│  └─ 刷新
├─ Tab：全部课程
│  ├─ 课程卡片
│  │  ├─ 选课 -> enrollCourse
│  │  └─ 查看课程考试 -> CourseExamsDialog
│  └─ 下拉刷新
├─ Tab：已选课程
│  ├─ 课程卡片
│  │  ├─ 退课 -> WithdrawCourseDialog
│  │  └─ 查看课程考试 -> CourseExamsDialog
│  └─ 下拉刷新
├─ WithdrawCourseDialog
│  └─ 确认退课
└─ CourseExamsDialog
   └─ 展示该课程下考试列表
```

### 教师/管理员课程管理

```text
courses -> CourseManageScreen
├─ 顶栏/桌面页头
│  ├─ 新建课程 -> CourseFormDialog
│  └─ 刷新
├─ 搜索框
├─ 课程卡片列表
│  ├─ 选课管理 -> EnrollmentManageDialog
│  ├─ 编辑 -> CourseFormDialog
│  └─ 删除 -> 删除确认 Dialog
├─ CourseFormDialog
│  ├─ 创建课程
│  └─ 编辑课程
└─ EnrollmentManageDialog
   ├─ 已选学生列表
   │  └─ 移除学生
   └─ 批量添加学生 -> StudentSelector
      ├─ 搜索/筛选学生
      ├─ 勾选学生
      └─ 确认添加
```

## 7. 考试页面层级

### 学生考试

```text
exams -> ExamsScreen
├─ 顶栏
│  └─ 刷新
├─ Tab：可参加
│  └─ 考试卡片
│     └─ 开始考试 -> navigationManager.enterExamMode(examId)
│        └─ ExamTakingScreen
└─ Tab：已结束
   └─ 考试卡片
      └─ 展示分数/结果信息
```

考试答题全屏层级：

```text
ExamTakingScreen
├─ Loading / Error / Ready / Submitted 状态
├─ Ready -> ExamContent
│  ├─ 顶栏
│  │  ├─ 倒计时
│  │  ├─ 监考切屏次数
│  │  └─ 交卷按钮
│  ├─ 答题展示模式
│  │  ├─ 单页单题
│  │  │  ├─ 上一题
│  │  │  └─ 下一题
│  │  └─ 列表展示
│  ├─ 答案输入/选择
│  ├─ 失焦/切屏上报 -> recordProctoringEvent
│  ├─ 监考提示 Dialog
│  ├─ 强制交卷 Dialog
│  └─ 交卷确认 Dialog
└─ Submitted -> ExamResultSummary
   └─ 退出考试 -> exams
```

### 教师/管理员考试管理

```text
exams -> TeacherExamManageScreen
├─ 顶栏/桌面页头
│  ├─ 新建考试 -> ExamFormDialog
│  ├─ 批量模式
│  └─ 刷新
├─ Tab：未开始/草稿
│  └─ 考试卡片
│     ├─ 编辑 -> ExamFormDialog
│     ├─ 删除 -> 删除确认 Dialog
│     ├─ 发布 -> publishExam
│     ├─ 组卷 -> ExamComposeScreen
│     └─ 批量选择 -> 批量删除确认 Dialog
├─ Tab：进行中/已发布
│  └─ 考试卡片
│     └─ 查看提交/阅卷 -> ExamSubmissionsScreen
└─ Tab：已结束
   └─ 考试卡片
      └─ 查看提交/阅卷 -> ExamSubmissionsScreen
```

考试表单：

```text
ExamFormDialog
├─ 课程选择
├─ 标题/描述
├─ 时长/总分
├─ 开始时间 -> DateTimePickerDialog
└─ 结束时间 -> DateTimePickerDialog
```

组卷页面：

```text
TeacherExamManageScreen
└─ 组卷 -> ExamComposeScreen
   ├─ 顶栏
   │  ├─ 返回
   │  └─ 智能组卷 -> RandomComposeDialog
   ├─ 桌面端：左右双栏
   │  ├─ 已选题目
   │  │  └─ 移除题目
   │  └─ 题库选题
   │     ├─ 搜索题目
   │     ├─ 题型筛选
   │     └─ 添加/移除题目
   └─ 移动端：Tab
      ├─ 已选
      │  └─ 移除题目
      └─ 题库选题
         ├─ 搜索题目
         ├─ 题型筛选
         └─ 添加/移除题目
```

智能组卷：

```text
RandomComposeDialog
├─ 选择题库
├─ 目标总分
├─ 分段配置
│  ├─ 题型
│  ├─ 难度
│  ├─ 数量
│  └─ 分值
├─ 随机打乱
├─ 宽松模式
└─ 确认 -> composeRandomExam
```

阅卷与监考：

```text
TeacherExamManageScreen
└─ 查看提交/阅卷 -> ExamSubmissionsScreen
   ├─ 提交列表
   │  ├─ 点击提交卡片 -> GradeSubmissionScreen
   │  └─ 监考记录 -> ProctoringDialog
   ├─ ProctoringDialog
   │  ├─ 切屏/失焦次数
   │  ├─ 提交状态
   │  └─ 事件明细
   └─ GradeSubmissionScreen
      ├─ 客观题得分展示
      ├─ 主观题列表
      │  ├─ 学生答案
      │  ├─ 参考答案
      │  ├─ 得分输入
      │  ├─ 批语输入
      │  └─ AI 判分
      └─ 保存批改 -> submitGrades -> 返回提交列表
```

## 8. 题库页面层级

```text
question_banks -> QuestionBankScreen
├─ 顶栏/桌面页头
│  ├─ 新建题库 -> EditBankDialog
│  ├─ 下载模板
│  ├─ 导入题目
│  └─ 刷新
├─ 桌面端
│  ├─ 左侧 BankListPanel
│  │  ├─ 搜索题库
│  │  ├─ 选择题库
│  │  ├─ 编辑题库 -> EditBankDialog
│  │  └─ 删除题库 -> DeleteBankDialog
│  └─ 右侧 QuestionListPanel
│     ├─ 新建题目 -> QuestionFormDialog
│     ├─ 导入题目
│     ├─ 搜索题目
│     ├─ 题型筛选
│     ├─ 难度筛选
│     ├─ 编辑题目 -> QuestionFormDialog
│     └─ 删除题目 -> removeQuestionFromBank
└─ 移动端
   ├─ 题库列表态
   │  └─ 点击题库 -> 进入题目列表态
   └─ 题目列表态
      ├─ 返回题库列表
      └─ 同 QuestionListPanel
```

题目表单：

```text
QuestionFormDialog
├─ 题目内容
├─ 题目类型：单选、多选、判断、填空、简答
├─ 难度
├─ 选项管理：选择题显示
├─ 正确答案
├─ 分值
├─ 分类
└─ 解析
```

## 9. 用户管理页面层级

```text
users -> UserManageScreen
├─ 顶栏/桌面页头
│  ├─ 批量模式
│  ├─ 新建用户 -> CreateUserDialog
│  └─ 刷新
├─ 筛选区
│  ├─ 关键字：用户名/姓名/邮箱
│  ├─ 角色：全部/学生/教师/管理员
│  └─ 状态：全部/启用/禁用
├─ 用户列表
│  ├─ 启用/禁用
│  ├─ 重置密码 -> ResetPasswordDialog
│  ├─ 编辑用户 -> EditUserDialog
│  └─ 删除用户 -> 删除确认 Dialog
├─ 分页栏
└─ 批量模式
   ├─ 全选
   ├─ 勾选用户
   └─ 批量删除 -> 批量删除确认 Dialog
```

## 10. 我的页面层级

```text
profile -> ProfileScreen
├─ ProfileMainScreen
│  ├─ 用户信息卡片 -> EditProfileDialog
│  │  ├─ 编辑姓名/邮箱/头像
│  │  ├─ 上传头像
│  │  └─ 修改密码按钮 -> ChangePasswordDialog
│  ├─ 学生：考试历史 -> GradeHistoryScreen
│  ├─ 通知中心 -> NotificationScreen
│  ├─ 帮助中心/使用说明 -> HelpDialog
│  ├─ 设置 -> AppSettingsScreen
│  ├─ 关于 -> AboutScreen
│  └─ 退出登录
├─ EditProfileDialog
├─ ChangePasswordDialog
└─ HelpDialog
```

学生考试历史：

```text
ProfileScreen
└─ 考试历史 -> GradeHistoryScreen
   ├─ 成绩统计
   ├─ 历史提交记录
   └─ 点击记录 -> GradeDetailScreen
      ├─ 总分
      ├─ 每题作答
      ├─ 正确答案/解析
      └─ 主观题教师批语/得分
```

通知中心：

```text
ProfileScreen
└─ 通知中心 -> NotificationScreen
   ├─ 顶栏
   │  ├─ 返回
   │  └─ 更多菜单
   │     ├─ 刷新通知
   │     └─ 全部已读
   ├─ 通知列表
   │  ├─ 标记已读
   │  └─ 删除通知
   ├─ 加载更多
   └─ 管理员：发送通知 -> SendNotificationDialog
```

设置页面：

```text
ProfileScreen
└─ 设置 -> AppSettingsScreen
   └─ AppSettingsContent
      ├─ 主题设置
      │  ├─ 跟随系统/日间/夜间
      │  ├─ 莫奈/系统色
      │  └─ 自定义主题色
      ├─ 考试设置
      │  └─ 单页单题/列表展示
      ├─ 字体大小
      └─ 下载原神 -> 外部浏览器 https://ys.mihoyo.com/
```

关于页面：

```text
ProfileScreen
└─ 关于 -> AboutScreen
   ├─ VersionCode
   ├─ VersionName
   ├─ 客户端 GitHub
   ├─ 后端 GitHub
   └─ 检查更新 -> GitHub Releases latest
```

## 11. 管理员系统设置 / AI 配置

```text
system_settings -> SystemSettingsScreen
├─ 顶栏/桌面页头
│  └─ 刷新
└─ SystemSettingsForm
   ├─ 模型来源
   │  ├─ 默认模型：固定 Kimi，不展示 Base URL / Model Name / API Key
   │  └─ 自定义模型
   │     ├─ 兼容服务预设
   │     ├─ 自定义 Base URL
   │     ├─ Model Name
   │     └─ API Key
   ├─ 评分参数
   │  ├─ 系统提示词
   │  ├─ Temperature
   │  ├─ Max Tokens
   │  └─ 并发数
   ├─ 重置
   └─ 保存配置
```

## 12. 开发/测试页面

当前代码中存在以下测试/示例页面，但未接入正式 `NavigationScreen` 主路由：

- `ApiTestScreen`
- `FileTestScreen`
- `ImageTestScreen`
- `DialogExampleScreen`

这些页面属于开发调试入口，正常用户路径不可达。若后续需要保留，应考虑统一放入 Debug 构建专用入口；若不再使用，应清理或标注为内部测试页面。

## 13. 页面与后端接口域对应关系

| 页面/功能 | 后端接口域 |
|---|---|
| 登录/注册/当前用户/修改资料/修改密码 | `/api/auth` |
| 用户管理 | `/api/admin/users` |
| 课程中心/课程管理/选课管理 | `/api/courses` |
| 考试列表/考试管理/发布考试/组卷 | `/api/exams` |
| 考试提交/阅卷/监考记录 | `/api/submissions` |
| 成绩历史/统计数据 | `/api/statistics` |
| 题库管理 | `/api/question-banks` |
| 题目管理 | `/api/questions` |
| 题目导入导出/模板下载 | `/api/question-import-export` |
| 通知中心/发布通知 | `/api/notifications` |
| 头像/文件上传 | `/api/files` |
| AI 判题与 AI 配置 | `/api/ai-grading` |

## 14. 总体页面树

```text
在线考试系统
├─ 未登录
│  ├─ 登录
│  └─ 注册
└─ 已登录
   ├─ 首页
   │  ├─ 管理员仪表盘
   │  └─ 教师/学生首页
   ├─ 课程
   │  ├─ 学生课程中心
   │  │  ├─ 全部课程
   │  │  ├─ 已选课程
   │  │  ├─ 退课确认
   │  │  └─ 课程考试列表
   │  └─ 课程管理
   │     ├─ 新建/编辑课程
   │     ├─ 删除课程
   │     └─ 选课管理
   ├─ 考试
   │  ├─ 学生考试列表
   │  │  ├─ 可参加
   │  │  ├─ 已结束
   │  │  └─ 考试答题
   │  └─ 教师/管理员考试管理
   │     ├─ 新建/编辑考试
   │     ├─ 发布考试
   │     ├─ 删除/批量删除考试
   │     ├─ 组卷
   │     │  ├─ 已选题目
   │     │  ├─ 题库选题
   │     │  └─ 智能组卷
   │     └─ 答卷批阅
   │        ├─ 提交列表
   │        ├─ 手动阅卷
   │        ├─ AI 判分
   │        └─ 监考记录
   ├─ 题库管理
   │  ├─ 题库列表
   │  ├─ 新建/编辑/删除题库
   │  ├─ 题目列表
   │  ├─ 新建/编辑/删除题目
   │  └─ 模板下载/导入题目
   ├─ 用户管理
   │  ├─ 用户列表
   │  ├─ 新建/编辑用户
   │  ├─ 启用/禁用
   │  ├─ 重置密码
   │  └─ 删除/批量删除
   ├─ 我的
   │  ├─ 修改资料
   │  ├─ 修改密码
   │  ├─ 考试历史
   │  │  └─ 答卷解析详情
   │  ├─ 通知中心
   │  │  └─ 管理员发布通知
   │  ├─ 帮助
   │  ├─ 设置
   │  ├─ 关于
   │  └─ 退出登录
   └─ 管理员系统设置
      └─ AI 配置
```

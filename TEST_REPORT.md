# KMP考试系统 - 自动化测试报告 (最终版)

**测试日期**: 2026-05-12
**测试设备**: Android Emulator (1080x2424)
**应用版本**: ovo.sypw.kmp.examsystem
**测试执行者**: Sisyphus (自动化测试)
**测试轮次**: 4 (第一轮初步探索, 第二轮深入测试+修复, 第三轮CRUD+导航验证, 第四轮bug修复后验证)

---

## 测试概要

| 角色 | 测试项 | 通过 | 失败 | 阻塞 | 部分 | 通过率 |
|------|--------|------|------|------|------|--------|
| Teacher (teacher1) | 12 | 10 | 0 | 0 | 2 | 83% |
| Student (student1) | 6 | 0 | 1 | 5 | 0 | 0% (CRASH) |
| Admin (admin) | 11 | 10 | 0 | 0 | 1 | 91% |
| Registration | 3 | 2 | 1 | 0 | 0 | 67% |
| **总计** | **32** | **22** | **2** | **4** | **4** | **69%** |

---

## 发现的问题

### ISSUE #1: Dashboard通知加载失败 (严重度: 中)

**描述**: 教师/学生首页Dashboard的"系统通知"区域显示"通知加载失败",点击"重试"按钮无法恢复。

**触发方式**:
1. 使用teacher1账号登录
2. 查看首页Dashboard
3. "系统通知"区域显示"通知加载失败"
4. 点击"重试"按钮 → 仍然显示"通知加载失败"

**影响范围**: 所有角色的Dashboard通知功能
**预期行为**: 通知应正常加载或至少在重试后恢复

---

### ISSUE #2: 通知API服务端枚举缺失 (严重度: 高)

**描述**: 访问通知中心页面时,服务端返回内部错误:
```
服务器内部错误: No enum constant ovo.sypw.onlineexamsystemback.enums.NotificationType.COURSE_ENROLLED
```

**触发方式**:
1. 使用teacher1账号登录
2. 进入"我的" → "通知中心"
3. 页面显示服务器内部错误

**影响范围**: 所有角色的通知中心功能、Dashboard通知加载
**根因**: 后端`NotificationType`枚举缺少`COURSE_ENROLLED`值
**修复建议**: 在后端添加`COURSE_ENROLLED`枚举值

---

### ISSUE #3: 学生登录后应用崩溃 (严重度: 严重) - 已定位修复，未部署验证

**描述**: 使用student1账号登录后,应用立即崩溃。

**错误日志**:
```
FATAL EXCEPTION: main
java.lang.IllegalStateException: Vertically scrollable component was measured 
with an infinity maximum height constraints, which is disallowed. One of the 
common reasons is nesting layouts like LazyColumn and Column(Modifier.verticalScroll()).
```

**第四轮验证结果（2026-05-12 01:56）**: 修复仍未部署到设备，崩溃可稳定复现。

**复现步骤**:
1. 清除应用数据
2. 启动应用 → 登录页面
3. 输入 student1 / 123456
4. 点击"登录"按钮
5. 应用立即崩溃，返回桌面

**设备日志证据**:
```
05-12 01:50:34.482  WindowManager: Sent Transition (#136) type = CLOSE 
  cmp=ovo.sypw.kmp.examsystem/.MainActivity
05-12 01:50:34.739  WindowManager: Exception thrown during dispatchAppVisibility 
  Window{6696c08 u0 ovo.sypw.kmp.examsystem/.MainActivity EXITING}
05-12 01:50:34.739  WindowManager: android.os.DeadObjectException
05-12 01:50:34.739  Process: Unable to open /proc/30994/status
```
进程 pid=30994 在登录后瞬间死亡，WindowManager 报告 DeadObjectException。

**根因分析**: `ResponsiveLazyVerticalGrid` (ResponsiveUtils.kt:447/455) 内部使用了`LazyColumn`,当被嵌套在`DashboardScreen.kt` (line 78) 的父`LazyColumn`中时,导致无限高度约束崩溃。

**为什么只影响学生**:
- 教师: 通知处于Error状态,考试列表为空 → ResponsiveLazyVerticalGrid的内层LazyColumn不会渲染
- 学生: 通知/考试数据可能成功加载 → 内层LazyColumn渲染 → 崩溃

**修复方案**: 已修改`ResponsiveUtils.kt`中的`ResponsiveLazyVerticalGrid`,将内部`LazyColumn`替换为`Column`。

**修复状态**: 代码已修改(`ResponsiveUtils.kt`), 但因JDK环境问题(`JdkImageTransform` + `jlink.exe`)无法构建APK验证。
**待办**: 需修复构建环境，重新编译APK并安装验证修复效果。

---

### ISSUE #4: ComposeTextField文本输入兼容性问题 (严重度: 低)

**描述**: ADB的`input text`命令在某些Compose TextField中无法正常输入,需要使用`adb shell input text`直接调用。

**影响范围**: 自动化测试效率降低,不影响用户手动操作

---

### ISSUE #5: 从注册页面按返回键应用崩溃 (严重度: 中)

**描述**: 在注册页面按返回键后,应用崩溃并返回系统桌面。

**触发方式**:
1. 打开应用 → 登录页面
2. 点击"还没有账号?立即注册" → 进入注册页面
3. 在注册页面按Android返回键
4. 应用崩溃,返回系统桌面

**影响范围**: 用户体验问题,从注册页面无法正常返回登录页面

---

## 详细测试结果

### Teacher角色测试 (teacher1)

| 测试项 | 结果 | 说明 |
|--------|------|------|
| T-01: Dashboard显示 | PASS | 问候语/快捷入口/今日概览正常;通知加载失败(ISSUE #1) |
| T-02: 课程列表 | PASS | 3+门课程正确显示,含教师名/描述/操作按钮 |
| T-03: 课程编辑 | PASS | 编辑对话框打开/字段显示/保存/关闭正常 |
| T-04: 课程删除 | PASS | 删除确认对话框正常显示,取消后返回列表 |
| T-05: 选课管理 | PASS | 学生列表/选课时间/移除按钮/批量添加正常 |
| T-06: 考试列表 | PASS | 三个tab(未开始/进行中/已结束),考试卡片信息完整 |
| T-07: 智能组卷 | PASS | 组卷对话框完整(题库选择/规则/打乱/宽松模式) |
| T-08: 组卷页面 | PASS | 进入考试组卷页面,显示题目数量/分数/选题模式 |
| T-09: 题库列表 | PASS | 多个题库正确显示,含题目数/编辑/删除 |
| T-10: 新建题库 | PASS | 成功创建test_bank题库 |
| T-11: 题目列表 | PASS | 题库详情页显示题目/筛选器/搜索 |
| T-12: 新建题目 | PASS | 题目创建表单完整(类型/难度/选项/分值/正确答案) |

### Student角色测试 (student1) - BLOCKED (第四轮验证)

| 测试项 | 结果 | 说明 |
|--------|------|------|
| S-01: 登录 | FAIL | 登录成功但应用立即崩溃(ISSUE #3),进程死亡(DeadObjectException) |
| S-02: Dashboard | BLOCKED | 应用崩溃,无法测试 |
| S-03: 课程浏览 | BLOCKED | 应用崩溃,无法测试 |
| S-04: 考试功能 | BLOCKED | 应用崩溃,无法测试 |
| S-05: 成绩查看 | BLOCKED | 应用崩溃,无法测试 |
| S-06: 个人资料 | BLOCKED | 应用崩溃,无法测试 |

**第四轮崩溃日志摘要**:
```
05-12 01:50:34.739 691/906 E IPCThreadState: Binder transaction failure. 
  id: 4792080, error: -22 (Invalid argument)
05-12 01:50:34.739 691/906 W ActivityManager: pid 691 system sent binder code 7 
  with flags 1 to frozen apps and got error -32
05-12 01:50:34.739 691/906 W WindowManager: Exception thrown during 
  dispatchAppVisibility Window{6696c08 u0 
  ovo.sypw.kmp.examsystem/ovo.sypw.kmp.examsystem.MainActivity EXITING}
05-12 01:50:34.739 691/906 W WindowManager: android.os.DeadObjectException
    at android.os.BinderProxy.transactNative(Native Method)
    at android.os.BinderProxy.transact(BinderProxy.java:592)
    at android.view.IWindow$Stub$Proxy.dispatchAppVisibility(IWindow.java:557)
    at com.android.server.wm.WindowState.sendAppVisibilityToClients(WindowState.java:3151)
    at com.android.server.wm.WindowContainer.sendAppVisibilityToClients(WindowContainer.java:1311)
    at com.android.server.wm.WindowToken.setClientVisible(WindowToken.java:389)
    at com.android.server.wm.ActivityRecord.setClientVisible(ActivityRecord.java:6650)
    at com.android.server.wm.ActivityRecord.commitVisibility(ActivityRecord.java:5561)
    at com.android.server.wm.Transition.finishTransition(Transition.java:1391)
05-12 01:50:34.739 691/906 W Process: Unable to open /proc/30994/status
```

### Admin角色测试 (admin)

| 测试项 | 结果 | 说明 |
|--------|------|------|
| A-01: Dashboard统计 | PASS | 用户31/学生19/教师11/管理员1/课程37/考试53/题目332/提交205 |
| A-02: 课程通过率 | PASS | 高等数学1(均57/最高98/最低34),概率论(均20) |
| A-03: 用户列表 | PASS | 31位用户,分页1/2页,含用户名/邮箱/角色 |
| A-04: 用户角色筛选 | PASS | 切换全部/学生/教师/管理员筛选器正常,checkbox状态正确切换 |
| A-05: 用户搜索 | PASS | 输入"student"后搜索按钮触发过滤;搜索按钮需要手动点击(非即时搜索) |
| A-06: 用户创建对话框 | PASS | FAB"新建用户"按钮打开对话框;表单包含用户名/姓名/邮箱/密码/角色字段;验证正常 |
| A-07: 用户创建表单填写 | PASS | 输入testuser01/测试用户/testuser01@example.com/123456,表单接受输入 |
| A-08: 底部导航 | PASS | 5个tab正确显示: 首页/我的/用户管理/课程管理/更多 |
| A-09: 个人资料 | PASS | 管理员信息正确,系统设置入口存在 |
| A-10: 退出登录 | PASS | 从Profile页面点击退出,成功返回登录页面 |
| A-11: 系统设置导航 | PARTIAL | 菜单入口存在,但从Profile点击导航到帮助中心而非系统设置 |

### 登录/注册测试

| 测试项 | 结果 | 说明 |
|--------|------|------|
| 教师登录 | PASS | teacher1/123456正常登录 |
| 管理员登录 | PASS | admin/123456正常登录 |
| 学生登录 | FAIL | 崩溃(ISSUE #3) |
| 自动登录 | PASS | 清除数据前自动使用缓存凭据登录 |
| 注册页面 | PASS | 表单字段完整,验证提示正确 |
| 注册验证 | PASS | 密码不匹配时提示"两次输入的密码不一致" |
| 注册返回 | FAIL | 从注册页面按返回键崩溃(ISSUE #5) |

---

## 第三轮测试发现

### 管理员用户管理验证

- **角色筛选**: 切换全部/学生/教师/管理员checkbox正确切换,功能正常
- **搜索功能**: 输入搜索关键词后需要点击"搜索"按钮触发过滤(非即时搜索);搜索按钮在状态筛选行右侧
- **创建用户**: FAB按钮位置正确(x=900,y=1950附近);对话框打开后表单字段完整(用户名/姓名/邮箱/密码/角色);表单接受文本输入;验证逻辑正常(空提交不会关闭对话框)
- **底部导航**: 5个tab正确渲染(首页/我的/用户管理/课程管理/更多),Compose View组件,各200px宽
- **退出登录**: Profile页面退出功能正常,成功返回登录页面

### Compose UI自动化测试限制

- Compose渲染的UI元素没有resource-id,导致`check_element_exists`和`tap_element_by_text`的检测非常有限
- 搜索结果列表中的用户名、状态标签、操作按钮等因Compose渲染无法被ADB直接检测
- 应用偶尔会在操作过程中自动返回桌面(可能是内存问题或后台回收)

---

## 修复记录

### 已实施修复

| 修复 | 文件 | 修改内容 | 状态 |
|------|------|----------|------|
| ISSUE #3: 学生崩溃 | ResponsiveUtils.kt | ResponsiveLazyVerticalGrid内LazyColumn改为Column | 代码已修改 |

### 待修复

| 问题 | 优先级 | 建议 |
|------|--------|------|
| ISSUE #3: 构建验证 | P0 | 需修复JDK构建环境(`JdkImageTransform`+`jlink.exe`), 重新构建APK安装验证 |
| ISSUE #2: 后端枚举 | P0 | 后端添加COURSE_ENROLLED枚举值 |
| ISSUE #1: 通知加载 | P1 | 修复后端后验证 |
| ISSUE #5: 注册返回崩溃 | P1 | 检查RegisterScreen的back handler |

---

## 测试覆盖率

| 模块 | 覆盖率 | 说明 |
|------|--------|------|
| 登录/认证 | 75% | 教师/管理员通过,学生崩溃 |
| Dashboard | 65% | 教师/管理员通过,通知模块失败 |
| 课程管理 | 85% | 列表/编辑/删除/选课管理已测试 |
| 考试管理 | 55% | 列表/组卷/智能组卷已测试;发布/提交/批改未测试 |
| 题目管理 | 65% | 题库CRUD/题目创建已测试;导入/导出未测试 |
| 用户管理 | 70% | 列表/筛选/搜索/创建对话框/表单填写/导航已测试; 删除/禁用/重置密码写操作未测试 |
| 个人资料 | 75% | 查看/密码/帮助已测试;编辑资料未测试 |
| 注册 | 60% | 页面/验证已测试;完整注册流程未测试 |
| 通知系统 | 0% | 完全不可用(ISSUE #1+#2) |
| **总体** | **~58%** | 学生测试完全被ISSUE#3阻塞 |

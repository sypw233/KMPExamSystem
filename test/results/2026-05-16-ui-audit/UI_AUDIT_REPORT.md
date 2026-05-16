# Android UI 审查报告

审查日期：2026-05-16  
设备：Android Emulator `emulator-5554`，1080 x 2424 竖屏  
账号范围：`teacher1`、`student1`、`admin`  
构建验证：`:composeApp:assembleDebug` 通过，Debug APK 已安装到模拟器

## 审查结论

- Android 顶部大面积空白已修复。根因是 Android 外层 `PlatformKoinApplication` 额外包了一层 `Scaffold(contentWindowInsets = WindowInsets.systemBars)`，与页面自身 `Scaffold/TopAppBar/NavigationBar` 的系统栏处理叠加。
- 教师端课程、考试、题库管理页不再出现悬浮按钮遮挡卡片操作区的问题。
- 管理员用户管理页不再出现“新建用户”悬浮按钮遮挡分页区的问题。
- 题库管理页移动端顶部操作从文字按钮改为图标按钮，横向拥挤明显降低。
- 学生端首页、课程、考试、我的页面未发现顶部异常留白、文字缺失或固定操作遮挡。

## 本轮修复

1. 移除 Android 根容器的重复系统栏 Insets，让具体页面自行处理状态栏和导航栏。
2. 将移动端管理页的新增入口移入顶部操作区：
   - 课程管理：顶部新增 `+`
   - 考试管理：顶部新增 `+`
   - 题库管理：顶部新增 `+`
   - 用户管理：顶部新增 `+`
3. 将题库管理移动端的下载模板、导入、刷新操作改为图标操作，避免顶部文本按钮过宽。
4. 让 `ResponsiveLazyVerticalGrid` 正确应用传入的 `contentPadding`。

## 截图证据

修复前后重点截图：

- `screenshots/01-current-after-insets-fix.png`
- `screenshots/02-teacher-course.png`
- `screenshots/03-teacher-exams.png`
- `screenshots/04-teacher-questions.png`
- `screenshots/05-teacher-profile.png`
- `screenshots/06-teacher-home-after-ui-fix.png`
- `screenshots/07-teacher-course-after-ui-fix.png`
- `screenshots/08-teacher-exams-after-ui-fix.png`
- `screenshots/09-teacher-question-bank-after-ui-fix.png`
- `screenshots/10-student-home.png`
- `screenshots/11-student-exams.png`
- `screenshots/12-student-courses.png`
- `screenshots/13-student-profile.png`
- `screenshots/14-admin-dashboard.png`
- `screenshots/15-admin-user-management.png`
- `screenshots/16-admin-course-management.png`
- `screenshots/17-admin-more.png`
- `screenshots/18-admin-user-management-after-ui-fix.png`

对应 UI Tree 已保存在 `ui-dumps/`，文件名与截图一致。

## 仍可继续优化

- 首页和个人页目前整体偏蓝，视觉统一性尚可，但后续可以进一步引入更清晰的角色色和状态色层级。
- 部分长列表首屏底部会自然露出半张卡片，这是可滚动列表的正常状态；当前未发现固定控件遮挡关键按钮。
- 题库详情页仍建议在下一轮继续压缩筛选区高度，让题目列表首屏信息密度更高。

# UI Bug 单

| ID | 严重级别 | 问题 | 影响范围 | 状态 | 证据 |
| --- | --- | --- | --- | --- | --- |
| UI-001 | 高 | Android 顶部出现额外大空白 | 全部 Android 页面 | 已修复 | `01-current-after-insets-fix.png`、`06-teacher-home-after-ui-fix.png` |
| UI-002 | 中 | 课程管理页悬浮按钮遮挡底部卡片操作 | 教师端、管理员端课程管理 | 已修复 | `02-teacher-course.png`、`07-teacher-course-after-ui-fix.png` |
| UI-003 | 中 | 考试管理页悬浮按钮遮挡列表内容 | 教师端考试管理 | 已修复 | `03-teacher-exams.png`、`08-teacher-exams-after-ui-fix.png` |
| UI-004 | 中 | 题库管理页顶部文字操作拥挤，且新增题库入口会遮挡列表 | 题库管理 | 已修复 | `04-teacher-questions.png`、`09-teacher-question-bank-after-ui-fix.png` |
| UI-005 | 中 | 用户管理页“新建用户”悬浮按钮遮挡分页“下一页” | 管理员用户管理 | 已修复 | `15-admin-user-management.png`、`18-admin-user-management-after-ui-fix.png` |

## 本轮未发现的新阻塞问题

- 学生端考试空状态、课程列表、个人中心未发现文字缺失或顶部异常留白。
- 管理员仪表盘、课程管理和更多功能入口未发现固定控件遮挡关键操作。
- 教师端首页、课程管理、考试管理、题库管理在 1080 x 2424 竖屏下未发现新增布局崩坏。

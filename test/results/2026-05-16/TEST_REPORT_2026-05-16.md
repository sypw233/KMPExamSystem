# KMP考试系统 Android 专项测试报告

## 基本信息

| 项目 | 内容 |
|------|------|
| 测试日期 | 2026-05-16 |
| 测试设备 | Android Emulator, 1080x2424, density 420 |
| 应用包名 | ovo.sypw.kmp.examsystem |
| 后端地址 | http://localhost:8080 / Android 10.0.2.2:8080 |
| 测试重点 | 学生考试、教师判题、教师组卷 |
| 测试账号 | admin/student1/teacher1, 密码均为 123456 |

## 测试数据

本轮创建了最小端到端测试数据, 详情见 `test-data.json`。

| 数据 | 值 |
|------|----|
| 课程ID | 105 |
| 题库ID | 57 |
| 单选题ID | 652 |
| 简答题ID | 653 |
| 考试ID | 104 |
| 考试名称 | E2E考试-0516152720 |
| 学生提交ID | 207 |

## 执行概要

| 模块 | 用例数 | 通过 | 失败 | 阻塞 | 备注 |
|------|--------|------|------|------|------|
| 学生登录与首页 | 3 | 3 | 0 | 0 | student1 可进入首页, 未复现历史崩溃 |
| 学生考试列表 | 3 | 1 | 1 | 1 | 后端有可参加考试, App 仍显示暂无考试 |
| 学生答题提交 | 4 | 1 | 0 | 3 | UI 入口阻塞, 通过 API 验证提交链路 |
| 教师考试管理 | 3 | 3 | 0 | 0 | 进行中考试可展示 |
| 教师判题 | 5 | 5 | 0 | 0 | 提交记录、批改详情、保存分数均通过 |
| 教师智能组卷 | 4 | 4 | 0 | 0 | 弹窗控件展示完整, 未执行最终组卷 |
| 合计 | 22 | 17 | 1 | 4 | 通过率约 77% |

## 关键结果

### 学生端

| 用例 | 结果 | 证据 |
|------|------|------|
| STU-EXAM-001 学生登录 | PASS | `screenshots/06-student-token-login.png` |
| STU-EXAM-002 学生首页显示 | PASS | 首页显示 Alice Student、系统通知、即将开始、底部导航 |
| STU-EXAM-003 进入考试列表 | PASS | `screenshots/08-student-exams.png` |
| STU-EXAM-004 可参加考试展示 | FAIL | 后端 `/api/exams/my-available` 返回考试 104, App 显示“暂无考试” |
| STU-EXAM-005 进入考试确认 | BLOCKED | 可参加考试未展示, 无法从 UI 进入 |
| STU-EXAM-006 答题页显示 | BLOCKED | 同上 |
| STU-EXAM-010 提交考试 | PARTIAL PASS | UI 阻塞, API 提交成功, 生成提交ID 207 |

### 教师端

| 用例 | 结果 | 证据 |
|------|------|------|
| TCH-COMP-001 教师登录 | PASS | `screenshots/10-teacher-home.png` |
| TCH-COMP-002 考试管理入口 | PASS | `screenshots/11-teacher-exam-manage.png` |
| TCH-GRADE-001 进行中考试列表 | PASS | `screenshots/12-teacher-exam-ongoing.png` |
| TCH-GRADE-002 进入批阅记录 | PASS | `screenshots/13-teacher-grading-records.png` |
| TCH-GRADE-004 进入批改详情 | PASS | `screenshots/14-teacher-grade-detail.png` |
| TCH-GRADE-005 人工评分保存 | PASS | 输入 55 分并保存 |
| TCH-GRADE-006 批改后状态 | PASS | `screenshots/15-teacher-grade-save.png`, 总分 95, 状态已批改 |
| TCH-COMP-005 智能组卷弹窗 | PASS | `screenshots/16-teacher-random-compose-dialog.png` |

## 后端接口辅助验证

| 接口 | 结果 | 说明 |
|------|------|------|
| POST `/api/auth/login` | PASS | student1/teacher1/admin 均可登录 |
| GET `/api/exams/my-available` | PASS | student1 返回考试 104 |
| POST `/api/submissions` | PASS | student1 提交考试成功 |
| 教师批改保存 | PASS | UI 保存后提交记录变为已批改, 总分 95 |

## 主要发现

1. 学生登录历史 P0 崩溃本轮未复现, 学生首页可正常显示。
2. 学生考试列表存在新的 P0/P1 阻塞问题: 后端已有可参加考试, App 可参加Tab仍显示空态。
3. 教师端进行中考试、批阅记录、人工判题保存链路通过。
4. 智能组卷弹窗基础控件完整, 但最终随机组卷未执行, 建议下一轮补充规则配置和成功组卷断言。
5. ADB 文本输入在 Compose TextField 中仍不稳定, 本轮主要通过 token 注入和 API 辅助完成测试准备。

## 证据目录

截图: `test/results/2026-05-16/screenshots/`

UI结构: `test/results/2026-05-16/dumps/`

接口结果: `student-submission-api.json`, `test-data.json`

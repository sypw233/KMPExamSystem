# 第二轮修复与端到端测试报告

测试时间：2026-05-16 15:42-15:56（Asia/Shanghai）

测试设备：Android Emulator `emulator-5554`

应用包名：`ovo.sypw.kmp.examsystem`

后端地址：`http://localhost:8080`，Android 端访问 `10.0.2.2:8080`

## 本轮修复

1. 学生端考试列表修复
   - 文件：`composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/data/api/ExamApi.kt`
   - 文件：`composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/data/repository/ExamRepository.kt`
   - 原因：`/api/exams/my-available` 和 `/api/exams/my-completed` 返回 Spring Page，但客户端按 List 解析，导致 UI 显示空列表。
   - 修复：API 改为 `PageExamResponse`，Repository 使用 `fetchAllPages` 拉取 `content`。

2. 教师批改页客观分显示修复
   - 文件：`composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/teacher/GradeSubmissionScreen.kt`
   - 原因：后端提交后 `objectiveScore` 字段可能为 null，但 `totalScore` 已包含客观题自动分。
   - 修复：当 `objectiveScore` 为空时，用 `totalScore - subjectiveScore` 兜底展示。

## 构建结果

`.\gradlew.bat --no-configuration-cache :composeApp:assembleDebug` 构建成功。

说明：默认 GraalVM 21 在 Android JDK image `jlink` 阶段失败；切换到 Android Studio JBR 后构建成功。

## 测试数据

- 课程：`E2ECourse0516154725`，courseId=`106`
- 题库：`E2EBank0516154725`，bankId=`58`
- 题目：`654, 655, 656, 657, 658`
- 考试：`E2EExam0516154725`，examId=`105`
- 题型：单选、多选、判断、填空、简答
- 学生提交：submissionId=`208`

## 复测结果

| 场景 | 结果 | 证据 |
|---|---|---|
| 学生首页显示新发布考试 | 通过 | `screenshots/01-student-home.png` |
| 学生“我的考试-可参加”不再显示“暂无考试” | 通过 | `screenshots/02-student-exams-fixed.png` |
| 学生进入考试并显示 5 种题型 | 通过 | `screenshots/03-exam-taking.png`, `screenshots/04-exam-middle.png` |
| 教师考试管理可看到进行中的新考试 | 通过 | `screenshots/09-teacher-ongoing.png` |
| 教师答卷列表可看到学生提交，客观总分 50、待批改 | 通过 | `screenshots/10-teacher-submissions.png` |
| 教师批改详情客观题得分显示为 50 | 通过 | `screenshots/11-grade-detail-fixed.png` |
| 后端批改后总分/状态正确 | 通过 | API 返回 totalScore=90, objectiveScore=50, subjectiveScore=40, status=2 |

## 完整流程覆盖

本轮完整覆盖了以下业务链路：

1. 教师创建课程。
2. 教师创建题库。
3. 教师创建题目，覆盖单选、多选、判断、填空、简答。
4. 教师创建并发布考试。
5. 学生选课后在客户端看到可参加考试。
6. 学生进入考试页面并展示全部题型。
7. 学生提交答卷。
8. 教师查看答卷列表。
9. 教师进入批改详情。
10. 教师批改主观题，最终成绩更新为 90 分。

## 限制说明

当前设备通过 `ime list -s` 只暴露：

- `com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME`
- `com.google.android.tts/com.google.android.apps.speech.tts.googletts.settings.asr.voiceime.VoiceInputMethodService`

未看到常见 ADB Keyboard 输入法包，因此 Compose 文本输入框的长文本 adb 注入仍不稳定。本轮对学生提交和最终教师批改的写入部分使用后端 API 完成，UI 侧验证到页面、题型展示、记录列表和批改详情展示。

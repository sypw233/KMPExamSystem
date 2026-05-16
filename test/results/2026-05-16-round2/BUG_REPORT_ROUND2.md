# 第二轮 Bug 单

## BUG-20260516-001 学生端可参加考试为空

- 状态：已修复，已验证
- 严重级别：P1
- 影响范围：学生端考试入口
- 原因：客户端将后端 Page 响应按 List 解析。
- 修复文件：
  - `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/data/api/ExamApi.kt`
  - `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/data/repository/ExamRepository.kt`
- 验证结果：`E2EExam0516154725` 可在学生“我的考试-可参加”中显示，证据 `screenshots/02-student-exams-fixed.png`。

## BUG-20260516-003 教师批改详情客观题得分显示为 0

- 状态：已修复，已验证
- 严重级别：P2
- 影响范围：教师端阅卷
- 原因：后端提交后 `objectiveScore` 可能为空，客户端没有兜底计算。
- 修复文件：
  - `composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/teacher/GradeSubmissionScreen.kt`
- 验证结果：批改详情显示 `客观题得分: 50`，证据 `screenshots/11-grade-detail-fixed.png`。

## BUG-20260516-004 ADB 驱动下 Compose 文本框长文本输入不稳定

- 状态：待确认环境
- 严重级别：P3
- 影响范围：自动化测试执行，不确认影响真实用户手动输入
- 现象：`adb shell input text Java` 在填空题输入框中只稳定写入首字符 `J`；主观题批改分数字段也未稳定写入。
- 环境观察：设备当前 `ime list -s` 未暴露 ADB Keyboard，只看到系统 LatinIME 和语音输入法。
- 建议：
  - 确认 ADB Keyboard 是否安装在当前 user/profile。
  - 如已安装，执行 `adb shell ime enable <adb-keyboard-id>` 和 `adb shell ime set <adb-keyboard-id>` 后复测。
  - 若该问题只存在于 adb 注入，不阻塞产品发布，但会影响自动化测试稳定性。

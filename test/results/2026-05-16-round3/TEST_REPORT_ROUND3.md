# Round3 测试报告

测试日期：2026-05-16  
测试范围：教师创建课程/题库/题目/考试、学生考试提交、教师阅卷、评分结果校验  
测试结论：核心链路通过。本轮发现 1 个客户端阅卷缺陷，已修复并完成回归验证。

## 环境

- 后端：`http://localhost:8080`，端口检查通过。
- 模拟器：`emulator-5554`
- 输入法：`com.android.adbkeyboard/.AdbIME`
- 客户端：`ovo.sypw.kmp.examsystem`
- 测试用户：`teacher1 / 123456`、`student1 / 123456`

## 测试数据

- 课程：`R3Course0516160039`，`courseId=107`
- 题库：`R3Bank0516160039`，`bankId=59`
- 题目：`questionIds=659,660,661,662,663`
- 考试：`R3Exam0516160039`，`examId=106`
- 提交：`submissionId=209`

## 执行结果

| 用例 | 结果 | 说明 |
| --- | --- | --- |
| TC-R3-001 后端与输入环境检查 | 通过 | 后端 8080 可访问，ADB Keyboard 已设置；`adb shell input text` 可写入 Compose 文本框。 |
| TC-R3-002 教师创建课程、题库、题目与考试 | 通过 | 通过教师身份创建课程、题库、5 种题型与考试，并完成学生选课/分发。 |
| TC-R3-003 学生参加考试并提交 | 通过 | 学生端显示考试，进入考试后完成 5/5 作答并交卷成功。 |
| TC-R3-004 教师查看提交与人工阅卷 | 通过 | 教师端显示提交记录，修复后填空题和简答题均可评分，保存后显示已批改。 |
| TC-R3-005 后端评分结果校验 | 通过 | `status=2`，总分 100，客观题 50，主观题 50。 |

## 关键验证点

- 学生端可参加考试列表分页解析问题已回归：`R3Exam0516160039` 正常显示。
- 学生端作答输入已验证：填空题 `Kotlin`、简答题 `APIContract` 被保存到后端。
- 交卷弹窗显示 `已答 5 / 5 题`。
- 教师端提交列表显示 `学生: Alice Student`、`总分: 50`、`待批改`。
- 阅卷页客观题显示为 `客观题得分: 50`。
- 修复后阅卷页同时显示：
  - 题目 4 填空题，学生回答 `Kotlin`
  - 题目 5 简答题，学生回答 `APIContract`
- 保存批改后提交列表显示 `总分: 100`、`已批改`。
- 后端最终记录：
  - `status=2`
  - `totalScore=100`
  - `objectiveScore=50`
  - `subjectiveScore=50`
  - `questionScores={"659":20,"660":20,"661":10,"662":10,"663":40}`

## 构建与安装

- 已执行 `:composeApp:assembleDebug`，构建成功。
- 已安装 `composeApp-debug.apk` 到模拟器。
- 本轮未修改后端代码，因此未重启后端。

## 证据文件

截图目录：`KMPExamSystem/test/results/2026-05-16-round3/screenshots/`

重点截图：
- `01-student-exams.png`：学生端考试列表显示 Round3 考试。
- `12-submit-dialog.png`：交卷确认弹窗显示 5/5 已答。
- `13-after-submit.png`：学生交卷成功。
- `17-teacher-submissions.png`：教师端显示待批改提交。
- `18-grade-detail.png`：修复前仅显示简答题，填空题缺失。
- `20-grade-detail-after-fix.png`：修复后填空题与简答题均出现。
- `22-after-q5-score.png`：两道人工评分题均输入分数。
- `23-after-save-grade.png`：保存后显示总分 100、已批改。


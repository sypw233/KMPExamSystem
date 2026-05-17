# 2026-05-17 Round 5 测试报告

## 结论
本轮客户端编译通过，远程后端完整业务链路通过，Android 模拟器关键 UI 验证通过。

## 编译验证
- 客户端：`./gradlew.bat assembleDebug --no-configuration-cache`
- 结果：通过
- 说明：保留 `android.disableJdkImageTransform=true` 以兼容当前本机 JDK/Android Gradle 构建环境。

## 后端接口流程验证
远程地址：`http://106.13.185.188:8080/api`

执行结果：
```json
{
  "stamp": "20260517115955",
  "courseId": 11,
  "questionBankId": 16,
  "questionIds": [1501, 1502, 1503, 1504, 1505],
  "examId": 11,
  "submissionId": 201,
  "finalScore": 47,
  "completedExamScore": 47,
  "detailScore": 47
}
```

通过的关键断言：
- 教师创建课程成功。
- 学生选课成功。
- 教师创建题库成功。
- 教师创建五种题型题目成功。
- 题目加入题库成功。
- 教师创建考试、添加题目、发布考试成功。
- 学生获取可参加考试、开始考试、获取试卷、提交答案成功。
- 教师查看提交并批改主观题成功。
- 学生已结束考试返回 `studentScore=47`。
- 学生提交详情返回 `totalScore=47`。

## Android 模拟器验证
设备：`emulator-5558`

截图证据位于：
`F:\IdeaProjects\ExamSysteFull\test-artifacts\2026-05-17-round5`

关键截图：
- `13-after-detail-fix-launch.png`：首页头像红点移除、通知堆叠正常。
- `14-profile-reverify.png`：我的页面用户卡片和设置菜单正常。
- `15-grade-history-reverify.png`：考试历史不再显示 success 错误页。
- `19-grade-detail-score-reverify.png`：答卷详情显示题目、答案、本题得分、教师批语。
- `21-ended-score-after-flow.png`：我的考试已结束列表显示 `我的成绩 47/50`。
- `23-grade-detail-cn-type.png`：答卷详情题型显示为中文，例如 `[单选题]`、`[多选题]`。

## 剩余风险
- 主题、字号、考试展示模式当前为运行期状态，应用重启后不持久化。
- 远程种子数据中历史待批改记录仍会显示“成绩待批改/未获取到成绩”，新阅卷流程的已评分记录显示正常。

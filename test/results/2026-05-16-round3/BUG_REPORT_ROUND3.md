# Round3 Bug 单

测试日期：2026-05-16  
范围：学生考试、教师阅卷、组卷/题型链路

## BUG-R3-001 教师阅卷页未展示填空题，导致填空题无法人工评分

- 状态：已修复，已回归通过
- 严重级别：高
- 影响范围：教师端阅卷、填空题评分、最终总分
- 发现环境：Android Emulator `emulator-5554`
- 关联考试：`R3Exam0516160039`
- 关联提交：`submissionId=209`

### 复现步骤

1. 教师创建包含单选、多选、判断、填空、简答的考试。
2. 学生完成全部 5 道题并提交，其中填空题答案为 `Kotlin`，简答题答案为 `APIContract`。
3. 教师进入考试管理 -> 进行中 -> 批阅与记录 -> 学生提交。
4. 查看阅卷页人工评分列表。

### 期望结果

填空题和简答题都应显示在人工评分列表中，教师可分别打分。

### 实际结果

修复前阅卷页只展示简答题，填空题未展示，教师无法给填空题评分。

### 根因

客户端 `GradeSubmissionScreen` 仅将 `QuestionType.SHORT_ANSWER` 过滤为人工评分题型，遗漏了 `QuestionType.FILL_BLANK`。

### 修复

文件：`composeApp/src/commonMain/kotlin/ovo/sypw/kmp/examsystem/presentation/screens/teacher/GradeSubmissionScreen.kt`

修复逻辑：

```kotlin
val subjectiveQuestions = questions.filter {
    it.question?.questionType in setOf(QuestionType.FILL_BLANK, QuestionType.SHORT_ANSWER)
}
```

### 回归结果

已重新构建并安装客户端。修复后阅卷页同时显示：

- 题目 4：填空题，学生回答 `Kotlin`，评分 10。
- 题目 5：简答题，学生回答 `APIContract`，评分 40。

保存后：

- UI 显示 `总分: 100`、`已批改`。
- 后端记录 `status=2`、`totalScore=100`、`objectiveScore=50`、`subjectiveScore=50`。

## 已回归的历史问题

| 编号 | 状态 | 说明 |
| --- | --- | --- |
| BUG-20260516-001 | 已修复 | 学生端可参加考试接口分页响应解析已修复，Round3 学生端可正常看到新考试。 |
| BUG-20260516-003 | 已修复 | 教师阅卷页客观题得分显示已修复，Round3 显示 `客观题得分: 50`。 |

## 未关闭风险

- 教师端创建课程、题库、题目、考试本轮主要通过教师身份后端接口完成，移动端 UI 侧重点放在考试分发可见性、学生答题提交、教师阅卷和保存批改。若后续要覆盖“移动端表单创建”的细颗粒交互，建议单独追加表单输入专项用例。


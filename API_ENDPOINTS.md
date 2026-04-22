# API 端点汇总

> 从 Apifox 文档提取: path / method / parameters / response schema

---

## 通用约定

- **Base URL**: `http://localhost:8080`
- **认证**: 除登录/注册外，所有接口需在 Header 中携带 `Authorization: Bearer <token>`
- **统一响应包装**: `Result<T> = { code: int, message: string, data: T }`
- **分页包装**: `Page<T> = { totalElements, totalPages, size, content: T[], number, sort, first, last, numberOfElements, pageable, empty }`

---

## 题目管理

| # | 名称 | Method | Path | 参数 | 响应 data |
|---|------|--------|------|------|-----------|
| 1 | 获取题目详情 | GET | `/api/questions/{id}` | Path: `id` (long) | `QuestionResponse` |
| 2 | 更新题目 | PUT | `/api/questions/{id}` | Path: `id` (long); Body: `QuestionRequest` | `QuestionResponse` |
| 3 | 删除题目 | DELETE | `/api/questions/{id}` | Path: `id` (long) | `String` |
| 4 | 查询题目列表 | GET | `/api/questions` | Query: `page`, `size`, `type`, `difficulty`, `category` | `Page<QuestionResponse>` |
| 5 | 创建题目 | POST | `/api/questions` | Body: `QuestionRequest` | `QuestionResponse` |
| 6 | 按类型筛选题目 | GET | `/api/questions/type/{type}` | Path: `type` (enum) | `List<QuestionResponse>` |
| 7 | 获取我的题目 | GET | `/api/questions/my` | - | `List<QuestionResponse>` |
| 8 | 按难度筛选题目 | GET | `/api/questions/difficulty/{difficulty}` | Path: `difficulty` (enum) | `List<QuestionResponse>` |
| 9 | 按分类筛选题目 | GET | `/api/questions/category/{category}` | Path: `category` (string) | `List<QuestionResponse>` |

---

## 题库管理

| # | 名称 | Method | Path | 参数 | 响应 data |
|---|------|--------|------|------|-----------|
| 10 | 获取题库详情 | GET | `/api/question-banks/{id}` | Path: `id` (long) | `QuestionBankResponse` |
| 11 | 更新题库 | PUT | `/api/question-banks/{id}` | Path: `id` (long); Body: `QuestionBankRequest` | `QuestionBankResponse` |
| 12 | 删除题库 | DELETE | `/api/question-banks/{id}` | Path: `id` (long) | `String` |
| 13 | 获取所有题库 | GET | `/api/question-banks` | Query: `page`, `size` | `Page<QuestionBankResponse>` |
| 14 | 创建题库 | POST | `/api/question-banks` | Body: `QuestionBankRequest` | `QuestionBankResponse` |
| 15 | 添加题目到题库 | POST | `/api/question-banks/{id}/questions/{questionId}` | Path: `id`, `questionId` (long) | `String` |
| 16 | 从题库移除题目 | DELETE | `/api/question-banks/{id}/questions/{questionId}` | Path: `id`, `questionId` (long) | `String` |
| 17 | 获取题库中的所有题目 | GET | `/api/question-banks/{id}/questions` | Path: `id` (long) | `List<QuestionResponse>` |
| 18 | 获取我的题库 | GET | `/api/question-banks/my` | - | `List<QuestionBankResponse>` |

---

## 考试管理

| # | 名称 | Method | Path | 参数 | 响应 data |
|---|------|--------|------|------|-----------|
| 19 | 获取考试详情 | GET | `/api/exams/{id}` | Path: `id` (long) | `ExamResponse` |
| 20 | 更新考试 | PUT | `/api/exams/{id}` | Path: `id` (long); Body: `ExamRequest` | `ExamResponse` |
| 21 | 删除考试 | DELETE | `/api/exams/{id}` | Path: `id` (long) | `String` |
| 22 | 查询考试列表 | GET | `/api/exams` | Query: `page`, `size`, `status`, `courseId` | `Page<ExamResponse>` |
| 23 | 创建考试 | POST | `/api/exams` | Body: `ExamRequest` | `ExamResponse` |
| 24 | 获取考试的所有题目 | GET | `/api/exams/{id}/questions` | Path: `id` (long) | `List<ExamQuestionResponse>` |
| 25 | 添加题目到考试 | POST | `/api/exams/{id}/questions` | Path: `id` (long); Body: `ExamQuestionRequest` | `String` |
| 26 | 发布考试 | POST | `/api/exams/{id}/publish` | Path: `id` (long) | `ExamResponse` |
| 27 | 按状态筛选考试 | GET | `/api/exams/status/{status}` | Path: `status` (0/1/2) | `List<ExamResponse>` |
| 28 | 获取我的考试 | GET | `/api/exams/my` | - | `List<ExamResponse>` |
| 29 | 获取课程的所有考试 | GET | `/api/exams/course/{courseId}` | Path: `courseId` (long) | `List<ExamResponse>` |
| 30 | 从考试移除题目 | DELETE | `/api/exams/{id}/questions/{questionId}` | Path: `id`, `questionId` (long) | `String` |
| 31 | 部分更新考试 | PATCH | `/api/exams/{id}` | Path: `id` (long); Query: `status` (1) | `ExamResponse` |
| 32 | 开始考试 | POST | `/api/exams/{id}/submissions` | Path: `id` (long) | `SubmissionResponse` |

---

## 课程管理

| # | 名称 | Method | Path | 参数 | 响应 data |
|---|------|--------|------|------|-----------|
| 33 | 获取课程详情 | GET | `/api/courses/{id}` | Path: `id` (long) | `CourseResponse` |
| 34 | 更新课程 | PUT | `/api/courses/{id}` | Path: `id` (long); Body: `CourseRequest` | `CourseResponse` |
| 35 | 删除课程 | DELETE | `/api/courses/{id}` | Path: `id` (long) | `String` |
| 36 | 获取所有活跃课程 | GET | `/api/courses` | Query: `page`, `size` | `Page<CourseResponse>` |
| 37 | 创建课程 | POST | `/api/courses` | Body: `CourseRequest` | `CourseResponse` |
| 38 | 选课 | POST | `/api/courses/{id}/enroll` | Path: `id` (long) | `EnrollmentResponse` |
| 39 | 获取选课学生列表 | GET | `/api/courses/{id}/students` | Path: `id` (long) | `List<EnrollmentResponse>` |
| 40 | 获取我的课程 | GET | `/api/courses/my` | - | `List<CourseResponse>` |
| 41 | 获取我的选课记录 | GET | `/api/courses/my-enrollments` | - | `List<EnrollmentResponse>` |

---

## 认证管理

| # | 名称 | Method | Path | 参数 | 响应 data |
|---|------|--------|------|------|-----------|
| 42 | 修改个人信息 | PUT | `/api/auth/profile` | Body: `UserProfileRequest` | `UserProfileResponse` |
| 43 | 用户注册 | POST | `/api/auth/register` | Body: `RegisterRequest` | `AuthResponse` |
| 44 | 刷新Token | POST | `/api/auth/refresh` | Header: `Authorization` | `AuthResponse` |
| 45 | 用户登录 | POST | `/api/auth/login` | Body: `LoginRequest` | `AuthResponse` |
| 46 | 修改密码 | POST | `/api/auth/change-password` | Body: `ChangePasswordRequest` | `String` |
| 47 | 获取当前用户信息 | GET | `/api/auth/me` | - | `UserProfileResponse` |

---

## 考试提交 / 答题评分

| # | 名称 | Method | Path | 参数 | 响应 data |
|---|------|--------|------|------|-----------|
| 48 | 提交考试答案 | POST | `/api/submissions` | Body: `SubmissionRequest` | `SubmissionResponse` |
| 49 | 主观题手动评分 | POST | `/api/submissions/{id}/grade` | Path: `id` (long); Body: `GradeRequest` | `SubmissionResponse` |
| 50 | 开始考试 | POST | `/api/submissions/start` | Query: `examId` (long) | `SubmissionResponse` |
| 51 | 记录监考事件 | POST | `/api/submissions/proctoring` | Body: `ProctoringEventRequest` | `Map<String, Object>` |
| 52 | 获取提交详情 | GET | `/api/submissions/{id}` | Path: `id` (long) | `SubmissionResponse` |
| 53 | 获取学生的所有成绩 | GET | `/api/submissions/user/{userId}` | Path: `userId` (long) | `List<SubmissionResponse>` |
| 54 | 获取考试的所有提交记录 | GET | `/api/submissions/exam/{examId}` | Path: `examId` (long) | `List<SubmissionResponse>` |
| 87 | 查询提交记录 | GET | `/api/submissions` | Query: `examId`, `userId`, `page`, `size` | `Page<SubmissionResponse>` |

---

## 文件管理

| # | 名称 | Method | Path | 参数 | 响应 data |
|---|------|--------|------|------|-----------|
| 55 | 上传图片 | POST | `/api/files/image` | Query: `category`; Body: multipart `file` | `FileUploadResponse` |
| 56 | 上传文档 | POST | `/api/files/document` | Query: `category`; Body: multipart `file` | `FileUploadResponse` |
| 57 | 获取文件URL | GET | `/api/files/url/{fileKey}` | Path: `fileKey` (string) | `Map<String, String>` |
| 58 | 删除文件 | DELETE | `/api/files/{fileKey}` | Path: `fileKey` (string) | `String` |

---

## 统计分析

| # | 名称 | Method | Path | 参数 | 响应 data |
|---|------|--------|------|------|-----------|
| 59 | 学生成绩统计 | GET | `/api/statistics/student/{studentId}` | Path: `studentId` (long) | `StudentStatisticsResponse` |
| 60 | 题目统计 | GET | `/api/statistics/question/{questionId}` | Path: `questionId` (long) | `QuestionStatisticsResponse` |
| 61 | 系统总览 | GET | `/api/statistics/overview` | - | `SystemOverviewResponse` |
| 62 | 考试统计 | GET | `/api/statistics/exam/{examId}` | Path: `examId` (long) | `ExamStatisticsResponse` |
| 63 | 课程统计 | GET | `/api/statistics/course/{courseId}` | Path: `courseId` (long) | `CourseStatisticsResponse` |

---

## AI 辅助判题

| # | 名称 | Method | Path | 参数 | 响应 data |
|---|------|--------|------|------|-----------|
| 64 | 获取AI配置 | GET | `/api/ai-grading/config` | - | `List<AiConfigResponse>` |
| 65 | 更新AI配置 | PUT | `/api/ai-grading/config` | Body: `AiConfigRequest` | `AiConfigResponse` |
| 66 | AI辅助判题 | POST | `/api/ai-grading/grade` | Body: `AiGradingRequest` | `AiGradingResponse` |

---

## 通知管理

| # | 名称 | Method | Path | 参数 | 响应 data |
|---|------|--------|------|------|-----------|
| 67 | 标记通知为已读 | PUT | `/api/notifications/{id}/read` | Path: `id` (long) | `NotificationResponse` |
| 68 | 全部标记为已读 | PUT | `/api/notifications/read-all` | - | `Map<String, Integer>` |
| 69 | 获取通知列表 | GET | `/api/notifications` | Query: `page`, `size` | `Page<NotificationResponse>` |
| 70 | 获取未读通知数量 | GET | `/api/notifications/unread-count` | - | `UnreadCountResponse` |
| 71 | 删除通知 | DELETE | `/api/notifications/{id}` | Path: `id` (long) | `String` |

---

## 题目导入导出

> 存在两套端点: `/api/questions/...` (legacy) 与 `/api/question-import-export/...` (新版)。

| # | 名称 | Method | Path | 参数 | 响应 data |
|---|------|--------|------|------|-----------|
| 72 | 导入题目(legacy) | POST | `/api/questions/import` | Query: `bankId`; Body: `file` (binary) | `ImportResultResponse` |
| 73 | 下载导入模板(legacy) | GET | `/api/questions/template` | - | (文件流) |
| 74 | 导出题库(legacy) | GET | `/api/questions/export` | Query: `bankId` | (文件流) |
| 75 | 导入题目 | POST | `/api/question-import-export/import` | Query: `bankId`; Body: `file` (binary) | `ImportResultResponse` |
| 76 | 下载导入模板 | GET | `/api/question-import-export/template` | - | `String` (URL) |
| 77 | 导出题库 | GET | `/api/question-import-export/export` | Query: `bankId` | `String` (URL) |

---

## 用户管理(管理员)

| # | 名称 | Method | Path | 参数 | 响应 data |
|---|------|--------|------|------|-----------|
| 78 | 查询用户详情 | GET | `/api/admin/users/{id}` | Path: `id` (long) | `UserResponse` |
| 79 | 更新用户信息 | PUT | `/api/admin/users/{id}` | Path: `id` (long); Body: `UserUpdateRequest` | `UserResponse` |
| 80 | 删除用户 | DELETE | `/api/admin/users/{id}` | Path: `id` (long) | `String` |
| 81 | 重置用户密码 | PUT | `/api/admin/users/{id}/reset-password` | Path: `id` (long); Body: `ResetPasswordRequest` | `String` |
| 82 | 启用用户账号 | PUT | `/api/admin/users/{id}/enable` | Path: `id` (long) | `UserResponse` |
| 83 | 禁用用户账号 | PUT | `/api/admin/users/{id}/disable` | Path: `id` (long) | `UserResponse` |
| 84 | 分页查询用户列表 | GET | `/api/admin/users` | Query: `role`, `status`, `keyword`, `page`, `size` | `Page<UserResponse>` |
| 85 | 新建用户 | POST | `/api/admin/users` | Body: `UserCreateRequest` | `UserResponse` |
| 86 | 按角色查询全部用户 | GET | `/api/admin/users/by-role/{role}` | Path: `role` (string) | `List<UserResponse>` |

---

## Schema 参考

### 题目相关

**QuestionRequest** (body)
- `content`*: string
- `type`*: string (single | multiple | true_false | fill_blank | short_answer)
- `options`: string (JSON 数组)
- `answer`: string
- `analysis`: string
- `difficulty`*: string (easy | medium | hard)
- `category`: string

**QuestionResponse**
- `id`: long
- `content`: string
- `type`: string
- `options`: string (JSON)
- `answer`: string
- `analysis`: string
- `difficulty`: string
- `category`: string
- `creatorId`: long
- `creatorName`: string
- `bankCount`: long
- `createTime`: datetime

### 题库相关

**QuestionBankRequest** (body)
- `name`*: string
- `description`: string

**QuestionBankResponse**
- `id`: long
- `name`: string
- `description`: string
- `creatorId`: long
- `creatorName`: string
- `questionCount`: long
- `createTime`: datetime

### 考试相关

**ExamRequest** (body)
- `title`*: string
- `description`: string
- `courseId`*: long
- `startTime`*: datetime
- `endTime`*: datetime
- `duration`: int
- `totalScore`*: int
- `needsGrading`: boolean
- `allowedPlatforms`: string (desktop | mobile | both)
- `strictMode`: boolean
- `maxSwitchCount`: int
- `fullscreenRequired`: boolean

**ExamResponse**
- `id`: long
- `title`: string
- `description`: string
- `courseId`: long
- `courseName`: string
- `creatorId`: long
- `creatorName`: string
- `startTime`: datetime
- `endTime`: datetime
- `duration`: int
- `totalScore`: int
- `status`: int (0=草稿, 1=已发布, 2=已结束)
- `statusDescription`: string
- `needsGrading`: boolean
- `questionCount`: long
- `allowedPlatforms`: string
- `strictMode`: boolean
- `maxSwitchCount`: int
- `fullscreenRequired`: boolean
- `createTime`: datetime

**ExamQuestionRequest** (body)
- `questionId`*: long
- `score`*: int
- `sequence`*: int

**ExamQuestionResponse**
- `examId`: long
- `questionId`: long
- `questionContent`: string
- `questionType`: string
- `questionDifficulty`: string
- `score`: int
- `sequence`: int

### 课程相关

**CourseRequest** (body)
- `courseName`*: string
- `description`: string
- `status`*: int

**CourseResponse**
- `id`: long
- `courseName`: string
- `description`: string
- `teacherId`: long
- `teacherName`: string
- `status`: int
- `enrollmentCount`: long
- `createTime`: datetime

**EnrollmentResponse**
- `id`: long
- `studentId`: long
- `studentName`: string
- `courseId`: long
- `courseName`: string
- `enrollmentTime`: datetime

### 认证相关

**LoginRequest** (body)
- `username`*: string
- `password`*: string

**RegisterRequest** (body)
- `username`*: string
- `password`*: string
- `realName`*: string
- `role`*: string (student | teacher)
- `email`: string

**AuthResponse**
- `accessToken`: string
- `refreshToken`: string
- `tokenType`: string
- `id`: long
- `username`: string
- `nickname`: string
- `realName`: string
- `role`: string
- `email`: string
- `avatar`: string

**UserProfileRequest** (body)
- `nickname`: string
- `email`: string
- `avatar`: string

**UserProfileResponse**
- `id`: long
- `username`: string
- `nickname`: string
- `realName`: string
- `role`: string
- `email`: string
- `avatar`: string

**ChangePasswordRequest** (body)
- `oldPassword`*: string
- `newPassword`*: string

### 提交 / 评分相关

**SubmissionRequest** (body)
- `examId`*: long
- `answers`*: Map<string, string> (key=questionId, value=answer)

**GradeRequest** (body)
- `questionScores`*: Map<string, int> (key=questionId, value=score)

**ProctoringEventRequest** (body)
- `examId`*: long
- `eventType`*: string (tab_switch | exit_fullscreen | blur)
- `detail`: string

**SubmissionResponse**
- `id`: long
- `examId`: long
- `examTitle`: string
- `userId`: long
- `userName`: string
- `answers`: string (JSON)
- `objectiveScore`: int
- `subjectiveScore`: int
- `totalScore`: int
- `status`: int (0=答题中, 1=已提交, 2=已评分)
- `statusDescription`: string
- `switchCount`: int
- `startTime`: datetime
- `submitTime`: datetime
- `submitDetail`: string (JSON)
- `proctoringData`: string (JSON)

### 文件相关

**FileUploadResponse**
- `fileKey`: string
- `fileUrl`: string
- `fileName`: string
- `fileSize`: long

### 统计相关

**StudentStatisticsResponse**
- `studentId`: long
- `studentName`: string
- `totalExams`: int
- `averageScore`: double
- `highestScore`: int
- `lowestScore`: int
- `scores`: List<StudentScoreRecord>

**StudentScoreRecord**
- `examId`: long
- `examTitle`: string
- `score`: int
- `submitTime`: datetime

**QuestionStatisticsResponse**
- `questionId`: long
- `questionContent`: string
- `questionType`: string
- `usageCount`: int
- `totalAttempts`: int
- `correctCount`: int
- `accuracy`: double
- `optionDistribution`: Map<string, int>

**SystemOverviewResponse**
- `totalUsers`: int
- `studentCount`: int
- `teacherCount`: int
- `adminCount`: int
- `totalCourses`: int
- `totalExams`: int
- `totalQuestions`: int
- `totalSubmissions`: int

**ExamStatisticsResponse**
- `examId`: long
- `examTitle`: string
- `totalStudents`: int
- `submittedCount`: int
- `completionRate`: double
- `averageScore`: double
- `highestScore`: int
- `lowestScore`: int
- `passCount`: int
- `passRate`: double
- `scoreDistribution`: Map<string, int>

**CourseStatisticsResponse**
- `courseId`: long
- `courseName`: string
- `totalStudents`: int
- `totalExams`: int
- `averageScore`: double
- `highestScore`: int
- `lowestScore`: int

### AI 相关

**AiConfigRequest** (body)
- `configKey`*: string
- `configValue`*: string

**AiConfigResponse**
- `id`: long
- `configKey`: string
- `configValue`: string
- `description`: string

**AiGradingRequest** (body)
- `questionId`*: long
- `studentAnswer`*: string
- `maxScore`*: int

**AiGradingResponse**
- `questionId`: long
- `maxScore`: int
- `suggestedScore`: int
- `explanation`: string
- `strengths`: List<string>
- `improvements`: List<string>

### 通知相关

**NotificationResponse**
- `id`: long
- `userId`: long
- `type`: string (EXAM_PUBLISHED | EXAM_REMINDER | GRADE_RELEASED | COURSE_UPDATE | SYSTEM_ANNOUNCEMENT)
- `title`: string
- `content`: string
- `relatedId`: long
- `isRead`: boolean
- `createTime`: datetime

**UnreadCountResponse**
- `count`: long

### 导入导出相关

**ImportResultResponse**
- `taskId`: string
- `totalRows`: int
- `successCount`: int
- `failedCount`: int
- `errors`: List<ImportErrorDetail>

**ImportErrorDetail**
- `row`: int
- `reason`: string

### 用户管理相关

**UserCreateRequest** (body)
- `username`*: string
- `password`*: string
- `realName`: string
- `role`*: string (admin | teacher | student)
- `email`: string

**UserUpdateRequest** (body)
- `realName`: string
- `email`: string
- `role`: string (admin | teacher | student)
- `status`: int

**ResetPasswordRequest** (body)
- `newPassword`*: string

**UserResponse**
- `id`: long
- `username`: string
- `nickname`: string
- `realName`: string
- `role`: string
- `email`: string
- `avatar`: string
- `status`: int
- `createTime`: datetime

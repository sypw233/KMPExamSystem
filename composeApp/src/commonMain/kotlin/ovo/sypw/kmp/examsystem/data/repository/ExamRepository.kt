package ovo.sypw.kmp.examsystem.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ovo.sypw.kmp.examsystem.data.api.ExamApi
import ovo.sypw.kmp.examsystem.data.dto.BatchDeleteRequest
import ovo.sypw.kmp.examsystem.data.dto.BatchDeleteResult
import ovo.sypw.kmp.examsystem.data.dto.ComposeRandomExamRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamPaperQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.data.dto.QuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.SubmissionResponse
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

class ExamRepository(
    private val examApi: ExamApi,
    tokenStorage: TokenStorage
) : BaseRepository(tokenStorage) {
    private val _publishedExams = MutableStateFlow<List<ExamResponse>>(emptyList())
    val publishedExams: StateFlow<List<ExamResponse>> = _publishedExams.asStateFlow()

    private val _myExams = MutableStateFlow<List<ExamResponse>>(emptyList())
    val myExams: StateFlow<List<ExamResponse>> = _myExams.asStateFlow()

    private val _availableExams = MutableStateFlow<List<ExamResponse>>(emptyList())
    val availableExams: StateFlow<List<ExamResponse>> = _availableExams.asStateFlow()

    private val _completedExams = MutableStateFlow<List<ExamResponse>>(emptyList())
    val completedExams: StateFlow<List<ExamResponse>> = _completedExams.asStateFlow()

    suspend fun loadPublishedExams(): Result<List<ExamResponse>> = runWithToken { token ->
        fetchAllPages(
            requestPage = { page, size -> examApi.getExamsByStatus(token, 1, page, size) },
            content = { it.content },
            last = { it.last },
            totalPages = { it.totalPages },
            distinctKey = { it.id }
        ).also {
            _publishedExams.value = it
        }
    }

    suspend fun loadAllExams(): Result<List<ExamResponse>> = runWithToken { token ->
        fetchAllPages(
            requestPage = { page, size -> examApi.getAllExams(token, page, size) },
            content = { it.content },
            last = { it.last },
            totalPages = { it.totalPages },
            distinctKey = { it.id }
        )
    }

    suspend fun loadExamsByStatus(status: Int): Result<List<ExamResponse>> = runWithToken { token ->
        fetchAllPages(
            requestPage = { page, size -> examApi.getExamsByStatus(token, status, page, size) },
            content = { it.content },
            last = { it.last },
            totalPages = { it.totalPages },
            distinctKey = { it.id }
        )
    }

    suspend fun getExamDetail(examId: Long): Result<ExamResponse> = runWithToken { token ->
        val r = examApi.getExamDetail(token, examId)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    suspend fun getExamQuestions(examId: Long): Result<List<ExamQuestionResponse>> = runWithToken { token ->
        val r = examApi.getExamQuestions(token, examId)
        if (r.code == 200) (r.data ?: emptyList()).map { normalizeExamQuestion(it) } else throw Exception(r.message)
    }

    suspend fun createExam(request: ExamRequest): Result<ExamResponse> {
        val result = runWithToken { token ->
            val r = examApi.createExam(token, request)
            if (r.code == 200 && r.data != null) r.data
            else throw Exception(r.message)
        }
        // 创建成功后刷新列表缓存
        if (result.isSuccess) {
            loadAllExams()
        }
        return result
    }

    suspend fun updateExam(examId: Long, request: ExamRequest): Result<ExamResponse> = runWithToken { token ->
        val r = examApi.updateExam(token, examId, request)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    suspend fun deleteExam(examId: Long): Result<Unit> = runWithToken { token ->
        val r = examApi.deleteExam(token, examId)
        if (r.code == 200) {
            _myExams.value = _myExams.value.filter { it.id != examId }
            Unit
        } else {
            throw Exception(r.message)
        }
    }

    suspend fun publishExam(examId: Long): Result<ExamResponse> = runWithToken { token ->
        val r = examApi.publishExam(token, examId)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    suspend fun addQuestionToExam(examId: Long, request: ExamQuestionRequest): Result<ExamQuestionResponse> = runWithToken { token ->
        val r = examApi.addQuestionToExam(token, examId, request)
        if (r.code == 200) {
            r.data?.let { return@runWithToken normalizeExamQuestion(it) }

            val questionsResponse = examApi.getExamQuestions(token, examId)
            val addedQuestion = questionsResponse.data
                ?.map { normalizeExamQuestion(it) }
                ?.firstOrNull { it.questionId == request.questionId }
            addedQuestion ?: ExamQuestionResponse(
                examId = examId,
                questionId = request.questionId,
                score = request.score,
                sequence = request.sequence,
                orderNum = request.sequence
            )
        } else {
            throw Exception(r.message)
        }
    }

    suspend fun removeQuestionFromExam(examId: Long, questionId: Long): Result<Unit> = runWithToken { token ->
        val r = examApi.removeQuestionFromExam(token, examId, questionId)
        if (r.code == 200) Unit else throw Exception(r.message)
    }

    suspend fun loadMyExams(): Result<List<ExamResponse>> = runWithToken { token ->
        val r = examApi.getMyExams(token)
        if (r.code == 200) {
            val data = r.data ?: emptyList()
            _myExams.value = data
            data
        } else {
            throw Exception(r.message)
        }
    }

    /** 学生：获取可参加的考试 */
    suspend fun getMyAvailableExams(): Result<List<ExamResponse>> = runWithToken { token ->
        fetchAllPages(
            requestPage = { page, size -> examApi.getMyAvailableExams(token, page, size) },
            content = { it.content },
            last = { it.last },
            totalPages = { it.totalPages },
            distinctKey = { it.id }
        ).also {
            _availableExams.value = filterSubmittedExams(it)
        }
    }

    /** 学生：获取已完成的考试 */
    suspend fun getMyCompletedExams(): Result<List<ExamResponse>> = runWithToken { token ->
        fetchAllPages(
            requestPage = { page, size -> examApi.getMyCompletedExams(token, page, size) },
            content = { it.content },
            last = { it.last },
            totalPages = { it.totalPages },
            distinctKey = { it.id }
        ).also {
            _completedExams.value = it
            _availableExams.value = filterSubmittedExams(_availableExams.value)
        }
    }

    fun markExamSubmitted(submission: SubmissionResponse) {
        val submittedExamId = submission.examId
        val existingAvailable = _availableExams.value.firstOrNull { it.id == submittedExamId }
        _availableExams.value = _availableExams.value.filterNot { it.id == submittedExamId }
        if (existingAvailable != null && _completedExams.value.none { it.id == submittedExamId }) {
            _completedExams.value = listOf(
                existingAvailable.copy(
                    studentScore = submission.totalScore,
                    needsGrading = submission.status < 2 && submission.subjectiveScore == null
                )
            ) + _completedExams.value
        }
    }

    fun markExamUnavailable(examId: Long) {
        _availableExams.value = _availableExams.value.filterNot { it.id == examId }
    }

    private fun filterSubmittedExams(exams: List<ExamResponse>): List<ExamResponse> {
        val submittedExamIds = _completedExams.value.map { it.id }.toSet()
        if (submittedExamIds.isEmpty()) return exams
        return exams.filterNot { it.id in submittedExamIds }
    }

    suspend fun getExamsByCourse(courseId: Long): Result<List<ExamResponse>> = runWithToken { token ->
        fetchAllPages(
            requestPage = { page, size -> examApi.getExamsByCourse(token, courseId, page, size) },
            content = { it.content },
            last = { it.last },
            totalPages = { it.totalPages },
            distinctKey = { it.id }
        )
    }

    suspend fun patchExam(examId: Long, status: Int): Result<ExamResponse> = runWithToken { token ->
        val r = examApi.patchExam(token, examId, status)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /**
     * 批量删除考试
     */
    suspend fun batchDeleteExams(ids: List<Long>): Result<BatchDeleteResult> = runWithToken { token ->
        val r = examApi.batchDeleteExams(token, BatchDeleteRequest(ids))
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /**
     * 获取考试试卷（学生，不含答案和解析）
     */
    suspend fun getExamPaper(examId: Long): Result<List<ExamPaperQuestionResponse>> = runWithToken { token ->
        val r = examApi.getExamPaper(token, examId)
        if (r.code == 200) r.data ?: emptyList() else throw Exception(r.message)
    }

    /**
     * 学生答题用试卷题目。
     * 使用不含标准答案和解析的 paper 接口，避免学生端拿到答案数据。
     */
    suspend fun getExamPaperQuestions(examId: Long): Result<List<ExamQuestionResponse>> = runWithToken { token ->
        val r = examApi.getExamPaper(token, examId)
        if (r.code == 200) {
            (r.data ?: emptyList()).map { normalizeExamPaperQuestion(it) }
        } else {
            throw Exception(r.message)
        }
    }

    /**
     * 智能随机组卷
     * @param examId 考试ID
     * @param request 组卷请求
     * @return 更新后的考试详情
     */
    suspend fun composeRandomExam(examId: Long, request: ComposeRandomExamRequest): Result<ExamResponse> = runWithToken { token ->
        val r = examApi.composeRandomExam(token, examId, request)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /**
     * 将后端扁平字段转换为前端嵌套结构，保证 UI 兼容性
     */
    private fun normalizeExamQuestion(eq: ExamQuestionResponse): ExamQuestionResponse {
        if (eq.question != null) return eq
        return eq.copy(
            orderNum = eq.sequence,
            question = QuestionResponse(
                id = eq.questionId,
                content = eq.questionContent ?: "",
                type = eq.questionType ?: "",
                difficulty = eq.questionDifficulty,
                score = eq.score
            )
        )
    }

    private fun normalizeExamPaperQuestion(question: ExamPaperQuestionResponse): ExamQuestionResponse =
        ExamQuestionResponse(
            examId = question.examId,
            questionId = question.questionId,
            score = question.score,
            questionContent = question.questionContent,
            questionType = question.questionType,
            questionDifficulty = question.questionDifficulty,
            sequence = question.sequence,
            orderNum = question.sequence,
            question = QuestionResponse(
                id = question.questionId,
                content = question.questionContent,
                type = question.questionType,
                options = question.options,
                answer = null,
                analysis = null,
                difficulty = question.questionDifficulty,
                score = question.score
            )
        )

}

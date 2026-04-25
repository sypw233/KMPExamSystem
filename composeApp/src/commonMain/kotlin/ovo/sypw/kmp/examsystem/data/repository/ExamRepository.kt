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
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

class ExamRepository(
    private val examApi: ExamApi,
    private val tokenStorage: TokenStorage
) {
    private val _publishedExams = MutableStateFlow<List<ExamResponse>>(emptyList())
    val publishedExams: StateFlow<List<ExamResponse>> = _publishedExams.asStateFlow()

    private val _myExams = MutableStateFlow<List<ExamResponse>>(emptyList())
    val myExams: StateFlow<List<ExamResponse>> = _myExams.asStateFlow()

    suspend fun loadPublishedExams(): Result<List<ExamResponse>> = runWithToken { token ->
        val r = examApi.getExamsByStatus(token, 1)
        if (r.code == 200) {
            val data = r.data?.content ?: emptyList()
            _publishedExams.value = data
            data
        } else {
            throw Exception(r.message)
        }
    }

    suspend fun loadAllExams(): Result<List<ExamResponse>> = runWithToken { token ->
        val r = examApi.getAllExams(token)
        if (r.code == 200) r.data?.content ?: emptyList() else throw Exception(r.message)
    }

    suspend fun loadExamsByStatus(status: Int): Result<List<ExamResponse>> = runWithToken { token ->
        val r = examApi.getExamsByStatus(token, status)
        if (r.code == 200) r.data?.content ?: emptyList() else throw Exception(r.message)
    }

    suspend fun getExamDetail(examId: Long): Result<ExamResponse> = runWithToken { token ->
        val r = examApi.getExamDetail(token, examId)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    suspend fun getExamQuestions(examId: Long): Result<List<ExamQuestionResponse>> = runWithToken { token ->
        val r = examApi.getExamQuestions(token, examId)
        if (r.code == 200) (r.data ?: emptyList()).map { normalizeExamQuestion(it) } else throw Exception(r.message)
    }

    suspend fun createExam(request: ExamRequest): Result<ExamResponse> = runWithToken { token ->
        val r = examApi.createExam(token, request)
        if (r.code == 200 && r.data != null) {
            loadAllExams()
            r.data
        } else {
            throw Exception(r.message)
        }
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
        if (r.code == 200 && r.data != null) normalizeExamQuestion(r.data) else throw Exception(r.message)
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

    suspend fun getExamsByCourse(courseId: Long): Result<List<ExamResponse>> = runWithToken { token ->
        val r = examApi.getExamsByCourse(token, courseId)
        if (r.code == 200) r.data?.content ?: emptyList() else throw Exception(r.message)
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

    private suspend fun <T> runWithToken(block: suspend (String) -> T): Result<T> {
        return try {
            val token = tokenStorage.getAccessToken() ?: throw Exception("Not logged in")
            Result.success(block(token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

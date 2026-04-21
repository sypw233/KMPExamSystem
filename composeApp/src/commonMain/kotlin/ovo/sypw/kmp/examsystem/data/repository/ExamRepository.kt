package ovo.sypw.kmp.examsystem.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ovo.sypw.kmp.examsystem.data.api.ExamApi
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamQuestionResponse
import ovo.sypw.kmp.examsystem.data.dto.ExamRequest
import ovo.sypw.kmp.examsystem.data.dto.ExamResponse
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

/**
 * 考试仓库（完整版）
 */
class ExamRepository(
    private val examApi: ExamApi,
    private val tokenStorage: TokenStorage
) {

    private val _publishedExams = MutableStateFlow<List<ExamResponse>>(emptyList())
    val publishedExams: StateFlow<List<ExamResponse>> = _publishedExams.asStateFlow()

    private val _myExams = MutableStateFlow<List<ExamResponse>>(emptyList())
    val myExams: StateFlow<List<ExamResponse>> = _myExams.asStateFlow()

    /** 获取所有已发布的考试 (status=1) */
    suspend fun loadPublishedExams(): Result<List<ExamResponse>> = runWithToken { token ->
        val r = examApi.getExamsByStatus(token, 1)
        if (r.code == 200) {
            val data = r.data ?: emptyList()
            _publishedExams.value = data
            data
        } else throw Exception(r.message)
    }

    /** 获取所有考试（无状态筛选） */
    suspend fun loadAllExams(): Result<List<ExamResponse>> = runWithToken { token ->
        val r = examApi.getAllExams(token)
        if (r.code == 200) r.data ?: emptyList() else throw Exception(r.message)
    }

    /** 按状态获取考试列表 */
    suspend fun loadExamsByStatus(status: Int): Result<List<ExamResponse>> = runWithToken { token ->
        val r = examApi.getExamsByStatus(token, status)
        if (r.code == 200) r.data ?: emptyList() else throw Exception(r.message)
    }

    /** 获取考试详情 */
    suspend fun getExamDetail(examId: Long): Result<ExamResponse> = runWithToken { token ->
        val r = examApi.getExamDetail(token, examId)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /** 获取考试的题目列表 */
    suspend fun getExamQuestions(examId: Long): Result<List<ExamQuestionResponse>> = runWithToken { token ->
        val r = examApi.getExamQuestions(token, examId)
        if (r.code == 200) r.data ?: emptyList() else throw Exception(r.message)
    }

    /** 创建考试 */
    suspend fun createExam(request: ExamRequest): Result<ExamResponse> = runWithToken { token ->
        val r = examApi.createExam(token, request)
        if (r.code == 200 && r.data != null) { loadMyExams(); r.data }
        else throw Exception(r.message)
    }

    /** 更新考试 */
    suspend fun updateExam(examId: Long, request: ExamRequest): Result<ExamResponse> = runWithToken { token ->
        val r = examApi.updateExam(token, examId, request)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /** 删除考试 */
    suspend fun deleteExam(examId: Long): Result<Unit> = runWithToken { token ->
        val r = examApi.deleteExam(token, examId)
        if (r.code == 200) {
            _myExams.value = _myExams.value.filter { it.id != examId }
            Unit
        } else throw Exception(r.message)
    }

    /** 发布考试 */
    suspend fun publishExam(examId: Long): Result<ExamResponse> = runWithToken { token ->
        val r = examApi.publishExam(token, examId)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /** 添加题目到考试 */
    suspend fun addQuestionToExam(examId: Long, request: ExamQuestionRequest): Result<ExamQuestionResponse> = runWithToken { token ->
        val r = examApi.addQuestionToExam(token, examId, request)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /** 从考试移除题目 */
    suspend fun removeQuestionFromExam(examId: Long, questionId: Long): Result<Unit> = runWithToken { token ->
        val r = examApi.removeQuestionFromExam(token, examId, questionId)
        if (r.code == 200) Unit else throw Exception(r.message)
    }

    /** 加载我的考试（教师） */
    suspend fun loadMyExams(): Result<List<ExamResponse>> = runWithToken { token ->
        val r = examApi.getMyExams(token)
        if (r.code == 200) {
            val data = r.data ?: emptyList()
            _myExams.value = data
            data
        } else throw Exception(r.message)
    }

    private suspend fun <T> runWithToken(block: suspend (String) -> T): Result<T> {
        return try {
            val token = tokenStorage.getAccessToken() ?: throw Exception("未登录")
            Result.success(block(token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

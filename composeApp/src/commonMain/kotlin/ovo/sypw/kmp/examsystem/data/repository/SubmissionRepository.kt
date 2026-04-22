package ovo.sypw.kmp.examsystem.data.repository

import ovo.sypw.kmp.examsystem.data.api.SubmissionApi
import ovo.sypw.kmp.examsystem.data.dto.ProctoringEventRequest
import ovo.sypw.kmp.examsystem.data.dto.PageSubmissionResponse
import ovo.sypw.kmp.examsystem.data.dto.SubmissionRequest
import ovo.sypw.kmp.examsystem.data.dto.SubmissionResponse
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage

/**
 * 答题提交仓库（完整版）
 */
class SubmissionRepository(
    private val submissionApi: SubmissionApi,
    private val tokenStorage: TokenStorage
) {

    /** 开始考试 */
    suspend fun startExam(examId: Long): Result<SubmissionResponse> = runWithToken { token ->
        val r = submissionApi.startExam(token, examId)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /** 提交考试答案 */
    suspend fun submitExam(examId: Long, answers: Map<String, String>): Result<SubmissionResponse> = runWithToken { token ->
        val r = submissionApi.submitExam(token, SubmissionRequest(examId, answers))
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /** 获取提交详情 */
    suspend fun getSubmissionDetail(submissionId: Long): Result<SubmissionResponse> = runWithToken { token ->
        val r = submissionApi.getSubmissionDetail(token, submissionId)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /** 获取用户的成绩列表 */
    suspend fun getMyGrades(userId: Long): Result<List<SubmissionResponse>> = runWithToken { token ->
        val r = submissionApi.getMyGrades(token, userId)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /** 记录监考事件 */
    suspend fun recordProctoringEvent(examId: Long, eventType: String, detail: String? = null) {
        try {
            val token = tokenStorage.getAccessToken() ?: return
            submissionApi.recordProctoringEvent(token, ProctoringEventRequest(examId, eventType, detail))
        } catch (_: Exception) {
            // 监考事件记录失败不阻断主流程
        }
    }

    /**
     * 主观题手动评分（教师）
     * @param submissionId 提交记录 ID
     * @param grades 评分 Map（questionId -> score）
     */
    suspend fun gradeSubmission(submissionId: Long, grades: Map<Long, Int>): Result<SubmissionResponse> =
        runWithToken { token ->
            val r = submissionApi.gradeSubmission(token, submissionId, grades)
            if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
        }

    /**
     * 获取某场考试的所有提交记录（教师/管理员）
     */
    suspend fun getExamSubmissions(examId: Long): Result<List<SubmissionResponse>> = runWithToken { token ->
        val r = submissionApi.getExamSubmissions(token, examId)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
    }

    /**
     * 分页查询提交记录
     */
    suspend fun querySubmissions(
        examId: Long? = null,
        userId: Long? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<PageSubmissionResponse> = runWithToken { token ->
        val r = submissionApi.querySubmissions(token, examId, userId, page, size)
        if (r.code == 200 && r.data != null) r.data else throw Exception(r.message)
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


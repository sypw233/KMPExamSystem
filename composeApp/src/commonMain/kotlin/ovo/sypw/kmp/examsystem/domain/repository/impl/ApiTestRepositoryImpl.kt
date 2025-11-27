package ovo.sypw.kmp.examsystem.domain.repository.impl

import kotlinx.coroutines.flow.Flow
import ovo.sypw.kmp.examsystem.data.api.ApiTestService
import ovo.sypw.kmp.examsystem.data.dto.ApiTestRequest
import ovo.sypw.kmp.examsystem.data.dto.ApiTestResponse
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult
import ovo.sypw.kmp.examsystem.domain.repository.ApiTestRepository
import ovo.sypw.kmp.examsystem.utils.Logger

/**
 * API测试Repository实现类
 * 负责API测试的数据操作和业务逻辑
 */
class ApiTestRepositoryImpl(
    private val apiTestService: ApiTestService
) : ApiTestRepository {

    /**
     * 执行API测试
     */
    override fun executeApiTest(request: ApiTestRequest): Flow<NetworkResult<ApiTestResponse>> {
        Logger.d("ApiTestRepositoryImpl", "开始执行API测试: ${request.method} ${request.url}")
        return apiTestService.executeApiTest(request)
    }

    /**
     * 验证URL格式
     */
    override fun isValidUrl(url: String): Boolean {
        return apiTestService.isValidUrl(url)
    }

    /**
     * 获取支持的HTTP方法列表
     */
    override fun getSupportedMethods(): List<String> {
        return apiTestService.getSupportedMethods()
    }
}
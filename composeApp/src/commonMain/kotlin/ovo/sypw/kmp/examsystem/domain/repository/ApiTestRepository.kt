package ovo.sypw.kmp.examsystem.domain.repository

import kotlinx.coroutines.flow.Flow
import ovo.sypw.kmp.examsystem.data.dto.ApiTestRequest
import ovo.sypw.kmp.examsystem.data.dto.ApiTestResponse
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult

/**
 * API测试Repository接口
 * 定义API测试相关的数据操作
 */
interface ApiTestRepository {

    /**
     * 执行API测试
     * @param request API测试请求
     * @return 测试结果Flow
     */
    fun executeApiTest(request: ApiTestRequest): Flow<NetworkResult<ApiTestResponse>>

    /**
     * 验证URL格式
     * @param url 待验证的URL
     * @return 是否为有效URL
     */
    fun isValidUrl(url: String): Boolean

    /**
     * 获取支持的HTTP方法列表
     * @return HTTP方法列表
     */
    fun getSupportedMethods(): List<String>
}
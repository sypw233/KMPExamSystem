package ovo.sypw.kmp.examsystem.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import ovo.sypw.kmp.examsystem.data.dto.result.NetworkResult

/**
 * 基础Repository接口
 * 提供通用的数据访问方法
 */
interface BaseRepository {

    /**
     * 执行网络请求并返回Flow
     * @param apiCall 网络请求函数
     * @return Flow<NetworkResult<T>>
     */
    fun <T> performNetworkCall(
        apiCall: suspend () -> NetworkResult<T>
    ): Flow<NetworkResult<T>> = flow {
        val result = apiCall()
        emit(result)
    }.onStart {
        emit(NetworkResult.Loading)
    }.catch { exception ->
        emit(NetworkResult.Error(exception, exception.message ?: "Unknown error"))
    }

    /**
     * 执行带缓存的网络请求
     * @param cacheCall 缓存获取函数
     * @param networkCall 网络请求函数
     * @param saveCall 缓存保存函数
     * @param shouldFetch 是否需要从网络获取数据
     * @return Flow<NetworkResult<T>>
     */
    fun <T> performNetworkCallWithCache(
        cacheCall: suspend () -> T?,
        networkCall: suspend () -> NetworkResult<T>,
        saveCall: suspend (T) -> Unit,
        shouldFetch: (T?) -> Boolean = { true }
    ): Flow<NetworkResult<T>> = flow {
        // 首先尝试从缓存获取数据
        val cachedData = cacheCall()
        if (cachedData != null) {
            emit(NetworkResult.Success(cachedData))
        }

        // 判断是否需要从网络获取数据
        if (shouldFetch(cachedData)) {
            emit(NetworkResult.Loading)

            when (val networkResult = networkCall()) {
                is NetworkResult.Success -> {
                    // 保存到缓存
                    saveCall(networkResult.data)
                    emit(networkResult)
                }

                is NetworkResult.Error -> {
                    // 如果有缓存数据，则不发送错误
                    if (cachedData == null) {
                        emit(networkResult)
                    }
                }

                else -> {
                    // 其他状态不处理
                }
            }
        }
    }.catch { exception ->
        emit(NetworkResult.Error(exception, exception.message ?: "Unknown error"))
    }

    /**
     * 执行简单的网络请求（不带加载状态）
     * @param apiCall 网络请求函数
     * @return Flow<NetworkResult<T>>
     */
    fun <T> performSimpleNetworkCall(
        apiCall: suspend () -> NetworkResult<T>
    ): Flow<NetworkResult<T>> = flow {
        val result = apiCall()
        emit(result)
    }.catch { exception ->
        emit(NetworkResult.Error(exception, exception.message ?: "Unknown error"))
    }
}
package ovo.sypw.kmp.examsystem.data.dto.result

/**
 * 网络请求结果封装类
 * @param T 数据类型
 */
sealed class NetworkResult<out T> {

    /**
     * 请求成功
     * @param data 响应数据
     */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /**
     * 请求失败
     * @param exception 异常信息
     * @param message 错误消息
     */
    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "Unknown error"
    ) : NetworkResult<Nothing>()

    /**
     * 加载中
     */
    data object Loading : NetworkResult<Nothing>()


    /**
     * 判断是否为成功状态
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * 判断是否为错误状态
     */
    val isError: Boolean
        get() = this is Error

    /**
     * 判断是否为加载状态
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * 获取数据，如果不是成功状态则返回null
     */
    fun getDataOrNull(): T? {
        return if (this is Success) data else null
    }

    /**
     * 获取错误信息，如果不是错误状态则返回null
     */
    fun getErrorOrNull(): String? {
        return if (this is Error) message else null
    }

    /**
     * 映射数据
     * @param transform 转换函数
     * @return 转换后的结果
     */
    inline fun <R> map(transform: (T) -> R): NetworkResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
    }

    /**
     * 当成功时执行操作
     * @param action 执行的操作
     */
    inline fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }

    /**
     * 当失败时执行操作
     * @param action 执行的操作
     */
    inline fun onError(action: (String) -> Unit): NetworkResult<T> {
        if (this is Error) {
            action(message)
        }
        return this
    }

    /**
     * 当加载时执行操作
     * @param action 执行的操作
     */
    inline fun onLoading(action: () -> Unit): NetworkResult<T> {
        if (this is Loading) {
            action()
        }
        return this
    }
}
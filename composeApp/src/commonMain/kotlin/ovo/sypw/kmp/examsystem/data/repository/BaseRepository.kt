package ovo.sypw.kmp.examsystem.data.repository

import ovo.sypw.kmp.examsystem.data.storage.TokenStorage
import ovo.sypw.kmp.examsystem.utils.Logger

/**
 * 仓库基类
 * 提供通用的 Token 获取和错误处理逻辑, 避免子类重复样板代码
 * @param tokenStorage Token 存储, 用于获取当前用户的访问令牌
 */
abstract class BaseRepository(
    protected val tokenStorage: TokenStorage
) {

    /**
     * 获取当前访问令牌, 未登录时抛出异常
     * @return 有效的访问令牌
     * @throws IllegalStateException 未登录时抛出
     */
    protected suspend fun requireToken(): String {
        return tokenStorage.getAccessToken()
            ?: throw IllegalStateException("未登录, 请先登录后再操作")
    }

    /**
     * 带 Token 执行操作的通用方法
     * 自动获取 Token, 统一异常处理, 返回 Result 封装
     * @param block 需要 Token 的操作, Token 作为参数传入
     * @return 操作结果封装为 Result
     */
    protected suspend fun <T> runWithToken(block: suspend (String) -> T): Result<T> {
        return try {
            val token = requireToken()
            Result.success(block(token))
        } catch (e: Exception) {
            Logger.e("BaseRepository", "操作失败: ${e.message}")
            Result.failure(e)
        }
    }
}

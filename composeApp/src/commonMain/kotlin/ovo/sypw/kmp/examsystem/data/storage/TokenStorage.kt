package ovo.sypw.kmp.examsystem.data.storage

/**
 * Token存储接口
 * 定义跨平台的Token存储和获取操作
 */
interface TokenStorage {

    /**
     * 保存访问令牌
     * @param token 访问令牌
     */
    suspend fun saveAccessToken(token: String)

    /**
     * 获取访问令牌
     * @return 访问令牌，如果不存在则返回null
     */
    suspend fun getAccessToken(): String?

    /**
     * 保存刷新令牌
     * @param refreshToken 刷新令牌
     */
    suspend fun saveRefreshToken(refreshToken: String)

    /**
     * 获取刷新令牌
     * @return 刷新令牌，如果不存在则返回null
     */
    suspend fun getRefreshToken(): String?

    /**
     * 清除所有令牌
     */
    suspend fun clearTokens()

    /**
     * 检查是否有有效的访问令牌
     * @return 如果有有效令牌返回true，否则返回false
     */
    suspend fun hasValidToken(): Boolean

    /**
     * 保存用户ID
     * @param userId 用户ID
     */
    suspend fun saveUserId(userId: String)

    /**
     * 获取用户ID
     * @return 用户ID，如果不存在则返回null
     */
    suspend fun getUserId(): String?

    /**
     * 保存用户信息（JSON格式）
     * @param userInfo 用户信息JSON字符串
     */
    suspend fun saveUserInfo(userInfo: String)

    /**
     * 获取用户信息
     * @return 用户信息JSON字符串，如果不存在则返回null
     */
    suspend fun getUserInfo(): String?
}
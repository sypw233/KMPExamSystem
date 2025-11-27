package ovo.sypw.kmp.examsystem.data.storage


/**
 * Token存储实现类
 * 使用LocalStorage进行跨平台的Token存储
 */
class TokenStorageImpl(private val localStorage: LocalStorage) : TokenStorage {

    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val USER_ID_KEY = "user_id"
        private const val USER_INFO_KEY = "user_info"
        private const val TOKEN_EXPIRY_KEY = "token_expiry"
    }

    /**
     * 保存访问令牌
     * @param token 访问令牌
     */
    override suspend fun saveAccessToken(token: String) {
        localStorage.saveString(ACCESS_TOKEN_KEY, token)
    }

    /**
     * 获取访问令牌
     * @return 访问令牌，如果不存在则返回null
     */
    override suspend fun getAccessToken(): String? {
        return localStorage.getString(ACCESS_TOKEN_KEY)
    }

    /**
     * 保存刷新令牌
     * @param refreshToken 刷新令牌
     */
    override suspend fun saveRefreshToken(refreshToken: String) {
        localStorage.saveString(REFRESH_TOKEN_KEY, refreshToken)
    }

    /**
     * 获取刷新令牌
     * @return 刷新令牌，如果不存在则返回null
     */
    override suspend fun getRefreshToken(): String? {
        return localStorage.getString(REFRESH_TOKEN_KEY)
    }

    /**
     * 清除所有令牌
     */
    override suspend fun clearTokens() {
        localStorage.remove(ACCESS_TOKEN_KEY)
        localStorage.remove(REFRESH_TOKEN_KEY)
        localStorage.remove(USER_ID_KEY)
        localStorage.remove(USER_INFO_KEY)
        localStorage.remove(TOKEN_EXPIRY_KEY)
    }

    /**
     * 检查是否有有效的访问令牌
     * @return 如果有有效令牌返回true，否则返回false
     */
    override suspend fun hasValidToken(): Boolean {
        val token = getAccessToken()
        return !token.isNullOrBlank()
    }

    /**
     * 保存用户ID
     * @param userId 用户ID
     */
    override suspend fun saveUserId(userId: String) {
        localStorage.saveString(USER_ID_KEY, userId)
    }

    /**
     * 获取用户ID
     * @return 用户ID，如果不存在则返回null
     */
    override suspend fun getUserId(): String? {
        return localStorage.getString(USER_ID_KEY)
    }

    /**
     * 保存用户信息（JSON格式）
     * @param userInfo 用户信息JSON字符串
     */
    override suspend fun saveUserInfo(userInfo: String) {
        localStorage.saveString(USER_INFO_KEY, userInfo)
    }

    /**
     * 获取用户信息
     * @return 用户信息JSON字符串，如果不存在则返回null
     */
    override suspend fun getUserInfo(): String? {
        return localStorage.getString(USER_INFO_KEY)
    }

    /**
     * 保存完整的登录信息
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @param userId 用户ID
     * @param userInfo 用户信息JSON字符串
     */
    suspend fun saveLoginInfo(
        accessToken: String,
        refreshToken: String? = null,
        userId: String? = null,
        userInfo: String? = null
    ) {
        saveAccessToken(accessToken)
        refreshToken?.let { saveRefreshToken(it) }
        userId?.let { saveUserId(it) }
        userInfo?.let { saveUserInfo(it) }
    }

    /**
     * 获取token过期时间戳
     * @return 时间戳，如果不存在则返回0
     */
    suspend fun getTokenExpiry(): Long {
        return localStorage.getLong(TOKEN_EXPIRY_KEY, 0L)
    }
}
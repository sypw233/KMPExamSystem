package ovo.sypw.kmp.examsystem.domain

import ovo.sypw.kmp.examsystem.data.dto.UserInfo

/**
 * 认证状态封装类
 */
sealed class AuthState {
    /**
     * 未认证
     */
    data object Unauthenticated : AuthState()

    /**
     * 加载中
     */
    data object Loading : AuthState()

    /**
     * 已认证
     * @param user 用户信息
     */
    data class Authenticated(val user: UserInfo) : AuthState()

    /**
     * 错误
     * @param message 错误消息
     */
    data class Error(val message: String) : AuthState()
}

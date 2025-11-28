package ovo.sypw.kmp.examsystem.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * 导航管理器类
 * 负责管理当前选中的页面状态和考试模式
 */
class NavigationManager {
    private val _currentScreen = mutableStateOf(AppScreen.HOME.route)
    val currentScreen: State<String> = _currentScreen

    // 考试模式状态
    private val _isInExamMode = mutableStateOf(false)
    val isInExamMode: State<Boolean> = _isInExamMode

    // 当前考试ID（用于单例模式）
    private val _currentExamId = mutableStateOf<Long?>(null)
    val currentExamId: State<Long?> = _currentExamId

    /**
     * 导航到指定页面
     * @param route 目标页面路由
     */
    fun navigateTo(route: String) {
        // 如果在考试模式中，不允许导航
        if (_isInExamMode.value) {
            return
        }
        _currentScreen.value = route
    }

    /**
     * 检查当前是否为指定页面
     * @param route 页面路由
     * @return 是否为当前页面
     */
    fun isCurrentScreen(route: String): Boolean {
        return _currentScreen.value == route
    }

    /**
     * 进入考试模式
     * @param examId 考试ID
     * @return 是否成功进入（如果已有考试进行中则返回false）
     */
    fun enterExamMode(examId: Long): Boolean {
        // 检查是否已有考试进行中（单例模式）
        if (_isInExamMode.value) {
            return false
        }
        
        _isInExamMode.value = true
        _currentExamId.value = examId
        return true
    }

    /**
     * 退出考试模式
     */
    fun exitExamMode() {
        _isInExamMode.value = false
        _currentExamId.value = null
    }

    /**
     * 检查指定考试是否为当前进行中的考试
     */
    fun isCurrentExam(examId: Long): Boolean {
        return _isInExamMode.value && _currentExamId.value == examId
    }
}

/**
 * 创建导航管理器的Composable函数
 * @return 导航管理器实例
 */
@Composable
fun rememberNavigationManager(): NavigationManager {
    return remember { NavigationManager() }
}
package ovo.sypw.kmp.examsystem.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * 导航管理器
 * 持有当前路由、考试模式状态，并感知用户角色以提供角色专属导航项
 */
class NavigationManager {

    // 当前页面路由，默认首页
    private val _currentScreen = mutableStateOf(AppRoutes.HOME)
    val currentScreen: State<String> = _currentScreen

    // 当前用户角色，未知时为 UNKNOWN
    private val _userRole = mutableStateOf(UserRole.UNKNOWN)
    val userRole: State<UserRole> = _userRole

    // 考试模式：进入全屏答题时为 true
    private val _isInExamMode = mutableStateOf(false)
    val isInExamMode: State<Boolean> = _isInExamMode

    // 当前答题的考试 ID
    private val _currentExamId = mutableStateOf<Long?>(null)
    val currentExamId: State<Long?> = _currentExamId

    // 导航历史栈
    private val _navigationHistory = mutableStateListOf<String>()
    val navigationHistory: SnapshotStateList<String> = _navigationHistory

    // ─── 角色 ─────────────────────────────────────────────────────────────

    /** 设置当前用户角色（登录成功后调用） */
    fun setRole(role: UserRole) {
        _userRole.value = role
        clearHistory()
        // 切换角色时，若当前路由在新角色的导航项中不存在，跳回首页
        val validRoutes = getNavigationItemsForRole(role).map { it.route }
        if (_currentScreen.value !in validRoutes) {
            _currentScreen.value = AppRoutes.HOME
        }
    }

    /** 根据角色字符串设置角色 */
    fun setRoleFromString(roleStr: String?) {
        setRole(UserRole.from(roleStr))
    }

    /** 获取当前角色的导航项列表 */
    fun navigationItems(): List<NavigationItem> =
        getNavigationItemsForRole(_userRole.value)

    /** 检查路由是否允许当前角色访问 */
    fun canNavigateTo(route: String): Boolean =
        getNavigationItemsForRole(_userRole.value).any { it.route == route }

    // ─── 路由 ─────────────────────────────────────────────────────────────

    /** 导航到指定路由（考试模式中不允许切换） */
    fun navigateTo(route: String) {
        if (_isInExamMode.value) return
        if (!canNavigateTo(route)) return
        if (route != _currentScreen.value) {
            _navigationHistory.add(_currentScreen.value)
        }
        _currentScreen.value = route
    }

    /** 检查当前是否在指定路由 */
    fun isCurrentScreen(route: String): Boolean =
        _currentScreen.value == route

    /** 返回上一个页面，若历史为空则不做操作 */
    fun popBack(): Boolean {
        if (_isInExamMode.value) return false
        if (_navigationHistory.isEmpty()) return false
        val previous = _navigationHistory.removeLast()
        _currentScreen.value = previous
        return true
    }

    /** 清空导航历史（角色切换时调用） */
    fun clearHistory() {
        _navigationHistory.clear()
    }

    // ─── 考试模式 ─────────────────────────────────────────────────────────

    /** 进入全屏考试模式 */
    fun enterExamMode(examId: Long): Boolean {
        if (_isInExamMode.value) return false
        _isInExamMode.value = true
        _currentExamId.value = examId
        return true
    }

    /** 退出考试模式 */
    fun exitExamMode() {
        _isInExamMode.value = false
        _currentExamId.value = null
    }

    /** 检查指定考试是否为当前进行中的考试 */
    fun isCurrentExam(examId: Long): Boolean =
        _isInExamMode.value && _currentExamId.value == examId
}

/** 创建并 remember NavigationManager 实例 */
@Composable
fun rememberNavigationManager(): NavigationManager = remember { NavigationManager() }

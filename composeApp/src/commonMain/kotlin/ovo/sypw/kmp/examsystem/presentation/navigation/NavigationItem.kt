package ovo.sypw.kmp.examsystem.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 应用内所有可导航的路由定义
 */
object AppRoutes {
    const val HOME        = "home"         // 首页（所有角色）
    const val COURSES     = "courses"      // 课程（学生浏览 / 教师&管理员管理）
    const val EXAMS       = "exams"        // 考试（学生参加 / 教师&管理员管理）
    const val USERS       = "users"        // 用户管理（仅管理员）
    const val PROFILE     = "profile"      // 我的（所有角色）
    const val STATISTICS  = "statistics"   // 数据概览（管理员首页 = statistics）
    const val NOTIFICATIONS = "notifications" // 通知（嵌套在 profile，这里保留路由）
    const val QUESTION_BANKS = "question_banks"
    const val SYSTEM_SETTINGS = "system_settings"
}

/**
 * 用户角色枚举
 */
enum class UserRole {
    STUDENT, TEACHER, ADMIN, UNKNOWN;

    companion object {
        fun from(role: String?): UserRole = when (role?.uppercase()) {
            "STUDENT" -> STUDENT
            "TEACHER" -> TEACHER
            "ADMIN"   -> ADMIN
            else      -> UNKNOWN
        }
    }
}

/**
 * 导航项描述
 */
data class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

/**
 * 根据角色返回底部/侧边导航项列表
 *
 * 学生:  首页 | 课程 | 考试 | 我的
 * 教师:  首页 | 课程管理 | 题目管理 | 考试管理 | 我的
 * 管理员: 首页 | 用户 | 课程 | 考试 | 题目 | 我的
 */
fun getNavigationItemsForRole(role: UserRole): List<NavigationItem> = when (role) {
    UserRole.STUDENT -> listOf(
        NavigationItem(AppRoutes.HOME,      "首页",     Icons.Default.Home),
        NavigationItem(AppRoutes.COURSES,   "课程",     Icons.Default.Book),
        NavigationItem(AppRoutes.EXAMS,     "考试",     Icons.AutoMirrored.Filled.Assignment),
        NavigationItem(AppRoutes.PROFILE,   "我的",     Icons.Default.Person)
    )
    UserRole.TEACHER -> listOf(
        NavigationItem(AppRoutes.HOME,      "首页",     Icons.Default.Home),
        NavigationItem(AppRoutes.COURSES,   "课程管理", Icons.Default.Book),
        NavigationItem(AppRoutes.QUESTION_BANKS, "题目管理", Icons.Default.Quiz),
        NavigationItem(AppRoutes.EXAMS,     "考试管理", Icons.AutoMirrored.Filled.Assignment),
        NavigationItem(AppRoutes.PROFILE,   "我的",     Icons.Default.Person)
    )
    UserRole.ADMIN -> listOf(
        NavigationItem(AppRoutes.HOME,      "首页",     Icons.Default.Dashboard),
        NavigationItem(AppRoutes.USERS,     "用户管理",     Icons.Default.ManageAccounts),
        NavigationItem(AppRoutes.COURSES,   "课程管理",     Icons.Default.Book),
        NavigationItem(AppRoutes.QUESTION_BANKS, "题目管理", Icons.Default.Quiz),
        NavigationItem(AppRoutes.EXAMS,     "考试管理",     Icons.AutoMirrored.Filled.Assignment),
        NavigationItem(AppRoutes.SYSTEM_SETTINGS, "系统", Icons.Default.SettingsApplications),
        NavigationItem(AppRoutes.PROFILE,   "我的",     Icons.Default.Person)
    )
    UserRole.UNKNOWN -> listOf(
        NavigationItem(AppRoutes.HOME,    "首页", Icons.Default.Home),
        NavigationItem(AppRoutes.PROFILE, "我的", Icons.Default.Person)
    )
}

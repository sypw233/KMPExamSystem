package ovo.sypw.kmp.examsystem.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ManageAccounts
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
 * @param priority 优先级（数值越小优先级越高，用于移动端底部导航排序）
 */
data class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val priority: Int = 5  // 默认优先级为5
)

/**
 * 底部导航展示模型
 *
 * M3 NavigationBar 适合展示 5 个以内目的地。超过时保留 4 个主入口，其余放入“更多”菜单。
 */
data class BottomNavigationItems(
    val primaryItems: List<NavigationItem>,
    val overflowItems: List<NavigationItem>
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
        NavigationItem(AppRoutes.HOME,      "首页",     Icons.Default.Home, priority = 1),
        NavigationItem(AppRoutes.COURSES,   "课程",     Icons.Default.Book, priority = 2),
        NavigationItem(AppRoutes.EXAMS,     "考试",     Icons.AutoMirrored.Filled.Assignment, priority = 3),
        NavigationItem(AppRoutes.PROFILE,   "我的",     Icons.Default.Person, priority = 1)
    )
    UserRole.TEACHER -> listOf(
        NavigationItem(AppRoutes.HOME,      "首页",     Icons.Default.Home, priority = 1),
        NavigationItem(AppRoutes.COURSES,   "课程管理", Icons.Default.Book, priority = 2),
        NavigationItem(AppRoutes.QUESTION_BANKS, "题目管理", Icons.Default.Quiz, priority = 4),
        NavigationItem(AppRoutes.EXAMS,     "考试管理", Icons.AutoMirrored.Filled.Assignment, priority = 3),
        NavigationItem(AppRoutes.PROFILE,   "我的",     Icons.Default.Person, priority = 1)
    )
    UserRole.ADMIN -> listOf(
        NavigationItem(AppRoutes.HOME,      "首页",     Icons.Default.Dashboard, priority = 1),
        NavigationItem(AppRoutes.USERS,     "用户管理",     Icons.Default.ManageAccounts, priority = 2),
        NavigationItem(AppRoutes.COURSES,   "课程管理",     Icons.Default.Book, priority = 3),
        NavigationItem(AppRoutes.QUESTION_BANKS, "题目管理", Icons.Default.Quiz, priority = 5),
        NavigationItem(AppRoutes.EXAMS,     "考试管理",     Icons.AutoMirrored.Filled.Assignment, priority = 4),
        NavigationItem(AppRoutes.SYSTEM_SETTINGS, "系统", Icons.Default.SettingsApplications, priority = 10),
        NavigationItem(AppRoutes.PROFILE,   "我的",     Icons.Default.Person, priority = 1)
    )
    UserRole.UNKNOWN -> listOf(
        NavigationItem(AppRoutes.HOME,    "首页", Icons.Default.Home, priority = 1),
        NavigationItem(AppRoutes.PROFILE, "我的", Icons.Default.Person, priority = 1)
    )
}

fun getBottomNavigationItemsForRole(role: UserRole): BottomNavigationItems {
    val items = getNavigationItemsForRole(role)
    // 按优先级排序（数值越小优先级越高），取前4个为主导航项
    val sortedItems = items.sortedBy { it.priority }
    return if (sortedItems.size <= 5) {
        BottomNavigationItems(primaryItems = sortedItems, overflowItems = emptyList())
    } else {
        BottomNavigationItems(primaryItems = sortedItems.take(4), overflowItems = sortedItems.drop(4))
    }
}

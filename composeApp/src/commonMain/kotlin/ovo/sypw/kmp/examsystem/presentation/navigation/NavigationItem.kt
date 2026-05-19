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

object AppRoutes {
    const val HOME = "home"
    const val COURSES = "courses"
    const val EXAMS = "exams"
    const val USERS = "users"
    const val PROFILE = "profile"
    const val QUESTION_BANKS = "question_banks"
    const val SYSTEM_SETTINGS = "system_settings"
}

enum class UserRole {
    STUDENT, TEACHER, ADMIN, UNKNOWN;

    companion object {
        fun from(role: String?): UserRole = when (role?.uppercase()) {
            "STUDENT" -> STUDENT
            "TEACHER" -> TEACHER
            "ADMIN" -> ADMIN
            else -> UNKNOWN
        }
    }
}

data class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val priority: Int = 5
)

data class BottomNavigationItems(
    val primaryItems: List<NavigationItem>,
    val overflowItems: List<NavigationItem>
)

fun getNavigationItemsForRole(role: UserRole): List<NavigationItem> = when (role) {
    UserRole.STUDENT -> listOf(
        NavigationItem(AppRoutes.HOME, "首页", Icons.Default.Home, priority = 1),
        NavigationItem(AppRoutes.COURSES, "课程", Icons.Default.Book, priority = 2),
        NavigationItem(AppRoutes.EXAMS, "考试", Icons.AutoMirrored.Filled.Assignment, priority = 3),
        NavigationItem(AppRoutes.PROFILE, "我的", Icons.Default.Person, priority = 4)
    )
    UserRole.TEACHER -> listOf(
        NavigationItem(AppRoutes.HOME, "首页", Icons.Default.Home, priority = 1),
        NavigationItem(AppRoutes.COURSES, "课程管理", Icons.Default.Book, priority = 2),
        NavigationItem(AppRoutes.EXAMS, "考试管理", Icons.AutoMirrored.Filled.Assignment, priority = 3),
        NavigationItem(AppRoutes.QUESTION_BANKS, "题库管理", Icons.Default.Quiz, priority = 4),
        NavigationItem(AppRoutes.PROFILE, "我的", Icons.Default.Person, priority = 5)
    )
    UserRole.ADMIN -> listOf(
        NavigationItem(AppRoutes.HOME, "首页", Icons.Default.Dashboard, priority = 1),
        NavigationItem(AppRoutes.USERS, "用户管理", Icons.Default.ManageAccounts, priority = 2),
        NavigationItem(AppRoutes.COURSES, "课程管理", Icons.Default.Book, priority = 3),
        NavigationItem(AppRoutes.EXAMS, "考试管理", Icons.AutoMirrored.Filled.Assignment, priority = 4),
        NavigationItem(AppRoutes.QUESTION_BANKS, "题库管理", Icons.Default.Quiz, priority = 5),
        NavigationItem(AppRoutes.PROFILE, "我的", Icons.Default.Person, priority = 6),
        NavigationItem(AppRoutes.SYSTEM_SETTINGS, "系统设置", Icons.Default.SettingsApplications, priority = 10)
    )
    UserRole.UNKNOWN -> listOf(
        NavigationItem(AppRoutes.HOME, "首页", Icons.Default.Home, priority = 1),
        NavigationItem(AppRoutes.PROFILE, "我的", Icons.Default.Person, priority = 2)
    )
}

fun getBottomNavigationItemsForRole(role: UserRole): BottomNavigationItems {
    val sortedItems = getNavigationItemsForRole(role).sortedBy { it.priority }
    return if (sortedItems.size <= 5) {
        BottomNavigationItems(primaryItems = sortedItems, overflowItems = emptyList())
    } else {
        BottomNavigationItems(primaryItems = sortedItems.take(5), overflowItems = sortedItems.drop(5))
    }
}

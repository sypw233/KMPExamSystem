package ovo.sypw.kmp.examsystem.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import ovo.sypw.kmp.examsystem.presentation.screens.DashboardScreen
import ovo.sypw.kmp.examsystem.presentation.screens.ExamsScreen
import ovo.sypw.kmp.examsystem.presentation.screens.CoursesScreen
import ovo.sypw.kmp.examsystem.presentation.screens.ProfileScreen

/**
 * 应用页面枚举，同时包含路由、标题与图标
 */
enum class AppScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    HOME(
        "home",
        "首页",
        Icons.Default.Home
    ),
    EXAMS(
        "exams",
        "考试",
        Icons.Default.Assignment
    ),
    COURSES(
        "courses",
        "课程",
        Icons.Default.Book
    ),
    PROFILE(
        "profile",
        "我的",
        Icons.Default.Person
    )
}

/**
 * 导航项目数据类
 */
data class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

/**
 * 获取所有导航项目
 */
fun getNavigationItems(): List<NavigationItem> {
    return AppScreen.entries.map {
        NavigationItem(
            route = it.route,
            title = it.title,
            icon = it.icon
        )
    }
}

@Composable
fun NavigationScreen(
    route: String,
    navigationManager: NavigationManager
) {
    when (route) {
        AppScreen.EXAMS.route -> {
            ExamsScreen(navigationManager = navigationManager)
        }

        AppScreen.COURSES.route -> {
            CoursesScreen()
        }

        AppScreen.PROFILE.route -> {
            ProfileScreen()
        }

        else -> {
            DashboardScreen()
        }
    }
}

package ovo.sypw.kmp.examsystem.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import ovo.sypw.kmp.examsystem.presentation.screens.HomeScreen
import ovo.sypw.kmp.examsystem.presentation.screens.test.ApiTestScreen
import ovo.sypw.kmp.examsystem.presentation.screens.test.FileTestScreen
import ovo.sypw.kmp.examsystem.presentation.screens.test.ImageTestScreen

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
        "HOME",
        Icons.Default.Home
    ),
    IMAGE_TEST(
        "image_test",
        "IMAGE TEST",
        Icons.Default.Image
    ),
    FILE_TEST(
        "file_test",
        "FILE TEST",
        Icons.Default.FileOpen
    ),
    API_TEST(
        "api_test",
        "API TEST",
        Icons.Default.Api
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
fun NavigationScreen(route: String) {
    when (route) {
        AppScreen.IMAGE_TEST.route -> {
            ImageTestScreen()
        }

        AppScreen.FILE_TEST.route -> {
            FileTestScreen()
        }

        AppScreen.API_TEST.route -> {
            ApiTestScreen()
        }

        else -> {
            HomeScreen()
        }
    }
}

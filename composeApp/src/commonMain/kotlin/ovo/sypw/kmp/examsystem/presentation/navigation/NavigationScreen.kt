package ovo.sypw.kmp.examsystem.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import ovo.sypw.kmp.examsystem.presentation.screens.CoursesScreen
import ovo.sypw.kmp.examsystem.presentation.screens.DashboardScreen
import ovo.sypw.kmp.examsystem.presentation.screens.ExamsScreen
import ovo.sypw.kmp.examsystem.presentation.screens.ProfileScreen
import ovo.sypw.kmp.examsystem.presentation.screens.admin.AdminDashboardScreen
import ovo.sypw.kmp.examsystem.presentation.screens.admin.QuestionBankScreen
import ovo.sypw.kmp.examsystem.presentation.screens.admin.SystemSettingsScreen
import ovo.sypw.kmp.examsystem.presentation.screens.admin.UserManageScreen
import ovo.sypw.kmp.examsystem.presentation.screens.teacher.TeacherExamManageScreen
import ovo.sypw.kmp.examsystem.utils.Logger

/**
 * 主路由分发。
 */
@Composable
fun NavigationScreen(
    route: String,
    navigationManager: NavigationManager
) {
    val role by navigationManager.userRole

    AnimatedContent(
        targetState = route,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "NavigationTransition"
    ) { targetRoute ->
        when (targetRoute) {
            AppRoutes.HOME -> {
                if (role == UserRole.ADMIN) {
                    AdminDashboardScreen()
                } else {
                    DashboardScreen(
                        onNavigateToExams = { navigationManager.navigateTo(AppRoutes.EXAMS) },
                        onNavigateToCourses = { navigationManager.navigateTo(AppRoutes.COURSES) },
                        onNavigateToNotifications = { navigationManager.navigateTo(AppRoutes.PROFILE) }
                    )
                }
            }

            AppRoutes.COURSES -> {
                CoursesScreen(role = role)
            }

            AppRoutes.EXAMS -> {
                when (role) {
                    UserRole.TEACHER, UserRole.ADMIN -> {
                        TeacherExamManageScreen(
                            onBack = {},
                            userRole = role
                        )
                    }

                    else -> {
                        ExamsScreen(
                            navigationManager = navigationManager,
                            onStartExam = { examId -> navigationManager.enterExamMode(examId) }
                        )
                    }
                }
            }

            AppRoutes.QUESTION_BANKS -> {
                QuestionBankScreen()
            }

            AppRoutes.USERS -> {
                UserManageScreen()
            }

            AppRoutes.PROFILE -> {
                ProfileScreen()
            }

            AppRoutes.SYSTEM_SETTINGS -> {
                if (role == UserRole.ADMIN) {
                    SystemSettingsScreen()
                } else {
                    Logger.w("NavigationScreen", "非管理员角色($role)访问 SYSTEM_SETTINGS，已重定向到首页")
                    DashboardScreen(
                        onNavigateToExams = { navigationManager.navigateTo(AppRoutes.EXAMS) },
                        onNavigateToCourses = { navigationManager.navigateTo(AppRoutes.COURSES) },
                        onNavigateToNotifications = { navigationManager.navigateTo(AppRoutes.PROFILE) }
                    )
                }
            }

            else -> {
                DashboardScreen(
                    onNavigateToExams = { navigationManager.navigateTo(AppRoutes.EXAMS) },
                    onNavigateToCourses = { navigationManager.navigateTo(AppRoutes.COURSES) },
                    onNavigateToNotifications = { navigationManager.navigateTo(AppRoutes.PROFILE) }
                )
            }
        }
    }
}

package ovo.sypw.kmp.examsystem.presentation.navigation

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
 * 主路由分发组件
 *
 * 注意：路由内容根据角色有语义差异：
 *   - HOME:      管理员 = 数据图表总览 / 教师&学生 = 首页(通知+即将考试)
 *   - COURSES:   管理员&教师 = 课程管理(增删改) / 学生 = 浏览选课
 *   - EXAMS:     管理员&教师 = 考试管理 / 学生 = 参加考试
 *   - QUESTIONS: 教师/管理员专属
 *   - USERS:     管理员专属
 *   - PROFILE:   所有角色
 */
@Composable
fun NavigationScreen(
    route: String,
    navigationManager: NavigationManager
) {
    val role by navigationManager.userRole

    when (route) {

        // ── 首页 ──────────────────────────────────────────────────────────
        AppRoutes.HOME -> {
            if (role == UserRole.ADMIN) {
                AdminDashboardScreen()
            } else {
                DashboardScreen()
            }
        }

        // ── 课程 ──────────────────────────────────────────────────────────
        AppRoutes.COURSES -> {
            // 教师和管理员看课程管理（增删改），学生看课程列表/选课
            CoursesScreen(role = role)
        }

        // ── 考试 ──────────────────────────────────────────────────────────
        AppRoutes.EXAMS -> {
            when (role) {
                UserRole.TEACHER, UserRole.ADMIN -> {
                    TeacherExamManageScreen(
                        onBack = { /* 顶层无返回 */ },
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

        // ── 题目 ──────────────────────────────────────────────────────────
        AppRoutes.QUESTION_BANKS -> {
            QuestionBankScreen()
        }

        // ── 用户（管理员） ──────────────────────────────────────────────────
        AppRoutes.USERS -> {
            UserManageScreen()
        }

        // ── 我的 ──────────────────────────────────────────────────────────
        AppRoutes.PROFILE -> {
            ProfileScreen()
        }

        AppRoutes.SYSTEM_SETTINGS -> {
            if (role == UserRole.ADMIN) {
                SystemSettingsScreen()
            } else {
                Logger.w("NavigationScreen: 非管理员角色($role)访问 SYSTEM_SETTINGS 路由，已重定向到首页")
                DashboardScreen()
            }
        }

        // 兜底
        else -> {
            DashboardScreen()
        }
    }
}

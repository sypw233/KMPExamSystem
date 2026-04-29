package ovo.sypw.kmp.examsystem.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.domain.AuthState
import ovo.sypw.kmp.examsystem.presentation.navigation.UserRole
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseViewModel

@Composable
fun CoursesScreen(role: UserRole? = null) {
    val courseViewModel: CourseViewModel = koinInject()
    val authRepository: AuthRepository = koinInject()

    val authState by authRepository.authState.collectAsState()
    val effectiveRole = role ?: UserRole.from((authState as? AuthState.Authenticated)?.user?.role)
    val isManager = effectiveRole == UserRole.ADMIN || effectiveRole == UserRole.TEACHER

    if (isManager) {
        CourseManageScreen(courseViewModel = courseViewModel, userRole = effectiveRole)
    } else {
        StudentCourseScreen(courseViewModel = courseViewModel)
    }
}

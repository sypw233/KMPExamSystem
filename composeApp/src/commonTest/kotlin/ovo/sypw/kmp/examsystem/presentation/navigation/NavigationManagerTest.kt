package ovo.sypw.kmp.examsystem.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavigationManagerTest {
    @Test
    fun studentCannotNavigateToAdminOnlyRoute() {
        val manager = NavigationManager()
        manager.setRole(UserRole.STUDENT)

        manager.navigateTo(AppRoutes.USERS)

        assertEquals(AppRoutes.HOME, manager.currentScreen.value)
        assertTrue(manager.navigationHistory.isEmpty())
    }

    @Test
    fun teacherCanNavigateToQuestionBanksButNotUserManagement() {
        val manager = NavigationManager()
        manager.setRole(UserRole.TEACHER)

        manager.navigateTo(AppRoutes.QUESTION_BANKS)
        manager.navigateTo(AppRoutes.USERS)

        assertEquals(AppRoutes.QUESTION_BANKS, manager.currentScreen.value)
        assertEquals(listOf(AppRoutes.HOME), manager.navigationHistory.toList())
    }

    @Test
    fun adminCanNavigateToSystemSettings() {
        val manager = NavigationManager()
        manager.setRole(UserRole.ADMIN)

        manager.navigateTo(AppRoutes.SYSTEM_SETTINGS)

        assertEquals(AppRoutes.SYSTEM_SETTINGS, manager.currentScreen.value)
        assertEquals(listOf(AppRoutes.HOME), manager.navigationHistory.toList())
    }

    @Test
    fun adminBottomNavigationUsesOverflowForMoreThanFiveDestinations() {
        val items = getBottomNavigationItemsForRole(UserRole.ADMIN)

        assertEquals(4, items.primaryItems.size)
        assertEquals(
            getNavigationItemsForRole(UserRole.ADMIN).size,
            items.primaryItems.size + items.overflowItems.size
        )
        assertTrue(items.overflowItems.isNotEmpty())
    }
}

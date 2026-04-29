package ovo.sypw.kmp.examsystem.presentation.screens.admin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ovo.sypw.kmp.examsystem.data.dto.UserQueryParams

class UserManageFilterTest {
    @Test
    fun applyingStatusFilterPreservesOtherFiltersAndResetsPage() {
        val params = UserQueryParams(
            role = "teacher",
            status = 1,
            keyword = "王老师",
            page = 3,
            size = 50
        )

        val updated = params.withUserStatusFilter(0)

        assertEquals("teacher", updated.role)
        assertEquals(0, updated.status)
        assertEquals("王老师", updated.keyword)
        assertEquals(0, updated.page)
        assertEquals(50, updated.size)
    }

    @Test
    fun clearingStatusFilterPreservesOtherFiltersAndResetsPage() {
        val params = UserQueryParams(
            role = "student",
            status = 0,
            keyword = "test",
            page = 2,
            size = 20
        )

        val updated = params.withUserStatusFilter(null)

        assertEquals("student", updated.role)
        assertNull(updated.status)
        assertEquals("test", updated.keyword)
        assertEquals(0, updated.page)
        assertEquals(20, updated.size)
    }
}

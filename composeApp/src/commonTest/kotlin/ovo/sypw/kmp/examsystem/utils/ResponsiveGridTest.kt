package ovo.sypw.kmp.examsystem.utils

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class ResponsiveGridTest {
    @Test
    fun adaptiveColumnsNeverDropsBelowOne() {
        val columns = ResponsiveUtils.Grid.getAdaptiveColumnCount(
            availableWidth = 120.dp,
            minItemWidth = 320.dp,
            maxColumns = 4
        )

        assertEquals(1, columns)
    }

    @Test
    fun adaptiveColumnsUsesAvailableWidth() {
        val columns = ResponsiveUtils.Grid.getAdaptiveColumnCount(
            availableWidth = 760.dp,
            minItemWidth = 320.dp,
            maxColumns = 4
        )

        assertEquals(2, columns)
    }

    @Test
    fun adaptiveColumnsRespectsMaximumColumnCount() {
        val columns = ResponsiveUtils.Grid.getAdaptiveColumnCount(
            availableWidth = 2400.dp,
            minItemWidth = 320.dp,
            maxColumns = 4
        )

        assertEquals(4, columns)
    }
}

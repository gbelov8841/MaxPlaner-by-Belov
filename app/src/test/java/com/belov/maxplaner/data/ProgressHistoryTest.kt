package com.belov.maxplaner.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ProgressHistoryTest {
    @Test fun rollingPeriodIncludesBothEdgesAndExcludesFutureAndMalformedDates() {
        val end = LocalDate.parse("2026-09-11")
        val records = listOf("2026-09-05", "2026-09-11", "2026-09-04", "2026-09-12", "invalid").mapIndexed { i, d -> TaskCompletionRecord("$i", "Task", "Работа", d) }
        val result = periodProgress(records, listOf(Habit(title = "Habit", completedDates = setOf("2026-09-05", "2026-09-11", "2026-09-12"))), mapOf("2026-09-05" to 25, "2026-09-11" to 50, "2026-09-12" to 100), end, 7)
        assertEquals(2, result.taskCount); assertEquals(2, result.habitCount); assertEquals(75, result.focusMinutes)
        assertEquals(mapOf("Работа" to 2), result.categories)
    }
    @Test fun adjacentComparisonWindowsDoNotOverlap() {
        val end = LocalDate.parse("2026-09-11")
        val records = listOf(TaskCompletionRecord("a", "Old", "Работа", "2026-09-04"), TaskCompletionRecord("b", "New", "Дом", "2026-09-05"))
        assertEquals(mapOf("Дом" to 1), periodProgress(records, emptyList(), emptyMap(), end, 7).categories)
        assertEquals(mapOf("Работа" to 1), periodProgress(records, emptyList(), emptyMap(), end.minusDays(7), 7).categories)
    }
}

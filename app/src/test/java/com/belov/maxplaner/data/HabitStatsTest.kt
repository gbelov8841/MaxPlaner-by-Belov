package com.belov.maxplaner.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class HabitStatsTest {
    private val today = LocalDate.of(2026, 9, 10)

    @Test
    fun `completed today counts streak through today`() {
        val dates = setOf(
            "2026-09-10",
            "2026-09-09",
            "2026-09-08"
        )
        assertEquals(3, activeHabitStreak(dates, today))
    }

    @Test
    fun `pending today preserves streak through yesterday`() {
        val dates = setOf(
            "2026-09-09",
            "2026-09-08",
            "2026-09-07"
        )
        assertEquals(3, activeHabitStreak(dates, today))
    }

    @Test
    fun `missing yesterday breaks streak`() {
        val dates = setOf("2026-09-08", "2026-09-07")
        assertEquals(0, activeHabitStreak(dates, today))
    }

    @Test
    fun `empty history has zero streak`() {
        assertEquals(0, activeHabitStreak(emptySet(), today))
    }

    @Test
    fun `best streak finds longest run`() {
        val dates = setOf(
            "2026-09-01",
            "2026-09-02",
            "2026-09-04",
            "2026-09-05",
            "2026-09-06",
            "2026-09-07"
        )
        assertEquals(4, bestHabitStreak(dates))
    }

    @Test
    fun `best streak ignores malformed saved dates`() {
        val dates = setOf("bad-date", "2026-09-08", "2026-09-09")
        assertEquals(2, bestHabitStreak(dates))
    }

    @Test
    fun `completion count only includes requested rolling window`() {
        val dates = setOf(
            "2026-09-10",
            "2026-09-09",
            "2026-09-05",
            "2026-09-03",
            "2026-09-11"
        )
        assertEquals(3, habitCompletionCount(dates, 7, today))
    }

    @Test
    fun `completion rate reports percentage for rolling window`() {
        val dates = setOf(
            "2026-09-10",
            "2026-09-09",
            "2026-09-08",
            "2026-09-07",
            "2026-09-06"
        )
        assertEquals(71, habitCompletionRate(dates, 7, today))
    }

    @Test
    fun `invalid window has zero metrics`() {
        assertEquals(0, habitCompletionCount(setOf("2026-09-10"), 0, today))
        assertEquals(0, habitCompletionRate(setOf("2026-09-10"), 0, today))
    }
}

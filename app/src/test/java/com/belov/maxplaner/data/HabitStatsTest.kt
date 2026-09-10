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
        val dates = setOf(
            "2026-09-08",
            "2026-09-07"
        )

        assertEquals(0, activeHabitStreak(dates, today))
    }

    @Test
    fun `empty history has zero streak`() {
        assertEquals(0, activeHabitStreak(emptySet(), today))
    }
}

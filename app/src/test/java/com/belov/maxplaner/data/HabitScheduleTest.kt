package com.belov.maxplaner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class HabitScheduleTest {
    private val thursday = LocalDate.of(2026, 9, 10)

    @Test
    fun `daily schedule includes every day`() {
        assertTrue(HabitSchedule.Daily.isScheduled(thursday))
        assertTrue(HabitSchedule.Daily.isScheduled(thursday.plusDays(1)))
    }

    @Test
    fun `daily scheduled streak matches existing daily behavior`() {
        val dates = setOf("2026-09-09", "2026-09-08", "2026-09-07")

        assertEquals(
            activeHabitStreak(dates, thursday),
            scheduledHabitStreak(dates, HabitSchedule.Daily, thursday)
        )
    }

    @Test
    fun `weekday schedule only includes selected days`() {
        val schedule = HabitSchedule.weekdays(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)

        assertTrue(schedule.isScheduled(LocalDate.of(2026, 9, 9)))
        assertTrue(schedule.isScheduled(LocalDate.of(2026, 9, 11)))
        assertFalse(schedule.isScheduled(thursday))
    }

    @Test
    fun `streak ignores unscheduled gap days`() {
        val schedule = HabitSchedule.weekdays(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val dates = setOf("2026-09-09", "2026-09-07", "2026-09-04")

        assertEquals(3, scheduledHabitStreak(dates, schedule, thursday))
    }

    @Test
    fun `pending scheduled today preserves previous streak`() {
        val schedule = HabitSchedule.weekdays(DayOfWeek.MONDAY, DayOfWeek.THURSDAY)
        val dates = setOf("2026-09-07", "2026-09-03")

        assertEquals(2, scheduledHabitStreak(dates, schedule, thursday))
    }

    @Test
    fun `missing previous scheduled day breaks streak`() {
        val schedule = HabitSchedule.weekdays(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val dates = setOf("2026-09-07", "2026-09-04")

        assertEquals(0, scheduledHabitStreak(dates, schedule, thursday))
    }

    @Test
    fun `best scheduled streak ignores rest days`() {
        val schedule = HabitSchedule.weekdays(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val dates = setOf(
            "2026-08-31",
            "2026-09-02",
            "2026-09-04",
            "2026-09-09"
        )

        assertEquals(3, bestScheduledHabitStreak(dates, schedule))
    }

    @Test
    fun `best scheduled streak resets after missed scheduled day`() {
        val schedule = HabitSchedule.weekdays(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val dates = setOf(
            "2026-08-31",
            "2026-09-02",
            "2026-09-07",
            "2026-09-09"
        )

        assertEquals(2, bestScheduledHabitStreak(dates, schedule))
    }

    @Test
    fun `completion rate only counts scheduled opportunities`() {
        val schedule = HabitSchedule.weekdays(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val dates = setOf("2026-09-04", "2026-09-07", "2026-09-09")

        assertEquals(3, scheduledOpportunityCount(schedule, 7, thursday))
        assertEquals(100, scheduledCompletionRate(dates, schedule, 7, thursday))
    }

    @Test
    fun `completion rate rounds instead of truncating`() {
        val schedule = HabitSchedule.weekdays(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val dates = setOf("2026-09-07", "2026-09-09")

        assertEquals(67, scheduledCompletionRate(dates, schedule, 7, thursday))
    }

    @Test
    fun `schedule with no weekdays has no opportunities`() {
        val schedule = HabitSchedule(type = HabitScheduleType.WEEKDAYS)

        assertEquals(0, scheduledOpportunityCount(schedule, 7, thursday))
        assertEquals(0, scheduledCompletionRate(setOf("2026-09-10"), schedule, 7, thursday))
        assertEquals(0, bestScheduledHabitStreak(setOf("2026-09-10"), schedule))
    }
}

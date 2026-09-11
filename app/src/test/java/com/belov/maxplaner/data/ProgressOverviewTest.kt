package com.belov.maxplaner.data

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.DayOfWeek

class ProgressOverviewTest {
    private val today = LocalDate.of(2026, 9, 11)
    @Test fun emptyPlanHasNoArtificialPercentageDenominator() {
        val s = progressOverview(emptyList(), emptyList(), emptyList(), today)
        assertEquals(0, s.today.total)
        assertEquals(0f, s.today.fraction, 0f)
        assertEquals(0, s.weeklyRhythm.total)
    }
    @Test fun todayDoesNotMixYesterdayOrFutureTasks() {
        val tasks = listOf(PlannerTask(title = "Today", dueDate = today.toString(), completed = true),
            PlannerTask(title = "Yesterday", dueDate = today.minusDays(1).toString()), PlannerTask(title = "Tomorrow", dueDate = today.plusDays(1).toString()))
        val s = progressOverview(tasks, emptyList(), emptyList(), today)
        assertEquals(CompletionSummary(1, 1), s.today)
    }
    @Test fun restDayHabitsAreNotMissedCompletions() {
        val h = Habit(title = "Rest", schedule = HabitSchedule.weekdays(DayOfWeek.MONDAY))
        val s = progressOverview(emptyList(), listOf(h), emptyList(), today)
        assertEquals(0, s.today.total)
        assertEquals(1, s.weeklyRhythm.total)
    }
    @Test fun measurementsAndLimitsAreSeparateFromCheckActions() {
        val base = Tracker(title = "Check", category = "", type = TrackerType.CHECK, period = ActionPeriod(today), values = mapOf(today.toString() to 1.0))
        val limit = base.copy(id = "limit", type = TrackerType.COUNTER, target = 0.0, direction = GoalDirection.AT_MOST)
        val absent = base.copy(id = "weight", type = TrackerType.NUMBER, values = emptyMap())
        val s = progressOverview(emptyList(), emptyList(), listOf(base, limit, absent), today)
        assertEquals(CompletionSummary(1, 1), s.today)
        assertEquals(1, s.measurementsRecorded)
        assertEquals(2, s.measurementsPlanned)
    }
    @Test fun weeklyRhythmWeightsScheduledOpportunitiesInsteadOfAveragingPercentages() {
        val daily = Habit(title = "Daily", completedDates = (0L..6L).map { today.minusDays(it).toString() }.toSet())
        val weekly = Habit(title = "Weekly", schedule = HabitSchedule.weekdays(DayOfWeek.MONDAY))
        val s = progressOverview(emptyList(), listOf(daily, weekly), emptyList(), today)
        assertEquals(CompletionSummary(7, 8), s.weeklyRhythm)
        assertEquals(.875f, s.weeklyRhythm.fraction, 0f)
    }
    @Test fun actionStartingTodayHasOnlyOneHistoricalOpportunity() {
        val a = Tracker(title = "New", category = "", type = TrackerType.STREAK, period = ActionPeriod(today, null))
        val s = progressOverview(emptyList(), emptyList(), listOf(a), today)
        assertEquals(CompletionSummary(0, 1), s.weeklyRhythm)
    }
}

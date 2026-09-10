package com.belov.maxplaner.data

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.DayOfWeek

class TrackerTest {
    private val monday = LocalDate.of(2026, 9, 7)
    private fun tracker(type: TrackerType = TrackerType.COUNTER) = Tracker(title = "Вода", category = "Здоровье", type = type, target = 8.0,
        period = ActionPeriod(monday, null))

    @Test fun upperLimitRequiresExplicitEntryIncludingZero() {
        val t = tracker().copy(target = 5.0, direction = GoalDirection.AT_MOST)
        assertFalse(t.isComplete(monday))
        assertTrue(t.copy(values = mapOf(monday.toString() to 0.0)).isComplete(monday))
        assertFalse(t.copy(values = mapOf(monday.toString() to 6.0)).isComplete(monday))
    }
    @Test fun weeklyPeriodHasSevenInclusiveDays() {
        val p = ActionPeriod(monday, monday.plusDays(6))
        assertFalse(p.includes(monday.minusDays(1)))
        assertTrue(p.includes(monday.plusDays(6)))
        assertFalse(p.includes(monday.plusDays(7)))
    }
    @Test fun selectedWeekdaysHonorStartAndEnd() {
        val p = ActionPeriod(monday, monday.plusDays(6), setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY))
        assertTrue(p.includes(monday))
        assertFalse(p.includes(monday.plusDays(1)))
        assertTrue(p.includes(monday.plusDays(4)))
        assertFalse(p.includes(monday.plusDays(7)))
    }
    @Test fun reductionStopsAtFinalGoalAndChangesOnlyWeekly() {
        val t = tracker(TrackerType.REDUCTION_GOAL).copy(target = 0.0, initialTarget = 20.0, weeklyStep = 5.0)
        assertEquals(20.0, t.targetOn(monday.plusDays(6))!!, 0.0)
        assertEquals(15.0, t.targetOn(monday.plusDays(7))!!, 0.0)
        assertEquals(0.0, t.targetOn(monday.plusDays(70))!!, 0.0)
    }
    @Test fun increaseStopsAtGoal() {
        val t = tracker(TrackerType.INCREASE_GOAL).copy(target = 10000.0, initialTarget = 4000.0, weeklyStep = 2000.0)
        assertEquals(6000.0, t.targetOn(monday.plusDays(7))!!, 0.0)
        assertEquals(10000.0, t.targetOn(monday.plusDays(70))!!, 0.0)
    }
    @Test fun validationRejectsNonfiniteNegativeAndInvalidRatings() {
        assertFalse(tracker().accepts(Double.NaN))
        assertFalse(tracker().accepts(Double.POSITIVE_INFINITY))
        assertFalse(tracker().accepts(-1.0))
        assertFalse(tracker().accepts(1.5))
        assertTrue(tracker(TrackerType.NUMBER).accepts(78.5))
        assertFalse(tracker(TrackerType.SCALE).accepts(0.0))
        assertFalse(tracker(TrackerType.SCALE).accepts(2.5))
        assertTrue(tracker(TrackerType.SCALE).accepts(5.0))
    }
    @Test fun streakSkipsRestDaysButBreaksOnMissedScheduledDay() {
        val t = tracker(TrackerType.STREAK).copy(period = ActionPeriod(monday, null, setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)),
            values = mapOf(monday.toString() to 1.0, monday.plusDays(2).toString() to 1.0))
        assertEquals(2, t.streak(monday.plusDays(4)))
        assertEquals(2, t.streak(monday.plusDays(7)))
        assertEquals(0, t.streak(monday.plusDays(8)))
    }
    @Test fun recordingValueIsNotAnIncreaseGoal() {
        val t = tracker(TrackerType.NUMBER).copy(direction = GoalDirection.RECORD, target = null, values = mapOf(monday.toString() to 78.5))
        assertTrue(t.isComplete(monday))
        assertFalse(t.isComplete(monday.plusDays(1)))
    }
    @Test fun catalogHasElevenCategoriesAndUniqueResolvablePopularItems() {
        assertEquals(11, ActionCategory.entries.size)
        assertTrue(ActionCategory.entries.all { c -> ActionCatalog.templates.any { it.category == c } })
        assertEquals(ActionCatalog.templates.size, ActionCatalog.templates.map { it.title }.toSet().size)
        assertEquals(16, ActionCatalog.popular.size)
        assertTrue(ActionCatalog.popular.all { it in ActionCatalog.templates })
        assertTrue(TrackerType.entries.all { t -> ActionCatalog.templates.any { it.type == t } })
    }
}

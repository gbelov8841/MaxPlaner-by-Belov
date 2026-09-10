package com.belov.maxplaner.data

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.DayOfWeek

class DayTimelineTest {
    private val date = LocalDate.of(2026, 9, 10)
    private fun item(id: String, start: Int?, duration: Int) = AgendaItem(id, AgendaKind.TASK, id, date, start, duration, false)
    @Test fun exactMinutePositionAndLength() {
        val segment = segmentOn(item("reading", 630, 45), date)!!
        assertEquals(630, segment.start)
        assertEquals(675, segment.end)
    }
    @Test fun midnightClipsAndShowsContinuationOnFollowingDay() {
        val task = item("night", 1410, 90)
        assertEquals(1440, segmentOn(task, date)!!.end)
        val next = segmentOn(task, date.plusDays(1))!!
        assertEquals(0, next.start)
        assertEquals(60, next.end)
        assertEquals(date, next.item.occurrenceDate)
        assertNull(segmentOn(task, date.plusDays(2)))
    }
    @Test fun endingAtMidnightDoesNotCreateZeroLengthTail() {
        assertNull(segmentOn(item("late", 1380, 60), date.plusDays(1)))
    }
    @Test fun untimedItemsRemainOutsideTimeline() {
        assertNull(segmentOn(item("legacy", null, 30), date))
    }
    @Test fun touchingIntervalsUseOneLane() {
        val placements = placeSegments(listOf(segmentOn(item("a", 600, 60), date)!!, segmentOn(item("b", 660, 60), date)!!))
        assertTrue(placements.all { it.lane == 0 && it.laneCount == 1 })
    }
    @Test fun overlappingGroupsReuseLanesWithoutOcclusion() {
        val segments = listOf(item("a", 600, 120), item("b", 630, 30), item("c", 660, 30), item("d", 800, 60)).mapNotNull { segmentOn(it, date) }
        val placements = placeSegments(segments)
        assertEquals(2, placements.first().laneCount)
        assertEquals(placements[1].lane, placements[2].lane)
        assertEquals(1, placements.last().laneCount)
        placements.forEach { a -> placements.filter { it != a && it.lane == a.lane }.forEach { b ->
            assertFalse(a.segment.start < b.segment.end && b.segment.start < a.segment.end)
        } }
    }
    @Test fun recurrenceMatchesDateAndTimingSurvivesCopies() {
        val habit = Habit(title = "Yoga", schedule = HabitSchedule.weekdays(DayOfWeek.THURSDAY), startMinutes = 480, durationMinutes = 45)
        val tracker = Tracker(title = "Read", category = "Study", type = TrackerType.DURATION,
            period = ActionPeriod(date, date.plusDays(6)), startMinutes = 1200, durationMinutes = 20)
        val task = PlannerTask(title = "Meeting", dueDate = date.toString(), startMinutes = 600, durationMinutes = 60)
        val entries = agendaItems(listOf(task), listOf(habit), listOf(tracker), date)
        assertEquals(3, entries.size)
        assertEquals(1, agendaItems(listOf(task), listOf(habit), listOf(tracker), date.plusDays(1)).size)
        assertEquals(1200, tracker.copy(values = mapOf(date.toString() to 10.0)).startMinutes)
        assertEquals(480, habit.copy(completedDates = setOf(date.toString())).startMinutes)
        assertEquals(600, task.copy(completed = true).startMinutes)
    }
    @Test fun oldModelsDefaultToUntimed() {
        assertNull(Habit(title = "Old").startMinutes)
        assertNull(Tracker(title = "Old", category = "", type = TrackerType.CHECK, period = ActionPeriod(date)).startMinutes)
    }
}

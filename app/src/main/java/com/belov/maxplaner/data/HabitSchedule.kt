package com.belov.maxplaner.data

import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Defines when a habit is expected to be completed.
 *
 * DAILY preserves the current MaxPlaner behavior. WEEKDAYS allows precise
 * selected-day schedules without forcing users to maintain a streak on days
 * when the habit is not planned.
 */
data class HabitSchedule(
    val type: HabitScheduleType = HabitScheduleType.DAILY,
    val weekdays: Set<DayOfWeek> = emptySet()
) {
    fun isScheduled(date: LocalDate): Boolean = when (type) {
        HabitScheduleType.DAILY -> true
        HabitScheduleType.WEEKDAYS -> date.dayOfWeek in weekdays
    }

    companion object {
        val Daily = HabitSchedule()

        fun weekdays(vararg days: DayOfWeek): HabitSchedule = HabitSchedule(
            type = HabitScheduleType.WEEKDAYS,
            weekdays = days.toSet()
        )
    }
}

enum class HabitScheduleType {
    DAILY,
    WEEKDAYS
}

/**
 * Active streak measured only across days on which the habit was scheduled.
 * A scheduled habit that is still pending today keeps yesterday's streak alive.
 */
fun scheduledHabitStreak(
    completedDates: Set<String>,
    schedule: HabitSchedule,
    today: LocalDate = LocalDate.now()
): Int {
    if (completedDates.isEmpty()) return 0

    var cursor = if (schedule.isScheduled(today) && today.toString() in completedDates) {
        today
    } else {
        previousScheduledDay(schedule, today.minusDays(1)) ?: return 0
    }

    var streak = 0
    while (true) {
        if (cursor.toString() !in completedDates) break
        streak++
        cursor = previousScheduledDay(schedule, cursor.minusDays(1)) ?: break
    }
    return streak
}

/** Counts scheduled opportunities in an inclusive rolling window ending today. */
fun scheduledOpportunityCount(
    schedule: HabitSchedule,
    days: Int,
    today: LocalDate = LocalDate.now()
): Int {
    if (days <= 0) return 0
    val start = today.minusDays((days - 1).toLong())
    var cursor = start
    var count = 0
    while (!cursor.isAfter(today)) {
        if (schedule.isScheduled(cursor)) count++
        cursor = cursor.plusDays(1)
    }
    return count
}

/** Completion rate considering only days when the habit was actually scheduled. */
fun scheduledCompletionRate(
    completedDates: Set<String>,
    schedule: HabitSchedule,
    days: Int,
    today: LocalDate = LocalDate.now()
): Int {
    val opportunities = scheduledOpportunityCount(schedule, days, today)
    if (opportunities == 0) return 0

    val start = today.minusDays((days - 1).toLong())
    val completedScheduled = completedDates.count { value ->
        val date = runCatching { LocalDate.parse(value) }.getOrNull() ?: return@count false
        !date.isBefore(start) && !date.isAfter(today) && schedule.isScheduled(date)
    }
    return ((completedScheduled * 100.0) / opportunities).roundToInt().coerceIn(0, 100)
}

private fun previousScheduledDay(schedule: HabitSchedule, start: LocalDate): LocalDate? {
    var cursor = start
    repeat(14) {
        if (schedule.isScheduled(cursor)) return cursor
        cursor = cursor.minusDays(1)
    }
    return null
}

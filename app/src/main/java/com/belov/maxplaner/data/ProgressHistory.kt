package com.belov.maxplaner.data

import java.time.LocalDate

data class TaskCompletionRecord(val taskId: String, val title: String, val category: String, val date: String)
data class PeriodProgress(val taskCount: Int, val habitCount: Int, val focusMinutes: Int, val categories: Map<String, Int>)

fun periodProgress(records: List<TaskCompletionRecord>, habits: List<Habit>, focusDays: Map<String, Int>, end: LocalDate, days: Int): PeriodProgress {
    require(days > 0)
    val start = end.minusDays(days.toLong() - 1)
    fun within(value: String): Boolean {
        val date = runCatching { LocalDate.parse(value) }.getOrNull() ?: return false
        return date >= start && date <= end
    }
    val done = records.filter { within(it.date) }
    return PeriodProgress(done.size, habits.sumOf { habit -> habit.completedDates.count(::within) },
        focusDays.filterKeys(::within).values.sum(), done.groupingBy { it.category }.eachCount())
}

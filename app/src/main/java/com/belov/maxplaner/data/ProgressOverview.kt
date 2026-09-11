package com.belov.maxplaner.data

import java.time.LocalDate

/** Counts only comparable planned completions; measurements and limits stay separate. */
data class CompletionSummary(val done: Int, val total: Int) {
    val fraction: Float get() = if (total == 0) 0f else done.toFloat() / total
    operator fun plus(other: CompletionSummary) = CompletionSummary(done + other.done, total + other.total)
}

data class ProgressOverview(
    val tasks: CompletionSummary,
    val habits: CompletionSummary,
    val actions: CompletionSummary,
    val weeklyRhythm: CompletionSummary,
    val measurementsRecorded: Int,
    val measurementsPlanned: Int
) {
    val today: CompletionSummary get() = tasks + habits + actions
}

fun progressOverview(tasks: List<PlannerTask>, habits: List<Habit>, trackers: List<Tracker>, today: LocalDate): ProgressOverview {
    val dayKey = today.toString()
    val dailyTasks = tasks.filter { it.dueDate == dayKey }
    val dailyHabits = habits.filter { it.schedule.isScheduled(today) }
    val actions = trackers.filter { it.type == TrackerType.CHECK || it.type == TrackerType.STREAK }
    val dailyActions = actions.filter { it.period.includes(today) }
    val measurements = trackers.filter { it.type != TrackerType.CHECK && it.type != TrackerType.STREAK && it.period.includes(today) }
    var weekly = CompletionSummary(0, 0)
    (0L..6L).forEach { ago ->
        val date = today.minusDays(ago)
        val scheduledHabits = habits.filter { it.schedule.isScheduled(date) }
        val scheduledActions = actions.filter { it.period.includes(date) }
        weekly += CompletionSummary(
            scheduledHabits.count { date.toString() in it.completedDates } + scheduledActions.count { it.isComplete(date) },
            scheduledHabits.size + scheduledActions.size
        )
    }
    return ProgressOverview(
        CompletionSummary(dailyTasks.count { it.completed }, dailyTasks.size),
        CompletionSummary(dailyHabits.count { dayKey in it.completedDates }, dailyHabits.size),
        CompletionSummary(dailyActions.count { it.isComplete(today) }, dailyActions.size),
        weekly, measurements.count { dayKey in it.values }, measurements.size
    )
}

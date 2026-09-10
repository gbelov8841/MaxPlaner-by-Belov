package com.belov.maxplaner.data

import java.time.LocalDate

/**
 * Returns the currently active streak for a habit.
 *
 * A streak that was alive yesterday remains visible during the current day
 * until the user has had a chance to complete today's habit. This avoids
 * showing a misleading zero every morning before today's check-in.
 */
fun activeHabitStreak(
    completedDates: Set<String>,
    today: LocalDate = LocalDate.now()
): Int {
    if (completedDates.isEmpty()) return 0

    var date = if (completedDates.contains(today.toString())) {
        today
    } else {
        today.minusDays(1)
    }

    var streak = 0
    while (completedDates.contains(date.toString())) {
        streak++
        date = date.minusDays(1)
    }
    return streak
}

/** Returns the longest uninterrupted daily streak in the stored history. */
fun bestHabitStreak(completedDates: Set<String>): Int {
    val dates = completedDates.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
        .distinct()
        .sorted()
    if (dates.isEmpty()) return 0

    var best = 1
    var current = 1
    for (index in 1 until dates.size) {
        if (dates[index] == dates[index - 1].plusDays(1)) {
            current++
            if (current > best) best = current
        } else {
            current = 1
        }
    }
    return best
}

/** Counts completions inside the inclusive rolling window ending today. */
fun habitCompletionCount(
    completedDates: Set<String>,
    days: Int,
    today: LocalDate = LocalDate.now()
): Int {
    if (days <= 0 || completedDates.isEmpty()) return 0
    val start = today.minusDays((days - 1).toLong())
    return completedDates.count { value ->
        val date = runCatching { LocalDate.parse(value) }.getOrNull() ?: return@count false
        !date.isBefore(start) && !date.isAfter(today)
    }
}

/** Completion percentage for the inclusive rolling window, from 0 to 100. */
fun habitCompletionRate(
    completedDates: Set<String>,
    days: Int,
    today: LocalDate = LocalDate.now()
): Int {
    if (days <= 0) return 0
    return ((habitCompletionCount(completedDates, days, today) * 100.0) / days)
        .toInt()
        .coerceIn(0, 100)
}

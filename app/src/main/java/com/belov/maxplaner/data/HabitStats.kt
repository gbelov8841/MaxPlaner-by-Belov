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

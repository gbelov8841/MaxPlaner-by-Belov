package com.belov.maxplaner.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

/** Stable storage identifiers. UI uses human-readable action names. */
enum class TrackerType { CHECK, COUNTER, NUMBER, DURATION, STREAK, SCALE, REDUCTION_GOAL, INCREASE_GOAL }
enum class GoalDirection { AT_LEAST, AT_MOST, RECORD }

data class ActionPeriod(
    val start: LocalDate,
    val end: LocalDate? = start,
    val weekdays: Set<DayOfWeek> = DayOfWeek.entries.toSet()
) {
    fun includes(date: LocalDate) = !date.isBefore(start) && (end == null || !date.isAfter(end)) && date.dayOfWeek in weekdays
}

data class Tracker(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val type: TrackerType,
    val unit: String = "",
    val target: Double? = null,
    val direction: GoalDirection = GoalDirection.AT_LEAST,
    val period: ActionPeriod,
    val values: Map<String, Double> = emptyMap(),
    val initialTarget: Double? = null,
    val weeklyStep: Double = 0.0,
    val startMinutes: Int? = null,
    val durationMinutes: Int = 30
) {
    fun targetOn(date: LocalDate): Double? {
        val goal = target ?: return null
        val initial = initialTarget ?: return goal
        val weeks = (ChronoUnit.DAYS.between(period.start, date).coerceAtLeast(0) / 7).toDouble()
        return when (type) {
            TrackerType.REDUCTION_GOAL -> maxOf(goal, initial - weeklyStep * weeks)
            TrackerType.INCREASE_GOAL -> minOf(goal, initial + weeklyStep * weeks)
            else -> goal
        }
    }
    fun isComplete(date: LocalDate): Boolean {
        if (!period.includes(date)) return false
        // Missing data is never a successful zero, especially for upper limits.
        val value = values[date.toString()] ?: return false
        val goal = targetOn(date)
        return when {
            type == TrackerType.CHECK || type == TrackerType.STREAK -> value >= 1
            type == TrackerType.SCALE || direction == GoalDirection.RECORD || goal == null -> true
            direction == GoalDirection.AT_MOST -> value <= goal
            else -> value >= goal
        }
    }
    fun accepts(value: Double): Boolean = value.isFinite() && value >= 0 && when (type) {
        TrackerType.SCALE -> value in 1.0..5.0 && value % 1.0 == 0.0
        TrackerType.CHECK, TrackerType.STREAK -> value == 0.0 || value == 1.0
        TrackerType.COUNTER, TrackerType.REDUCTION_GOAL -> value % 1.0 == 0.0
        else -> true
    }
    fun streak(today: LocalDate): Int {
        var date = today
        if (!isComplete(date)) date = date.minusDays(1)
        var count = 0
        while (!date.isBefore(period.start)) {
            if (period.includes(date)) {
                if (!isComplete(date)) break
                count++
            }
            date = date.minusDays(1)
        }
        return count
    }
}

fun displayNumber(value: Double): String = if (value % 1.0 == 0.0) value.toLong().toString() else value.toString().replace('.', ',')

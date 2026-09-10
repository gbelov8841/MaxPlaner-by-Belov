package com.belov.maxplaner.data

import java.time.LocalDate

enum class AgendaKind { TASK, HABIT, TRACKER }

data class AgendaItem(
    val id: String, val kind: AgendaKind, val title: String,
    val occurrenceDate: LocalDate, val startMinutes: Int?, val durationMinutes: Int,
    val completed: Boolean
) {
    val key: String get() = "${kind.name}-$id-$occurrenceDate"
}

data class DaySegment(val item: AgendaItem, val start: Int, val end: Int)
data class PlacedSegment(val segment: DaySegment, val lane: Int, val laneCount: Int)

/** Clip to this day, including the tail of an event that started yesterday. */
fun segmentOn(item: AgendaItem, day: LocalDate): DaySegment? {
    val start = item.startMinutes ?: return null
    val offset = java.time.temporal.ChronoUnit.DAYS.between(day, item.occurrenceDate).toInt() * 1440
    val from = start + offset
    val to = from + item.durationMinutes
    if (to <= 0 || from >= 1440) return null
    return DaySegment(item, maxOf(0, from), minOf(1440, to))
}

/** Half-open intervals: a task ending at 11:00 does not clash with one starting at 11:00. */
fun placeSegments(segments: List<DaySegment>): List<PlacedSegment> {
    val sorted = segments.sortedWith(compareBy({ it.start }, { it.end }, { it.item.key }))
    val result = mutableListOf<PlacedSegment>()
    var cluster = mutableListOf<DaySegment>()
    var clusterEnd = -1
    fun flush() {
        val laneEnds = mutableListOf<Int>()
        val allocated = cluster.map { segment ->
            var lane = laneEnds.indexOfFirst { it <= segment.start }
            if (lane < 0) { lane = laneEnds.size; laneEnds.add(segment.end) } else laneEnds[lane] = segment.end
            segment to lane
        }
        allocated.forEach { (segment, lane) -> result += PlacedSegment(segment, lane, laneEnds.size) }
        cluster = mutableListOf()
    }
    sorted.forEach { segment ->
        if (cluster.isNotEmpty() && segment.start >= clusterEnd) flush()
        if (cluster.isEmpty()) clusterEnd = segment.end else clusterEnd = maxOf(clusterEnd, segment.end)
        cluster += segment
    }
    if (cluster.isNotEmpty()) flush()
    return result
}

fun agendaItems(tasks: List<PlannerTask>, habits: List<Habit>, trackers: List<Tracker>, date: LocalDate): List<AgendaItem> = buildList {
    tasks.filter { it.dueDate == date.toString() }.forEach {
        add(AgendaItem(it.id, AgendaKind.TASK, it.title, date, it.startMinutes, it.durationMinutes, it.completed))
    }
    habits.filter { it.schedule.isScheduled(date) }.forEach {
        add(AgendaItem(it.id, AgendaKind.HABIT, it.title, date, it.startMinutes, it.durationMinutes, date.toString() in it.completedDates))
    }
    trackers.filter { it.period.includes(date) }.forEach {
        add(AgendaItem(it.id, AgendaKind.TRACKER, it.title, date, it.startMinutes, it.durationMinutes, it.isComplete(date)))
    }
}

fun timeOfDay(minutes: Int): String = "%02d:%02d".format((minutes / 60) % 24, minutes % 60)
fun timeRange(start: Int, duration: Int): String = timeOfDay(start) + "–" + timeOfDay(start + duration) + if (start + duration >= 1440) " (+1 день)" else ""

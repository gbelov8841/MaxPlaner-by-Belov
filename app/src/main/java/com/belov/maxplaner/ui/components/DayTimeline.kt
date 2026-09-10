package com.belov.maxplaner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.belov.maxplaner.data.*
import com.belov.maxplaner.ui.theme.LocalStyleTokens
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun DayTimeline(store: PlannerStore, date: LocalDate, onOpen: (AgendaItem) -> Unit, onAdd: (Int) -> Unit) {
    val entries = agendaItems(store.tasks, store.habits, store.trackers, date)
    val previous = agendaItems(store.tasks, store.habits, store.trackers, date.minusDays(1))
    val placed = placeSegments((entries + previous).mapNotNull { segmentOn(it, date) })
    val firstHour = minOf(6, placed.minOfOrNull { it.segment.start / 60 } ?: 6)
    val lastHour = 24
    val pixelsPerMinute = 3.2f // Fifteen minutes remain a 48dp target, without distorting time.
    val totalHeight = ((lastHour - firstHour) * 60 * pixelsPerMinute).dp
    val tokens = LocalStyleTokens.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val untimed = entries.filter { it.startMinutes == null }
        val timedCount = entries.size - untimed.size
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = tokens.cardShape,
            color = Color(0xFF102437),
            tonalElevation = 1.dp
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Распорядок дня", style = MaterialTheme.typography.titleLarge, color = Color(0xFFE8C56A))
                Text(
                    if (entries.isEmpty()) "День пока свободен" else "$timedCount по времени · ${untimed.size} без времени",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA8BAC8)
                )
            }
        }
        if (untimed.isNotEmpty()) {
            Text("Без времени", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            untimed.forEach { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { onOpen(item) },
                    shape = tokens.compactShape,
                    color = Color(0xFF101C29)
                ) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(item.title, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("Назначить время", style = MaterialTheme.typography.labelMedium, color = Color(0xFFE8C56A))
                    }
                }
            }
        }
        Text("Нажми на свободный час, чтобы добавить дело. Нажми на блок, чтобы изменить его.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8FA3B3))
        if (placed.any { it.laneCount > 1 }) Text("Пересекающиеся дела показаны рядом.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8FA3B3))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val maxLanes = placed.maxOfOrNull { it.laneCount } ?: 1
            val canvasWidth = maxOf(maxWidth, (54 + maxLanes * 140).dp)
            Box(Modifier.horizontalScroll(rememberScrollState())) {
                Box(Modifier.width(canvasWidth).height(totalHeight + 24.dp)) {
                    for (hour in firstHour..lastHour) {
                        Row(Modifier.offset(y = ((hour - firstHour) * 60 * pixelsPerMinute).dp).fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Text(if (hour == 24) "24:00" else timeOfDay(hour * 60), style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF7890A3), modifier = Modifier.width(54.dp))
                            Box(Modifier.weight(1f).height(if (hour == lastHour) 1.dp else (60 * pixelsPerMinute).dp)
                                .clickable(enabled = hour < 24) { onAdd(hour * 60) }) {
                                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF203142)))
                            }
                        }
                    }
                    placed.forEach { placedItem ->
                        val segment = placedItem.segment
                        val item = segment.item
                        val laneWidth = (canvasWidth - 54.dp) / placedItem.laneCount
                        val segmentHeight = ((segment.end - segment.start) * pixelsPerMinute).dp
                        Box(Modifier.offset(x = 54.dp + laneWidth * placedItem.lane, y = ((segment.start - firstHour * 60) * pixelsPerMinute).dp)
                            .width(laneWidth).height(segmentHeight).padding(end = 4.dp, bottom = 2.dp)
                            .clip(tokens.compactShape)
                            .background(if (item.completed) Color(0xFF18242F) else Color(0xFF183149))
                            .clickable { onOpen(item) }) {
                            Column(Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 3.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text((if (item.completed) "✓ " else agendaKindIcon(item.kind) + " ") + item.title, maxLines = if (segmentHeight >= 90.dp) 3 else 1,
                                    overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge)
                                Text(timeRange(item.startMinutes!!, item.durationMinutes), style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (item.occurrenceDate < date && segmentHeight >= 90.dp) Text("Началось вчера", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    // Read the observable clock so the line follows time while the app is visible.
                    val clock = store.focusClock
                    val now = java.time.Instant.ofEpochMilli(clock.wallMillis).atZone(java.time.ZoneId.systemDefault()).toLocalTime()
                    val nowMinute = now.hour * 60 + now.minute
                    if (date == store.today && nowMinute in firstHour * 60 until lastHour * 60) {
                        Row(Modifier.offset(y = ((nowMinute - firstHour * 60) * pixelsPerMinute).dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(timeOfDay(nowMinute), modifier = Modifier.width(54.dp), color = Color(0xFFE8C56A), style = MaterialTheme.typography.labelSmall)
                            Box(Modifier.weight(1f).height(2.dp).background(Color(0xFFE8C56A)))
                        }
                    }
                }
            }
        }
    }
}

/** Opening a calendar entry preserves its occurrence date, including overnight continuations. */
@Composable
fun AgendaEntryDialog(store: PlannerStore, item: AgendaItem, onDismiss: () -> Unit) {
    var editTime by rememberSaveable(item.key) { mutableStateOf(false) }
    val current = agendaItems(store.tasks, store.habits, store.trackers, item.occurrenceDate).firstOrNull { it.kind == item.kind && it.id == item.id }
    if (current == null) { LaunchedEffect(Unit) { onDismiss() }; return }
    val tracker = if (item.kind == AgendaKind.TRACKER) store.trackers.firstOrNull { it.id == item.id } else null
    val canToggle = tracker == null || tracker.type in listOf(TrackerType.CHECK, TrackerType.STREAK)
    fun toggle() {
        when (item.kind) {
            AgendaKind.TASK -> store.toggleTask(item.id)
            AgendaKind.HABIT -> store.toggleHabitOn(item.id, item.occurrenceDate)
            AgendaKind.TRACKER -> store.setTrackerValue(item.id, item.occurrenceDate, if (current.completed) null else 1.0)
        }
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(item.title) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(item.occurrenceDate.toString())
            if (tracker != null) TrackerCard(store, tracker, item.occurrenceDate)
            else {
                Text(current.startMinutes?.let { timeRange(it, current.durationMinutes) } ?: "Время не задано")
                TextButton(onClick = { editTime = true }) { Text("Изменить время") }
            }
            if (canToggle) Button(onClick = { toggle() }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(if (current.completed) "Вернуть в план" else "Отметить выполненным")
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } })
    if (editTime) EditTimeSlotDialog(item.title, current.startMinutes, current.durationMinutes, { editTime = false }) { start, duration ->
        when (item.kind) {
            AgendaKind.TASK -> store.tasks.firstOrNull { it.id == item.id }?.let { store.updateTask(it.copy(startMinutes = start, durationMinutes = duration)) }
            AgendaKind.HABIT -> store.updateHabitTime(item.id, start, duration)
            AgendaKind.TRACKER -> store.updateTrackerTime(item.id, start, duration)
        }
    }
}

private fun agendaKindIcon(kind: AgendaKind): String = when (kind) {
    AgendaKind.TASK -> "✓"
    AgendaKind.HABIT -> "↻"
    AgendaKind.TRACKER -> "◉"
}

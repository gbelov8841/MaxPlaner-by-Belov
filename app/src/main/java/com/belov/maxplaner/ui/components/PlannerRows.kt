package com.belov.maxplaner.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.runtime.saveable.rememberSaveable
import com.belov.maxplaner.ui.theme.LocalStyleTokens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.*
import com.belov.maxplaner.ui.theme.*
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PlannerWeekStrip(selected: LocalDate, today: LocalDate, onSelect: (LocalDate) -> Unit,
    count: (LocalDate) -> Int = { 0 }) {
    val start = selected.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val dates = (0L..6L).map(start::plusDays)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // At narrow display zoom, wrap instead of clipping days or shrinking touch targets.
        val rows = if (maxWidth < 336.dp) dates.chunked(4) else listOf(dates)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            rows.forEach { days ->
                Row(Modifier.fillMaxWidth()) {
                    days.forEach { date ->
                        val busy = count(date)
                        Surface(onClick = { onSelect(date) }, modifier = Modifier.weight(1f).heightIn(min = 56.dp).semantics {
                            this.selected = date == selected
                            contentDescription = date.format(DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale("ru"))) + if (date == today) ", сегодня" else ""
                            stateDescription = if (busy == 0) "Нет дел" else "Дел: $busy"
                        }, shape = LocalStyleTokens.current.compactShape,
                            color = if (date == selected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface) {
                            Column(Modifier.padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale("ru")), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (date == selected) FontWeight.SemiBold else FontWeight.Normal)
                                Row(Modifier.height(6.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    repeat(minOf(busy, 3)) { Box(Modifier.size(3.dp).background(MaterialTheme.colorScheme.secondary, androidx.compose.foundation.shape.CircleShape)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlannerTaskRow(store: PlannerStore, task: PlannerTask, onOpen: () -> Unit, onLongPress: (() -> Unit)? = null) {
    var actions by rememberSaveable(task.id) { mutableStateOf(false) }
    val toggle = LocalTaskCompletion.current
    if (actions) TaskQuickActions(store, task, { actions = false }, onOpen)
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).combinedClickable(
        onClickLabel = "Открыть дело", onLongClickLabel = "Действия с делом", onClick = onOpen, onLongClick = { onLongPress?.invoke() ?: run { actions = true } }
    ).padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        CompletionButton(task.completed, task.title) { toggle(task.id) }
        Column(Modifier.weight(1f).padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(task.title, style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (task.completed) TextDecoration.LineThrough else null)
            Text(task.startMinutes?.let { timeRange(it, task.durationMinutes) } ?: categoryLabel(task.category),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Rounded.ChevronRight, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun InlineAdd(label: String = "Добавить задачу", onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Icon(Icons.Rounded.Add, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(label)
    }
}

@Composable
fun PanelHeading(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).padding(vertical = 10.dp))
        if (onAction != null && action != null) TextButton(onClick = onAction) { Text(action, style = MaterialTheme.typography.labelMedium) }
    }
}

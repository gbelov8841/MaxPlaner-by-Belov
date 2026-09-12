package com.belov.maxplaner.ui.components

import androidx.compose.foundation.combinedClickable
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
fun PlannerWeekStrip(selected: LocalDate, today: LocalDate, onSelect: (LocalDate) -> Unit) {
    val start = selected.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cellWidth = maxOf(48.dp, (maxWidth - 12.dp) / 7)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            items((0L..6L).map(start::plusDays), key = { it.toString() }) { date ->
                PlannerSurface(modifier = Modifier.width(cellWidth).heightIn(min = 64.dp).semantics {
                    this.selected = date == selected
                    contentDescription = date.format(DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale("ru"))) + if (date == today) ", сегодня" else ""
                }, selected = date == selected, onClick = { onSelect(date) },
                    color = if (date == selected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent) {
                    Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale("ru")).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (date == selected) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
fun PlannerTaskRow(store: PlannerStore, task: PlannerTask, onOpen: () -> Unit, onLongPress: () -> Unit = onOpen) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).combinedClickable(
        onClickLabel = "Открыть дело", onLongClickLabel = "Действия с делом", onClick = onOpen, onLongClick = onLongPress
    ).padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        CompletionButton(task.completed, task.title) { store.toggleTask(task.id) }
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

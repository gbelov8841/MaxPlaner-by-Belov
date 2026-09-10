package com.belov.maxplaner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.data.Habit
import com.belov.maxplaner.data.PlannerStore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TasksScreen(store: PlannerStore) {
    var showAdd by remember { mutableStateOf(false) }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Header("Задачи", "Все дела в одном месте")
            Button(onClick = { showAdd = true }, modifier = Modifier.padding(top = 12.dp)) {
                Icon(Icons.Rounded.Add, null); Text("  Быстро добавить")
            }
        }
        val sorted = store.tasks.sortedWith(compareBy({ it.completed }, { -(it.priority) }))
        items(sorted, key = { it.id }) { task ->
            Card(shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { store.toggleTask(task.id) }) {
                        Icon(if (task.completed) Icons.Rounded.CheckCircle else Icons.Rounded.Circle, null)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(task.title, fontWeight = FontWeight.Medium)
                        Text(task.dueDate ?: "Без даты", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { store.deleteTask(task.id) }) { Icon(Icons.Rounded.Delete, null) }
                }
            }
        }
    }
    if (showAdd) AddTaskDialog(onDismiss = { showAdd = false }) {
        store.addTask(it)
        showAdd = false
    }
}

@Composable
fun HabitsScreen(store: PlannerStore) {
    var showAdd by remember { mutableStateOf(false) }
    val today = LocalDate.now().toString()
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Header("Привычки", "Серии без давления и чувства вины")
            Button(onClick = { showAdd = true }, modifier = Modifier.padding(top = 12.dp)) {
                Icon(Icons.Rounded.Add, null); Text("  Новая привычка")
            }
        }
        items(store.habits, key = { it.id }) { habit -> HabitCard(store, habit, today) }
    }
    if (showAdd) SimpleNameDialog("Новая привычка", "Например: прогулка 30 минут", { showAdd = false }) {
        store.addHabit(it); showAdd = false
    }
}

@Composable
private fun HabitCard(store: PlannerStore, habit: Habit, today: String) {
    val completed = habit.completedDates.contains(today)
    Card(
        modifier = Modifier.fillMaxWidth().clickable { store.toggleHabitToday(habit.id) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (completed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (completed) Icons.Rounded.CheckCircle else Icons.Rounded.Circle, null)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(habit.title, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocalFireDepartment, null)
                    Text(" ${store.streak(habit)} дней подряд", style = MaterialTheme.typography.bodyMedium)
                }
            }
            IconButton(onClick = { store.deleteHabit(habit.id) }) { Icon(Icons.Rounded.Delete, null) }
        }
    }
}

@Composable
fun CalendarScreen(store: PlannerStore) {
    val fmt = remember { DateTimeFormatter.ofPattern("EEE, d MMM", Locale("ru")) }
    val days = remember { (0L..6L).map { LocalDate.now().plusDays(it) } }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Header("Календарь", "Ближайшие 7 дней без перегруза") }
        items(days) { day ->
            val dayTasks = store.tasks.filter { it.dueDate == day.toString() }
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(day.format(fmt).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                    if (dayTasks.isEmpty()) Text("Свободно", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    dayTasks.forEach { task ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (task.completed) Icons.Rounded.CheckCircle else Icons.Rounded.Circle, null)
                            Text("  ${task.title}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsScreen(store: PlannerStore) {
    val total = store.tasks.size
    val completed = store.tasks.count { it.completed }
    val taskProgress = if (total == 0) 0f else completed.toFloat() / total
    val today = LocalDate.now().toString()
    val habitProgress = if (store.habits.isEmpty()) 0f else store.habits.count { it.completedDates.contains(today) }.toFloat() / store.habits.size
    val bestStreak = store.habits.maxOfOrNull { store.streak(it) } ?: 0

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Header("Прогресс", "Только полезные цифры, без визуального шума") }
        item { ProgressCard("Задачи", "$completed из $total выполнено", taskProgress) }
        item { ProgressCard("Привычки сегодня", "${(habitProgress * 100).toInt()}% выполнено", habitProgress) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("${store.focusMinutes}", "минут фокуса", Modifier.weight(1f))
                StatCard("$bestStreak", "лучшая серия", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Header(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProgressCard(title: String, subtitle: String, progress: Float) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SimpleNameDialog(title: String, hint: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(text, { text = it }, label = { Text(hint) }, singleLine = true) },
        confirmButton = { TextButton(onClick = { if (text.isNotBlank()) onSave(text) }) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

package com.belov.maxplaner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.ui.components.ActionSetupDialog
import com.belov.maxplaner.ui.components.TrackerCard
import com.belov.maxplaner.data.ActionTemplate
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import com.belov.maxplaner.data.ActionCatalog
import com.belov.maxplaner.data.ActionCategory
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.ui.components.CompletionButton
import com.belov.maxplaner.ui.components.PlannerCard
import com.belov.maxplaner.ui.components.TaskEditorDialog
import com.belov.maxplaner.ui.theme.LocalStyleTokens

@Composable
fun TasksV2Screen(store: PlannerStore) {
    var showCustom by rememberSaveable { mutableStateOf(false) }
    var showCatalog by rememberSaveable { mutableStateOf(false) }

    var selectedTaskId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedTask = store.tasks.firstOrNull { it.id == selectedTaskId }
    if (selectedTask != null) {
        TaskDetailScreen(store, selectedTask) { selectedTaskId = null }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = LocalStyleTokens.current.screenPadding),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Мои дела", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
                Text("Выбери готовое действие или добавь своё", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { showCatalog = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Text("Выбрать готовое", modifier = Modifier.padding(start = 8.dp), maxLines = 1)
                }
                OutlinedButton(onClick = { showCustom = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Создать своё", maxLines = 1)
                }
            }
        }

        if (store.tasks.isEmpty() && store.trackers.isEmpty()) {
            item {
                PlannerCard {
                    Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding)) {
                        Text("Добавь первое дело", style = MaterialTheme.typography.titleMedium)
                        Text("Готовые варианты уже собраны по понятным категориям.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (store.trackers.isNotEmpty()) {
            item { Text("Мои действия и показатели", style = MaterialTheme.typography.titleLarge) }
            items(store.trackers, key = { "tracker-${it.id}" }) { tracker -> TrackerCard(store, tracker) }
            if (store.tasks.isNotEmpty()) item { Text("Дела", style = MaterialTheme.typography.titleLarge) }
        }
        val sorted = store.tasks.sortedWith(compareBy({ it.completed }, { -it.priority }))
        items(sorted, key = { it.id }) { task ->
            PlannerCard(modifier = Modifier.fillMaxWidth().clickable { selectedTaskId = task.id }) {
                Row(
                    Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompletionButton(task.completed, task.title) { store.toggleTask(task.id) }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            task.title,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (task.completed) TextDecoration.LineThrough else null
                        )
                        Text(
                            task.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { store.deleteTask(task.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Удалить")
                    }
                }
            }
        }
    }

    if (showCustom) ActionSetupDialog(store, onDismiss = { showCustom = false })
    if (showCatalog) ActionCatalogDialog(store, onDismiss = { showCatalog = false })
}

@Composable
internal fun ActionCatalogDialog(store: PlannerStore, onDismiss: () -> Unit) {
    var category by rememberSaveable { mutableStateOf<ActionCategory?>(null) }
    var selectedTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var custom by rememberSaveable { mutableStateOf(false) }
    var showCategories by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val selected = ActionCatalog.templates.firstOrNull { it.title == selectedTitle }
    if (selected != null || custom) {
        ActionSetupDialog(store, onDismiss = onDismiss, template = selected, category = category?.title ?: "Личное")
        return
    }
    val visible = if (query.isNotBlank()) ActionCatalog.templates.filter { it.title.contains(query.trim(), ignoreCase = true) }
        else category?.let { c -> ActionCatalog.templates.filter { it.category == c } } ?: ActionCatalog.popular
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(category?.title ?: "⭐ Популярное") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(query, { query = it }, label = { Text("Найти действие") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item {
                    TextButton(onClick = { showCategories = !showCategories }) { Text(if (showCategories) "Скрыть категории" else "Все категории · 11") }
                    if (showCategories) FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(category == null, { category = null; query = "" }, label = { Text("⭐ Популярное") })
                        ActionCategory.entries.forEach { c -> FilterChip(category == c, { category = c; query = ""; showCategories = false }, label = { Text(c.title) }) }
                    }
                }
                item { OutlinedButton(onClick = { custom = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("+ Создать своё", maxLines = 1) } }
                if (visible.isEmpty()) item { Text("Ничего не найдено. Можно создать своё действие.") }
                items(visible, key = { it.title }) { action ->
                    PlannerCard(modifier = Modifier.fillMaxWidth().clickable { selectedTitle = action.title }) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Text(action.title, fontWeight = FontWeight.SemiBold)
                            Text(if (action.unit.isNotBlank()) action.unit else "Настроить и добавить", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

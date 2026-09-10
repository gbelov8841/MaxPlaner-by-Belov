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

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = LocalStyleTokens.current.screenPadding),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Мои дела", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
                Text("Выбери готовое действие или добавь своё", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { showCatalog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Text("Выбрать готовое", modifier = Modifier.padding(start = 8.dp), maxLines = 1)
                }
                OutlinedButton(onClick = { showCustom = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Создать своё", maxLines = 1)
                }
            }
        }

        if (store.tasks.isEmpty()) {
            item {
                PlannerCard {
                    Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding)) {
                        Text("Добавь первое дело", style = MaterialTheme.typography.titleMedium)
                        Text("Готовые варианты уже собраны по понятным категориям.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        val sorted = store.tasks.sortedWith(compareBy({ it.completed }, { -it.priority }))
        items(sorted, key = { it.id }) { task ->
            PlannerCard(modifier = Modifier.fillMaxWidth()) {
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

    if (showCustom) {
        TaskEditorDialog(store = store, onDismiss = { showCustom = false })
    }

    if (showCatalog) {
        ActionCatalogDialog(
            onDismiss = { showCatalog = false },
            onAdd = { title, category, minutes, priority ->
                store.addTask(
                    title = title,
                    category = category,
                    durationMinutes = minutes,
                    priority = priority
                )
                showCatalog = false
            }
        )
    }
}

@Composable
private fun ActionCatalogDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int, Int) -> Unit
) {
    var category by rememberSaveable { mutableStateOf<ActionCategory?>(null) }
    val visible = category?.let { selected ->
        ActionCatalog.templates.filter { it.category == selected }
    } ?: ActionCatalog.templates

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Готовые действия" else category!!.title) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (category == null) {
                    items(ActionCategory.entries) { item ->
                        PlannerCard(modifier = Modifier.fillMaxWidth().clickable { category = item }) {
                            Text(
                                item.title,
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                } else {
                    item {
                        TextButton(onClick = { category = null }) { Text("← Все категории") }
                    }
                    items(visible) { action ->
                        PlannerCard(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onAdd(action.title, action.category.title, action.defaultMinutes, action.priority)
                            }
                        ) {
                            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                                Text(action.title, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Добавить в план",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

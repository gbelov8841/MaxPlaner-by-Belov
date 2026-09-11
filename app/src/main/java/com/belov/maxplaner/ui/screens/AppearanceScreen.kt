package com.belov.maxplaner.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.ui.components.*
import com.belov.maxplaner.ui.theme.*

@Composable
fun AppearanceScreen(appearance: AppearanceStore) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Оформление", style = MaterialTheme.typography.headlineLarge)
            Text("Theme Collection 2.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(ThemePacks, key = { it.id }) { pack ->
            val selected = appearance.themeId == pack.id
            Card(onClick = { appearance.selectTheme(pack.id) }, modifier = Modifier.fillMaxWidth().semantics { this.selected = selected },
                shape = LocalStyleTokens.current.cardShape, border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null) {
                Box(Modifier.fillMaxWidth().heightIn(min = 150.dp)) {
                    Image(painterResource(pack.artwork.resource), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop, alignment = Alignment.TopCenter)
                    Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(pack.background.copy(alpha = .2f), pack.background.copy(alpha = .92f)))))
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(pack.name, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = pack.text)
                            if (selected) Icon(Icons.Rounded.CheckCircle, "Выбрана", tint = pack.accent)
                        }
                        Text(pack.description, style = MaterialTheme.typography.bodySmall, color = pack.text)
                        Surface(color = pack.surfaces.color.copy(alpha = pack.surfaces.opacity), contentColor = pack.text,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(pack.surfaces.compactRadius)) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.RadioButtonUnchecked, null, tint = pack.accent, modifier = Modifier.size(20.dp))
                                Text("Пример задачи", Modifier.weight(1f).padding(start = 10.dp), style = MaterialTheme.typography.bodyMedium)
                                Text("09:00", style = MaterialTheme.typography.labelSmall, color = pack.secondaryText)
                            }
                        }
                    }
                }
            }
        }
        item { Text("Тема применяется ко всем экранам и сохраняется на устройстве.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun MoreScreen(onNavigate: (String) -> Unit) {
    val sections = listOf(Triple("habits", "Привычки", Icons.Rounded.Spa), Triple("tasks", "Дела и каталог действий", Icons.Rounded.GridView),
        Triple("appearance", "Оформление", Icons.Rounded.Palette), Triple("settings", "Настройки", Icons.Rounded.Settings))
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Ещё", style = MaterialTheme.typography.headlineLarge) }
        items(sections) { (route, title, icon) ->
            PlannerSurface(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate(route) }) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, Modifier.size(22.dp)); Text(title, Modifier.weight(1f).padding(horizontal = 12.dp))
                    Icon(Icons.Rounded.ChevronRight, null, Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(appearance: AppearanceStore) {
    var name by rememberSaveable { mutableStateOf(appearance.displayName) }
    var saved by rememberSaveable { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Настройки", style = MaterialTheme.typography.headlineLarge) }
        item {
            PlannerCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Как к тебе обращаться", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(name, { name = it.take(60); saved = false }, label = { Text("Имя") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { appearance.rename(name); saved = true }, enabled = name.trim() != appearance.displayName) { Text("Сохранить") }
                    if (saved) Text("Имя сохранено", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            PlannerCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PrimePlaner by Belov", style = MaterialTheme.typography.titleMedium)
                    Text("Дела, привычки и показатели хранятся на этом устройстве. Приложение работает без интернета.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

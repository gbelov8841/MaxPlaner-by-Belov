package com.belov.maxplaner.ui.screens

import com.belov.maxplaner.ui.theme.LocalStyleTokens
import com.belov.maxplaner.ui.components.PlannerCard
import com.belov.maxplaner.ui.components.PlannerSurface
import com.belov.maxplaner.ui.components.PlannerProgressIndicator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.ui.theme.AppearanceStore
import com.belov.maxplaner.ui.theme.MaxPlanerStyles
import com.belov.maxplaner.ui.theme.PaletteOption

@Composable
fun AppearanceScreen(appearance: AppearanceStore, onOpenTasks: () -> Unit = {}, onOpenHabits: () -> Unit = {}) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text("Ещё", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onOpenTasks, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Все мои дела и показатели") }
            TextButton(onClick = onOpenHabits, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Мои привычки") }
        }
        item {
            Text("Оформление", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "Стиль меняет характер интерфейса, цветовая схема — его настроение.",
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item { CurrentStylePreview(appearance) }

        item {
            Text("Стиль", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MaxPlanerStyles.forEachIndexed { index, style ->
                    val selected = appearance.styleId == style.id
                    PlannerCard(
                        modifier = Modifier.fillMaxWidth().clickable { appearance.selectStyle(style.id) },
                        selected = selected,
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        shape = LocalStyleTokens.current.cardShape
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("${index + 1}. ${style.name}", fontWeight = FontWeight.SemiBold)
                                Text(style.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (selected) Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        item {
            Text("Цветовая схема", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                appearance.style.name,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(appearance.style.palettes, key = { it.id }) { palette ->
                    PaletteCard(
                        palette = palette,
                        selected = appearance.paletteId == palette.id,
                        onClick = { appearance.selectPalette(palette.id) }
                    )
                }
            }
        }

        item {
            Text(
                "Выбор сохраняется на этом устройстве и применяется ко всем экранам PrimePlaner.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CurrentStylePreview(appearance: AppearanceStore) {
    PlannerCard(
        modifier = Modifier.fillMaxWidth(),
        hero = true,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(LocalStyleTokens.current.cardPadding), verticalArrangement = Arrangement.spacedBy(LocalStyleTokens.current.sectionSpacing)) {
            Text("PrimePlaner by Belov", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(appearance.style.name, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlannerSurface(shape = LocalStyleTokens.current.compactShape, color = MaterialTheme.colorScheme.primary) {
                    Text("Сегодня", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onPrimary)
                }
                PlannerSurface(shape = LocalStyleTokens.current.compactShape, color = MaterialTheme.colorScheme.surface) {
                    Text("6 из 8 задач", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                }
            }
            Text("${appearance.palette.name} · Пример прогресса", fontWeight = FontWeight.Medium)
            PlannerProgressIndicator(progress = { .78f }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun PaletteCard(palette: PaletteOption, selected: Boolean, onClick: () -> Unit) {
    PlannerCard(
        modifier = Modifier
            .size(width = 152.dp, height = 118.dp)
            .clickable(onClick = onClick),
        shape = LocalStyleTokens.current.cardShape,
        selected = selected,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ColorDot(palette.background)
                ColorDot(palette.primary)
                ColorDot(palette.accent)
            }
            Text(palette.name, fontWeight = FontWeight.SemiBold)
            Text(if (palette.isDark) "Тёмная" else "Светлая", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ColorDot(color: Color) {
    Box(
        Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
    )
}

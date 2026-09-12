package com.belov.maxplaner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.ui.components.*
import com.belov.maxplaner.ui.icons.PrimeIcons
import com.belov.maxplaner.ui.screens.*
import com.belov.maxplaner.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import java.time.LocalDate

private data class Tab(val route: String, val label: String, val icon: ImageVector)
private val tabs = listOf(Tab("today", "Главная", PrimeIcons.Home), Tab("calendar", "План", PrimeIcons.Calendar),
    Tab("analytics", "Прогресс", PrimeIcons.Progress), Tab("more", "Ещё", PrimeIcons.More))

@Composable
fun MaxPlanerApp(appearance: AppearanceStore) {
    val context = LocalContext.current
    val store = remember { PlannerStore(context.applicationContext) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var undoJob by remember { mutableStateOf<Job?>(null) }
    val toggleTask: (String) -> Unit = { id ->
        val before = store.tasks.firstOrNull { it.id == id }
        if (before != null) {
            undoJob?.cancel()
            store.toggleTask(id)
            if (!before.completed) undoJob = scope.launch {
                if (snackbar.showSnackbar("Выполнено", "Отменить", withDismissAction = true) == SnackbarResult.ActionPerformed) {
                    if (store.tasks.firstOrNull { it.id == id }?.completed == true) store.toggleTask(id)
                }
            }
        }
    }
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(store, owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) { store.refreshFocus(); delay(1000) }
        }
    }
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: "today"
    var selectedDate by rememberSaveable { mutableStateOf(store.today.toString()) }
    var quickAdd by rememberSaveable { mutableStateOf(false) }
    val p = LocalThemePack.current
    fun navigate(destination: String) {
        nav.navigate(destination) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true; restoreState = true
        }
    }
    CompositionLocalProvider(LocalTaskCompletion provides toggleTask) {
    Box(Modifier.fillMaxSize()) {
        PlannerBackdrop()
        Scaffold(snackbarHost = { SnackbarHost(snackbar) }, containerColor = Color.Transparent, contentColor = p.text, bottomBar = {
            Box(Modifier.navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
                PlannerSurface(shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().heightIn(min = p.navigationHeight).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        tabs.forEachIndexed { index, tab ->
                            if (index == 2) Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                IconButton(onClick = { quickAdd = true }, modifier = Modifier.size(48.dp)) {
                                    PlannerSurface(modifier = Modifier.size(p.addSize), shape = CircleShape,
                                        color = p.accent.copy(alpha = .14f)) {
                                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Add, "Добавить", Modifier.size(24.dp), tint = p.text) }
                                    }
                                }
                            }
                            val selected = route == tab.route || (tab.route == "more" && route in listOf("appearance", "habits", "tasks", "settings"))
                            Column(Modifier.weight(1f).heightIn(min = 56.dp).selectable(selected = selected, role = Role.Tab,
                                onClick = { navigate(tab.route) }).padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Icon(tab.icon, null, Modifier.size(20.dp), tint = if (selected) p.accent else p.secondaryText)
                                Text(tab.label, style = MaterialTheme.typography.labelSmall, color = if (selected) p.text else p.secondaryText)
                            }
                        }
                    }
                }
            }
        }) { padding ->
            NavHost(nav, startDestination = "today", modifier = Modifier.padding(padding)) {
                composable("today") { TodayScreen(store, appearance.displayName,
                    onOpenPlan = { selectedDate = it.toString(); navigate("calendar") },
                    onOpenProgress = { navigate("analytics") }, onOpenHabits = { navigate("habits") }) }
                composable("calendar") { PlanDayScreen(store, selectedDate) { selectedDate = it.toString() } }
                composable("tasks") { TasksV2Screen(store) }
                composable("habits") { HabitsV2Screen(store) }
                composable("analytics") { AnalyticsV2Screen(store, { navigate("tasks") }, { navigate("habits") }, { quickAdd = true }) }
                composable("more") { MoreScreen(onNavigate = { navigate(it) }) }
                composable("appearance") { AppearanceScreen(appearance) }
                composable("settings") { SettingsScreen(appearance) }
            }
        }
    }
    if (quickAdd) ActionSetupDialog(store, onDismiss = { quickAdd = false })
    }
}

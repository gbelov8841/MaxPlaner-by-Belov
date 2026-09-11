package com.belov.maxplaner.ui

import com.belov.maxplaner.ui.icons.PrimeIcons
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.belov.maxplaner.data.PlannerStore
import com.belov.maxplaner.ui.screens.AnalyticsV2Screen
import com.belov.maxplaner.ui.screens.AppearanceScreen
import com.belov.maxplaner.ui.screens.CalendarScreen
import com.belov.maxplaner.ui.screens.HabitsV2Screen
import com.belov.maxplaner.ui.screens.TasksV2Screen
import com.belov.maxplaner.ui.screens.TodayScreen
import com.belov.maxplaner.ui.components.ActionSetupDialog
import com.belov.maxplaner.ui.components.PlannerBackdrop
import com.belov.maxplaner.ui.components.styleBorder
import androidx.compose.material3.MaterialTheme
import com.belov.maxplaner.ui.theme.LocalStyleTokens
import com.belov.maxplaner.ui.theme.AppearanceStore

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    Tab("today", "Главная", PrimeIcons.Home),
    Tab("calendar", "План дня", PrimeIcons.Calendar),
    Tab("analytics", "Прогресс", PrimeIcons.Progress),
    Tab("appearance", "Ещё", PrimeIcons.More)
)

@Composable
fun MaxPlanerApp(appearance: AppearanceStore) {
    val context = LocalContext.current
    val store = remember { PlannerStore(context.applicationContext) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(store, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                store.refreshFocus()
                delay(1000)
            }
        }
    }
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: "today"
    val tokens = LocalStyleTokens.current
    val motionDuration = tokens.motionDurationMillis
    var showQuickAdd by remember { mutableStateOf(false) }

    val navigationContent: @Composable () -> Unit = {
        NavigationBar(
            containerColor = if (tokens.floatingGlass) Color.Transparent else MaterialTheme.colorScheme.surface,
            tonalElevation = if (tokens.floatingGlass) 0.dp else tokens.navigationElevation
        ) {
                tabs.take(2).forEach { tab ->
                    NavigationBarItem(
                        selected = route == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, maxLines = 1) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = if (tokens.floatingGlass) MaterialTheme.colorScheme.primary.copy(alpha = .12f) else MaterialTheme.colorScheme.surfaceVariant,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .72f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .72f)
                        )
                    )
                }
                FloatingActionButton(
                    onClick = { showQuickAdd = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = tokens.navigationElevation)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Добавить")
                }
                tabs.drop(2).forEach { tab ->
                    NavigationBarItem(
                        selected = route == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, maxLines = 1) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = if (tokens.floatingGlass) MaterialTheme.colorScheme.primary.copy(alpha = .12f) else MaterialTheme.colorScheme.surfaceVariant,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .72f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .72f)
                        )
                    )
                }
            }
        }

    Box {
        PlannerBackdrop()
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            bottomBar = {
                if (tokens.floatingGlass) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .navigationBarsPadding(),
                        shape = tokens.heroShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = .82f),
                        border = styleBorder(),
                        tonalElevation = tokens.navigationElevation
                    ) {
                        navigationContent()
                    }
                } else {
                    navigationContent()
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = "today",
                enterTransition = {
                    val from = tabs.indexOfFirst { it.route == initialState.destination.route }
                    val to = tabs.indexOfFirst { it.route == targetState.destination.route }
                    val direction = if (from >= 0 && to >= 0 && to < from) -1 else 1
                    fadeIn(tween(motionDuration)) +
                        slideInHorizontally(tween(motionDuration)) { full -> direction * full / 10 }
                },
                exitTransition = {
                    val from = tabs.indexOfFirst { it.route == initialState.destination.route }
                    val to = tabs.indexOfFirst { it.route == targetState.destination.route }
                    val direction = if (from >= 0 && to >= 0 && to < from) -1 else 1
                    fadeOut(tween(motionDuration / 2)) +
                        slideOutHorizontally(tween(motionDuration)) { full -> -direction * full / 12 }
                },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                composable("today") {
                    TodayScreen(
                        store = store,
                        onOpenPlan = { navController.navigate("calendar") { launchSingleTop = true } },
                        onOpenProgress = { navController.navigate("analytics") { launchSingleTop = true } },
                        onOpenHabits = { navController.navigate("habits") { launchSingleTop = true } }
                    )
                }
                composable("calendar") { CalendarScreen(store) }
                composable("tasks") { TasksV2Screen(store) }
                composable("habits") { HabitsV2Screen(store) }
                composable("analytics") { AnalyticsV2Screen(store, onOpenTasks = { navController.navigate("tasks") { launchSingleTop = true } }, onOpenHabits = { navController.navigate("habits") { launchSingleTop = true } }, onAdd = { showQuickAdd = true }) }
                composable("appearance") { AppearanceScreen(appearance, onOpenTasks = { navController.navigate("tasks") { launchSingleTop = true } }, onOpenHabits = { navController.navigate("habits") { launchSingleTop = true } }) }
            }
        }
    }
    }
    if (showQuickAdd) ActionSetupDialog(store, onDismiss = { showQuickAdd = false })
}



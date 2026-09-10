package com.belov.maxplaner.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
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
import androidx.compose.material3.MaterialTheme
import com.belov.maxplaner.ui.theme.LocalStyleTokens
import com.belov.maxplaner.ui.theme.AppearanceStore

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    Tab("today", "Главная", Icons.Rounded.Home),
    Tab("calendar", "План дня", Icons.Rounded.CalendarMonth),
    Tab("analytics", "Прогресс", Icons.Rounded.Analytics),
    Tab("appearance", "Ещё", Icons.Rounded.MoreHoriz)
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
    val motionDuration = LocalStyleTokens.current.motionDurationMillis
    var showQuickAdd by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = LocalStyleTokens.current.navigationElevation
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
                        alwaysShowLabel = route == tab.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = androidx.compose.ui.graphics.Color(0xFFE8C56A),
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = androidx.compose.ui.graphics.Color(0xFF243246),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .72f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .72f)
                        )
                    )
                }
                FloatingActionButton(
                    onClick = { showQuickAdd = true },
                    containerColor = androidx.compose.ui.graphics.Color(0xFFE8C56A),
                    contentColor = androidx.compose.ui.graphics.Color(0xFF0B1118),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = LocalStyleTokens.current.navigationElevation)
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
                        alwaysShowLabel = route == tab.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = androidx.compose.ui.graphics.Color(0xFFE8C56A),
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = androidx.compose.ui.graphics.Color(0xFF243246),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .72f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .72f)
                        )
                    )
                }
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
                composable("today") { TodayScreen(store) }
                composable("calendar") { CalendarScreen(store) }
                composable("tasks") { TasksV2Screen(store) }
                composable("habits") { HabitsV2Screen(store) }
                composable("analytics") { AnalyticsV2Screen(store) }
                composable("appearance") { AppearanceScreen(appearance) }
            }
        }
    }
    if (showQuickAdd) ActionSetupDialog(store, onDismiss = { showQuickAdd = false })
}


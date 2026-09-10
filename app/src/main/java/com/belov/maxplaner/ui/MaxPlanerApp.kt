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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.material3.MaterialTheme
import com.belov.maxplaner.ui.theme.LocalStyleTokens
import com.belov.maxplaner.ui.theme.AppearanceStore

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    Tab("today", "Сегодня", Icons.Rounded.Home),
    Tab("calendar", "Календарь", Icons.Rounded.CalendarMonth),
    Tab("tasks", "Дела", Icons.Rounded.CheckCircle),
    Tab("habits", "Привычки", Icons.Rounded.Spa),
    Tab("analytics", "Прогресс", Icons.Rounded.Analytics),
    Tab("appearance", "Стиль", Icons.Rounded.Palette)
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

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = LocalStyleTokens.current.navigationElevation
            ) {
                tabs.forEach { tab ->
                    val selected = route == tab.route
                    val iconScale by animateFloatAsState(
                        targetValue = if (selected) 1.12f else 1f,
                        animationSpec = tween(LocalStyleTokens.current.motionDurationMillis),
                        label = "navIconScale"
                    )
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                            )
                        },
                        label = { Text(tab.label, maxLines = 1) },
                        alwaysShowLabel = selected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
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
                    fadeIn(tween(LocalStyleTokens.current.motionDurationMillis)) +
                        slideInHorizontally(tween(LocalStyleTokens.current.motionDurationMillis)) { full -> direction * full / 10 }
                },
                exitTransition = {
                    val from = tabs.indexOfFirst { it.route == initialState.destination.route }
                    val to = tabs.indexOfFirst { it.route == targetState.destination.route }
                    val direction = if (from >= 0 && to >= 0 && to < from) -1 else 1
                    fadeOut(tween(LocalStyleTokens.current.motionDurationMillis / 2)) +
                        slideOutHorizontally(tween(LocalStyleTokens.current.motionDurationMillis)) { full -> -direction * full / 12 }
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
}

package com.idlestables.app.idlestables.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.idlestables.core.demo.DemoRepository
import com.idlestables.app.idlestables.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdleStablesApp(
    repo: DemoRepository,
    onConnectWallet: () -> Unit,
    authTokenPreview: String?,
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val tabs = listOf(
        BottomTab(ScreenRoute.Dashboard, "Dashboard"),
        BottomTab(ScreenRoute.Stable, "Stable"),
        BottomTab(ScreenRoute.Tracks, "Tracks"),
        BottomTab(ScreenRoute.Breed, "Breed"),
        BottomTab(ScreenRoute.Silks, "Silks"),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IdleStables") },
                actions = {
                    TextButton(onClick = onConnectWallet) {
                        Text(if (authTokenPreview != null) "Wallet: ${authTokenPreview}…" else "Connect Wallet")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(tab.label) },
                        icon = { }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ScreenRoute.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(ScreenRoute.Dashboard.route) {
                DashboardScreen(repo = repo)
            }
            composable(ScreenRoute.Stable.route) {
                StableScreen(repo = repo)
            }
            composable(ScreenRoute.Tracks.route) {
                TracksScreen(
                    repo = repo,
                    onOpenTrack = { trackId -> navController.navigate(ScreenRoute.TrackDetail.create(trackId)) }
                )
            }
            composable(ScreenRoute.TrackDetail.route) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                TrackDetailScreen(
                    repo = repo,
                    trackId = id,
                    onOpenRace = { raceId -> navController.navigate(ScreenRoute.Race.create(raceId)) },
                )
            }
            composable(ScreenRoute.Race.route) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                RaceScreen(repo = repo, raceId = id)
            }
            composable(ScreenRoute.Breed.route) {
                BreedScreen(repo = repo)
            }
            composable(ScreenRoute.Silks.route) {
                SilksScreen(repo = repo)
            }
        }
    }
}

private data class BottomTab(val route: ScreenRoute, val label: String)

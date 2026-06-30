package com.gamelauncher.ui

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gamelauncher.data.preference.SettingsPreferences
import com.gamelauncher.ui.dashboard.DashboardScreen
import com.gamelauncher.ui.games.GameDetailsScreen
import com.gamelauncher.ui.games.GameListScreen
import com.gamelauncher.ui.onboarding.OnboardingScreen
import com.gamelauncher.ui.settings.SettingsScreen
import com.gamelauncher.ui.theme.GameLauncherTheme
import com.gamelauncher.ui.theme.PrimaryNeon
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsPreferences: SettingsPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            var onboardingDone by remember { mutableStateOf(true) }
            var ready by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    isDarkTheme = settingsPreferences.isDarkTheme.first()
                    onboardingDone = settingsPreferences.onboardingCompleted.first()
                    ready = true
                }
            }

            if (!ready) return@setContent

            GameLauncherTheme(darkTheme = isDarkTheme) {
                if (!onboardingDone) {
                    OnboardingScreen(onComplete = {
                        lifecycleScope.launch {
                            settingsPreferences.setOnboardingCompleted()
                            onboardingDone = true
                        }
                    })
                } else {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Dashboard,
        Screen.Games,
        Screen.Settings
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = items.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = com.gamelauncher.ui.theme.SurfaceDark,
                    contentColor = Color.White
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontWeight = FontWeight.Bold) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = PrimaryNeon,
                                indicatorColor = PrimaryNeon,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Games.route) { GameListScreen(navController = navController) }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(
                route = Screen.GameDetails.route + "/{packageName}",
                arguments = listOf(navArgument("packageName") { type = NavType.StringType })
            ) { backStackEntry ->
                val packageName = backStackEntry.arguments?.getString("packageName")?.let(Uri::decode) ?: return@composable
                GameDetailsScreen(
                    packageName = packageName,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object Games : Screen("games", "Games", Icons.Filled.Games)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object GameDetails : Screen("game_details", "Game Details", Icons.Filled.Games)
}

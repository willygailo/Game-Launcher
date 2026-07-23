package com.gamelauncher.ui

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gamelauncher.data.preference.SettingsPreferences
import com.gamelauncher.feature.network.ui.NetworkScreen
import com.gamelauncher.feature.network.ui.NetworkViewModel
import com.gamelauncher.feature.tweaks.ui.TweaksScreen
import com.gamelauncher.feature.tweaks.ui.TweaksViewModel
import com.gamelauncher.ui.dashboard.DashboardScreen
import com.gamelauncher.ui.games.GameDetailsScreen
import com.gamelauncher.ui.onboarding.OnboardingScreen
import com.gamelauncher.ui.settings.SettingsScreen
import com.gamelauncher.ui.theme.GameLauncherTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsPreferences: SettingsPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable immersive full-screen display
        enableEdgeToEdge()
        
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

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("dashboard") { 
                DashboardScreen(
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToTweaks = { navController.navigate("tweaks") },
                    onNavigateToNetwork = { navController.navigate("network") },
                    onNavigateToMonitor = { navController.navigate("monitor") },
                    onNavigateToGameDetails = { packageName -> 
                        navController.navigate("game_details/$packageName") 
                    }
                ) 
            }
            composable("settings") { 
                SettingsScreen()
            }
            composable("tweaks") {
                val viewModel: TweaksViewModel = hiltViewModel()
                TweaksScreen(viewModel = viewModel)
            }
            composable("network") {
                val viewModel: NetworkViewModel = hiltViewModel()
                NetworkScreen(viewModel = viewModel)
            }
            composable("monitor") {
                val viewModel: com.gamelauncher.feature.monitor.ui.MonitorViewModel = hiltViewModel()
                com.gamelauncher.feature.monitor.ui.MonitorScreen(viewModel = viewModel)
            }
            composable("gamespace") {
                val viewModel: com.gamelauncher.feature.gamespace.ui.GameSpaceViewModel = hiltViewModel()
                com.gamelauncher.feature.gamespace.ui.GameSpaceDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTweaks = { navController.navigate("tweaks") },
                    onNavigateToHuaweiGuide = { navController.navigate("huawei_pairing_guide") }
                )
            }
            composable("huawei_pairing_guide") {
                val context = LocalContext.current
                com.gamelauncher.feature.gamespace.ui.HuaweiPairingGuideScreen(
                    onBack = { navController.popBackStack() },
                    onLaunchShizuku = {
                        try {
                            val intent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                            if (intent != null) context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )
            }

            composable(
                route = "game_details/{packageName}",
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


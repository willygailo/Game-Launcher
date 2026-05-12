package com.gamelauncher.ui.games

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gamelauncher.core.startManagedService
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.services.GameBoosterService
import com.gamelauncher.ui.Screen
import com.gamelauncher.ui.theme.*

@Composable
fun GameListScreen(
    navController: NavController? = null,
    viewModel: GamesViewModel = hiltViewModel()
) {
    val filteredGames by viewModel.filteredGames.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Games",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = viewModel::refreshGames) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh games",
                    tint = PrimaryNeon
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::setSearchQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search games...", color = TextSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = PrimaryNeon,
                focusedBorderColor = PrimaryNeon,
                unfocusedBorderColor = SurfaceVariantDark,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (availableCategories.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCategories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { viewModel.setSelectedCategory(category) },
                        label = { Text(category, fontSize = MaterialTheme.typography.labelSmall.fontSize) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryNeon.copy(alpha = 0.3f),
                            selectedLabelColor = PrimaryNeon
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (isScanning) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryNeon)
            }
        } else if (filteredGames.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedCategory != "All") "No matching games" else "No games detected yet",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedCategory != "All") "Try a different search or filter" else "Tap refresh after installing or updating games",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = viewModel::refreshGames) {
                        Text("Scan Games")
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredGames, key = { it.packageName }) { game ->
                    GameCard(
                        game = game,
                        onLaunch = { 
                            if (game.highPerformanceMode) {
                                val intent = Intent(context, GameBoosterService::class.java).apply {
                                    action = GameBoosterService.ACTION_START_BOOST
                                    putExtra(GameBoosterService.EXTRA_PACKAGE, game.packageName)
                                    putExtra(GameBoosterService.EXTRA_TARGET_FPS, game.targetFps)
                                    putExtra(GameBoosterService.EXTRA_ENABLE_NETWORK, game.wifiLockEnabled)
                                }
                                context.startManagedService(intent)
                            }
                            viewModel.launchGame(game)
                        },
                        onUpdate = { viewModel.updateGameSettings(it) },
                        onDetails = {
                            navController?.navigate("${Screen.GameDetails.route}/${game.packageName}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GameCard(
    game: GameModel,
    onLaunch: () -> Unit,
    onUpdate: (GameModel) -> Unit,
    onDetails: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val pm = context.packageManager
    
    val icon = remember(game.packageName) {
        try {
            pm.getApplicationIcon(game.packageName).toBitmap().asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    val gameInfo = remember(game.packageName) {
        com.gamelauncher.core.SupportedGames.findGame(game.packageName)
    }
    val deviceMaxFps = remember {
        val dm = context.getSystemService(android.hardware.display.DisplayManager::class.java)
        val display = dm?.getDisplay(android.view.Display.DEFAULT_DISPLAY)
        display?.supportedModes?.maxOfOrNull { it.refreshRate.toInt() } ?: 165
    }
    val gameMaxFps = gameInfo?.maxFps?.coerceAtMost(deviceMaxFps) ?: deviceMaxFps

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Image(
                        bitmap = icon,
                        contentDescription = game.name,
                        modifier = Modifier.size(56.dp)
                    )
                } else {
                    Box(modifier = Modifier
                        .size(56.dp)
                        .background(Color.Gray, RoundedCornerShape(8.dp)))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(game.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text(game.customCategory, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                
                if (onDetails != null) {
                    IconButton(onClick = onDetails) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Game details",
                            tint = TextSecondary
                        )
                    }
                }
                Button(
                    onClick = onLaunch,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("PLAY", fontWeight = FontWeight.Bold)
                }
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(16.dp)
                ) {
                    Text("Boost Settings", color = WarningOrange, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Auto Boost on Launch", color = TextPrimary, modifier = Modifier.weight(1f))
                        Switch(
                            checked = game.highPerformanceMode,
                            onCheckedChange = { onUpdate(game.copy(highPerformanceMode = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryNeon, checkedTrackColor = PrimaryNeon.copy(alpha=0.5f))
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Network Low-Latency Lock", color = TextPrimary, modifier = Modifier.weight(1f))
                        Switch(
                            checked = game.wifiLockEnabled,
                            onCheckedChange = { onUpdate(game.copy(wifiLockEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryNeon, checkedTrackColor = PrimaryNeon.copy(alpha=0.5f))
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Graphics Mode: ${game.graphicsMode}", color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.gamelauncher.data.model.GraphicsMode.entries.forEach { mode ->
                            FilterChip(
                                selected = game.graphicsMode == mode.name,
                                onClick = { onUpdate(game.copy(graphicsMode = mode.name)) },
                                label = { Text(mode.displayName, fontSize = MaterialTheme.typography.labelSmall.fontSize) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryNeon.copy(alpha = 0.3f),
                                    selectedLabelColor = SecondaryNeon
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Target FPS: ${game.targetFps} / ${gameMaxFps} max", color = TextSecondary)
                    Slider(
                        value = game.targetFps.toFloat(),
                        onValueChange = { onUpdate(game.copy(targetFps = it.toInt())) },
                        valueRange = 30f..gameMaxFps.toFloat(),
                        steps = ((gameMaxFps - 30) / 15),
                        colors = SliderDefaults.colors(thumbColor = PrimaryNeon, activeTrackColor = PrimaryNeon)
                    )
                    Text("Device supports up to ${deviceMaxFps}Hz", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

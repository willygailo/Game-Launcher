package com.gamelauncher.ui.games

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.gamelauncher.core.startManagedService
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.services.GameBoosterService
import com.gamelauncher.ui.theme.*

@Composable
fun GameListScreen(
    viewModel: GamesViewModel = hiltViewModel()
) {
    val games by viewModel.games.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
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
                .padding(bottom = 16.dp),
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

        if (isScanning) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryNeon)
            }
        } else if (games.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No games detected yet",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap refresh after installing or updating games",
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
                items(games, key = { it.packageName }) { game ->
                    GameCard(
                        game = game,
                        onLaunch = { 
                            // Start background booster service if enabled
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
                        onUpdate = { viewModel.updateGameSettings(it) }
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
    onUpdate: (GameModel) -> Unit
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
                    Text("Target FPS: ${game.targetFps}", color = TextSecondary)
                    Slider(
                        value = game.targetFps.toFloat(),
                        onValueChange = { onUpdate(game.copy(targetFps = it.toInt())) },
                        valueRange = 30f..165f,
                        steps = 5,
                        colors = SliderDefaults.colors(thumbColor = PrimaryNeon, activeTrackColor = PrimaryNeon)
                    )
                }
            }
        }
    }
}

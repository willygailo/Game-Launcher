package com.gamelauncher.ui.dashboard

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import coil.compose.rememberAsyncImagePainter
import com.gamelauncher.data.model.DeviceSpecs
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.ui.components.AngledCard
import com.gamelauncher.ui.components.ArcGauge
import com.gamelauncher.ui.components.HexagonButton
import com.gamelauncher.ui.games.GamesViewModel
import com.gamelauncher.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    gamesViewModel: GamesViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToGameDetails: (String) -> Unit = {}
) {
    val specs by viewModel.deviceSpecs.collectAsStateWithLifecycle()
    val isDndEnabled by viewModel.isDndEnabled.collectAsStateWithLifecycle()
    val isBrightnessLocked by viewModel.isBrightnessLocked.collectAsStateWithLifecycle()
    val gamesUiState by gamesViewModel.uiState.collectAsStateWithLifecycle()

    var currentMode by remember { mutableStateOf(PerformanceMode.BALANCED) }
    val modeColor = getModeColor(currentMode)

    LifecycleResumeEffect(Unit) {
        viewModel.refreshPermissionStates()
        gamesViewModel.refreshGames()
        onPauseOrDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Futuristic background overlay pattern could go here

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "GAME SPACE",
                        color = modeColor,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                    Text(
                        specs.socName.takeIf { it.isNotBlank() } ?: "System Monitor Active",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = modeColor)
                }
            }

            // Game Carousel
            if (gamesUiState.filteredGames.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(gamesUiState.filteredGames) { game ->
                        GameCoverCard(
                            game = game,
                            accentColor = modeColor,
                            onClick = { gamesViewModel.launchGame(game) },
                            onLongClick = { onNavigateToGameDetails(Uri.encode(game.packageName)) }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No games found. Scanning...", color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Central Boost Button
            HexagonButton(
                text = "BOOST",
                color = modeColor,
                onClick = { viewModel.optimizeRam() }
            )

            if (specs.freedRamMb > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Freed ${specs.freedRamMb} MB of RAM",
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Performance Mode Selector
            SegmentedModeSelector(
                currentMode = currentMode,
                onModeSelected = { currentMode = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Gauges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ArcGauge(
                    progress = specs.cpuUsagePercent / 100f,
                    color = modeColor,
                    label = "CPU",
                    valueText = "${specs.cpuUsagePercent.toInt()}%",
                    size = 110.dp
                )
                
                val totalRam = if (specs.ramUsedMb + specs.ramFreeMb > 0) specs.ramUsedMb + specs.ramFreeMb else 1
                val ramProgress = specs.ramUsedMb.toFloat() / totalRam.toFloat()
                ArcGauge(
                    progress = ramProgress,
                    color = modeColor,
                    label = "RAM",
                    valueText = "${(ramProgress * 100).toInt()}%",
                    size = 110.dp
                )
                
                val batColor = when {
                    specs.batteryTemperature > 42f -> ErrorRed
                    specs.batteryLevel < 20 -> WarningOrange
                    else -> SuccessGreen
                }
                ArcGauge(
                    progress = specs.batteryLevel / 100f,
                    color = batColor,
                    label = "${specs.batteryTemperature}°C",
                    valueText = "${specs.batteryLevel}%",
                    size = 110.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toggles
            AngledCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                borderColor = modeColor.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("System Toggles", color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Block Notifications (DND)", color = TextSecondary)
                        Switch(
                            checked = isDndEnabled,
                            onCheckedChange = viewModel::toggleDnd,
                            colors = SwitchDefaults.colors(checkedThumbColor = modeColor, checkedTrackColor = modeColor.copy(alpha = 0.4f))
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SurfaceVariantDark)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Lock Brightness", color = TextSecondary)
                        Switch(
                            checked = isBrightnessLocked,
                            onCheckedChange = viewModel::toggleBrightnessLock,
                            colors = SwitchDefaults.colors(checkedThumbColor = modeColor, checkedTrackColor = modeColor.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameCoverCard(
    game: GameModel,
    accentColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val icon = remember(game.packageName) {
        try {
            pm.getApplicationIcon(game.packageName).toBitmap().asImageBitmap()
        } catch (e: Exception) { null }
    }

    Box(
        modifier = Modifier
            .width(140.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(2.dp, accentColor.copy(alpha = if (game.highPerformanceMode) 1f else 0f), RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = game.name,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp).background(SurfaceVariantDark, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎮", fontSize = 48.sp)
            }
        }
        
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 100f
                    )
                )
        )
        
        Text(
            text = game.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )
    }
}

@Composable
fun SegmentedModeSelector(
    currentMode: PerformanceMode,
    onModeSelected: (PerformanceMode) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark)
            .padding(4.dp)
    ) {
        PerformanceMode.values().forEach { mode ->
            val isSelected = currentMode == mode
            val modeColor = getModeColor(mode)
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) modeColor.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.name,
                    color = if (isSelected) modeColor else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

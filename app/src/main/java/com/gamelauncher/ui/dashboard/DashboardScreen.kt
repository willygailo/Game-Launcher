// app/src/main/java/com/gamelauncher/ui/dashboard/DashboardScreen.kt
package com.gamelauncher.ui.dashboard

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.ui.components.ArcGauge
import com.gamelauncher.ui.components.HexagonButton
import com.gamelauncher.ui.components.RogArmorCard
import com.gamelauncher.ui.games.GamesViewModel
import com.gamelauncher.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    gamesViewModel: GamesViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToTweaks: () -> Unit = {},
    onNavigateToNetwork: () -> Unit = {},
    onNavigateToMonitor: () -> Unit = {},
    onNavigateToGameDetails: (String) -> Unit = {}
) {
    val specs by viewModel.deviceSpecs.collectAsStateWithLifecycle()
    val isDndEnabled by viewModel.isDndEnabled.collectAsStateWithLifecycle()
    val isBrightnessLocked by viewModel.isBrightnessLocked.collectAsStateWithLifecycle()
    val isGpuRenderingEnabled by viewModel.isGpuRenderingEnabled.collectAsStateWithLifecycle()
    val gamesUiState by gamesViewModel.uiState.collectAsStateWithLifecycle()

    val currentMode by viewModel.performanceMode.collectAsStateWithLifecycle()
    val modeColor = getModeColor(currentMode)

    LifecycleResumeEffect(Unit) {
        viewModel.refreshPermissionStates()
        gamesViewModel.refreshGames()
        onPauseOrDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DockBgDark,
                        Color(0xFF060709)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cyber HUD Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(modeColor, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "GAME LAUNCHER PRO",
                            color = TextPrimary,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        specs.socName.takeIf { it.isNotBlank() } ?: "SYSTEM HARDWARE MONITOR ACTIVE",
                        color = modeColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(modeColor.copy(alpha = 0.15f))
                        .border(1.dp, modeColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = modeColor)
                }
            }

            // Game Library Carousel
            if (gamesUiState.filteredGames.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
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
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No games detected. Scanning library...", color = TextSecondary, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Central Reactor Core Boost Hub
            HexagonButton(
                text = "BOOST",
                color = modeColor,
                onClick = { viewModel.optimizeRam() }
            )

            if (specs.freedRamMb > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "✓ FREED ${specs.freedRamMb} MB RAM",
                    color = SuccessGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }



            // Telemetry Gauge HUD Cluster
            RogArmorCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                accentColor = modeColor
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "HARDWARE TELEMETRY HUD",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArcGauge(
                            progress = specs.cpuUsagePercent / 100f,
                            color = modeColor,
                            label = "CPU UTIL",
                            valueText = "${specs.cpuUsagePercent.toInt()}%"
                        )

                        val totalRam = if (specs.ramUsedMb + specs.ramFreeMb > 0) specs.ramUsedMb + specs.ramFreeMb else 1
                        val ramProgress = specs.ramUsedMb.toFloat() / totalRam.toFloat()
                        ArcGauge(
                            progress = ramProgress,
                            color = modeColor,
                            label = "RAM PRESS",
                            valueText = "${(ramProgress * 100).toInt()}%"
                        )

                        val batColor = when {
                            specs.batteryTemperature > 42f -> ErrorRed
                            specs.batteryLevel < 20 -> WarningOrange
                            else -> SuccessGreen
                        }
                        ArcGauge(
                            progress = specs.batteryLevel / 100f,
                            color = batColor,
                            label = "TEMP",
                            valueText = "${specs.batteryTemperature}°C"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Booster Modules Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToTweaks,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = modeColor.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, modeColor.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                ) {
                    Text("⚡ TWEAKS", color = modeColor, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                }

                Button(
                    onClick = onNavigateToNetwork,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = modeColor.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, modeColor.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                ) {
                    Text("🌐 NETWORK", color = modeColor, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                }

                Button(
                    onClick = onNavigateToMonitor,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = modeColor.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, modeColor.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                ) {
                    Text("📊 FPS HUD", color = modeColor, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // System Control Toggles Armor Card
            RogArmorCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                accentColor = modeColor
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "SYSTEM CONTROL TOGGLES",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Block Notifications (DND)", color = TextSecondary, fontSize = 13.sp)
                        Switch(
                            checked = isDndEnabled,
                            onCheckedChange = viewModel::toggleDnd,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = modeColor
                            )
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SurfaceVariantDark)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lock Display Brightness", color = TextSecondary, fontSize = 13.sp)
                        Switch(
                            checked = isBrightnessLocked,
                            onCheckedChange = viewModel::toggleBrightnessLock,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = modeColor
                            )
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SurfaceVariantDark)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Force GPU Acceleration", color = TextSecondary, fontSize = 13.sp)
                        Switch(
                            checked = isGpuRenderingEnabled,
                            onCheckedChange = viewModel::toggleGpuRendering,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = modeColor
                            )
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
            .height(190.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DockSurfaceDark)
            .border(2.dp, if (game.highPerformanceMode) accentColor else accentColor.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .background(SurfaceVariantDark, RoundedCornerShape(10.dp)),
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
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                        startY = 90f
                    )
                )
        )

        Text(
            text = game.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )
    }
}



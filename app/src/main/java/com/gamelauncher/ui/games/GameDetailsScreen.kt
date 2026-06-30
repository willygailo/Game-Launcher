package com.gamelauncher.ui.games

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.ui.components.PrimaryGradientButton
import com.gamelauncher.ui.components.StatusBadge
import com.gamelauncher.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsScreen(
    packageName: String,
    onBack: () -> Unit,
    viewModel: GameDetailsViewModel = hiltViewModel()
) {
    val game by viewModel.game.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val totalPlayTime by viewModel.totalPlayTime.collectAsStateWithLifecycle()
    val averageFps by viewModel.averageFps.collectAsStateWithLifecycle()
    val sessionCount by viewModel.sessionCount.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(packageName) { viewModel.loadGameDetails(packageName) }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(game?.name ?: "Game Details", color = TextPrimary, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryNeon)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            game?.let { currentGame ->
                item {
                    GameDetailsHeader(
                        game = currentGame,
                        onLaunch = viewModel::launchGame,
                        onBoostToggle = { enabled ->
                            viewModel.updateGame(
                                if (enabled) {
                                    currentGame.copy(
                                        highPerformanceMode = true,
                                        graphicsMode = "PERFORMANCE",
                                        forceMaxRefreshRate = true,
                                        touchLatencyBoost = true,
                                        gpuTuning = true,
                                        wifiLockEnabled = true,
                                        ramAggressiveness = "AGGRESSIVE"
                                    )
                                } else {
                                    currentGame.copy(highPerformanceMode = false)
                                }
                            )
                        }
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Sessions", "$sessionCount", SecondaryNeon, Modifier.weight(1f))
                    StatCard("Play Time", "${totalPlayTime}m", TertiaryAccent, Modifier.weight(1f))
                    StatCard("Avg FPS", if (averageFps > 0) "${averageFps.toInt()}" else "--", PrimaryNeon, Modifier.weight(1f))
                }
            }

            game?.let { currentGame ->
                item {
                    BoostProfileSummary(game = currentGame)
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Session History", color = SecondaryNeon, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${sessions.size} recorded sessions", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (sessions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No sessions recorded yet", color = TextSecondary)
                    }
                }
            }

            items(sessions) { session ->
                SessionCard(session)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun GameDetailsHeader(
    game: GameModel,
    onLaunch: () -> Unit,
    onBoostToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val icon = remember(game.packageName) {
        try {
            context.packageManager.getApplicationIcon(game.packageName).toBitmap().asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryNeon.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Image(
                        bitmap = icon,
                        contentDescription = game.name,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(SurfaceVariantDark, RoundedCornerShape(12.dp))
                            .border(1.dp, SurfaceVariantDark, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("GAME", color = TextSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(game.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(game.packageName, color = TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusBadge(
                        text = if (game.highPerformanceMode) "BOOST ON" else "BOOST OFF",
                        color = if (game.highPerformanceMode) PrimaryNeon else TextSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrimaryGradientButton(
                    text = "Launch Game",
                    onClick = onLaunch,
                    icon = Icons.Default.PlayArrow,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = game.highPerformanceMode,
                    onCheckedChange = onBoostToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PrimaryNeon,
                        checkedTrackColor = PrimaryNeon.copy(alpha = 0.45f),
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = SurfaceVariantDark
                    )
                )
            }
        }
    }
}

@Composable
private fun BoostProfileSummary(game: GameModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryNeon.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Boost Profile", color = SecondaryNeon, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileChip("${game.targetFps} FPS", PrimaryNeon, Modifier.weight(1f))
                ProfileChip(game.ramAggressiveness, WarningOrange, Modifier.weight(1f))
                ProfileChip(game.graphicsMode, SecondaryNeon, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(
                    text = if (game.forceMaxRefreshRate) "MAX HZ" else "AUTO HZ",
                    color = if (game.forceMaxRefreshRate) SecondaryNeon else TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(
                    text = if (game.touchLatencyBoost) "TOUCH+" else "TOUCH AUTO",
                    color = if (game.touchLatencyBoost) TertiaryAccent else TextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProfileChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun SessionCard(session: com.gamelauncher.data.model.GamingSession) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val durationMinutes = session.durationMs / 60_000
    val durationSeconds = (session.durationMs % 60_000) / 1000

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(dateFormat.format(Date(session.startTime)), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                if (session.wasBoosted) Text("Boosted", color = PrimaryNeon, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("${durationMinutes}m ${durationSeconds}s", color = TextPrimary, fontWeight = FontWeight.Bold)
                if (session.avgFps > 0) Text("${session.avgFps} FPS", color = if (session.avgFps >= 55) SuccessGreen else WarningOrange)
                if (session.batteryDrain > 0) Text("-${session.batteryDrain}% batt", color = ErrorRed)
            }
        }
    }
}

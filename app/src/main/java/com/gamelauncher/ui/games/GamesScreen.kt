package com.gamelauncher.ui.games

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.os.Build
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
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SurfaceDark, BackgroundDark)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Games",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${filteredGames.size} games detected",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryNeon.copy(alpha = 0.7f)
                    )
                }
                IconButton(
                    onClick = viewModel::refreshGames,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceVariantDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh games",
                        tint = PrimaryNeon
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search games...", color = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
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

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            if (availableCategories.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCategories.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedCategory(category) },
                            label = {
                                Text(
                                    category,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryNeon.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryNeon,
                                containerColor = SurfaceDark,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = PrimaryNeon.copy(alpha = 0.6f),
                                borderColor = SurfaceVariantDark
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Content
            if (isScanning) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryNeon, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Scanning installed games...", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else if (filteredGames.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎮", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedCategory != "All") "No matching games" else "No games detected yet",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedCategory != "All") "Try a different search or filter" else "Tap refresh after installing games",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = viewModel::refreshGames,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryNeon),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryNeon.copy(alpha = 0.5f))
                        ) {
                            Text("Scan Games")
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.startService(intent)
                                    }
                                }
                                viewModel.launchGame(game)
                            },
                            onUpdate = { viewModel.updateGameSettings(it) },
                            onDetails = {
                                navController?.navigate("${Screen.GameDetails.route}/${game.packageName}")
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
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
        } catch (e: Exception) { null }
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

    val neonBorderColor = if (game.highPerformanceMode) PrimaryNeon else SurfaceVariantDark

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = neonBorderColor.copy(alpha = if (game.highPerformanceMode) 0.6f else 0.2f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Main row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Game icon
                if (icon != null) {
                    Image(
                        bitmap = icon,
                        contentDescription = game.name,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(SurfaceVariantDark, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎮", fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(game.name, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text(game.customCategory, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    if (game.highPerformanceMode) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            MiniTag("${game.targetFps}FPS", PrimaryNeon)
                            if (game.forceMaxRefreshRate) MiniTag("MAX Hz", SecondaryNeon)
                            if (game.touchLatencyBoost) MiniTag("TOUCH+", TertiaryAccent)
                        }
                    }
                }

                if (onDetails != null) {
                    IconButton(onClick = onDetails, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Game details",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Play Button with gradient
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (game.highPerformanceMode)
                                Brush.horizontalGradient(listOf(PrimaryNeon, SecondaryNeon.copy(alpha = 0.8f)))
                            else
                                Brush.horizontalGradient(listOf(SurfaceVariantDark, SurfaceVariantDark))
                        )
                        .clickable { onLaunch() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "PLAY",
                        color = if (game.highPerformanceMode) BackgroundDark else TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }

            // Divider with expand indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(SurfaceVariantDark))
                Text(
                    if (expanded) "▲ Boost Settings" else "▼ Boost Settings",
                    color = WarningOrange,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Box(modifier = Modifier.weight(1f).height(1.dp).background(SurfaceVariantDark))
            }

            // Expanded settings panel
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(250)),
                exit = shrinkVertically(animationSpec = tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundDark.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Section: Boost Controls
                    Text("⚡ Boost Controls", color = WarningOrange, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)

                    BoostToggleRow(
                        icon = Icons.Default.Speed,
                        label = "Auto Boost on Launch",
                        subtitle = "Apply all settings on game start",
                        checked = game.highPerformanceMode,
                        color = PrimaryNeon,
                        onCheckedChange = { onUpdate(game.copy(highPerformanceMode = it)) }
                    )

                    BoostToggleRow(
                        icon = Icons.Default.GraphicEq,
                        label = "Force Max Refresh Rate",
                        subtitle = "Lock display to highest Hz (e.g. 144/165/240Hz)",
                        checked = game.forceMaxRefreshRate,
                        color = SecondaryNeon,
                        onCheckedChange = { onUpdate(game.copy(forceMaxRefreshRate = it)) }
                    )

                    BoostToggleRow(
                        icon = Icons.Default.TouchApp,
                        label = "Touch Latency Boost",
                        subtitle = "Ultra-low input latency, high poll rate",
                        checked = game.touchLatencyBoost,
                        color = TertiaryAccent,
                        onCheckedChange = { onUpdate(game.copy(touchLatencyBoost = it)) }
                    )

                    BoostToggleRow(
                        icon = Icons.Default.Memory,
                        label = "GPU Performance Tuning",
                        subtitle = "Force GPU governor to performance mode",
                        checked = game.gpuTuning,
                        color = SuccessGreen,
                        onCheckedChange = { onUpdate(game.copy(gpuTuning = it)) }
                    )

                    BoostToggleRow(
                        icon = Icons.Default.Refresh,
                        label = "Network Low-Latency Lock",
                        subtitle = "WiFi low-latency mode for online games",
                        checked = game.wifiLockEnabled,
                        color = PrimaryNeon,
                        onCheckedChange = { onUpdate(game.copy(wifiLockEnabled = it)) }
                    )

                    // RAM Aggressiveness
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🧹 RAM Cleanup Aggressiveness", color = TextSecondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("LIGHT", "NORMAL", "AGGRESSIVE", "EXTREME").forEach { mode ->
                            val isSelected = game.ramAggressiveness == mode
                            val chipColor = when (mode) {
                                "LIGHT" -> SuccessGreen
                                "NORMAL" -> PrimaryNeon
                                "AGGRESSIVE" -> WarningOrange
                                "EXTREME" -> TertiaryAccent
                                else -> TextSecondary
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdate(game.copy(ramAggressiveness = mode)) },
                                label = {
                                    Text(
                                        mode,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = chipColor.copy(alpha = 0.25f),
                                    selectedLabelColor = chipColor,
                                    containerColor = SurfaceDark,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = chipColor.copy(alpha = 0.7f),
                                    borderColor = SurfaceVariantDark
                                )
                            )
                        }
                    }

                    // Graphics Mode
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🎨 Graphics Mode", color = TextSecondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        com.gamelauncher.data.model.GraphicsMode.entries.forEach { mode ->
                            val isSelected = game.graphicsMode == mode.name
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdate(game.copy(graphicsMode = mode.name)) },
                                label = { Text(mode.displayName, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryNeon.copy(alpha = 0.25f),
                                    selectedLabelColor = SecondaryNeon,
                                    containerColor = SurfaceDark,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    // FPS Slider
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎯 Target FPS", color = TextSecondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${game.targetFps} FPS",
                            color = PrimaryNeon,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Slider(
                        value = game.targetFps.toFloat(),
                        onValueChange = { onUpdate(game.copy(targetFps = it.toInt())) },
                        valueRange = 30f..gameMaxFps.toFloat(),
                        steps = ((gameMaxFps - 30) / 15),
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryNeon,
                            activeTrackColor = PrimaryNeon,
                            inactiveTrackColor = SurfaceVariantDark
                        )
                    )
                    Text(
                        "Device max: ${deviceMaxFps}Hz | Game max: ${gameMaxFps}FPS",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun BoostToggleRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    checked: Boolean,
    color: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = color,
                checkedTrackColor = color.copy(alpha = 0.4f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceVariantDark
            )
        )
    }
}

@Composable
fun MiniTag(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

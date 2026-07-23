// feature/tweaks/src/main/java/com/gamelauncher/feature/tweaks/ui/TweaksScreen.kt
package com.gamelauncher.feature.tweaks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamelauncher.core.shizuku.ShizukuState
import com.gamelauncher.feature.tweaks.domain.model.TweakCategory
import com.gamelauncher.feature.tweaks.domain.model.TweakItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TweaksScreen(
    viewModel: TweaksViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val shizukuState by viewModel.shizukuState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedCategoryFilter by remember { mutableStateOf<TweakCategory?>(null) }

    val darkBackground = Color(0xFF0B0F19)
    val cardBackground = Color(0xFF151C2C)
    val accentNeonRed = Color(0xFFFF2A5F)
    val accentNeonBlue = Color(0xFF00E5FF)
    val accentGreen = Color(0xFF00E676)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ROG GAME SPACE TUNING",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Shizuku + ADB Secure Performance Engine",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBackground)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = darkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is TweaksUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = accentNeonBlue
                    )
                }

                is TweaksUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading tweaks: ${state.message}",
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { viewModel.loadTweaks() }) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }

                is TweaksUiState.Success -> {
                    state.userMessage?.let { message ->
                        LaunchedEffect(message) {
                            snackbarHostState.showSnackbar(message)
                            viewModel.clearUserMessage()
                        }
                    }

                    val filteredTweaks = remember(state.tweaks, selectedCategoryFilter) {
                        if (selectedCategoryFilter == null) state.tweaks
                        else state.tweaks.filter { it.category == selectedCategoryFilter }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // ── Engine Active Banner ──────────────────────────────
                        item {
                            val shizukuActive = shizukuState is ShizukuState.Connected
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (shizukuActive) accentNeonBlue.copy(alpha = 0.6f) else Color(0xFF475569)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (shizukuActive) "⚡ ENGINE: SHIZUKU ACTIVE" else "🔓 ENGINE: ADB WRITE SECURE / DUAL-MODE",
                                        color = if (shizukuActive) accentNeonBlue else accentGreen,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (shizukuActive) accentNeonBlue.copy(alpha = 0.2f) else accentGreen.copy(alpha = 0.2f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "UNLOCKED",
                                            color = if (shizukuActive) accentNeonBlue else accentGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }

                        // ── ROG Armoury Mode Preset Header ──────────────────────
                        item {
                            val currentRogMode = state.tweaks.find { it.id == "rog_armoury_mode" }?.selectedValue ?: "Dynamic"
                            RogArmouryHeaderCard(
                                selectedMode = currentRogMode,
                                onSelectMode = { modeName ->
                                    viewModel.applyRogArmouryMode(modeName)
                                },
                                accentNeonRed = accentNeonRed,
                                accentNeonBlue = accentNeonBlue,
                                accentGreen = accentGreen
                            )
                        }


                        // ── Category Filter Chips ─────────────────────────────
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChipItem(
                                        text = "ALL",
                                        isSelected = selectedCategoryFilter == null,
                                        onClick = { selectedCategoryFilter = null },
                                        accentColor = accentNeonBlue
                                    )
                                }
                                item {
                                    FilterChipItem(
                                        text = "120/144/165 HZ & FPS",
                                        isSelected = selectedCategoryFilter == TweakCategory.REFRESH_RATE || selectedCategoryFilter == TweakCategory.FPS_UNLOCK,
                                        onClick = { selectedCategoryFilter = TweakCategory.REFRESH_RATE },
                                        accentColor = accentNeonRed
                                    )
                                }
                                item {
                                    FilterChipItem(
                                        text = "TOUCH ULTRA",
                                        isSelected = selectedCategoryFilter == TweakCategory.TOUCH,
                                        onClick = { selectedCategoryFilter = TweakCategory.TOUCH },
                                        accentColor = accentNeonBlue
                                    )
                                }
                                item {
                                    FilterChipItem(
                                        text = "GPU & CPU",
                                        isSelected = selectedCategoryFilter == TweakCategory.GPU_RENDERING || selectedCategoryFilter == TweakCategory.CPU_PERFORMANCE,
                                        onClick = { selectedCategoryFilter = TweakCategory.GPU_RENDERING },
                                        accentColor = accentGreen
                                    )
                                }
                                item {
                                    FilterChipItem(
                                        text = "NETWORK SPEED",
                                        isSelected = selectedCategoryFilter == TweakCategory.NETWORK_SPEED,
                                        onClick = { selectedCategoryFilter = TweakCategory.NETWORK_SPEED },
                                        accentColor = accentNeonBlue
                                    )
                                }
                            }
                        }

                        // ── Tweak Cards List ──────────────────────────────────
                        items(filteredTweaks, key = { it.id }) { tweak ->
                            TweakCardItem(
                                tweak = tweak,
                                cardBackground = cardBackground,
                                accentColor = accentNeonBlue,
                                onRefreshRateSelected = { viewModel.applyRefreshRate(it) },
                                onFpsUnlockSelected = { viewModel.applyFpsUnlock(it) },
                                onClearHighRefreshRateBlacklist = { viewModel.clearHighRefreshRateBlacklist() },
                                onGpuRenderingToggled = { viewModel.applyGpuRendering(it) },
                                onCpuBoostToggled = { viewModel.applyCpuPerformanceBoost(it) },
                                onTouchUltraToggled = { viewModel.applyTouchUltra(it) },
                                onSuperFastLaunchClicked = { viewModel.applySuperFastLaunch() },
                                onNetworkSpeedToggled = { viewModel.applyNetworkSpeedBoost(it) },
                                onClearGameDriver = { viewModel.clearGameDriverConfig() },
                                onThermalBypassToggled = { viewModel.applyThermalThrottlingBypass(it) },
                                onGameModeToggled = { viewModel.applyGameModeBooster(it) },
                                onPhantomProcsToggled = { viewModel.disablePhantomProcessKilling(it) },
                                onAdaptiveBatteryToggled = { viewModel.disableAdaptiveBattery(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RogArmouryHeaderCard(
    selectedMode: String,
    onSelectMode: (String) -> Unit,
    accentNeonRed: Color,
    accentNeonBlue: Color,
    accentGreen: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accentNeonRed.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🔥 ROG GAME SPACE MODES",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = accentNeonRed,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "ONE-TAP TUNER",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // X-Mode Button
                val isXMode = selectedMode == "X-Mode"
                Button(
                    onClick = { onSelectMode("X-Mode") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isXMode) accentNeonRed else Color(0xFF0F172A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isXMode) 2.dp else 1.dp,
                        if (isXMode) accentNeonRed else Color(0xFF334155)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔴 X-MODE", fontWeight = FontWeight.Black, fontSize = 12.sp, color = if (isXMode) Color.White else accentNeonRed)
                        Text("165 FPS ULTRA", fontSize = 9.sp, color = if (isXMode) Color.White.copy(alpha = 0.9f) else Color(0xFF94A3B8))
                    }
                }

                // Dynamic Mode Button
                val isDynamic = selectedMode == "Dynamic"
                Button(
                    onClick = { onSelectMode("Dynamic") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDynamic) accentNeonBlue else Color(0xFF0F172A),
                        contentColor = if (isDynamic) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isDynamic) 2.dp else 1.dp,
                        if (isDynamic) accentNeonBlue else Color(0xFF334155)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔵 DYNAMIC", fontWeight = FontWeight.Black, fontSize = 12.sp, color = if (isDynamic) Color.Black else accentNeonBlue)
                        Text("BALANCED 120Hz", fontSize = 9.sp, color = if (isDynamic) Color.Black.copy(alpha = 0.8f) else Color(0xFF94A3B8))
                    }
                }

                // Esports Mode Button
                val isEsports = selectedMode == "Esports"
                Button(
                    onClick = { onSelectMode("Esports") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEsports) accentGreen else Color(0xFF0F172A),
                        contentColor = if (isEsports) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isEsports) 2.dp else 1.dp,
                        if (isEsports) accentGreen else Color(0xFF334155)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🟢 ESPORTS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = if (isEsports) Color.Black else accentGreen)
                        Text("STABLE 60Hz", fontSize = 9.sp, color = if (isEsports) Color.Black.copy(alpha = 0.8f) else Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) accentColor else Color(0xFF1E293B))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else Color.White
        )
    }
}

@Composable
fun TweakCardItem(
    tweak: TweakItem,
    cardBackground: Color,
    accentColor: Color,
    onRefreshRateSelected: (Float) -> Unit,
    onFpsUnlockSelected: (String) -> Unit,
    onClearHighRefreshRateBlacklist: () -> Unit,
    onGpuRenderingToggled: (Boolean) -> Unit,
    onCpuBoostToggled: (Boolean) -> Unit,
    onTouchUltraToggled: (Boolean) -> Unit,
    onSuperFastLaunchClicked: () -> Unit,
    onNetworkSpeedToggled: (Boolean) -> Unit,
    onClearGameDriver: () -> Unit,
    onThermalBypassToggled: (Boolean) -> Unit,
    onGameModeToggled: (Boolean) -> Unit,
    onPhantomProcsToggled: (Boolean) -> Unit,
    onAdaptiveBatteryToggled: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tweak.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                if (!tweak.isSupportedByDevice) {
                    val labelText = tweak.badgeNote ?: "Unsupported"
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF334155), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = labelText,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tweak.description,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (tweak.category) {
                TweakCategory.TOUCH -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (tweak.isToggleActive) "Ultra Latency Active (Pointer Max)" else "Standard Touch Response",
                            fontSize = 12.sp,
                            color = if (tweak.isToggleActive) accentColor else Color(0xFF94A3B8)
                        )
                        Switch(
                            checked = tweak.isToggleActive,
                            onCheckedChange = onTouchUltraToggled,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor, uncheckedThumbColor = Color(0xFF94A3B8), uncheckedTrackColor = Color(0xFF151C2C))
                        )
                    }
                }

                TweakCategory.SUPER_FAST_LAUNCH -> {
                    Button(
                        onClick = onSuperFastLaunchClicked,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🚀  Purge RAM & Accelerate Launch", fontWeight = FontWeight.Bold)
                    }
                }

                TweakCategory.REFRESH_RATE -> {
                    if (tweak.id == "high_refresh_rate_blacklist") {
                        Button(
                            onClick = onClearHighRefreshRateBlacklist,
                            enabled = tweak.isSupportedByDevice,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Clear High Refresh Rate Blacklist")
                        }
                    } else if (tweak.isSupportedByDevice) {
                        DropdownSelector(
                            label = "Target Hz",
                            currentValue = tweak.selectedValue ?: "165",
                            options = tweak.supportedValues,
                            onOptionSelected = { value ->
                                value.toFloatOrNull()?.let { onRefreshRateSelected(it) }
                            }
                        )
                    }
                }

                TweakCategory.FPS_UNLOCK -> {
                    DropdownSelector(
                        label = "Unlocked Target FPS",
                        currentValue = tweak.selectedValue ?: "165",
                        options = tweak.supportedValues,
                        onOptionSelected = onFpsUnlockSelected
                    )
                }

                TweakCategory.GPU_RENDERING -> {
                    if (tweak.id == "game_driver_clear") {
                        Button(
                            onClick = onClearGameDriver,
                            enabled = tweak.isSupportedByDevice,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset Game Driver Settings")
                        }
                    } else if (tweak.isSupportedByDevice) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (tweak.isToggleActive) "2D GPU & Skia Vulkan HW Active" else "Standard UI Rendering",
                                fontSize = 12.sp,
                                color = if (tweak.isToggleActive) accentColor else Color(0xFF94A3B8)
                            )
                            Switch(
                                checked = tweak.isToggleActive,
                                onCheckedChange = onGpuRenderingToggled,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor, uncheckedThumbColor = Color(0xFF94A3B8), uncheckedTrackColor = Color(0xFF151C2C))
                            )
                        }
                    }
                }

                TweakCategory.CPU_PERFORMANCE -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (tweak.isToggleActive) "CPU Max Governor Boost Active" else "Standard Power Scaling",
                            fontSize = 12.sp,
                            color = if (tweak.isToggleActive) accentColor else Color(0xFF94A3B8)
                        )
                        Switch(
                            checked = tweak.isToggleActive,
                            onCheckedChange = onCpuBoostToggled,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor, uncheckedThumbColor = Color(0xFF94A3B8), uncheckedTrackColor = Color(0xFF151C2C))
                        )
                    }
                }

                TweakCategory.NETWORK_SPEED -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (tweak.isToggleActive) "Low Latency Wi-Fi & Mobile Data On" else "Standard Wi-Fi Mode",
                            fontSize = 12.sp,
                            color = if (tweak.isToggleActive) accentColor else Color(0xFF94A3B8)
                        )
                        Switch(
                            checked = tweak.isToggleActive,
                            onCheckedChange = onNetworkSpeedToggled,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor, uncheckedThumbColor = Color(0xFF94A3B8), uncheckedTrackColor = Color(0xFF151C2C))
                        )
                    }
                }

                TweakCategory.THERMAL_THROTTLING -> {
                    if (tweak.isSupportedByDevice) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (tweak.isToggleActive) "Thermal Override Active" else "Standard Thermal Throttling",
                                fontSize = 12.sp,
                                color = if (tweak.isToggleActive) accentColor else Color(0xFF94A3B8)
                            )
                            Switch(
                                checked = tweak.isToggleActive,
                                onCheckedChange = onThermalBypassToggled,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor, uncheckedThumbColor = Color(0xFF94A3B8), uncheckedTrackColor = Color(0xFF151C2C))
                            )
                        }
                    }
                }

                TweakCategory.GAME_MODE -> {
                    if (tweak.isSupportedByDevice) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (tweak.isToggleActive) "Booster Enabled" else "Booster Disabled",
                                fontSize = 12.sp,
                                color = if (tweak.isToggleActive) accentColor else Color(0xFF94A3B8)
                            )
                            Switch(
                                checked = tweak.isToggleActive,
                                onCheckedChange = onGameModeToggled,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor, uncheckedThumbColor = Color(0xFF94A3B8), uncheckedTrackColor = Color(0xFF151C2C))
                            )
                        }
                    }
                }

                TweakCategory.MEMORY -> {
                    if (tweak.isSupportedByDevice) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (tweak.isToggleActive) "Phantom Killer Disabled" else "Standard Android Monitor",
                                fontSize = 12.sp,
                                color = if (tweak.isToggleActive) accentColor else Color(0xFF94A3B8)
                            )
                            Switch(
                                checked = tweak.isToggleActive,
                                onCheckedChange = onPhantomProcsToggled,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor, uncheckedThumbColor = Color(0xFF94A3B8), uncheckedTrackColor = Color(0xFF151C2C))
                            )
                        }
                    }
                }

                TweakCategory.POWER -> {
                    if (tweak.isSupportedByDevice) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (tweak.isToggleActive) "Battery Throttling Disabled" else "Adaptive Battery Active",
                                fontSize = 12.sp,
                                color = if (tweak.isToggleActive) accentColor else Color(0xFF94A3B8)
                            )
                            Switch(
                                checked = tweak.isToggleActive,
                                onCheckedChange = onAdaptiveBatteryToggled,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor, uncheckedThumbColor = Color(0xFF94A3B8), uncheckedTrackColor = Color(0xFF151C2C))
                            )
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun DropdownSelector(
    label: String,
    currentValue: String,
    options: List<String>,
    enabled: Boolean = true,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.alpha(if (enabled) 1.0f else 0.5f)) {
        OutlinedButton(
            onClick = { if (enabled) expanded = true },
            enabled = enabled,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "$label: $currentValue", color = if (enabled) Color.White else Color(0xFF94A3B8), fontSize = 12.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color(0xFF151C2C))
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            containerColor = Color(0xFF151C2C),
            shadowElevation = 8.dp
        ) {
            options.forEach { option ->
                val isSelected = option == currentValue
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = Color.White
                    ),
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    }
                )
            }
        }

    }
}

package com.gamelauncher.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.gamelauncher.data.model.DeviceSpecs
import com.gamelauncher.ui.components.PrimaryGradientButton
import com.gamelauncher.ui.components.ScreenHeader
import com.gamelauncher.ui.components.StatusBadge
import com.gamelauncher.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val specs by viewModel.deviceSpecs.collectAsStateWithLifecycle()
    val isDndEnabled by viewModel.isDndEnabled.collectAsStateWithLifecycle()
    val isBrightnessLocked by viewModel.isBrightnessLocked.collectAsStateWithLifecycle()
    val brightnessLevel by viewModel.brightnessLevel.collectAsStateWithLifecycle()
    val isRootAvailable by viewModel.isRootAvailable.collectAsStateWithLifecycle()
    val totalSessions by viewModel.totalSessions.collectAsStateWithLifecycle()
    val totalPlayTimeMinutes by viewModel.totalPlayTimeMinutes.collectAsStateWithLifecycle()
    val benchmarkResult by viewModel.benchmarkResult.collectAsStateWithLifecycle()
    val isBenchmarking by viewModel.isBenchmarking.collectAsStateWithLifecycle()
    val hasWriteSecure by viewModel.hasWriteSecureSettings.collectAsStateWithLifecycle()
    var advancedExpanded by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        viewModel.refreshPermissionStates()
        onPauseOrDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val deviceTierLabel = remember(specs.deviceRating) {
            when {
                specs.deviceRating >= 9 -> "Elite gaming device"
                specs.deviceRating >= 7 -> "High-performance device"
                specs.deviceRating >= 5 -> "Mid-range gaming profile"
                else -> "Entry-level device profile"
            }
        }
        val accessMode = when {
            isRootAvailable -> "ROOT"
            hasWriteSecure -> "ADB+"
            else -> "STANDARD"
        }
        val accessColor = when {
            isRootAvailable -> SuccessGreen
            hasWriteSecure -> PrimaryNeon
            else -> TextSecondary
        }
        val thermalColor = when {
            specs.batteryTemperature >= 43f || specs.thermalStatus >= 3 -> ErrorRed
            specs.batteryTemperature >= 39f || specs.thermalStatus == 2 -> WarningOrange
            else -> SuccessGreen
        }

        ScreenHeader(
            title = "System Monitor",
            subtitle = deviceTierLabel,
            trailing = {
                StatusBadge(text = accessMode, color = accessColor)
            }
        )

        DashboardCockpitCard(
            specs = specs,
            accessMode = accessMode,
            thermalColor = thermalColor
        )

        PrimaryGradientButton(
            text = "Optimize Now",
            onClick = { viewModel.optimizeRam() },
            icon = Icons.Default.Memory,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )

        // RAM freed banner
        if (specs.freedRamMb > 0) {
            DashboardNotice(
                title = "Memory optimized",
                message = "Freed ${specs.freedRamMb} MB of RAM",
                color = SuccessGreen
            )
        }

        // Advanced secure boosts banner
        if (hasWriteSecure || isRootAvailable) {
            DashboardNotice(
                title = if (isRootAvailable) "Root mode active" else "Advanced mode active",
                message = if (isRootAvailable) {
                    "System-level performance controls are available."
                } else {
                    "ADB secure tweaks are unlocked for deeper optimization."
                },
                color = if (isRootAvailable) SuccessGreen else PrimaryNeon
            )
        }

        // ── Gauge Row: CPU & GPU ───────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GaugeCard(
                title = "CPU",
                percent = specs.cpuUsagePercent / 100f,
                value = "${specs.cpuUsagePercent.toInt()}%",
                subtitle = "${specs.cpuFreqMhz} MHz · ${specs.cpuGovernor}",
                color = TertiaryAccent,
                modifier = Modifier.weight(1f)
            )
            GaugeCard(
                title = "GPU",
                percent = specs.gpuUsagePercent / 100f,
                value = if (specs.gpuFreqMhz > 0) "${specs.gpuFreqMhz} MHz" else "Ready",
                subtitle = specs.gpuRenderer.take(20),
                color = SecondaryNeon,
                modifier = Modifier.weight(1f)
            )
        }

        // ── Progress Bar Row: RAM & Battery ───────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val totalRam = if (specs.ramUsedMb + specs.ramFreeMb > 0) specs.ramUsedMb + specs.ramFreeMb else 1
            ProgressMetricCard(
                title = "RAM",
                value = "${specs.ramUsedMb} MB used",
                subtitle = "${specs.ramFreeMb} MB free",
                progress = specs.ramUsedMb.toFloat() / totalRam.toFloat(),
                color = PrimaryNeon,
                modifier = Modifier.weight(1f)
            )
            val batColor = when {
                specs.batteryTemperature > 42f -> ErrorRed
                specs.batteryLevel < 20 -> WarningOrange
                else -> SuccessGreen
            }
            ProgressMetricCard(
                title = "Battery",
                value = "${specs.batteryLevel}%",
                subtitle = "${specs.batteryTemperature}°C · ${specs.batteryHealth}",
                progress = specs.batteryLevel / 100f,
                color = batColor,
                modifier = Modifier.weight(1f)
            )
        }

        // ── Display / FPS / Hz ─────────────────────────────────
        val maxHz = specs.supportedRefreshRates.maxOrNull()?.toInt() ?: 60
        val currentHz = specs.displayRefreshRateHz.toInt()
        val currentFps = specs.currentFps.toInt()

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryNeon.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Display & FPS", color = TextSecondary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    if (specs.adpfPreferredRate > 0 && specs.adpfPreferredRate != 60f) {
                        Box(
                            modifier = Modifier
                                .background(SecondaryNeon.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ADPF ${specs.adpfPreferredRate.toInt()}Hz", color = SecondaryNeon, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BigStatBox("FPS", if (currentFps > 0) "$currentFps" else "N/A", PrimaryNeon)
                    BigStatBox("Hz", "$currentHz", SecondaryNeon)
                    BigStatBox("Max Hz", "$maxHz", TertiaryAccent)
                    BigStatBox("Cores", "${specs.cpuCoreCount}", WarningOrange)
                }
                Spacer(modifier = Modifier.height(8.dp))
                val hzList = specs.supportedRefreshRates.joinToString("  ·  ") { "${it.toInt()}Hz" }
                Text("Supported: $hzList", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }

        // ── Network & Sessions ─────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoMetricCard(
                indicator = "NET",
                title = "Network",
                value = specs.networkType,
                subtitle = buildNetworkSubtitle(specs),
                color = when {
                    !specs.hasValidatedInternet -> ErrorRed
                    specs.is5GPlus -> TertiaryAccent
                    specs.is5G -> SecondaryNeon
                    specs.networkQualityScore >= 75 -> SuccessGreen
                    else -> PrimaryNeon
                },
                modifier = Modifier.weight(1f)
            )
            InfoMetricCard(
                indicator = "PLAY",
                title = "Sessions",
                value = "$totalSessions",
                subtitle = "${totalPlayTimeMinutes}m total",
                color = SecondaryNeon,
                modifier = Modifier.weight(1f)
            )
        }

        NetworkStackCard(specs = specs)

        // ── Benchmark ─────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (benchmarkResult != null) WarningOrange.copy(alpha = 0.6f) else SurfaceVariantDark
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Benchmark",
                        color = WarningOrange,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (isBenchmarking) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = WarningOrange)
                    } else {
                        Button(
                            onClick = viewModel::runBenchmark,
                            colors = ButtonDefaults.buttonColors(containerColor = WarningOrange, contentColor = BackgroundDark),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) { Text("RUN", fontWeight = FontWeight.Black, fontSize = 13.sp) }
                    }
                }
                benchmarkResult?.let { result ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BenchmarkStat("CPU", "${result.cpuScore}", TertiaryAccent)
                        BenchmarkStat("GPU", "${result.gpuScore}", SecondaryNeon)
                        BenchmarkStat("MEM", "${result.memoryScore}", PrimaryNeon)
                        BenchmarkStat("TOTAL", "${result.overallScore}", WarningOrange)
                    }
                }
            }
        }

        // ── Immersive Controls ─────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryNeon.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Immersive Mode", style = MaterialTheme.typography.titleMedium, color = SecondaryNeon, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Block Notifications", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        Text("DND mode while gaming", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                    Switch(
                        checked = isDndEnabled,
                        onCheckedChange = viewModel::toggleDnd,
                        colors = SwitchDefaults.colors(checkedThumbColor = SecondaryNeon, checkedTrackColor = SecondaryNeon.copy(alpha = 0.4f))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = SurfaceVariantDark)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Lock Brightness", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        Text("Prevent auto-dimming", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                    Switch(
                        checked = isBrightnessLocked,
                        onCheckedChange = viewModel::toggleBrightnessLock,
                        colors = SwitchDefaults.colors(checkedThumbColor = SecondaryNeon, checkedTrackColor = SecondaryNeon.copy(alpha = 0.4f))
                    )
                }

                if (isBrightnessLocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Brightness", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                        Text("${(brightnessLevel * 100).toInt()}%", color = SecondaryNeon, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = brightnessLevel,
                        onValueChange = viewModel::setBrightnessLevel,
                        valueRange = 0.1f..1f,
                        colors = SliderDefaults.colors(thumbColor = SecondaryNeon, activeTrackColor = SecondaryNeon, inactiveTrackColor = SurfaceVariantDark)
                    )
                }
            }
        }

        // ── Advanced Controls ─────────────────────────────────
        if (isRootAvailable) {
            LaunchedEffect(Unit) { viewModel.refreshCoreStatus() }
        }
        val coreStatus by viewModel.coreOnlineStatus.collectAsStateWithLifecycle()
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isRootAvailable) TertiaryAccent.copy(alpha = 0.38f) else SurfaceVariantDark
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Advanced Controls",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (isRootAvailable) "CPU core control and storage optimizer"
                            else "Root access required for CPU core control and FSTRIM",
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    StatusBadge(
                        text = if (isRootAvailable) "READY" else "LOCKED",
                        color = if (isRootAvailable) TertiaryAccent else TextSecondary
                    )
                    IconButton(onClick = { advancedExpanded = !advancedExpanded }) {
                        Icon(
                            imageVector = if (advancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (advancedExpanded) "Collapse advanced controls" else "Expand advanced controls",
                            tint = TextSecondary
                        )
                    }
                }

                AnimatedVisibility(visible = advancedExpanded) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        HorizontalDivider(color = SurfaceVariantDark)
                        Spacer(modifier = Modifier.height(12.dp))
                        if (isRootAvailable) {
                            Text("CPU Core Control", color = TertiaryAccent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                coreStatus.forEachIndexed { index, online ->
                                    FilterChip(
                                        selected = online,
                                        onClick = { viewModel.toggleCore(index, !online) },
                                        label = {
                                            Text(
                                                "C$index",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SuccessGreen.copy(alpha = 0.25f),
                                            selectedLabelColor = SuccessGreen,
                                            containerColor = SurfaceVariantDark,
                                            labelColor = TextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = online,
                                            selectedBorderColor = SuccessGreen.copy(alpha = 0.5f),
                                            borderColor = SurfaceVariantDark
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.triggerFstrim() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = TertiaryAccent, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Run Storage Optimizer", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(text = "ROOT", color = TextSecondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Root Features Locked", color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("Grant root access to enable CPU core control and storage optimizer.", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DashboardCockpitCard(
    specs: DeviceSpecs,
    accessMode: String,
    thermalColor: Color
) {
    val currentFps = specs.currentFps.toInt()
    val currentHz = specs.displayRefreshRateHz.toInt()
    val ageSeconds = ((System.currentTimeMillis() - specs.timestamp) / 1000L).coerceAtLeast(0L)
    val isStale = ageSeconds > 3L
    val thermalLabel = when {
        specs.batteryTemperature >= 43f || specs.thermalStatus >= 3 -> "Hot"
        specs.batteryTemperature >= 39f || specs.thermalStatus == 2 -> "Warm"
        else -> "Stable"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryNeon.copy(alpha = 0.28f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = specs.socName,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    Text(
                        text = "${specs.cpuCoreCount} cores · ${specs.architecture}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
                StatusBadge(
                    text = "Score ${specs.deviceRating}/10",
                    color = if (specs.deviceRating >= 7) SuccessGreen else WarningOrange
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CockpitStat(
                    label = "FPS",
                    value = if (currentFps > 0) "$currentFps" else "--",
                    color = PrimaryNeon,
                    modifier = Modifier.weight(1f)
                )
                CockpitStat(
                    label = "Refresh",
                    value = "${currentHz}Hz",
                    color = SecondaryNeon,
                    modifier = Modifier.weight(1f)
                )
                CockpitStat(
                    label = "Thermal",
                    value = thermalLabel,
                    color = thermalColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isStale) "Stale · ${ageSeconds}s ago" else "Live · ${ageSeconds}s ago",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$accessMode · ${networkStatusLabel(specs)} · ${specs.batteryLevel}% battery",
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun NetworkStackCard(specs: DeviceSpecs) {
    val accent = when {
        !specs.hasValidatedInternet -> ErrorRed
        specs.is5GPlus -> TertiaryAccent
        specs.is5G -> SecondaryNeon
        specs.networkQualityScore >= 75 -> SuccessGreen
        else -> PrimaryNeon
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Network Stack", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text(
                        if (specs.hasValidatedInternet) "Validated internet connection" else "Connected network has no validated internet",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                StatusBadge(
                    text = if (specs.is5GPlus) "5G+" else if (specs.is5G) "5G" else "Q${specs.networkQualityScore}",
                    color = accent
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NetworkTransportBox(
                    title = "WiFi",
                    value = specs.wifiLabel.ifBlank { "Off" },
                    subtitle = listOf(
                        specs.wifiBandLabel,
                        if (specs.wifiLinkSpeedMbps > 0) "${specs.wifiLinkSpeedMbps} Mbps" else "",
                        if (specs.wifiSignalBars > 0) "${specs.wifiSignalBars}/4 bars" else ""
                    ).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "No WiFi transport" },
                    color = if (specs.wifiLabel.isNotBlank()) PrimaryNeon else TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                NetworkTransportBox(
                    title = "Mobile Data",
                    value = specs.cellularLabel.ifBlank { "Off" },
                    subtitle = listOf(
                        if (specs.cellularSignalDbm > -120) "${specs.cellularSignalDbm} dBm" else "",
                        if (specs.cellularSignalBars > 0) "${specs.cellularSignalBars}/4 bars" else "",
                        if (specs.isNetworkMetered) "Metered" else "Unmetered"
                    ).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "No cellular transport" },
                    color = when {
                        specs.is5GPlus -> TertiaryAccent
                        specs.is5G -> SecondaryNeon
                        specs.cellularLabel.isNotBlank() -> SuccessGreen
                        else -> TextSecondary
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                "Down ${specs.networkDownstreamKbps / 1000} Mbps · Up ${specs.networkUpstreamKbps / 1000} Mbps",
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun NetworkTransportBox(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(title, color = TextSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = color, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, maxLines = 1)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 2)
    }
}

private fun buildNetworkSubtitle(specs: DeviceSpecs): String {
    val internet = if (specs.hasValidatedInternet) "Internet OK" else "No internet"
    val score = "Q${specs.networkQualityScore}"
    val wifi = if (specs.wifiLinkSpeedMbps > 0) {
        "${specs.wifiLinkSpeedMbps} Mbps"
    } else {
        ""
    }
    val signal = when {
        specs.wifiLabel.isNotBlank() -> "${specs.networkStrengthDbm} dBm"
        specs.cellularLabel.isNotBlank() -> "${specs.cellularSignalDbm} dBm"
        else -> ""
    }
    return listOf(internet, score, wifi, signal)
        .filter { it.isNotBlank() }
        .joinToString(" · ")
}

private fun networkStatusLabel(specs: DeviceSpecs): String {
    return when {
        specs.is5GPlus -> "5G+"
        specs.is5G -> "5G"
        specs.networkType.contains("+") -> "Dual net"
        specs.hasValidatedInternet -> "Online"
        else -> "Offline"
    }
}

@Composable
private fun CockpitStat(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = color,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

@Composable
private fun DashboardNotice(
    title: String,
    message: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.36f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = color,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message,
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// ── Gauge Card (circular arc style) ──────────────────────────
@Composable
fun GaugeCard(
    title: String,
    percent: Float,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animPercent by animateFloatAsState(
        targetValue = percent.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "gauge"
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = TextSecondary, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(72.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    val startAngle = 135f
                    val sweepMax = 270f
                    // Background arc
                    drawArc(
                        color = Color.White.copy(alpha = 0.07f),
                        startAngle = startAngle,
                        sweepAngle = sweepMax,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth)
                    )
                    // Foreground arc
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepMax * animPercent,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth)
                    )
                }
                Text(
                    value,
                    color = color,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

// ── Progress Bar Metric Card ──────────────────────────────────
@Composable
fun ProgressMetricCard(
    title: String,
    value: String,
    subtitle: String,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "progress"
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = TextSecondary, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = color, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = color,
                trackColor = SurfaceVariantDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ── Info Metric Card ──────────────────────────────────────────
@Composable
fun InfoMetricCard(
    indicator: String,
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(indicator, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = TextSecondary, style = MaterialTheme.typography.labelMedium)
            Text(value, color = color, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ── Stat Box ──────────────────────────────────────────────────
@Composable
fun BigStatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun BenchmarkStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}

// Legacy compat alias
@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    ProgressMetricCard(
        title = title,
        value = value,
        subtitle = subtitle,
        progress = 0f,
        color = color,
        modifier = modifier
    )
}

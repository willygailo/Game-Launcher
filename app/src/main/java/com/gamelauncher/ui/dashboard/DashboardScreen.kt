package com.gamelauncher.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.gamelauncher.data.model.DeviceSpecs
import com.gamelauncher.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val specs by viewModel.deviceSpecs.collectAsState()
    val isDndEnabled by viewModel.isDndEnabled.collectAsState()
    val isBrightnessLocked by viewModel.isBrightnessLocked.collectAsState()
    val brightnessLevel by viewModel.brightnessLevel.collectAsState()
    val isRootAvailable by viewModel.isRootAvailable.collectAsState()
    val totalSessions by viewModel.totalSessions.collectAsState()
    val totalPlayTimeMinutes by viewModel.totalPlayTimeMinutes.collectAsState()
    val benchmarkResult by viewModel.benchmarkResult.collectAsState()
    val isBenchmarking by viewModel.isBenchmarking.collectAsState()
    val hasWriteSecure by viewModel.hasWriteSecureSettings.collectAsState()

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
        // ── Header ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "System Monitor",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
                val deviceTierLabel = when {
                    specs.deviceRating >= 9 -> "🏆 Elite Gaming Device"
                    specs.deviceRating >= 7 -> "⚡ High-Performance"
                    specs.deviceRating >= 5 -> "✅ Mid-Range"
                    else -> "📱 Entry Level"
                }
                Text(deviceTierLabel, color = PrimaryNeon, style = MaterialTheme.typography.labelMedium)
            }
            // Root Badge
            if (isRootAvailable) {
                Box(
                    modifier = Modifier
                        .background(SuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("ROOT", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // RAM freed banner
        if (specs.freedRamMb > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(PrimaryNeon.copy(alpha = 0.15f), SuccessGreen.copy(alpha = 0.1f))),
                        RoundedCornerShape(10.dp)
                    )
                    .border(1.dp, PrimaryNeon.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "✅ Freed ${specs.freedRamMb} MB of RAM",
                    color = PrimaryNeon,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Advanced secure boosts banner
        if (hasWriteSecure || isRootAvailable) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                if (isRootAvailable) SuccessGreen.copy(alpha = 0.18f) else PrimaryNeon.copy(alpha = 0.12f),
                                SecondaryNeon.copy(alpha = 0.08f)
                            )
                        ),
                        RoundedCornerShape(10.dp)
                    )
                    .border(
                        1.dp,
                        (if (isRootAvailable) SuccessGreen else PrimaryNeon).copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        val modeLabel = when {
                            isRootAvailable -> "🔓 ROOT MODE"
                            hasWriteSecure -> "⚡ ADVANCED MODE"
                            else -> ""
                        }
                        Text(modeLabel, color = if (isRootAvailable) SuccessGreen else PrimaryNeon, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                        Text(
                            if (isRootAvailable) "All system-level performance tweaks are active"
                            else "6 secure system tweaks active — animation kill, game driver, sync freeze & more",
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background((if (isRootAvailable) SuccessGreen else PrimaryNeon).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (isRootAvailable) "ROOT" else "ADB+",
                            color = if (isRootAvailable) SuccessGreen else PrimaryNeon,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
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
                emoji = "📡",
                title = "Network",
                value = specs.networkType,
                subtitle = "${specs.wifiLinkSpeedMbps} Mbps · ${specs.networkStrengthDbm} dBm",
                color = PrimaryNeon,
                modifier = Modifier.weight(1f)
            )
            InfoMetricCard(
                emoji = "🎮",
                title = "Sessions",
                value = "$totalSessions",
                subtitle = "${totalPlayTimeMinutes}m total",
                color = SecondaryNeon,
                modifier = Modifier.weight(1f)
            )
        }

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
                        "⚡ Benchmark",
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
                Text("🎯 Immersive Mode", style = MaterialTheme.typography.titleMedium, color = SecondaryNeon, fontWeight = FontWeight.Bold)
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

        // ── Root-only Section ─────────────────────────────────
        if (isRootAvailable) {
            LaunchedEffect(Unit) { viewModel.refreshCoreStatus() }
            val coreStatus by viewModel.coreOnlineStatus.collectAsState()

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, TertiaryAccent.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔧 CPU Core Control", color = TertiaryAccent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
                }
            }

            Button(
                onClick = { viewModel.triggerFstrim() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TertiaryAccent, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Root: Storage Optimizer (FSTRIM)", fontWeight = FontWeight.Bold)
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantDark)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🔒", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Root Features Locked", color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("CPU Core Control & FSTRIM require root access", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // ── RAM Optimize Button ───────────────────────────────
        Button(
            onClick = { viewModel.optimizeRam() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(PrimaryNeon, SecondaryNeon.copy(alpha = 0.8f))),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🧹 Clear RAM & Optimize",
                    color = BackgroundDark,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
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
    emoji: String,
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
            Text(emoji, fontSize = 20.sp)
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

package com.gamelauncher.feature.monitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamelauncher.feature.monitor.domain.model.FpsMetrics
import com.gamelauncher.feature.monitor.domain.model.SystemHardwareStats

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    viewModel: MonitorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.startTelemetryObservability()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val darkBackground = Color(0xFF0F172A)

    val cardBackground = Color(0xFF1E293B)
    val accentColor = Color(0xFF38BDF8)
    val successColor = Color(0xFF22C55E)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "FPS & Hardware Telemetry",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground
                )
            )
        },
        containerColor = darkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is MonitorUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = accentColor
                    )
                }

                is MonitorUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { viewModel.startTelemetryObservability() }) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }

                is MonitorUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            FpsMeterCard(
                                fpsMetrics = state.fpsMetrics,
                                cardBackground = cardBackground,
                                accentColor = accentColor,
                                successColor = successColor
                            )
                        }

                        item {
                            CpuRamMeterCard(
                                stats = state.hardwareStats,
                                cardBackground = cardBackground,
                                accentColor = accentColor
                            )
                        }

                        item {
                            OverlayToggleCard(
                                isEnabled = state.isOverlayEnabled,
                                cardBackground = cardBackground,
                                accentColor = accentColor,
                                onToggle = { viewModel.toggleOverlay(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FpsMeterCard(
    fpsMetrics: FpsMetrics,
    cardBackground: Color,
    accentColor: Color,
    successColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Frame Pacing Telemetry",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${fpsMetrics.currentFps} FPS",
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        color = successColor
                    )
                    Text(
                        text = "Target: ${fpsMetrics.targetFps} Hz",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${String.format("%.1f", fpsMetrics.frameTimeMs)} ms",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = accentColor
                    )
                    Text(
                        text = "Frame Budget: 16.6ms",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
fun CpuRamMeterCard(
    stats: SystemHardwareStats,
    cardBackground: Color,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Hardware Load Metrics",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CPU Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("CPU Utilization", color = Color.White, fontSize = 13.sp)
                Text(
                    text = if (stats.cpuUsagePercent != null) "${stats.cpuUsagePercent.toInt()}%" else "UNAVAILABLE (SELinux / No Root)",
                    color = if (stats.cpuUsagePercent != null) accentColor else Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { ((stats.cpuUsagePercent ?: 0f) / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (stats.cpuUsagePercent != null) accentColor else Color.Gray,
                trackColor = Color(0xFF334155)
            )


            Spacer(modifier = Modifier.height(16.dp))

            // RAM Meter
            val ramProgress = if (stats.ramTotalMb > 0) {
                stats.ramUsedMb.toFloat() / stats.ramTotalMb.toFloat()
            } else 0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("RAM Usage", color = Color.White, fontSize = 13.sp)
                Text("${stats.ramUsedMb} / ${stats.ramTotalMb} MB", color = Color(0xFFA855F7), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { ramProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFA855F7),
                trackColor = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Thermal & Battery
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Battery Temp: ${stats.batteryTemperatureCelsius}°C",
                    color = if (stats.batteryTemperatureCelsius > 42f) Color(0xFFEF4444) else Color(0xFF22C55E),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Charge: ${stats.batteryLevelPercent}%",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun OverlayToggleCard(
    isEnabled: Boolean,
    cardBackground: Color,
    accentColor: Color,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "In-Game Floating Overlay",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Display real-time FPS and thermal metrics over active game windows.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = accentColor,
                    checkedTrackColor = accentColor.copy(alpha = 0.4f)
                )
            )
        }
    }
}

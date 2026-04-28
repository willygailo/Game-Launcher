package com.gamelauncher.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelauncher.data.model.DeviceSpecs
import com.gamelauncher.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val specs by viewModel.deviceSpecs.collectAsState()
    val isDndEnabled by viewModel.isDndEnabled.collectAsState()
    val isBrightnessLocked by viewModel.isBrightnessLocked.collectAsState()
    val isRootAvailable by viewModel.isRootAvailable.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "System Monitor",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        if (specs.freedRamMb > 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryNeon.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Optimized! Freed ${specs.freedRamMb} MB of RAM",
                    color = PrimaryNeon,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(
                title = "CPU Usage",
                value = "${specs.cpuUsagePercent.toInt()}%",
                subtitle = "${specs.cpuFreqMhz} MHz",
                color = TertiaryAccent,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "RAM",
                value = "${specs.ramUsedMb} MB",
                subtitle = "of ${specs.ramTotalMb} MB",
                color = PrimaryNeon,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(
                title = "GPU",
                value = if (specs.gpuFreqMhz > 0) "${specs.gpuFreqMhz} MHz" else "Ready",
                subtitle = specs.gpuRenderer,
                color = SecondaryNeon,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Battery",
                value = "${specs.batteryLevel}%",
                subtitle = "${specs.batteryTemperature}°C | ${specs.batteryChargingStatus}",
                color = if (specs.batteryTemperature > 40f) ErrorRed else SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        MetricCard(
            title = "Display",
            value = "${specs.displayRefreshRateHz} Hz",
            subtitle = "Supported: ${specs.supportedRefreshRates.joinToString { "${it.toInt()}" }}",
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth()
        )

        // Immersive Controls
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryNeon.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Immersive Mode", style = MaterialTheme.typography.titleMedium, color = SecondaryNeon, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Block Notifications (DND)", color = TextPrimary)
                    Switch(
                        checked = isDndEnabled, 
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                viewModel.requestDndPermission()
                            } else {
                                viewModel.disableDnd()
                            }
                        }, 
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SecondaryNeon, 
                            checkedTrackColor = SecondaryNeon.copy(alpha = 0.5f)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Lock Max Brightness", color = TextPrimary)
                    Switch(
                        checked = isBrightnessLocked, 
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                viewModel.requestBrightnessPermission()
                            } else {
                                viewModel.disableBrightness()
                            }
                        }, 
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SecondaryNeon, 
                            checkedTrackColor = SecondaryNeon.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        if (isRootAvailable) {
            Button(
                onClick = { viewModel.triggerFstrim() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TertiaryAccent, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Root: Storage Optimizer (FSTRIM)", fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = { viewModel.optimizeRam() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Clear RAM & Optimize", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MetricCard(
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
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = TextSecondary, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = color, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.labelLarge)
        }
    }
}
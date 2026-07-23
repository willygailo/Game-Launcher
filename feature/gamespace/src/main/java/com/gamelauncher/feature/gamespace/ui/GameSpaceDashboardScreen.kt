package com.gamelauncher.feature.gamespace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamelauncher.core.device.OemBrand

private val NeonCyan = Color(0xFF00F0FF)
private val NeonPink = Color(0xFFFF0055)
private val DarkBg = Color(0xFF0B0E14)
private val CardBg = Color(0xFF141923)
private val BorderColor = Color(0xFF263044)
private val MutedText = Color(0xFF8A99AD)
private val SuccessGreen = Color(0xFF00FF88)

@Composable
fun GameSpaceDashboardScreen(
    viewModel: GameSpaceViewModel,
    onNavigateToTweaks: () -> Unit = {},
    onNavigateToHuaweiGuide: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    if (state.showResetConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissResetDialog() },
            title = { Text("Reset All Performance Tweaks?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("This will revert all modified hidden flags back to their pre-boost baseline snapshot captured during the initial device probe.", color = MutedText, fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmResetAllTweaks() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                ) {
                    Text("RESET TO BASELINE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissResetDialog() }) {
                    Text("CANCEL", color = MutedText, fontSize = 11.sp)
                }
            },
            containerColor = CardBg
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // ROG Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "ROG GAME SPACE HUB",
                    color = NeonPink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    "ARMOURY CRATE HARDWARE DASHBOARD",
                    color = MutedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (state.detectedOemBrand == OemBrand.HUAWEI) {
                    Button(
                        onClick = onNavigateToHuaweiGuide,
                        colors = ButtonDefaults.buttonColors(containerColor = BorderColor),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("HW GUIDE", color = NeonPink, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
                Button(
                    onClick = onNavigateToTweaks,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("OEM FLAGS", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emergency Reset Action Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, NeonPink.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
            color = CardBg
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "BASELINE SNAPSHOT RESTORE",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { viewModel.promptResetAllTweaks() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text("RESET ALL TWEAKS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Performance Mode Selector Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(14.dp)),
            color = CardBg
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("PERFORMANCE PROFILES", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ECO", "BALANCED", "TURBO", "ROG ULTRA").forEach { mode ->
                        val isSelected = state.activeProfileMode == mode
                        val accentColor = when (mode) {
                            "ROG ULTRA" -> NeonPink
                            "TURBO" -> NeonCyan
                            "BALANCED" -> SuccessGreen
                            else -> MutedText
                        }

                        Button(
                            onClick = { viewModel.selectProfileMode(mode) },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) accentColor.copy(alpha = 0.25f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) accentColor else BorderColor
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                mode,
                                color = if (isSelected) accentColor else MutedText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // System Telemetry Overview Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(14.dp)),
            color = CardBg
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("HARDWARE STATUS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryGaugeItem(label = "OEM BRAND", value = state.detectedOemBrand.displayName, color = NeonCyan)
                    TelemetryGaugeItem(label = "SHIZUKU IPC", value = if (state.isShizukuReady) "CONNECTED" else "DISCONNECTED", color = if (state.isShizukuReady) SuccessGreen else NeonPink)
                    TelemetryGaugeItem(label = "ACTIVE FLAGS", value = "${state.flags.count { it.status is com.gamelauncher.core.oemflags.ProbeStatus.Supported }} PROBED", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Embedded OEM Tweaks list
        OemTweaksScreen(viewModel = viewModel)
    }
}

@Composable
fun TelemetryGaugeItem(label: String, value: String, color: Color) {
    Column {
        Text(label, color = MutedText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

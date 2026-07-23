package com.gamelauncher.feature.gamespace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.gamelauncher.core.oemflags.FlagConfidence
import com.gamelauncher.core.oemflags.OemFlag
import com.gamelauncher.core.oemflags.ProbeStatus

private val NeonCyan = Color(0xFF00F0FF)
private val NeonPink = Color(0xFFFF0055)
private val DarkBg = Color(0xFF0B0E14)
private val CardBg = Color(0xFF141923)
private val BorderColor = Color(0xFF263044)
private val MutedText = Color(0xFF8A99AD)
private val SuccessGreen = Color(0xFF00FF88)
private val WarningAmber = Color(0xFFFFB700)

@Composable
fun OemTweaksScreen(
    viewModel: GameSpaceViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "ROG GAME SPACE TUNER",
                    color = NeonCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    "OEM: ${state.detectedOemBrand.displayName} (${state.detectedOemBrand.osName})",
                    color = MutedText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!state.isShizukuReady) {
                Button(
                    onClick = { viewModel.requestShizukuPermission() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CONNECT SHIZUKU", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            } else {
                Surface(
                    color = SuccessGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                ) {
                    Text(
                        "SHIZUKU ACTIVE",
                        color = SuccessGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else if (state.flags.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hidden performance flags detected for this device model", color = MutedText)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.flags, key = { it.id }) { flag ->
                    OemFlagCard(
                        flag = flag,
                        onToggle = { enable -> viewModel.toggleFlag(flag, enable) }
                    )
                }
            }
        }
    }
}

@Composable
fun OemFlagCard(
    flag: OemFlag,
    onToggle: (Boolean) -> Unit
) {
    val isSupported = flag.status is ProbeStatus.Supported
    val currentValue = (flag.status as? ProbeStatus.Supported)?.currentValue ?: ""
    val isChecked = currentValue == flag.activeValue
    val isSamsungGos = flag.id == "samsung_gos_thermal"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, if (isSupported) BorderColor else Color.Red.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
        color = CardBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        flag.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (isSupported) NeonCyan.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isSupported) "SUPPORTED" else if (isSamsungGos) "Not verified on this One UI build" else "UNSUPPORTED",
                            color = if (isSupported) NeonCyan else WarningAmber,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    if (flag.confidence == FlagConfidence.NEEDS_TESTING && !isSamsungGos) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = WarningAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "EXPERIMENTAL",
                                color = WarningAmber,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    flag.description,
                    color = MutedText,
                    fontSize = 11.sp
                )
                if (isSamsungGos && !isSupported) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Fallback: Standard ADPF performance session active (GOS touch bypassed)",
                        color = WarningAmber.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Scope: ${flag.scope.namespace} | Key: ${flag.key}",
                    color = MutedText.copy(alpha = 0.6f),
                    fontSize = 9.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = isChecked,
                enabled = isSupported,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = NeonCyan.copy(alpha = 0.4f),
                    uncheckedThumbColor = MutedText,
                    uncheckedTrackColor = CardBg
                )
            )
        }
    }
}

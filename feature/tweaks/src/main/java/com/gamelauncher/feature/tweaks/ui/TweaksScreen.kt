package com.gamelauncher.feature.tweaks.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamelauncher.feature.tweaks.domain.model.TweakCategory
import com.gamelauncher.feature.tweaks.domain.model.TweakItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TweaksScreen(
    viewModel: TweaksViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val darkBackground = Color(0xFF0F172A)
    val cardBackground = Color(0xFF1E293B)
    val accentColor = Color(0xFF38BDF8)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "System Performance Tweaks",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground
                )
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
                        color = accentColor
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

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.tweaks, key = { it.id }) { tweak ->
                            TweakCardItem(
                                tweak = tweak,
                                cardBackground = cardBackground,
                                accentColor = accentColor,
                                onRefreshRateSelected = { viewModel.applyRefreshRate(it) },
                                onCpuGovernorSelected = { viewModel.applyCpuGovernor(it) },
                                onGpuRenderingToggled = { viewModel.applyGpuRendering(it) },
                                onThermalBypassToggled = { viewModel.applyThermalThrottlingBypass(it) },
                                onGameModeToggled = { viewModel.applyGameModeBooster(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TweakCardItem(
    tweak: TweakItem,
    cardBackground: Color,
    accentColor: Color,
    onRefreshRateSelected: (Float) -> Unit,
    onCpuGovernorSelected: (String) -> Unit,
    onGpuRenderingToggled: (Boolean) -> Unit,
    onThermalBypassToggled: (Boolean) -> Unit,
    onGameModeToggled: (Boolean) -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tweak.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                if (!tweak.isSupportedByDevice) {
                    val labelText = tweak.badgeNote
                        ?: if (tweak.category == TweakCategory.CPU_GOVERNOR) "Requires Root" else "Unsupported"
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF334155), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = labelText,
                            fontSize = 11.sp,
                            color = if (tweak.category == TweakCategory.CPU_GOVERNOR) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tweak.description,
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (tweak.category) {
                TweakCategory.REFRESH_RATE -> {
                    if (tweak.isSupportedByDevice) {
                        DropdownSelector(
                            currentValue = tweak.selectedValue ?: "Default",
                            options = tweak.supportedValues,
                            enabled = true,
                            onOptionSelected = { value ->
                                value.toFloatOrNull()?.let { onRefreshRateSelected(it) }
                            }
                        )
                    }
                }

                TweakCategory.CPU_GOVERNOR -> {
                    DropdownSelector(
                        currentValue = tweak.selectedValue ?: "schedutil",
                        options = tweak.supportedValues,
                        enabled = tweak.isSupportedByDevice,
                        onOptionSelected = { onCpuGovernorSelected(it) }
                    )
                }

                TweakCategory.GPU_RENDERING -> {
                    if (tweak.isSupportedByDevice) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (tweak.isToggleActive) "2D Hardware Acceleration Active" else "Standard UI Rendering",
                                fontSize = 12.sp,
                                color = if (tweak.isToggleActive) accentColor else Color(0xFF94A3B8)
                            )
                            Switch(
                                checked = tweak.isToggleActive,
                                onCheckedChange = onGpuRenderingToggled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = accentColor
                                )
                            )
                        }
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
                                text = if (tweak.isToggleActive) "Bypass Active" else "Standard Thermal Throttling",
                                fontSize = 12.sp,
                                color = if (tweak.isToggleActive) accentColor else Color(0xFF94A3B8)
                            )
                            Switch(
                                checked = tweak.isToggleActive,
                                onCheckedChange = onThermalBypassToggled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = accentColor
                                )
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
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = accentColor
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownSelector(
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
            Text(text = "Option: $currentValue", color = if (enabled) Color.White else Color(0xFF94A3B8), fontSize = 12.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    }
                )
            }
        }
    }
}

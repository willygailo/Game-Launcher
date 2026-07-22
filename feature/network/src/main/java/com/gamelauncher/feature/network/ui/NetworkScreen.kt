package com.gamelauncher.feature.network.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamelauncher.feature.network.domain.model.DnsProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                        text = "Network & Private DNS Booster",
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
                is NetworkUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = accentColor
                    )
                }

                is NetworkUiState.Error -> {
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
                        OutlinedButton(onClick = { viewModel.loadNetworkState() }) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }

                is NetworkUiState.Success -> {
                    state.message?.let { msg ->
                        LaunchedEffect(msg) {
                            snackbarHostState.showSnackbar(msg)
                            viewModel.clearMessage()
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            LatencyHeaderCard(
                                state = state,
                                cardBackground = cardBackground,
                                accentColor = accentColor,
                                successColor = successColor,
                                onRunPing = { viewModel.runPingProbe("1.1.1.1") }
                            )
                        }

                        item {
                            Text(
                                text = "Select Private DNS Provider",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(state.dnsProviders, key = { it.id }) { provider ->
                            val isSelected = if (provider.isSystemDefault) {
                                state.activeHostname.isNullOrEmpty()
                            } else {
                                state.activeHostname == provider.hostname
                            }

                            DnsProviderCard(
                                provider = provider,
                                isSelected = isSelected,
                                cardBackground = cardBackground,
                                accentColor = accentColor,
                                onClick = { viewModel.applyDnsProvider(provider) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LatencyHeaderCard(
    state: NetworkUiState.Success,
    cardBackground: Color,
    accentColor: Color,
    successColor: Color,
    onRunPing: () -> Unit
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
                    text = "Live Ping Measurement",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                Button(
                    onClick = onRunPing,
                    enabled = !state.isPinging,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (state.isPinging) "Probing..." else "Ping Test",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val ping = state.latestPing
            val pingText = when {
                state.isPinging -> "Probing network latency..."
                ping == null -> "Press 'Ping Test' to measure latency"
                !ping.isReachable -> "Host Unreachable"
                else -> "${ping.latencyMs} ms"
            }

            val badgeColor = when {
                ping != null && ping.isReachable && ping.latencyMs < 50 -> successColor
                ping != null && ping.isReachable && ping.latencyMs < 120 -> Color(0xFFF59E0B)
                else -> Color(0xFF94A3B8)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(badgeColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = pingText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DnsProviderCard(
    provider: DnsProvider,
    isSelected: Boolean,
    cardBackground: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) accentColor else Color.Transparent
    val borderWidth = if (isSelected) 2.dp else 0.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
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
                    text = provider.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = provider.description,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                provider.hostname?.let { host ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TLS Host: $host",
                        fontSize = 11.sp,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .background(accentColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

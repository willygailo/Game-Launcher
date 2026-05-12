package com.gamelauncher.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelauncher.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsScreen(
    packageName: String,
    onBack: () -> Unit,
    viewModel: GameDetailsViewModel = hiltViewModel()
) {
    val game by viewModel.game.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val totalPlayTime by viewModel.totalPlayTime.collectAsState()
    val averageFps by viewModel.averageFps.collectAsState()
    val sessionCount by viewModel.sessionCount.collectAsState()

    LaunchedEffect(packageName) { viewModel.loadGameDetails(packageName) }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text(game?.name ?: "Game Details", color = TextPrimary, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryNeon)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Sessions", "$sessionCount", SecondaryNeon, Modifier.weight(1f))
                    StatCard("Play Time", "${totalPlayTime}m", TertiaryAccent, Modifier.weight(1f))
                    StatCard("Avg FPS", if (averageFps > 0) "${averageFps.toInt()}" else "--", PrimaryNeon, Modifier.weight(1f))
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Session History", color = SecondaryNeon, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${sessions.size} recorded sessions", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (sessions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No sessions recorded yet", color = TextSecondary)
                    }
                }
            }

            items(sessions) { session ->
                SessionCard(session)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun SessionCard(session: com.gamelauncher.data.model.GamingSession) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val durationMinutes = session.durationMs / 60_000
    val durationSeconds = (session.durationMs % 60_000) / 1000

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(dateFormat.format(Date(session.startTime)), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                if (session.wasBoosted) Text("Boosted", color = PrimaryNeon, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("${durationMinutes}m ${durationSeconds}s", color = TextPrimary, fontWeight = FontWeight.Bold)
                if (session.avgFps > 0) Text("${session.avgFps} FPS", color = if (session.avgFps >= 55) SuccessGreen else WarningOrange)
                if (session.batteryDrain > 0) Text("-${session.batteryDrain}% batt", color = ErrorRed)
            }
        }
    }
}

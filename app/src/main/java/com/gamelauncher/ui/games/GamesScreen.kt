package com.gamelauncher.ui.games

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.os.Build
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.ui.Screen
import com.gamelauncher.ui.components.EmptyState
import com.gamelauncher.ui.components.GameCard
import com.gamelauncher.ui.components.LoadingState
import com.gamelauncher.ui.components.ScreenHeader
import com.gamelauncher.ui.theme.*

@Composable
fun GameListScreen(
    navController: NavController? = null,
    viewModel: GamesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredGames = uiState.filteredGames
    val isScanning = uiState.isScanning
    val scanProgressPercent = uiState.scanProgressPercent
    val lastScannedAt = uiState.lastScannedAt
    val searchQuery = uiState.searchQuery
    val selectedCategory = uiState.selectedCategory
    val selectedSortMode = uiState.selectedSortMode
    val availableCategories = uiState.availableCategories
    val hasActiveFilters = searchQuery.isNotBlank() || selectedCategory != "All"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SurfaceDark, BackgroundDark)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            ScreenHeader(
                title = "My Games",
                subtitle = when {
                    isScanning -> "Scanning installed apps · $scanProgressPercent%"
                    lastScannedAt > 0L -> "${filteredGames.size} games detected · scan complete"
                    else -> "${filteredGames.size} games detected"
                },
                trailing = {
                IconButton(
                    onClick = viewModel::refreshGames,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceVariantDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh games",
                        tint = PrimaryNeon
                    )
                }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search games or package name", color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryNeon,
                    focusedBorderColor = PrimaryNeon,
                    unfocusedBorderColor = SurfaceVariantDark,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            if (availableCategories.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCategories.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedCategory(category) },
                            label = {
                                Text(
                                    category,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryNeon.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryNeon,
                                containerColor = SurfaceDark,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = PrimaryNeon.copy(alpha = 0.6f),
                                borderColor = SurfaceVariantDark
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                GameSortMode.entries.forEach { sortMode ->
                    val isSelected = selectedSortMode == sortMode
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSortMode(sortMode) },
                        label = {
                            Text(
                                sortMode.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryNeon.copy(alpha = 0.2f),
                            selectedLabelColor = SecondaryNeon,
                            containerColor = SurfaceDark,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = SecondaryNeon.copy(alpha = 0.6f),
                            borderColor = SurfaceVariantDark
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Content
            if (isScanning) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        CircularProgressIndicator(color = PrimaryNeon, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Scanning installed games... $scanProgressPercent%",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { scanProgressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)),
                            color = PrimaryNeon,
                            trackColor = SurfaceVariantDark
                        )
                    }
                }
            } else if (filteredGames.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.SportsEsports,
                    title = if (hasActiveFilters) "No matching games" else "No games detected yet",
                    message = if (hasActiveFilters) {
                        "Try a different search or category filter."
                    } else {
                        "Install a game or scan again after adding apps."
                    },
                    actionLabel = if (hasActiveFilters) "Clear Filters" else "Scan Games",
                    onAction = {
                        if (hasActiveFilters) {
                            viewModel.setSearchQuery("")
                            viewModel.setSelectedCategory("All")
                        } else {
                            viewModel.refreshGames()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredGames, key = { it.packageName }) { game ->
                        GameCard(
                            game = game,
                            onLaunch = { viewModel.launchGame(game) },
                            onUpdate = { viewModel.updateGameSettings(it) },
                            onDetails = {
                                navController?.navigate("${Screen.GameDetails.route}/${Uri.encode(game.packageName)}")
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

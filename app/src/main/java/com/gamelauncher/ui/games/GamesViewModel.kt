package com.gamelauncher.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.data.repository.GamesRepository
import com.gamelauncher.domain.usecase.GetInstalledGamesUseCase
import com.gamelauncher.domain.usecase.LaunchGameAndBoostUseCase
import com.gamelauncher.data.repository.ScanProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GamesUiState(
    val isScanning: Boolean = true,
    val scanProgressPercent: Int = 0,
    val lastScannedAt: Long = 0L,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val selectedSortMode: GameSortMode = GameSortMode.NAME,
    val availableCategories: List<String> = listOf("All"),
    val filteredGames: List<GameModel> = emptyList()
)

enum class GameSortMode(val displayName: String) {
    NAME("Name"),
    RECENTLY_PLAYED("Recent"),
    MOST_BOOSTED("Most Boosted"),
    PERFORMANCE_MODE("Boost On")
}

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val getInstalledGamesUseCase: GetInstalledGamesUseCase,
    private val launchGameAndBoostUseCase: LaunchGameAndBoostUseCase,
    private val gamesRepository: GamesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    private val _allGames = MutableStateFlow<List<GameModel>>(emptyList())

    init {
        refreshGames()
        viewModelScope.launch {
            getInstalledGamesUseCase().collect { games ->
                _allGames.value = games
                updateFilteredGames()
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateFilteredGames()
    }

    fun setSelectedCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        updateFilteredGames()
    }

    fun setSortMode(sortMode: GameSortMode) {
        _uiState.update { it.copy(selectedSortMode = sortMode) }
        updateFilteredGames()
    }

    fun refreshGames() {
        viewModelScope.launch {
            gamesRepository.scanAndSaveGames().collect { progress ->
                when (progress) {
                    is ScanProgress.Scanning -> {
                        _uiState.update {
                            it.copy(
                                isScanning = true,
                                scanProgressPercent = progress.percentage.coerceIn(0, 100)
                            )
                        }
                    }
                    ScanProgress.Completed -> {
                        _uiState.update {
                            it.copy(
                                isScanning = false,
                                scanProgressPercent = 100,
                                lastScannedAt = System.currentTimeMillis()
                            )
                        }
                    }
                    ScanProgress.Idle -> {}
                }
            }
        }
    }

    private fun updateFilteredGames() {
        val query = _uiState.value.searchQuery
        val category = _uiState.value.selectedCategory
        val sortMode = _uiState.value.selectedSortMode
        val allGames = _allGames.value

        val filtered = allGames.filter { game ->
            val matchesQuery = query.isBlank() ||
                game.name.contains(query, ignoreCase = true) ||
                game.packageName.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || game.customCategory == category
            matchesQuery && matchesCategory
        }

        val sorted = when (sortMode) {
            GameSortMode.NAME -> filtered.sortedBy { it.name.lowercase() }
            GameSortMode.RECENTLY_PLAYED -> filtered.sortedWith(
                compareByDescending<GameModel> { it.lastLaunched }.thenBy { it.name.lowercase() }
            )
            GameSortMode.MOST_BOOSTED -> filtered.sortedWith(
                compareByDescending<GameModel> { it.totalBoostSessions }.thenBy { it.name.lowercase() }
            )
            GameSortMode.PERFORMANCE_MODE -> filtered.sortedWith(
                compareByDescending<GameModel> { it.highPerformanceMode }.thenBy { it.name.lowercase() }
            )
        }

        val categories = listOf("All") + allGames.map { it.customCategory }.distinct().sorted()

        _uiState.update {
            it.copy(
                filteredGames = sorted,
                availableCategories = categories
            )
        }
    }

    fun launchGame(game: GameModel) {
        viewModelScope.launch {
            launchGameAndBoostUseCase(game)
        }
    }

    fun updateGameSettings(game: GameModel) {
        viewModelScope.launch {
            gamesRepository.updateGame(game)
        }
    }
}

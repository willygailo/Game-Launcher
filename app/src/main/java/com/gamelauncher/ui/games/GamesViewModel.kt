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
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val availableCategories: List<String> = listOf("All"),
    val filteredGames: List<GameModel> = emptyList()
)

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

    fun refreshGames() {
        viewModelScope.launch {
            gamesRepository.scanAndSaveGames().collect { progress ->
                when (progress) {
                    is ScanProgress.Scanning -> {
                        _uiState.update { it.copy(isScanning = true) }
                    }
                    ScanProgress.Completed -> {
                        _uiState.update { it.copy(isScanning = false) }
                    }
                    ScanProgress.Idle -> {}
                }
            }
        }
    }

    private fun updateFilteredGames() {
        val query = _uiState.value.searchQuery
        val category = _uiState.value.selectedCategory
        val allGames = _allGames.value

        val filtered = allGames.filter { game ->
            val matchesQuery = query.isBlank() ||
                game.name.contains(query, ignoreCase = true) ||
                game.packageName.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || game.customCategory == category
            matchesQuery && matchesCategory
        }

        val categories = listOf("All") + allGames.map { it.customCategory }.distinct().sorted()

        _uiState.update {
            it.copy(
                filteredGames = filtered,
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

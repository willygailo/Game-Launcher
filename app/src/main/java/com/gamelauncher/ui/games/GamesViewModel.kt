package com.gamelauncher.ui.games

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.data.local.GameDao
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.data.model.GamingSession
import com.gamelauncher.data.repository.GamesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gamesRepository: GamesRepository,
    private val gameDao: GameDao
) : ViewModel() {
    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _allGames = MutableStateFlow<List<GameModel>>(emptyList())

    val games: StateFlow<List<GameModel>> = gamesRepository.allGames.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredGames: StateFlow<List<GameModel>> = combine(
        _allGames, _searchQuery, _selectedCategory
    ) { allGames, query, category ->
        allGames.filter { game ->
            val matchesQuery = query.isBlank() ||
                game.name.contains(query, ignoreCase = true) ||
                game.packageName.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || game.customCategory == category
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCategories: StateFlow<List<String>> = _allGames.map { games ->
        listOf("All") + games.map { it.customCategory }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    init {
        refreshGames()
        viewModelScope.launch {
            games.collect { _allGames.value = it }
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSelectedCategory(category: String) { _selectedCategory.value = category }

    fun refreshGames() {
        viewModelScope.launch {
            _isScanning.value = true
            runCatching {
                gamesRepository.scanAndSaveGames()
            }
            _isScanning.value = false
        }
    }

    fun launchGame(game: GameModel) {
        viewModelScope.launch {
            try {
                gamesRepository.recordGameLaunch(game.packageName)
                gameDao.insertSession(GamingSession(
                    packageName = game.packageName,
                    gameName = game.name,
                    startTime = System.currentTimeMillis(),
                    wasBoosted = game.highPerformanceMode
                ))
            } catch (_: Exception) {}
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(game.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                }
            } catch (_: Exception) {}
        }
    }

    fun updateGameSettings(game: GameModel) {
        viewModelScope.launch {
            gamesRepository.updateGame(game)
        }
    }
}

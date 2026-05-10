package com.gamelauncher.ui.games

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.data.repository.GamesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gamesRepository: GamesRepository
) : ViewModel() {
    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    val games: StateFlow<List<GameModel>> = gamesRepository.allGames.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        refreshGames()
    }

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
            gamesRepository.recordGameLaunch(game.packageName)
            val launchIntent = context.packageManager.getLaunchIntentForPackage(game.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        }
    }

    fun updateGameSettings(game: GameModel) {
        viewModelScope.launch {
            gamesRepository.updateGame(game)
        }
    }
}

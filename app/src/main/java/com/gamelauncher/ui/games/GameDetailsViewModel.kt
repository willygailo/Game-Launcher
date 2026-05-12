package com.gamelauncher.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.data.local.GameDao
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.data.model.GamingSession
import com.gamelauncher.data.repository.GamesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameDetailsViewModel @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val gameDao: GameDao
) : ViewModel() {

    private val _game = MutableStateFlow<GameModel?>(null)
    val game: StateFlow<GameModel?> = _game.asStateFlow()

    private val _sessions = MutableStateFlow<List<GamingSession>>(emptyList())
    val sessions: StateFlow<List<GamingSession>> = _sessions.asStateFlow()

    private val _totalPlayTime = MutableStateFlow(0L)
    val totalPlayTime: StateFlow<Long> = _totalPlayTime.asStateFlow()

    private val _averageFps = MutableStateFlow(0f)
    val averageFps: StateFlow<Float> = _averageFps.asStateFlow()

    private val _sessionCount = MutableStateFlow(0)
    val sessionCount: StateFlow<Int> = _sessionCount.asStateFlow()

    fun loadGameDetails(packageName: String) {
        viewModelScope.launch {
            _game.value = gamesRepository.getGame(packageName)
            gameDao.getSessionsForGame(packageName).collect { sessionList ->
                _sessions.value = sessionList
                _totalPlayTime.value = sessionList.sumOf { it.durationMs } / 60_000
                _sessionCount.value = sessionList.size
                val fpsValues = sessionList.filter { it.avgFps > 0 }.map { it.avgFps }
                _averageFps.value = if (fpsValues.isNotEmpty()) fpsValues.average().toFloat() else 0f
            }
        }
    }

    fun updateGame(game: GameModel) {
        viewModelScope.launch { gamesRepository.updateGame(game) }
    }
}

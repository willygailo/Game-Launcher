package com.gamelauncher.domain.usecase

import com.gamelauncher.data.model.GameModel
import com.gamelauncher.data.repository.GamesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInstalledGamesUseCase @Inject constructor(
    private val gamesRepository: GamesRepository
) {
    operator fun invoke(): Flow<List<GameModel>> {
        return gamesRepository.allGames
    }
}

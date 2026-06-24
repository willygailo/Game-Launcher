package com.gamelauncher.domain.usecase

import com.gamelauncher.data.model.GameModel
import com.gamelauncher.data.repository.GamesRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class GetInstalledGamesUseCaseTest {

    private val gamesRepository: GamesRepository = mock()
    private val useCase = GetInstalledGamesUseCase(gamesRepository)

    @Test
    fun `invoke returns flow of games from repository`() = runTest {
        val sampleGames = listOf(
            GameModel("com.test", "Test")
        )
        `when`(gamesRepository.allGames).thenReturn(flowOf(sampleGames))

        val result = useCase().first()
        assertThat(result).isEqualTo(sampleGames)
    }
}

package com.gamelauncher.ui.games

import com.gamelauncher.data.model.GameModel
import com.gamelauncher.data.repository.GamesRepository
import com.gamelauncher.domain.usecase.GetInstalledGamesUseCase
import com.gamelauncher.domain.usecase.LaunchGameAndBoostUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class GamesViewModelTest {

    private lateinit var viewModel: GamesViewModel
    private val getInstalledGamesUseCase: GetInstalledGamesUseCase = mock()
    private val launchGameAndBoostUseCase: LaunchGameAndBoostUseCase = mock()
    private val gamesRepository: GamesRepository = mock()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val sampleGames = listOf(
            GameModel(
                packageName = "com.test.game1",
                name = "Test Game 1",
                customCategory = "RPG"
            ),
            GameModel(
                packageName = "com.test.game2",
                name = "Test Game 2",
                customCategory = "Action"
            )
        )

        `when`(getInstalledGamesUseCase()).thenReturn(flowOf(sampleGames))
        `when`(gamesRepository.scanAndSaveGames()).thenReturn(flowOf(com.gamelauncher.data.repository.ScanProgress.Completed))

        viewModel = GamesViewModel(
            getInstalledGamesUseCase,
            launchGameAndBoostUseCase,
            gamesRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state shows all games and categories`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.filteredGames).hasSize(2)
        assertThat(state.availableCategories).containsExactly("All", "Action", "RPG").inOrder()
        assertThat(state.isScanning).isFalse()
    }

    @Test
    fun `search filters games by name`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSearchQuery("Game 1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.filteredGames).hasSize(1)
        assertThat(state.filteredGames[0].name).isEqualTo("Test Game 1")
    }

    @Test
    fun `category filters games correctly`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSelectedCategory("Action")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.filteredGames).hasSize(1)
        assertThat(state.filteredGames[0].name).isEqualTo("Test Game 2")
    }

    @Test
    fun `launchGame calls use case`() = runTest {
        val game = GameModel("com.test.game1", "Test Game 1")
        viewModel.launchGame(game)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(launchGameAndBoostUseCase).invoke(game)
    }

    @Test
    fun `refreshGames calls repository`() = runTest {
        viewModel.refreshGames()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(gamesRepository, times(2)).scanAndSaveGames()
    }
}

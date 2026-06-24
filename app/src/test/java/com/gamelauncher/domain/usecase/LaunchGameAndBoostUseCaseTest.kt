package com.gamelauncher.domain.usecase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.gamelauncher.data.local.GameDao
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.data.repository.GamesRepository
import com.gamelauncher.data.repository.ScanProgress
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class LaunchGameAndBoostUseCaseTest {

    private val context: Context = mock()
    private val packageManager: PackageManager = mock()
    private val gameDao: GameDao = mock()

    private class FakeGamesRepository : GamesRepository(mock(), mock(), mock()) {
        var recordGameLaunchCalledWith: String? = null

        override suspend fun recordGameLaunch(packageName: String) {
            recordGameLaunchCalledWith = packageName
        }

        override fun scanAndSaveGames(): Flow<ScanProgress> {
            return flowOf(ScanProgress.Completed)
        }
    }

    private lateinit var gamesRepository: FakeGamesRepository
    private lateinit var useCase: LaunchGameAndBoostUseCase

    @Before
    fun setup() {
        `when`(context.packageManager).thenReturn(packageManager)
        `when`(packageManager.getLaunchIntentForPackage(org.mockito.ArgumentMatchers.anyString())).thenReturn(mock(Intent::class.java))
        
        gamesRepository = FakeGamesRepository()
        useCase = LaunchGameAndBoostUseCase(context, gamesRepository, gameDao)
    }

    @Test
    fun `invoke records launch and starts service if high performance enabled`() = runTest {
        val game = GameModel(
            packageName = "com.test",
            name = "Test",
            highPerformanceMode = true
        )

        useCase(game)

        assertThat(gamesRepository.recordGameLaunchCalledWith).isEqualTo("com.test")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            verify(context).startForegroundService(org.mockito.ArgumentMatchers.any())
        } else {
            verify(context).startService(org.mockito.ArgumentMatchers.any())
        }
        verify(context).startActivity(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `invoke does not start booster service if high performance disabled`() = runTest {
        val game = GameModel(
            packageName = "com.test",
            name = "Test",
            highPerformanceMode = false
        )

        useCase(game)

        assertThat(gamesRepository.recordGameLaunchCalledWith).isEqualTo("com.test")
        verify(context, never()).startForegroundService(org.mockito.ArgumentMatchers.any())
        verify(context, never()).startService(org.mockito.ArgumentMatchers.any())
        verify(context).startActivity(org.mockito.ArgumentMatchers.any())
    }
}

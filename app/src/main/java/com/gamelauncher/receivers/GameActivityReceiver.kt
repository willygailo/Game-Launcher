package com.gamelauncher.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gamelauncher.data.repository.GamesRepository
import com.gamelauncher.di.ApplicationScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GameActivityReceiver : BroadcastReceiver() {
    @Inject lateinit var gamesRepository: GamesRepository

    // Injected application-scoped supervised CoroutineScope.
    // Avoids leaking a raw CoroutineScope that can't be cancelled.
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in setOf(
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_REMOVED,
                Intent.ACTION_PACKAGE_REPLACED
            )
        ) return

        val pendingResult = goAsync()
        appScope.launch {
            try {
                gamesRepository.scanAndSaveGames().collect()
            } finally {
                pendingResult.finish()
            }
        }
    }
}

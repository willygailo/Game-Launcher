package com.gamelauncher.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gamelauncher.data.repository.GamesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GameActivityReceiver : BroadcastReceiver() {
    @Inject
    lateinit var gamesRepository: GamesRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in setOf(Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_REMOVED, Intent.ACTION_PACKAGE_REPLACED)) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                gamesRepository.scanAndSaveGames().collect()
            } finally {
                pendingResult.finish()
            }
        }
    }
}

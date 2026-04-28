package com.gamelauncher.services

import android.service.quicksettings.TileService
import android.os.Build
import android.service.quicksettings.Tile
import com.gamelauncher.core.PerformanceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GameBoosterTileService : TileService() {

    @Inject lateinit var performanceManager: PerformanceManager
    
    private var isBoostActive = false

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        
        if (!isBoostActive) {
            enableBoost()
        } else {
            disableBoost()
        }
        
        updateTile()
    }

    private fun enableBoost() {
        isBoostActive = true
        performanceManager.boostThreadPriority()
        performanceManager.disableAnimations()
        performanceManager.startPerformanceSession(120)
    }

    private fun disableBoost() {
        isBoostActive = false
        performanceManager.restoreThreadPriority()
        performanceManager.restoreAnimations()
        performanceManager.stopPerformanceSession()
    }

    private fun updateTile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile?.let { tile ->
                tile.state = if (isBoostActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                tile.label = if (isBoostActive) "BOOST ON" else "Game Booster"
                tile.updateTile()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
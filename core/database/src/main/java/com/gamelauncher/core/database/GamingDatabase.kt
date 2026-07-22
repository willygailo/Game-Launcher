package com.gamelauncher.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * GamingDatabase — Main Room database instance for Game Launcher Pro core storage layer.
 */
@Database(
    entities = [GameProfileEntity::class],
    version = 1,
    exportSchema = true
)
abstract class GamingDatabase : RoomDatabase() {
    abstract fun gameProfileDao(): GameProfileDao

    companion object {
        const val DATABASE_NAME = "gaming_launcher.db"
    }
}

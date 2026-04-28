package com.gamelauncher.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.data.model.GamingSession

@Database(
    entities = [GameModel::class, GamingSession::class], 
    version = 2, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
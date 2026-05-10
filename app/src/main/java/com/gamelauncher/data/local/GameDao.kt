package com.gamelauncher.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.data.model.GamingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY lastLaunched DESC")
    fun getAllGames(): Flow<List<GameModel>>

    @Query("SELECT * FROM games WHERE packageName = :packageName")
    suspend fun getGameByPackageName(packageName: String): GameModel?

    @Query("SELECT packageName FROM games")
    suspend fun getAllGamePackages(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameModel>)

    @Update
    suspend fun updateGame(game: GameModel)

    @Delete
    suspend fun deleteGame(game: GameModel)
    
    @Query("DELETE FROM games WHERE packageName = :packageName")
    suspend fun deleteGameByPackage(packageName: String)

    @Query("SELECT * FROM gaming_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<GamingSession>>

    @Query("SELECT * FROM gaming_sessions WHERE packageName = :packageName ORDER BY startTime DESC")
    fun getSessionsForGame(packageName: String): Flow<List<GamingSession>>

    @Query("SELECT * FROM gaming_sessions WHERE packageName = :packageName ORDER BY startTime DESC LIMIT 1")
    suspend fun getLastSessionForGame(packageName: String): GamingSession?

    @Query("SELECT SUM(durationMs) FROM gaming_sessions WHERE packageName = :packageName")
    suspend fun getTotalPlayTimeForGame(packageName: String): Long?

    @Query("SELECT AVG(avgFps) FROM gaming_sessions WHERE packageName = :packageName")
    suspend fun getAverageFpsForGame(packageName: String): Float?

    @Query("SELECT COUNT(*) FROM gaming_sessions WHERE packageName = :packageName")
    suspend fun getSessionCountForGame(packageName: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GamingSession): Long

    @Update
    suspend fun updateSession(session: GamingSession)

    @Query("DELETE FROM gaming_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("DELETE FROM gaming_sessions")
    suspend fun deleteAllSessions()
}

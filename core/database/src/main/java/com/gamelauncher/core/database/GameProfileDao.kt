package com.gamelauncher.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * GameProfileDao — Data Access Object for managing game tuning profiles in Room database.
 */
@Dao
interface GameProfileDao {

    /**
     * Reactive Flow emitting all stored game profiles ordered by package name.
     */
    @Query("SELECT * FROM game_profiles ORDER BY package_name ASC")
    fun getAllGameProfiles(): Flow<List<GameProfileEntity>>

    /**
     * Reactive Flow observing a single game's profile by package name. Emits null if unprofiled.
     */
    @Query("SELECT * FROM game_profiles WHERE package_name = :packageName LIMIT 1")
    fun getGameProfileByPackage(packageName: String): Flow<GameProfileEntity?>

    /**
     * One-shot suspend query for immediate background retrieval during game launch service hooks.
     */
    @Query("SELECT * FROM game_profiles WHERE package_name = :packageName LIMIT 1")
    suspend fun getGameProfileDirect(packageName: String): GameProfileEntity?

    /**
     * Atomic upsert (INSERT or UPDATE) for game profiles, preserving row IDs without cascade deletes.
     */
    @Upsert
    suspend fun upsertGameProfile(profile: GameProfileEntity)

    /**
     * Suspend query deleting a game profile directly by package string.
     */
    @Query("DELETE FROM game_profiles WHERE package_name = :packageName")
    suspend fun deleteGameProfileByPackage(packageName: String)
}

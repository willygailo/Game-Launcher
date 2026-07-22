package com.gamelauncher.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * GameProfileEntity — Room entity storing custom performance tuning parameters per game package.
 * Uses nullable types (Float? = null, String? = null) to avoid magic numbers and explicitly differentiate
 * between "no override / system default" (null) vs explicit user-configured overrides.
 */
@Entity(tableName = "game_profiles")
data class GameProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "game_title")
    val gameTitle: String,

    @ColumnInfo(name = "custom_refresh_rate")
    val customRefreshRate: Float? = null,

    @ColumnInfo(name = "game_mode_enabled")
    val gameModeEnabled: Boolean = true,

    @ColumnInfo(name = "thermal_throttling_bypass")
    val thermalThrottlingBypass: Boolean = false,

    @ColumnInfo(name = "forced_cpu_governor")
    val forcedCpuGovernor: String? = null
)

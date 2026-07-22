package com.gamelauncher.core.database

import org.junit.Assert.assertEquals
import org.junit.Test

class GamingDatabaseTest {

    @Test
    fun testDatabaseNameConstant() {
        assertEquals("gaming_launcher.db", GamingDatabase.DATABASE_NAME)
    }

    @Test
    fun testGameProfileEntity_NullableDefaults() {
        val entity = GameProfileEntity(
            packageName = "com.epicgames.fortnite",
            gameTitle = "Fortnite"
        )

        assertEquals("com.epicgames.fortnite", entity.packageName)
        assertEquals("Fortnite", entity.gameTitle)
        assertEquals(null, entity.customRefreshRate)
        assertEquals(true, entity.gameModeEnabled)
        assertEquals(false, entity.thermalThrottlingBypass)
        assertEquals(null, entity.forcedCpuGovernor)
    }
}

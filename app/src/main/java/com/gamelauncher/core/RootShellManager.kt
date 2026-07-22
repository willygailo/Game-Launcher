// app/src/main/java/com/gamelauncher/core/RootShellManager.kt
package com.gamelauncher.core

import javax.inject.Inject
import javax.inject.Singleton

/**
 * RootShellManager — Non-root policy enforcer.
 * Game Launcher Pro operates strictly via Shizuku (UID 2000) on non-root devices.
 * Root/su execution is permanently disabled in compliance with Hard Constraint #1.
 */
@Singleton
class RootShellManager @Inject constructor() {

    suspend fun isRootAvailable(): Boolean {
        return false
    }

    suspend fun executeCommand(command: String): Pair<Boolean, String> {
        return Pair(false, "Root access is disabled. Operations route via Shizuku UserService AIDL.")
    }
}

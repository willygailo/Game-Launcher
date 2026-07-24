package com.gamelauncher.core.oemflags

import com.gamelauncher.core.device.OemBrand

/**
 * FlagScope — Target system configuration namespace.
 */
enum class FlagScope(val namespace: String) {
    GLOBAL("global"),
    SYSTEM("system"),
    SECURE("secure"),
    SHELL_CMD("shell"),
    SYSTEM_PROP("prop")
}

/**
 * FlagConfidence — Production verification status per OEM setting.
 */
enum class FlagConfidence {
    CONFIRMED_WORKING,
    NEEDS_TESTING
}

/**
 * ProbeStatus — Live capability state after dynamic probing.
 */
sealed interface ProbeStatus {
    object Unprobed : ProbeStatus
    data class Supported(val currentValue: String) : ProbeStatus
    data class Unsupported(val reason: String) : ProbeStatus
}

/**
 * OemFlag — Represents a cross-OEM hidden performance configuration setting.
 */
data class OemFlag(
    val id: String,
    val key: String,
    val scope: FlagScope,
    val targetOem: OemBrand,
    val title: String,
    val description: String,
    val activeValue: String,
    val defaultValue: String,
    val initialSnapshotValue: String? = null,
    val confidence: FlagConfidence,
    val minSdk: Int = 33,
    val status: ProbeStatus = ProbeStatus.Unprobed
)

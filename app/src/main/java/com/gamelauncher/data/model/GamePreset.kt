// app/src/main/java/com/gamelauncher/data/model/GamePreset.kt
package com.gamelauncher.data.model

data class GamePreset(
    val id: String,
    val gameName: String,
    val packageSignatures: List<String>,
    val badgeLabel: String,
    val targetFps: Int,
    val targetHz: Float,
    val touchSensitivity: Int,
    val gpuAcceleration: Boolean,
    val networkPriority: Boolean,
    val thermalBypass: Boolean,
    val description: String
) {
    companion object {
        val PUBG_MOBILE = GamePreset(
            id = "pubg_mobile",
            gameName = "PUBG Mobile",
            packageSignatures = listOf(
                "com.tencent.ig",
                "com.pubg.krmobile",
                "com.vng.pubgmobile",
                "com.pubg.imobile"
            ),
            badgeLabel = "PUBG ULTRA 120FPS",
            targetFps = 120,
            targetHz = 120f,
            touchSensitivity = 10,
            gpuAcceleration = true,
            networkPriority = true,
            thermalBypass = true,
            description = "120 FPS Extreme+ unlock, Skia Vulkan GPU drawing, and thermal throttle bypass for PUBG Mobile"
        )

        val MOBILE_LEGENDS = GamePreset(
            id = "mobile_legends",
            gameName = "Mobile Legends",
            packageSignatures = listOf(
                "com.mobile.legends"
            ),
            badgeLabel = "MLBB ULTRA TOUCH 120HZ",
            targetFps = 120,
            targetHz = 120f,
            touchSensitivity = 10,
            gpuAcceleration = true,
            networkPriority = true,
            thermalBypass = false,
            description = "Ultra 1ms Touch Slop, 120Hz frame lock, and low jitter ping stabilizer for MLBB"
        )

        val COD_MOBILE = GamePreset(
            id = "cod_mobile",
            gameName = "Call of Duty: Mobile",
            packageSignatures = listOf(
                "com.activision.callofduty.shooter",
                "com.garena.game.codm"
            ),
            badgeLabel = "CODM MAX REFRESH 144HZ",
            targetFps = 144,
            targetHz = 144f,
            touchSensitivity = 10,
            gpuAcceleration = true,
            networkPriority = true,
            thermalBypass = true,
            description = "144Hz Max panel lock, 480Hz touch sampling rate, and memory purge for CODM"
        )

        val ALL_PRESETS = listOf(PUBG_MOBILE, MOBILE_LEGENDS, COD_MOBILE)

        fun findPresetForPackage(packageName: String): GamePreset? {
            return ALL_PRESETS.firstOrNull { preset ->
                preset.packageSignatures.any { sig -> packageName.equals(sig, ignoreCase = true) }
            }
        }
    }
}

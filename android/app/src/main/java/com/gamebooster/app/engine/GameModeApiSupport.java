package com.gamebooster.app.engine;

import android.os.Build;

/**
 * Phase 2.1 — SDK gates for privileged GameMode shell commands.
 *
 * <p>The AIDL/Shizuku commands issued during Tier 1 were historically attempted
 * on every device (minSdk 24). On older Android versions several of them fail
 * silently or print to stderr yet were still counted as "applied". This class
 * centralizes the API-level knowledge: one recommendation gate (Android 14+,
 * the full command set) plus the per-command minimums for callers that want
 * granular behavior. All sdk-parameterized overloads are pure and unit-tested.
 */
public final class GameModeApiSupport {

    /** GameManager service (games framework) — Android 12. */
    public static final int MIN_GAME_MODE_API = 31;

    /** `device_config put game_overlay` namespace — Android 13. */
    public static final int MIN_GAME_OVERLAY_API = 33;

    /** `cmd game set --fps` and `cmd window set-app-refresh-rate` — Android 14. */
    public static final int MIN_APP_REFRESH_RATE_API = 34;

    private GameModeApiSupport() {
    }

    /** True when the full privileged GameMode command set is usable (Android 14+). */
    public static boolean isAvailable() {
        return isAvailable(Build.VERSION.SDK_INT);
    }

    /** Pure: full command set (game mode + fps override + overlay + refresh rate). */
    public static boolean isAvailable(int sdk) {
        return sdk >= MIN_APP_REFRESH_RATE_API;
    }

    /** `cmd game mode performance` (GameManager game modes). */
    public static boolean isGameModeApiAvailable(int sdk) {
        return sdk >= MIN_GAME_MODE_API;
    }

    /** `device_config put game_overlay` namespace. */
    public static boolean isGameOverlayApiAvailable(int sdk) {
        return sdk >= MIN_GAME_OVERLAY_API;
    }

    /** `cmd game set --fps` FPS override. */
    public static boolean isGameFpsOverrideAvailable(int sdk) {
        return sdk >= MIN_APP_REFRESH_RATE_API;
    }

    /** `cmd window set-app-refresh-rate`. */
    public static boolean isAppRefreshRateApiAvailable(int sdk) {
        return sdk >= MIN_APP_REFRESH_RATE_API;
    }

    /** Forces Android Game Mode Performance via Shizuku shell. */
    public static void setGameModePerformance(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommands(
                "cmd game mode performance " + packageName.trim(),
                "cmd game mode 2 " + packageName.trim()
        );
    }
}
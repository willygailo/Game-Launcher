package com.gamebooster.app.spoofer;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * ShizukuDisplayForcer — Forces System Display Refresh Rates, Game Drivers, and Game Mode API.
 *
 * Utilizes Shizuku (ADB shell UID 2000) to override global and system settings:
 * - min_refresh_rate and peak_refresh_rate (120Hz / 144Hz / 165Hz / 185Hz)
 * - updatable_driver_production_opt_in_apps (Vulkan / Production Game Driver routing)
 * - cmd game mode performance (Android 12+ Game Mode API)
 */
public final class ShizukuDisplayForcer {

    private static final String TAG = "ShizukuDisplayForcer";

    private ShizukuDisplayForcer() {}

    /**
     * Forces display refresh rate to target Hz using Shizuku system settings.
     */
    public static boolean forceDisplayRefreshRate(int targetHz) {
        if (targetHz <= 60) return true;

        List<String> commands = new ArrayList<>();
        commands.add("settings put system min_refresh_rate " + targetHz);
        commands.add("settings put system peak_refresh_rate " + targetHz);
        commands.add("settings put global user_refresh_rate " + targetHz);

        executeCommands(commands);
        Log.i(TAG, "Forced display refresh rate to " + targetHz + "Hz via Shizuku/ADB");
        return true;
    }

    /**
     * Forces the Updatable Game Driver (ANGLE / Vulkan pipeline) for the target game.
     */
    public static boolean forceGameDriverForPackage(@NonNull String packageName) {
        if (packageName.isEmpty()) return false;

        List<String> commands = new ArrayList<>();
        commands.add("settings put global game_driver_all_apps 1");
        commands.add("settings put global updatable_driver_all_apps 1");
        commands.add("settings put global updatable_driver_production_opt_in_apps \"" + packageName + "\"");
        commands.add("settings put global angle_gl_driver_selection_pkgs \"" + packageName + "\"");
        commands.add("settings put global angle_gl_driver_selection_values angle");

        executeCommands(commands);
        Log.i(TAG, "Forced Production Game Driver for " + packageName);
        return true;
    }

    /**
     * Forces the Android 12+ (API 31+) Game Mode to Performance Mode (mode 2).
     */
    public static boolean forceGameModePerformance(@NonNull String packageName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !packageName.isEmpty()) {
            String cmd = "cmd game mode performance " + packageName + " 2>/dev/null";
            executeCommand(cmd);
            Log.i(TAG, "Applied Android Game Mode Performance for " + packageName);
            return true;
        }
        return false;
    }

    /**
     * Restores default adaptive refresh rate settings.
     */
    public static void restoreDefaultDisplaySettings() {
        List<String> commands = new ArrayList<>();
        commands.add("settings delete system min_refresh_rate 2>/dev/null");
        commands.add("settings delete system peak_refresh_rate 2>/dev/null");
        commands.add("settings delete global user_refresh_rate 2>/dev/null");
        executeCommands(commands);
        Log.i(TAG, "Restored default display refresh rate settings");
    }

    private static void executeCommands(List<String> commands) {
        for (String cmd : commands) {
            executeCommand(cmd);
        }
    }

    private static void executeCommand(String command) {
        if (ShizukuFileManager.hasFullAccess()) {
            ShizukuExecutor.executeShizukuCommand(command);
        } else {
            CommandExecutor.executeSystemCommand(command);
        }
    }
}

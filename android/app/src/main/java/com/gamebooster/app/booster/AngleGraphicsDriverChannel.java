package com.gamebooster.app.booster;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * AngleGraphicsDriverChannel manages Android's ANGLE (Vulkan OpenGL ES translation layer)
 * and Game Driver selection settings to force hardware graphics acceleration.
 */
public class AngleGraphicsDriverChannel {

    private static final String TAG = "AngleDriverChannel";

    public static class AngleResult {
        public final boolean success;
        public final String message;

        public AngleResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static AngleResult enableAngleDriverForPackage(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return new AngleResult(false, "Invalid package name");
        }
        String pkg = packageName.trim().toLowerCase();

        Log.d(TAG, "Enabling ANGLE Vulkan Driver for " + pkg);

        // 1. Per-app ANGLE selection
        CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_selection_pkgs " + pkg);
        CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_selection_values angle");

        ShizukuExecutor.executeShizukuCommand("settings put global angle_gl_driver_selection_pkgs " + pkg);
        ShizukuExecutor.executeShizukuCommand("settings put global angle_gl_driver_selection_values angle");

        // 2. Set backend to Vulkan (2)
        CommandExecutor.executeSystemCommand("setprop debug.angle.backend 2");
        ShizukuExecutor.executeShizukuCommand("setprop debug.angle.backend 2");

        // 3. Opt-in Game Driver API
        CommandExecutor.executeSystemCommand("settings put global game_driver_opt_in_apps " + pkg);
        ShizukuExecutor.executeShizukuCommand("settings put global game_driver_opt_in_apps " + pkg);

        return new AngleResult(true, "ANGLE Vulkan Graphics Driver enabled for " + pkg);
    }

    public static AngleResult enableGlobalAngleDriver() {
        Log.d(TAG, "Enabling Global ANGLE Driver");

        CommandExecutor.executeSystemCommand("settings put global angle_gl_driver_all_angle 1");
        CommandExecutor.executeSystemCommand("settings put global game_driver_all_apps 1");
        CommandExecutor.executeSystemCommand("setprop debug.angle.backend 2");

        ShizukuExecutor.executeShizukuCommand("settings put global angle_gl_driver_all_angle 1");
        ShizukuExecutor.executeShizukuCommand("settings put global game_driver_all_apps 1");
        ShizukuExecutor.executeShizukuCommand("setprop debug.angle.backend 2");

        return new AngleResult(true, "Global ANGLE Vulkan Driver enabled across system");
    }

    public static AngleResult resetAngleDriver() {
        Log.d(TAG, "Resetting ANGLE Driver settings to default");

        CommandExecutor.executeSystemCommand("settings delete global angle_gl_driver_all_angle");
        CommandExecutor.executeSystemCommand("settings delete global angle_gl_driver_selection_pkgs");
        CommandExecutor.executeSystemCommand("settings delete global angle_gl_driver_selection_values");
        CommandExecutor.executeSystemCommand("settings delete global game_driver_all_apps");
        CommandExecutor.executeSystemCommand("setprop debug.angle.backend \"\"");

        ShizukuExecutor.executeShizukuCommand("settings delete global angle_gl_driver_all_angle");
        ShizukuExecutor.executeShizukuCommand("settings delete global angle_gl_driver_selection_pkgs");
        ShizukuExecutor.executeShizukuCommand("settings delete global angle_gl_driver_selection_values");
        ShizukuExecutor.executeShizukuCommand("settings delete global game_driver_all_apps");
        ShizukuExecutor.executeShizukuCommand("setprop debug.angle.backend \"\"");

        return new AngleResult(true, "Reset ANGLE graphics driver settings to stock default");
    }
}

package com.gamebooster.app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.util.Log;

import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.booster.TouchLatencyChannel;
import com.gamebooster.app.config.CfgProfileManager;
import com.gamebooster.app.config.CompetitiveCfgProfile;
import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.gamemanager.GameManagerLauncher;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.gamebooster.app.shizuku.ShizukuPermissionEnforcer;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    public static final String ACTION_LAUNCH_GAME = "com.gamebooster.app.ACTION_LAUNCH_GAME";
    public static final String ACTION_APPLY_CONFIG_ONLY = "com.gamebooster.app.ACTION_APPLY_CONFIG_ONLY";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            try {
                if (ShizukuManager.isShizukuRunningAndGranted()) {
                    ShizukuUserServiceConnector.getInstance().bindService();
                    ShizukuPermissionEnforcer.enforceAllPermissions(context);
                }
            } catch (Throwable ignored) {}

            GpuTweaksChannel.enableVulkanRenderer();
            TouchLatencyChannel.enableUltraTouchResponse();
        } else if (ACTION_LAUNCH_GAME.equals(action) || ACTION_APPLY_CONFIG_ONLY.equals(action)) {
            final PendingResult pendingResult = goAsync();
            final String rawPkg = intent.getStringExtra("package_name") != null ? intent.getStringExtra("package_name") : intent.getStringExtra("pkg");
            if (rawPkg == null || rawPkg.trim().isEmpty()) {
                Log.w(TAG, "No package_name specified in intent");
                pendingResult.finish();
                return;
            }
            final String pkg = rawPkg.trim();
            int resolvedFps = intent.getIntExtra("target_fps", 185);
            if (resolvedFps <= 0) resolvedFps = 185;
            final int fps = resolvedFps;

            com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                try {
                    Log.i(TAG, "Processing broadcast " + action + " for " + pkg + " @ " + fps + " FPS in background thread");
                    String gameKey = CfgProfileManager.resolveGameKey(pkg);
                    CompetitiveCfgProfile profile = new CompetitiveCfgProfile(gameKey, fps, true, true);
                    CfgProfileManager.saveProfile(context, profile);
                    GameProfilePreferences.setTargetHz(context, pkg, fps);

                    if (ACTION_APPLY_CONFIG_ONLY.equals(action)) {
                        GameManagerLauncher.preparePreLaunchConfigInjection(context, pkg, fps);
                    } else {
                        GameManagerLauncher.launchGame(context, pkg);
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "Error in broadcast action: " + t.getMessage(), t);
                } finally {
                    try {
                        pendingResult.finish();
                    } catch (Throwable ignored) {}
                }
            });
        }
    }
}

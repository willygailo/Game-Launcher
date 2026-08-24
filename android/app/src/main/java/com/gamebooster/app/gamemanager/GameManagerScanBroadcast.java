package com.gamebooster.app.gamemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.gamebooster.app.config.GameConfigStorageAccessEngine;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.shizuku.ShizukuPermissionEnforcer;
import com.gamebooster.app.spoofer.HardwareMaskEngine;

/**
 * GameManagerScanBroadcast — BroadcastReceiver that listens for new game installations
 * and updates. Automatically grants permissions, unlocks storage paths, and applies
 * optimal performance configurations via Shizuku upon installation.
 */
public class GameManagerScanBroadcast extends BroadcastReceiver {

    private static final String TAG = "GameManagerScan";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action) || Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            Uri data = intent.getData();
            if (data == null) return;
            String packageName = data.getSchemeSpecificPart();
            if (packageName == null || packageName.isEmpty()) return;

            Log.i(TAG, "New or updated package detected: " + packageName);

            AppExecutors.getInstance().executeCommand(() -> {
                String pkgLower = packageName.toLowerCase(java.util.Locale.ROOT);
                boolean isGame = GamePackageRegistry.isKnownGame(packageName)
                        || pkgLower.contains("game")
                        || pkgLower.contains("pubg")
                        || pkgLower.contains("cod")
                        || pkgLower.contains("mobilelegends");

                if (isGame) {
                    Log.i(TAG, "⚡ Auto-configuring newly installed game: " + packageName);
                    GameConfigStorageAccessEngine.grantAllPathsAccess(context, packageName);
                    HardwareMaskEngine.maskPackage(context, packageName);
                    GameProfileAutoConfigurator.autoConfigGamePackage(context, packageName, 185);
                    ShizukuPermissionEnforcer.enforceAllPermissions(context);
                }
            });
        }
    }
}

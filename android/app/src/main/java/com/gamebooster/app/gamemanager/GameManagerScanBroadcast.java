package com.gamebooster.app.gamemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GamePackageRegistry;

/**
 * GameManagerScanBroadcast — BroadcastReceiver that listens for new game installations
 * and updates. It refreshes launcher state and saves a capability-checked
 * display preference for recognized games; it never mutates another app.
 */
public class GameManagerScanBroadcast extends BroadcastReceiver {

    private static final String TAG = "GameManagerScan";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)
                || Intent.ACTION_PACKAGE_REPLACED.equals(action)
                || Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            Uri data = intent.getData();
            if (data == null) return;
            String packageName = data.getSchemeSpecificPart();
            if (packageName == null || packageName.isEmpty()) return;

            // Invalidate in-memory game repository cache immediately
            com.gamebooster.app.games.GameManagerRepository.invalidateCache();

            if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                Log.i(TAG, "Package uninstalled: " + packageName);
                return;
            }

            Log.i(TAG, "New or updated package detected: " + packageName);

            AppExecutors.getInstance().executeCommand(() -> {
                String pkgLower = packageName.toLowerCase(java.util.Locale.ROOT);
                boolean isGame = GamePackageRegistry.isKnownGame(packageName)
                        || pkgLower.contains("game")
                        || pkgLower.contains("pubg")
                        || pkgLower.contains("cod")
                        || pkgLower.contains("mobilelegends");

                if (isGame) {
                    int displayHz = GameProfileAutoConfigurator.getTargetFpsHz(context);
                    GameProfileAutoConfigurator.autoConfigGamePackage(context, packageName, displayHz);
                    Log.i(TAG, "Saved " + displayHz + "Hz display preference for " + packageName);
                }
            });
        }
    }
}

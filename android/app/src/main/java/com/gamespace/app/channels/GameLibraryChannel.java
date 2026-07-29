package com.gamespace.app.channels;

import android.content.Context;
import android.content.Intent;

import com.gamespace.app.core.GameAppInfo;
import com.gamespace.app.data.CommandExecutor;
import com.gamespace.app.data.GameManagerRepository;

import java.util.List;

public class GameLibraryChannel {

    public static List<GameAppInfo> getInstalledGames(Context context) {
        return GameManagerRepository.getInstalledGames(context);
    }

    public static boolean setAndroidGameMode(String packageName, boolean performanceMode) {
        if (packageName == null || packageName.isEmpty()) return false;
        String mode = performanceMode ? "performance" : "standard";
        String cmd = "cmd game mode " + mode + " " + packageName;
        String res = CommandExecutor.executeSystemCommand(cmd);
        return CommandExecutor.isSuccessOutput(res);
    }

    public static boolean compileGameAotSpeed(String packageName) {
        if (packageName == null || packageName.isEmpty()) return false;
        String cmd = "cmd package compile -m speed " + packageName;
        String res = CommandExecutor.executeSystemCommand(cmd);
        return CommandExecutor.isSuccessOutput(res);
    }

    public static boolean launchGame(Context context, String packageName) {
        if (context == null || packageName == null) return false;

        // Auto-optimize game on launch
        setAndroidGameMode(packageName, true);
        HzFpsChannel.forceGameFps(packageName, 120);
        PerformanceChannel.executeOneTapBoost(context);

        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
            return true;
        }
        return false;
    }
}

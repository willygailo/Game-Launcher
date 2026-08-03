package com.gamebooster.app.games;

import com.gamebooster.app.functions.HzFpsChannel;
import com.gamebooster.app.functions.PerformanceChannel;

import android.content.Context;
import android.content.Intent;

import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.root.CommandExecutor;
import com.gamebooster.app.games.GameManagerRepository;

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
        int targetHz = GameProfileAutoConfigurator.getTargetFpsHz(context);
        HzFpsChannel.forceGameFps(context, packageName, targetHz);
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

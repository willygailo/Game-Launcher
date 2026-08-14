package com.gamebooster.app.feature.performance.booster;

import android.content.Context;
import android.os.Build;
import com.gamebooster.app.platform.shell.CommandExecutor;

/**
 * GameManagerAdapter — Integration bridge for Android 12+ (API 31 to 36) GameManager framework.
 *
 * Interacts with system GameManager services to request Performance mode,
 * and falls back to privileged Shizuku IPC (`cmd game set`) when explicit permission is required.
 */
public class GameManagerAdapter {

    public static final int GAME_MODE_UNSUPPORTED = 0;
    public static final int GAME_MODE_STANDARD    = 1;
    public static final int GAME_MODE_PERFORMANCE = 2;
    public static final int GAME_MODE_BATTERY     = 3;
    public static final int GAME_MODE_CUSTOM      = 4;

    private static final String GAME_SERVICE_NAME = "game";

    private final Context context;

    public GameManagerAdapter(Context context) {
        this.context = context != null ? context.getApplicationContext() : null;
    }

    /**
     * Attempts to set the Game Mode for a target package using system GameManager API or Shizuku CLI fallback.
     *
     * @param packageName Package name of the target game.
     * @param gameMode Target game mode constant (e.g. GAME_MODE_PERFORMANCE).
     * @return true if applied successfully or commanded via Shizuku.
     */
    @SuppressWarnings("WrongConstant")
    public boolean setGameMode(String packageName, int gameMode) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context != null) {
            try {
                Object gameManager = context.getSystemService(GAME_SERVICE_NAME);
                if (gameManager != null) {
                    java.lang.reflect.Method setModeMethod = gameManager.getClass().getMethod("setGameMode", String.class, int.class);
                    setModeMethod.invoke(gameManager, packageName, gameMode);
                    return true;
                }
            } catch (Throwable ignored) {
                // Hidden system API or MANAGE_GAME_MODE permission missing; fallback to Shizuku shell below.
            }
        }

        // Shizuku Privileged Shell Command Fallback (`cmd game set --mode <mode> <packageName>`)
        String modeName;
        switch (gameMode) {
            case GAME_MODE_PERFORMANCE:
                modeName = "2";
                break;
            case GAME_MODE_BATTERY:
                modeName = "3";
                break;
            case GAME_MODE_CUSTOM:
                modeName = "4";
                break;
            case GAME_MODE_STANDARD:
            default:
                modeName = "1";
                break;
        }

        String cmd = "cmd game set --mode " + modeName + " " + packageName;
        String shellResult = CommandExecutor.executeSystemCommand(cmd);
        CommandExecutor.executeSystemCommand("cmd game mode performance " + packageName);
        return CommandExecutor.isSuccessOutput(shellResult);
    }

    /**
     * Sets Game Mode and FPS simultaneously via Android Game Mode framework.
     */
    public boolean setGameModeAndFps(String packageName, int gameMode, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return false;
        }

        setGameMode(packageName, gameMode);
        if (targetFps > 0) {
            setSurfaceFrameRateHint(packageName, targetFps);
            applyGameOverlay(packageName, gameMode, targetFps, 1.0f, false);
        }
        return true;
    }

    /**
     * Applies official AOSP Game Overlay intervention for target game package.
     */
    public boolean applyGameOverlay(String packageName, int gameMode, int targetFps, float downscaleFactor, boolean useAngle) {
        if (packageName == null || packageName.trim().isEmpty() || "global".equalsIgnoreCase(packageName.trim())) {
            return false;
        }

        int modeVal = gameMode > 0 ? gameMode : GAME_MODE_PERFORMANCE;
        StringBuilder config = new StringBuilder();
        config.append("mode=").append(modeVal);
        if (targetFps > 0) {
            config.append(",fps=").append(targetFps);
        }
        if (downscaleFactor > 0.0f && downscaleFactor < 1.0f) {
            config.append(String.format(java.util.Locale.US, ",downscaleFactor=%.2f", downscaleFactor));
        }
        if (useAngle) {
            config.append(",useAngle=true");
        }

        String cmd = "device_config put game_overlay " + packageName + " " + config.toString();
        String result = CommandExecutor.executeSystemCommand(cmd);
        return CommandExecutor.isSuccessOutput(result);
    }

    /**
     * Resets Game Mode interventions for target package.
     */
    public boolean resetGameMode(String packageName) {
        if (packageName == null || packageName.trim().isEmpty() || "global".equalsIgnoreCase(packageName.trim())) {
            return false;
        }
        CommandExecutor.executeSystemCommand("cmd game reset " + packageName);
        CommandExecutor.executeSystemCommand("device_config delete game_overlay " + packageName);
        return true;
    }

    /**
     * Queries the current active Game Mode for a target package (API 31+).
     *
     * @param packageName Target package name.
     * @return Active game mode integer constant.
     */
    @SuppressWarnings("WrongConstant")
    public int getGameMode(String packageName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context != null && packageName != null) {
            try {
                Object gameManager = context.getSystemService(GAME_SERVICE_NAME);
                if (gameManager != null) {
                    try {
                        java.lang.reflect.Method getModeMethod = gameManager.getClass().getMethod("getGameMode", String.class);
                        Object res = getModeMethod.invoke(gameManager, packageName);
                        if (res instanceof Integer) {
                            return (Integer) res;
                        }
                    } catch (NoSuchMethodException e) {
                        java.lang.reflect.Method getModeNoArg = gameManager.getClass().getMethod("getGameMode");
                        Object res = getModeNoArg.invoke(gameManager);
                        if (res instanceof Integer) {
                            return (Integer) res;
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
        return GAME_MODE_UNSUPPORTED;
    }

    /**
     * Injects a SurfaceFlinger frame rate hint command for the target package.
     *
     * @param packageName Target package name.
     * @param frameRate Desired rendering frame rate (e.g. 165.0f).
     * @return true if command succeeded.
     */
    public boolean setSurfaceFrameRateHint(String packageName, float frameRate) {
        if (packageName == null || packageName.trim().isEmpty() || "global".equalsIgnoreCase(packageName.trim())) {
            return false;
        }
        String cmd = "cmd game set --fps " + ((int) frameRate) + " " + packageName;
        String shellResult = CommandExecutor.executeSystemCommand(cmd);
        return CommandExecutor.isSuccessOutput(shellResult);
    }
}


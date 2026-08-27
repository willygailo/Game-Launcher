package com.gamebooster.app.engine;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.HomeGameScanner;
import com.gamebooster.app.shizuku.RishManager;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

import java.util.List;

/**
 * ArtCompilerEngine — Android Runtime (ART) Ahead-Of-Time (AOT) Compiler Engine.
 *
 * Utilizes elevated Shizuku / Rish / Shell execution to invoke `cmd package compile -m speed -f <pkg>`
 * and `dumpsys package <pkg>` to compile Dalvik/ART bytecode into native machine instructions
 * ahead of time, eliminating runtime JIT compilation overhead, frame drops, and asset loading stutters.
 */
public final class ArtCompilerEngine {

    private static final String TAG = "ArtCompilerEngine";

    private ArtCompilerEngine() {}

    public enum CompileFilter {
        SPEED("speed", "Maximum Native AOT (Zero JIT Overhead)"),
        SPEED_PROFILE("speed-profile", "Profile-Guided AOT (Balanced)"),
        QUICKEN("quicken", "Fast Quicken (Basic Bytecode Tuning)");

        public final String filterName;
        public final String description;

        CompileFilter(String filterName, String description) {
            this.filterName = filterName;
            this.description = description;
        }
    }

    public interface CompileCallback {
        void onProgress(String message);
        void onComplete(boolean success, String details);
    }

    public interface BatchCompileCallback {
        void onGameStarted(String packageName, String gameLabel, int currentIndex, int totalGames);
        void onGameFinished(String packageName, boolean success);
        void onAllFinished(int successCount, int totalGames);
    }

    /**
     * Checks whether elevated shell/Shizuku access is ready to execute compilation commands.
     */
    public static boolean isCompilerAvailable() {
        return ShizukuExecutor.hasShizukuPermission()
                || ShizukuUserServiceConnector.getInstance().isServiceConnected()
                || RishManager.isRishAvailable()
                || ShellExecutor.isRootSuAvailable();
    }

    /**
     * Executes elevated shell command returning the raw output.
     */
    private static String executePrivileged(String command) {
        if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            String out = ShizukuUserServiceConnector.getInstance().executeCommand(command);
            if (out != null) return out;
        }

        if (ShizukuExecutor.hasShizukuPermission()) {
            String out = ShizukuExecutor.executeShizukuCommand(command);
            if (out != null && !out.startsWith("ERROR")) return out;
        }

        if (RishManager.isRishAvailable()) {
            String out = RishManager.executeRishCommand(null, command);
            if (out != null && !out.startsWith("ERROR")) return out;
        }

        if (ShellExecutor.isRootSuAvailable()) {
            ShellExecutor.CommandResult cr = ShellExecutor.executeCommand(command, true);
            if (cr != null && cr.isSuccess()) return cr.stdout;
        }

        ShellExecutor.CommandResult cr = ShellExecutor.executeCommand(command, false);
        return cr != null ? cr.stdout : "";
    }

    /**
     * Compiles a single package synchronously.
     */
    public static boolean compilePackageSync(String packageName, CompileFilter filter) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String mode = (filter != null ? filter.filterName : CompileFilter.SPEED.filterName);
        String cmd = "cmd package compile -m " + mode + " -f " + packageName.trim();
        String output = executePrivileged(cmd);
        Log.i(TAG, "compilePackageSync [" + packageName + "] (" + mode + "): " + output);
        return output != null && (output.contains("Success") || (!output.contains("Error") && !output.contains("Exception")));
    }

    /**
     * Compiles a single package asynchronously with UI callbacks.
     */
    public static void compilePackageAsync(String packageName, CompileFilter filter, CompileCallback callback) {
        if (packageName == null || packageName.trim().isEmpty()) {
            if (callback != null) callback.onComplete(false, "Invalid package name");
            return;
        }

        final String pkg = packageName.trim();
        final CompileFilter targetFilter = (filter != null ? filter : CompileFilter.SPEED);

        AppExecutors.getInstance().executeCommand(() -> {
            if (callback != null) {
                AppExecutors.getInstance().postToMainThread(() ->
                        callback.onProgress("⚡ Triggering ART dex2oat (" + targetFilter.filterName + ") for " + pkg + "..."));
            }

            boolean success = compilePackageSync(pkg, targetFilter);
            String status = getCompilationStatus(pkg);

            if (callback != null) {
                AppExecutors.getInstance().postToMainThread(() ->
                        callback.onComplete(success, "Status: " + status));
            }
        });
    }

    /**
     * Resets dexopt status of a package back to default.
     */
    public static void resetPackageDexoptAsync(String packageName, CompileCallback callback) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        final String pkg = packageName.trim();

        AppExecutors.getInstance().executeCommand(() -> {
            String cmd = "cmd package compile --reset " + pkg;
            String out = executePrivileged(cmd);
            boolean success = out != null && !out.contains("Error");
            if (callback != null) {
                AppExecutors.getInstance().postToMainThread(() ->
                        callback.onComplete(success, "Reset completed"));
            }
        });
    }

    /**
     * Inspects package compilation status from dumpsys.
     */
    public static String getCompilationStatus(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return "Unknown";
        String out = executePrivileged("dumpsys package " + packageName.trim() + " | grep -E \"dexopt|compilation filter|status=\" 2>/dev/null");
        if (out == null || out.trim().isEmpty()) {
            return "Installed";
        }
        out = out.trim();
        if (out.contains("status=speed") || out.contains("filter=[speed]") || out.contains("compilation_filter=speed")) {
            return "⚡ Speed (Fully Native AOT)";
        } else if (out.contains("speed-profile")) {
            return "🚀 Speed-Profile (Profile AOT)";
        } else if (out.contains("quicken")) {
            return "⚙️ Quicken";
        } else if (out.contains("verify") || out.contains("extract")) {
            return "📦 Verify/Extract (JIT Only)";
        }
        return "⚡ AOT Optimized";
    }

    /**
     * Compiles all installed competitive games sequentially in the background.
     */
    public static void compileAllInstalledGamesAsync(Context context, CompileFilter filter, BatchCompileCallback callback) {
        if (context == null) return;
        final Context appContext = context.getApplicationContext();
        final CompileFilter targetFilter = (filter != null ? filter : CompileFilter.SPEED);

        AppExecutors.getInstance().executeCommand(() -> {
            List<GameAppInfo> installedGames = HomeGameScanner.scanTargetGames(appContext);
            if (installedGames == null || installedGames.isEmpty()) {
                if (callback != null) {
                    AppExecutors.getInstance().postToMainThread(() -> callback.onAllFinished(0, 0));
                }
                return;
            }

            int total = installedGames.size();
            int successCount = 0;

            for (int i = 0; i < total; i++) {
                GameAppInfo game = installedGames.get(i);
                final int currentIdx = i + 1;
                final String pkg = game.getPackageName();
                final String label = game.getLabel();

                if (callback != null) {
                    AppExecutors.getInstance().postToMainThread(() ->
                            callback.onGameStarted(pkg, label, currentIdx, total));
                }

                boolean ok = compilePackageSync(pkg, targetFilter);
                if (ok) successCount++;

                if (callback != null) {
                    final boolean finalOk = ok;
                    AppExecutors.getInstance().postToMainThread(() ->
                            callback.onGameFinished(pkg, finalOk));
                }
            }

            final int finalSuccessCount = successCount;
            if (callback != null) {
                AppExecutors.getInstance().postToMainThread(() ->
                        callback.onAllFinished(finalSuccessCount, total));
            }
        });
    }
}

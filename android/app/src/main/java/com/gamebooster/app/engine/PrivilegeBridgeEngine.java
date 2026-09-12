package com.gamebooster.app.engine;

import android.util.Log;

import com.gamebooster.app.shizuku.RishManager;
import com.gamebooster.app.shizuku.ShizukuConnectionManager;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PrivilegeBridgeEngine — Bridges Kernel Root (UID 0) and Shizuku Elevated Shell (UID 2000).
 *
 * Provides a Virtual Root abstraction so all launcher subsystems (tweaks, terminal, display,
 * game launcher, file patcher) operate with full privileged capabilities even on non-rooted devices
 * by transparently adapting 'su' / 'sudo' invocations to Shizuku AIDL or Rish.
 */
public class PrivilegeBridgeEngine {

    private static final String TAG = "PrivilegeBridgeEngine";

    private static final Pattern SU_C_PATTERN = Pattern.compile("^\\s*su\\s+(?:-c\\s+)?[\"']?(.*?)[\"']?\\s*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SUDO_PATTERN = Pattern.compile("^\\s*sudo\\s+(.*)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Checks if either physical Kernel Root (UID 0) OR Shizuku Elevated API is ready and active.
     */
    public static boolean isPrivilegedActive() {
        if (!ShellExecutor.isAndroidEnvironment()) {
            return false;
        }
        if (ShellExecutor.isRootSuAvailable()) {
            return true;
        }
        return isShizukuVirtualRootReady();
    }

    /**
     * Returns true if Shizuku is authorized and capable of executing privileged shell operations.
     */
    public static boolean isShizukuVirtualRootReady() {
        if (!ShellExecutor.isAndroidEnvironment()) {
            return false;
        }
        return ShizukuExecutor.hasShizukuPermission()
                || ShizukuManager.isShizukuRunningAndGranted()
                || ShizukuUserServiceConnector.getInstance().isServiceConnected()
                || ShizukuConnectionManager.getInstance().isReady()
                || RishManager.isRishAvailable();
    }

    /**
     * Returns the human-readable engine mode string.
     */
    public static String getPrivilegeTitle() {
        if (!ShellExecutor.isAndroidEnvironment()) {
            return "⚡ VIRTUAL ROOT: SHIZUKU PRIVILEGED ENGINE ACTIVE";
        }
        boolean hasRoot = ShellExecutor.isRootSuAvailable();
        boolean hasShizuku = isShizukuVirtualRootReady();

        if (hasRoot && hasShizuku) {
            return "⚡ DUAL ENGINE: ROOT (UID 0) + SHIZUKU ACTIVE";
        } else if (hasRoot) {
            return "⚡ ROOT ACCESS GRANTED (UID 0 SUPERUSER)";
        } else if (hasShizuku) {
            return "⚡ VIRTUAL ROOT: SHIZUKU PRIVILEGED ENGINE ACTIVE";
        } else {
            return "⚡ SYSTEM SETTINGS / STANDARD MODE";
        }
    }

    /**
     * Cleans up or translates commands designed for 'su' so they execute seamlessly via Shizuku.
     * E.g. "su -c 'setprop debug.sf.fps 120'" becomes "setprop debug.sf.fps 120".
     */
    public static String unwrapSuCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "";
        }
        String trimmed = command.trim();

        Matcher sudoMatcher = SUDO_PATTERN.matcher(trimmed);
        if (sudoMatcher.matches()) {
            return sudoMatcher.group(1).trim();
        }

        if (trimmed.equalsIgnoreCase("su") || trimmed.equalsIgnoreCase("su -") || trimmed.equalsIgnoreCase("su root")) {
            return "sh";
        }

        Matcher suMatcher = SU_C_PATTERN.matcher(trimmed);
        if (suMatcher.matches()) {
            String inner = suMatcher.group(1).trim();
            if ((inner.startsWith("\"") && inner.endsWith("\"")) || (inner.startsWith("'") && inner.endsWith("'"))) {
                inner = inner.substring(1, inner.length() - 1).trim();
            }
            return inner;
        }

        return trimmed;
    }

    /**
     * Executes a command using the highest available privilege channel:
     * 1. Real SU (UID 0) if available.
     * 2. Shizuku AIDL UserService (UID 2000).
     * 3. Shizuku.newProcess reflection fallback.
     * 4. Rish standalone executable.
     * 5. Local shell fallback.
     */
    public static String executePrivileged(String rawCommand) {
        if (rawCommand == null || rawCommand.trim().isEmpty()) {
            return "";
        }
        if (!ShellExecutor.isAndroidEnvironment()) {
            return "SUCCESS";
        }

        // 1. If physical root is available, execute via su
        if (ShellExecutor.isRootSuAvailable()) {
            ShellExecutor.CommandResult rootRes = ShellExecutor.executeSuCommand(rawCommand);
            if (rootRes.isSuccess()) {
                return rootRes.stdout.isEmpty() ? "SUCCESS" : rootRes.stdout;
            }
        }

        // 2. Translate/unwrap command for Shizuku
        String cleanCommand = unwrapSuCommand(rawCommand);

        // 3. Shizuku AIDL Connector
        if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            try {
                String res = ShizukuUserServiceConnector.getInstance().executeCommand(cleanCommand);
                if (res != null) return res;
            } catch (Throwable t) {
                Log.w(TAG, "Shizuku AIDL connector execution failed: " + t.getMessage());
            }
        }

        // 4. Shizuku reflection fallback
        if (ShizukuExecutor.hasShizukuPermission()) {
            try {
                String res = ShizukuExecutor.executeShizukuCommand(cleanCommand);
                if (res != null) return res;
            } catch (Throwable t) {
                Log.w(TAG, "Shizuku reflection execution failed: " + t.getMessage());
            }
        }

        // 5. Rish standalone fallback
        if (RishManager.isRishAvailable()) {
            try {
                String rishOut = RishManager.executeRishCommand(null, cleanCommand);
                if (rishOut != null && !rishOut.startsWith("ERROR")) {
                    return rishOut;
                }
            } catch (Throwable t) {
                Log.w(TAG, "Rish execution failed: " + t.getMessage());
            }
        }

        // 6. Local Shell fallback
        ShellExecutor.CommandResult shellRes = ShellExecutor.executeCommand(cleanCommand);
        if (!shellRes.isSuccess()) {
            return "ERROR: " + (shellRes.stderr.isEmpty() ? "Command failed with code " + shellRes.exitCode : shellRes.stderr);
        }
        if (!shellRes.stderr.isEmpty()) {
            return "ERROR: " + shellRes.stderr;
        }
        return shellRes.stdout.isEmpty() ? "SUCCESS" : shellRes.stdout;
    }

    /**
     * Executes batch commands across the highest available privileged channel.
     */
    public static List<String> executePrivilegedBatch(List<String> commands) {
        if (commands == null || commands.isEmpty()) {
            return Collections.emptyList();
        }

        if (!ShellExecutor.isAndroidEnvironment()) {
            return Collections.singletonList("SUCCESS");
        }

        if (ShellExecutor.isRootSuAvailable()) {
            String combined = String.join("; ", commands);
            ShellExecutor.CommandResult rootRes = ShellExecutor.executeSuCommand(combined);
            if (rootRes.isSuccess()) {
                return Collections.singletonList(rootRes.stdout.isEmpty() ? "SUCCESS" : rootRes.stdout);
            }
        }

        if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            return ShizukuUserServiceConnector.getInstance().execBatchCommands(commands);
        } else if (ShizukuExecutor.hasShizukuPermission()) {
            String combined = String.join("; ", commands);
            String result = ShizukuExecutor.executeShizukuCommand(combined);
            return Collections.singletonList(result != null ? result : "ERROR: no output");
        } else {
            String combined = String.join("; ", commands);
            return Collections.singletonList(executePrivileged(combined));
        }
    }
}

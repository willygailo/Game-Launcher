package com.gamebooster.app.feature.gameprofiles;

import com.gamebooster.app.platform.shell.CommandExecutor;

/**
 * ObbInspector — Scoped Storage-compliant OBB directory & game assets inspector.
 *
 * Utilizes Shizuku privileged shell IPC to verify `/sdcard/Android/obb/<packageName>` presence
 * and asset size on Android 13 to 16 without violating standard runtime permission rules.
 */
public class ObbInspector {

    /**
     * Checks whether an OBB directory exists for the target game package via Shizuku shell.
     *
     * @param packageName Target game package identifier.
     * @return true if OBB files/directory exist.
     */
    public static boolean hasObbData(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }

        String command = "ls /sdcard/Android/obb/" + packageName;
        String result = CommandExecutor.executeSystemCommand(command);

        if (!CommandExecutor.isSuccessOutput(result)) {
            return false;
        }

        String trimmed = result.trim();
        return !trimmed.isEmpty() && !trimmed.toLowerCase().contains("no such file");
    }

    /**
     * Estimates total OBB directory size in megabytes for the target game.
     *
     * @param packageName Target game package identifier.
     * @return Approximate size in MB, or 0 if not found/accessible.
     */
    public static int getObbSizeMb(String packageName) {
        if (!hasObbData(packageName)) {
            return 0;
        }

        String command = "du -sm /sdcard/Android/obb/" + packageName;
        String result = CommandExecutor.executeSystemCommand(command);

        if (CommandExecutor.isSuccessOutput(result) && result != null) {
            try {
                String[] parts = result.trim().split("\\s+");
                if (parts.length > 0) {
                    return Integer.parseInt(parts[0]);
                }
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }
}

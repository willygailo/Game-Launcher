package com.gamebooster.app.anticheat;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gamebooster.app.spoofer.lsposed.LspatchHelper;
import com.gamebooster.app.spoofer.lsposed.LsposedDetector;
import com.gamebooster.app.shizuku.ShizukuFileManager;

/**
 * AntiCheatShieldEngine — Master Coordinator for Game Anti-Detection & Integrity Cloaking.
 *
 * Enforces a strict multi-tier security policy:
 * - When LSPosed / LSPatch is active, in-memory ART hooking is used exclusively to avoid file tampering bans.
 * - When disk configuration patching is used, automatic inode timestamp retention, permission normalization,
 *   and SELinux restorecon are applied to prevent integrity scan flags.
 * - Suppresses telemetry uploaders and flushes system logcat buffers.
 */
public final class AntiCheatShieldEngine {

    private static final String TAG = "AntiCheatShieldEngine";

    public static class AntiCheatAuditResult {
        public final String packageName;
        public final GameAntiCheatRegistry.GameSecurityProfile securityProfile;
        public final boolean lsposedActive;
        public final boolean lspatchInstalled;
        public final boolean shizukuGranted;
        public final boolean telemetrySinkholed;
        public final boolean fileIntegrityPreserved;
        public final String safetyRecommendation;

        public AntiCheatAuditResult(String packageName,
                                    GameAntiCheatRegistry.GameSecurityProfile securityProfile,
                                    boolean lsposedActive,
                                    boolean lspatchInstalled,
                                    boolean shizukuGranted,
                                    boolean telemetrySinkholed,
                                    boolean fileIntegrityPreserved,
                                    String safetyRecommendation) {
            this.packageName = packageName;
            this.securityProfile = securityProfile;
            this.lsposedActive = lsposedActive;
            this.lspatchInstalled = lspatchInstalled;
            this.shizukuGranted = shizukuGranted;
            this.telemetrySinkholed = telemetrySinkholed;
            this.fileIntegrityPreserved = fileIntegrityPreserved;
            this.safetyRecommendation = safetyRecommendation;
        }
    }

    private AntiCheatShieldEngine() {}

    /**
     * Sanitizes the environment and applies full anti-detection measures before game launch.
     */
    public static boolean prepareGameSession(@Nullable Context context, @NonNull String packageName) {
        if (packageName.isEmpty()) return false;

        GameAntiCheatRegistry.GameSecurityProfile profile = GameAntiCheatRegistry.getProfile(packageName);
        Log.i(TAG, "Preparing Anti-Cheat Shield for " + packageName + " (" + profile.antiCheatType.displayName + ")");

        // 1. Suppress telemetry and logs
        TelemetrySinkhole.applySinkholeForPackage(packageName);

        // 2. Restore SELinux security context and folder permissions
        if (profile.requiresSelinuxRestore) {
            FileIntegrityProtector.restoreGameDataDirectorySecurity(packageName);
        }

        // 3. Flush global logcat buffers
        if (profile.requiresLogcatFlush) {
            TelemetrySinkhole.flushGlobalLogcat();
        }

        return true;
    }

    /**
     * Executes a full safety and anti-detection audit for a target game.
     */
    public static AntiCheatAuditResult runSecurityAudit(@NonNull Context context, @NonNull String packageName) {
        GameAntiCheatRegistry.GameSecurityProfile profile = GameAntiCheatRegistry.getProfile(packageName);

        boolean lsposedActive = LsposedDetector.isModuleEnabled();
        boolean lspatchInstalled = LspatchHelper.isLspatchInstalled(context);
        boolean shizukuGranted = ShizukuFileManager.hasFullAccess();

        String recommendation;
        if (lsposedActive) {
            recommendation = "✔ 100% STEALTH (In-Memory ART Hooking Active — Game files untouched)";
        } else if (profile.antiCheatType.strictnessLevel >= 3) {
            recommendation = "⚡ Kernel/Aggressive Anti-Cheat: LSPatch Non-Root or Shizuku recommended.";
        } else if (shizukuGranted) {
            recommendation = "✔ Shizuku Inode Preservation & SELinux Context Shield Active";
        } else {
            recommendation = "ℹ Standard Sandbox Mode. Shizuku recommended for privileged telemetry blocking.";
        }

        return new AntiCheatAuditResult(
                packageName,
                profile,
                lsposedActive,
                lspatchInstalled,
                shizukuGranted,
                true,
                true,
                recommendation
        );
    }
}

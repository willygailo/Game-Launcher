package com.gamebooster.app.spoofer.lsposed;

import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.spoofer.SpoofProfile;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * SpoofModule — LSPosed / LSPatch module entry (registered in assets/xposed_init).
 *
 * Loaded by the LSPosed framework (or embedded LSPatch loader) inside scoped game processes.
 * Applies in-memory hardware, identity, GPU, RAM, display refresh rate, and anti-detection hooks
 * at ART level — zero game files modified on disk, eliminating file-tampering bans.
 */
public class SpoofModule implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        try {
            if (lpparam.packageName == null) return;
            // Never hook the launcher itself or critical system services
            if (isExcludedPackage(lpparam.packageName)) return;

            boolean allApps = SpoofConfigBridge.isSpoofAllApps();
            boolean knownGame = GamePackageRegistry.isKnownGame(lpparam.packageName);
            if (!allApps && !knownGame) return;

            SpoofProfile profile = SpoofConfigBridge.resolveProfile(lpparam.packageName);
            if (profile == null) return;

            XposedBridge.log("[GameBooster] SpoofModule active for " + lpparam.packageName
                    + " -> " + profile.displayName + " [" + profile.model + "]");

            // 1. Anti-Detection Hooks FIRST (hides root, xposed, lsposed, lspatch, magisk, ksu)
            AntiDetectionHooks.apply(lpparam);

            // 2. Android Build & Version Identity Override
            BuildHooks.apply(lpparam, profile);

            // 3. Multi-Partition SystemProperties Override (ro.*)
            SystemPropertiesHooks.apply(lpparam, profile);

            // 4. High Refresh Rate & Display Mode Hooking (120Hz/144Hz/165Hz/185Hz)
            DisplayHooks.apply(lpparam, profile);

            // 5. OpenGL ES & EGL GPU Hooking (Adreno / Immortalis)
            GlesHooks.apply(lpparam, profile);

            // 6. Runtime CPU Cores & Memory Limits Hooking
            RuntimeMemoryHooks.apply(profile);

            // 7. ActivityManager RAM Telemetry Hooking
            RamInfoHooks.apply(lpparam, profile);

            // 8. /proc/cpuinfo, /proc/meminfo, and Shell Execution Interception
            ProcFileHooks.apply(lpparam, profile);

            // 9. Telephony, Hardware Identifiers, Android ID & User-Agent Hooking
            IdentityHooks.apply(lpparam, profile);

            XposedBridge.log("[GameBooster] All 9 SpoofModule hook layers successfully installed for " + lpparam.packageName);
        } catch (Throwable t) {
            XposedBridge.log("[GameBooster] SpoofModule initialization error in " + lpparam.packageName + ": " + t);
        }
    }

    public static boolean isExcludedPackage(String pkg) {
        if (pkg == null) return true;
        return pkg.equals("com.gamebooster.app")
                || pkg.equals("android")
                || pkg.equals("com.android.systemui")
                || pkg.equals("com.android.shell")
                || pkg.equals("com.android.phone")
                || pkg.equals("com.android.settings")
                || pkg.equals("com.android.vending")
                || pkg.equals("com.google.android.gms")
                || pkg.equals("com.google.android.gsf")
                || pkg.equals("org.lsposed.manager")
                || pkg.equals("org.lsposed.lspd")
                || pkg.startsWith("com.android.")
                || pkg.startsWith("com.google.android.");
    }
}
package com.gamebooster.app.spoofer.lsposed;

import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.spoofer.SpoofProfile;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * SpoofModule — LSPosed module entry (registered in assets/xposed_init).
 *
 * Loaded by the LSPosed framework inside EVERY scoped app process. Applies
 * in-memory device spoofing hooks ONLY for the supported games registered in
 * GamePackageRegistry (or every app when spoof_all_apps is enabled), using the
 * profile the user selected in the launcher (read via XSharedPreferences).
 *
 * All changes are in-memory at ART level — no game files are ever touched,
 * which removes the config-file tampering ban vector.
 */
public class SpoofModule implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        try {
            if (lpparam.packageName == null) return;
            // Never hook the launcher itself or system-critical processes.
            // spoof_all_apps must never destabilize the system.
            if (isExcludedPackage(lpparam.packageName)) return;

            boolean allApps = SpoofConfigBridge.isSpoofAllApps();
            boolean knownGame = GamePackageRegistry.isKnownGame(lpparam.packageName);
            if (!allApps && !knownGame) return;

            SpoofProfile profile = SpoofConfigBridge.resolveProfile(lpparam.packageName);
            if (profile == null) return;

            XposedBridge.log("[GameBooster] SpoofModule active for " + lpparam.packageName
                    + " -> " + profile.displayName + " [" + profile.model + "]");

            BuildHooks.apply(lpparam, profile);
            SystemPropertiesHooks.apply(lpparam, profile);
            RuntimeMemoryHooks.apply(profile);
            RamInfoHooks.apply(lpparam);
            GlesHooks.apply(lpparam, profile);
            ProcFileHooks.apply(lpparam, profile);
            IdentityHooks.apply(lpparam, profile);
            AntiDetectionHooks.apply(lpparam);

            XposedBridge.log("[GameBooster] SpoofModule hooks installed for " + lpparam.packageName);
        } catch (Throwable t) {
            XposedBridge.log("[GameBooster] SpoofModule error in " + lpparam.packageName + ": " + t);
        }
    }

    private static boolean isExcludedPackage(String pkg) {
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
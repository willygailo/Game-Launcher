package com.gamebooster.app.spoofer.lsposed;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Debug;
import android.provider.Settings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * AntiDetectionHooks — Prevents games and applications from detecting the device's
 * real identity, root binaries, Xposed/LSPosed/LSPatch framework, KernelSU, APatch, and Shizuku.
 *
 * Intercepts:
 * 1. PackageManager scans (hiding LSPatch, Magisk, KernelSU, Shizuku, Game Booster).
 * 2. File checks for su / root / xposed binaries in /system, /sbin, /data/adb.
 * 3. StackTrace inspection for xposed / lsposed / lspatch trace entries.
 * 4. Runtime.exec & ProcessBuilder checks for su / which / daemonsu.
 * 5. Debugger and developer options status inspection.
 */
public final class AntiDetectionHooks {

    private static final Set<String> HIDDEN_PACKAGES = new HashSet<>(Arrays.asList(
            "org.lsposed.lspatch",
            "org.lsposed.lspatch.metamod",
            "org.lsposed.manager",
            "org.lsposed.lspd",
            "moe.shizuku.privileged.api",
            "com.gamebooster.app",
            "com.topjohnwu.magisk",
            "io.github.vvb2060.magisk",
            "io.github.a13e300.ksu",
            "me.bmax.apatch",
            "com.solohsu.android.edxp.manager",
            "de.robv.android.xposed.installer",
            "bin.mt.plus",
            "com.sukhavati.gmscontainer",
            "com.vphonegaga.tit",
            "com.f1player"
    ));

    private static final Set<String> BLOCKED_PATHS = new HashSet<>(Arrays.asList(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su-backup",
            "/system/xbin/mu",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su",
            "/data/local/tmp/su",
            "/su/bin/su",
            "/data/adb/magisk",
            "/data/adb/ksu",
            "/data/adb/apatch",
            "/data/adb/lspd",
            "/data/adb/modules",
            "/system/app/superuser.apk",
            "/system/app/supersu.apk",
            "/system/app/magisk.apk",
            "/system/framework/xposedbridge.jar",
            "/system/lib/libxposed_art.so",
            "/system/lib64/libxposed_art.so"
    ));

    private AntiDetectionHooks() {}

    public static void apply(LoadPackageParam lpparam) {
        hookPackageManager(lpparam);
        hookFileChecks();
        hookStackTrace();
        hookProcessExecution();
        hookEnvironmentSettings(lpparam);

        XposedBridge.log("[GameBooster] AntiDetectionHooks active for " + lpparam.packageName);
    }

    private static void hookPackageManager(LoadPackageParam lpparam) {
        Class<?> pmClass = XposedHelpers.findClass("android.app.ApplicationPackageManager", lpparam.classLoader);
        if (pmClass == null) {
            pmClass = XposedHelpers.findClass("android.content.pm.PackageManager", lpparam.classLoader);
        }
        if (pmClass == null) return;

        // getPackageInfo(String, int)
        try {
            XposedHelpers.findAndHookMethod(pmClass, "getPackageInfo", String.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String pkg = (String) param.args[0];
                            if (pkg != null && isHiddenPackage(pkg)) {
                                param.setThrowable(new PackageManager.NameNotFoundException("Package not found: " + pkg));
                            }
                        }
                    });
        } catch (Throwable ignored) {}

        // getApplicationInfo(String, int)
        try {
            XposedHelpers.findAndHookMethod(pmClass, "getApplicationInfo", String.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String pkg = (String) param.args[0];
                            if (pkg != null && isHiddenPackage(pkg)) {
                                param.setThrowable(new PackageManager.NameNotFoundException("Application not found: " + pkg));
                            }
                        }
                    });
        } catch (Throwable ignored) {}

        // getInstalledPackages(int)
        try {
            XposedHelpers.findAndHookMethod(pmClass, "getInstalledPackages", int.class,
                    new XC_MethodHook() {
                        @Override
                        @SuppressWarnings("unchecked")
                        protected void afterHookedMethod(MethodHookParam param) {
                            List<PackageInfo> list = (List<PackageInfo>) param.getResult();
                            if (list == null) return;
                            List<PackageInfo> filtered = new ArrayList<>(list.size());
                            for (PackageInfo pi : list) {
                                if (pi != null && !isHiddenPackage(pi.packageName)) {
                                    filtered.add(pi);
                                }
                            }
                            param.setResult(filtered);
                        }
                    });
        } catch (Throwable ignored) {}

        // getInstalledApplications(int)
        try {
            XposedHelpers.findAndHookMethod(pmClass, "getInstalledApplications", int.class,
                    new XC_MethodHook() {
                        @Override
                        @SuppressWarnings("unchecked")
                        protected void afterHookedMethod(MethodHookParam param) {
                            List<ApplicationInfo> list = (List<ApplicationInfo>) param.getResult();
                            if (list == null) return;
                            List<ApplicationInfo> filtered = new ArrayList<>(list.size());
                            for (ApplicationInfo ai : list) {
                                if (ai != null && !isHiddenPackage(ai.packageName)) {
                                    filtered.add(ai);
                                }
                            }
                            param.setResult(filtered);
                        }
                    });
        } catch (Throwable ignored) {}

        // queryIntentActivities(Intent, int)
        try {
            XposedHelpers.findAndHookMethod(pmClass, "queryIntentActivities", Intent.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        @SuppressWarnings("unchecked")
                        protected void afterHookedMethod(MethodHookParam param) {
                            List<ResolveInfo> list = (List<ResolveInfo>) param.getResult();
                            if (list == null) return;
                            List<ResolveInfo> filtered = new ArrayList<>(list.size());
                            for (ResolveInfo ri : list) {
                                if (ri != null && ri.activityInfo != null && !isHiddenPackage(ri.activityInfo.packageName)) {
                                    filtered.add(ri);
                                }
                            }
                            param.setResult(filtered);
                        }
                    });
        } catch (Throwable ignored) {}

        // getInstallerPackageName(String)
        try {
            XposedHelpers.findAndHookMethod(pmClass, "getInstallerPackageName", String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            String pkg = (String) param.args[0];
                            if (pkg != null && !isHiddenPackage(pkg)) {
                                // Default Google Play store installer for authenticity
                                param.setResult("com.android.vending");
                            }
                        }
                    });
        } catch (Throwable ignored) {}
    }

    private static void hookFileChecks() {
        try {
            XposedHelpers.findAndHookMethod(File.class, "exists", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    File file = (File) param.thisObject;
                    if (file != null && isBlockedPath(file.getAbsolutePath())) {
                        param.setResult(false);
                    }
                }
            });
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(File.class, "canRead", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    File file = (File) param.thisObject;
                    if (file != null && isBlockedPath(file.getAbsolutePath())) {
                        param.setResult(false);
                    }
                }
            });
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(File.class, "canExecute", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    File file = (File) param.thisObject;
                    if (file != null && isBlockedPath(file.getAbsolutePath())) {
                        param.setResult(false);
                    }
                }
            });
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(File.class, "list", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    String[] list = (String[]) param.getResult();
                    if (list == null) return;
                    List<String> clean = new ArrayList<>(list.length);
                    for (String s : list) {
                        if (s != null && !s.equalsIgnoreCase("su") && !s.equalsIgnoreCase("magisk") && !s.equalsIgnoreCase("daemonsu")) {
                            clean.add(s);
                        }
                    }
                    param.setResult(clean.toArray(new String[0]));
                }
            });
        } catch (Throwable ignored) {}
    }

    private static void hookStackTrace() {
        try {
            XposedHelpers.findAndHookMethod(Throwable.class, "getStackTrace", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    StackTraceElement[] stack = (StackTraceElement[]) param.getResult();
                    if (stack == null) return;
                    List<StackTraceElement> clean = new ArrayList<>(stack.length);
                    for (StackTraceElement elem : stack) {
                        if (elem == null) continue;
                        String cls = elem.getClassName();
                        if (cls != null && (cls.contains("de.robv.android.xposed")
                                || cls.contains("org.lsposed")
                                || cls.contains("lspatch")
                                || cls.contains("sandhook")
                                || cls.contains("edxposed")
                                || cls.contains("pine")
                                || cls.contains("com.gamebooster.app.spoofer"))) {
                            continue;
                        }
                        clean.add(elem);
                    }
                    param.setResult(clean.toArray(new StackTraceElement[0]));
                }
            });
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(Thread.class, "getStackTrace", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    StackTraceElement[] stack = (StackTraceElement[]) param.getResult();
                    if (stack == null) return;
                    List<StackTraceElement> clean = new ArrayList<>(stack.length);
                    for (StackTraceElement elem : stack) {
                        if (elem == null) continue;
                        String cls = elem.getClassName();
                        if (cls != null && (cls.contains("de.robv.android.xposed")
                                || cls.contains("org.lsposed")
                                || cls.contains("lspatch")
                                || cls.contains("sandhook")
                                || cls.contains("edxposed")
                                || cls.contains("com.gamebooster.app.spoofer"))) {
                            continue;
                        }
                        clean.add(elem);
                    }
                    param.setResult(clean.toArray(new StackTraceElement[0]));
                }
            });
        } catch (Throwable ignored) {}
    }

    private static void hookProcessExecution() {
        try {
            XposedHelpers.findAndHookMethod(Runtime.class, "exec", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String cmd = (String) param.args[0];
                    if (cmd != null && isRootCommand(cmd)) {
                        param.setThrowable(new IOException("Command not found: " + cmd));
                    }
                }
            });
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(Runtime.class, "exec", String[].class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String[] cmdArray = (String[]) param.args[0];
                    if (cmdArray != null && cmdArray.length > 0) {
                        String first = cmdArray[0];
                        if (first != null && isRootCommand(first)) {
                            param.setThrowable(new IOException("Command not found: " + first));
                        }
                    }
                }
            });
        } catch (Throwable ignored) {}
    }

    private static void hookEnvironmentSettings(LoadPackageParam lpparam) {
        // Anti-Debugging: Debug.isDebuggerConnected() -> false
        try {
            XposedHelpers.findAndHookMethod(Debug.class, "isDebuggerConnected",
                    XC_MethodReplacement.returnConstant(false));
        } catch (Throwable ignored) {}

        // Settings.Secure / Settings.Global adb_enabled -> 0
        try {
            XposedHelpers.findAndHookMethod(Settings.Secure.class, "getInt",
                    ContentResolver.class, String.class, int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            String name = (String) param.args[1];
                            if ("adb_enabled".equals(name) || "development_settings_enabled".equals(name)) {
                                param.setResult(0);
                            }
                        }
                    });
        } catch (Throwable ignored) {}

        try {
            XposedHelpers.findAndHookMethod(Settings.Global.class, "getInt",
                    ContentResolver.class, String.class, int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            String name = (String) param.args[1];
                            if ("adb_enabled".equals(name) || "development_settings_enabled".equals(name)) {
                                param.setResult(0);
                            }
                        }
                    });
        } catch (Throwable ignored) {}
    }

    public static boolean isHiddenPackage(String pkg) {
        if (pkg == null) return false;
        String lower = pkg.toLowerCase();
        for (String hidden : HIDDEN_PACKAGES) {
            if (lower.equals(hidden) || lower.startsWith(hidden + ".")) return true;
        }
        return false;
    }

    public static boolean isBlockedPath(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        for (String blocked : BLOCKED_PATHS) {
            if (lower.equals(blocked) || lower.startsWith(blocked + "/")) return true;
        }
        return false;
    }

    private static boolean isRootCommand(String cmd) {
        String lower = cmd.toLowerCase().trim();
        return lower.equals("su") || lower.startsWith("su ") || lower.endsWith("/su") ||
                lower.contains("which su") || lower.contains("which magisk") ||
                lower.contains("which ksu") || lower.contains("which apatch") ||
                lower.contains("daemonsu");
    }
}

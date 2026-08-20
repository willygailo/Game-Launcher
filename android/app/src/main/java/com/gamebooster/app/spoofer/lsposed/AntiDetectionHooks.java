package com.gamebooster.app.spoofer.lsposed;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * AntiDetectionHooks — Prevents games and applications from detecting the device's
 * real identity, root binaries, Xposed/LSPatch framework, and Shizuku.
 *
 * Intercepts:
 * 1. PackageManager scans (hiding LSPatch, Magisk, Shizuku, Game Booster).
 * 2. File checks for su / root / xposed binaries in /system, /sbin, /data/adb.
 * 3. StackTrace inspection for xposed / lspatch trace entries.
 * 4. Runtime.exec & ProcessBuilder checks for su / which / getprop.
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
            "/data/adb/lspd",
            "/data/adb/modules",
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            "/system/app/Magisk.apk",
            "/system/framework/XposedBridge.jar",
            "/system/lib/libxposed_art.so",
            "/system/lib64/libxposed_art.so"
    ));

    private AntiDetectionHooks() {}

    public static void apply(LoadPackageParam lpparam) {
        hookPackageManager(lpparam);
        hookFileChecks();
        hookStackTrace();
        hookProcessExecution();
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
                    if (cmd != null && (cmd.equals("su") || cmd.startsWith("su ") || cmd.contains("which su"))) {
                        param.setThrowable(new java.io.IOException("Command not found: " + cmd));
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
                        if (first != null && (first.equals("su") || first.endsWith("/su") || first.equals("which"))) {
                            param.setThrowable(new java.io.IOException("Command not found: " + first));
                        }
                    }
                }
            });
        } catch (Throwable ignored) {}
    }

    private static boolean isHiddenPackage(String pkg) {
        if (pkg == null) return false;
        String lower = pkg.toLowerCase();
        for (String hidden : HIDDEN_PACKAGES) {
            if (lower.equals(hidden) || lower.startsWith(hidden + ".")) return true;
        }
        return false;
    }

    private static boolean isBlockedPath(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        for (String blocked : BLOCKED_PATHS) {
            if (lower.equals(blocked) || lower.startsWith(blocked + "/")) return true;
        }
        return false;
    }
}

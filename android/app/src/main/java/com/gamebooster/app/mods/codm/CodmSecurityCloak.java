package com.gamebooster.app.mods.codm;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * CodmSecurityCloak — Deep Anticheat & Telemetry Suppression for CODM.
 * Neutralizes:
 *  - Tencent ACE (Anti-Cheat Expert): libanort.so, libanogs.so
 *  - CrashSight: libCrashSight.so, libCrashSightPlugin.so
 *  - Security Anti-Fraud: libsaf.so
 *  - TGPA (Tencent Game Performance Analyzer): libtgpa.so
 */
public final class CodmSecurityCloak {

    private static final String TAG = "CodmSecurityCloak";
    public static final String PKG_CODM = "com.garena.game.codm";

    private CodmSecurityCloak() {}

    /**
     * Deploys anticheat stealth cloaking:
     * 1. Nullifies telemetry log & crash dump endpoints via iptables / hosts redirect
     * 2. Sets system security disguise properties
     * 3. Drops ACE watchdog timers and heartbeat signals
     */
    public static void applyCloak(Context ctx) {
        try {
            Log.i(TAG, "🛡️ Engaging CODM Security Cloak...");

            // 1. Anticheat reporting properties spoofing
            setprop("debug.tencent.ace.disabled", "1");
            setprop("ro.tencent.anogs.disable", "1");
            setprop("persist.sys.crashsight.disable", "1");
            setprop("gamebooster.codm.antiban", "1");

            // 2. In-Storage preferences & directories suppression
            com.gamebooster.app.config.CodmAceBypassPatcher.applyBypass(PKG_CODM);

            // 3. Native in-process cloaking
            if (com.gamebooster.app.engine.AceCloakEngine.isAvailable()) {
                com.gamebooster.app.engine.AceCloakEngine.cloakAceModules();
            }

            Log.i(TAG, "✅ CODM Security Cloak engaged. Watchdogs nullified.");
        } catch (Throwable t) {
            Log.w(TAG, "CODM Security Cloak warning: " + t.getMessage());
        }
    }

    private static void setprop(String key, String value) {
        exec("setprop " + key + " " + value);
    }

    private static void exec(String cmd) {
        try {
            ShizukuExecutor.executeShizukuCommand(cmd);
        } catch (Throwable ignored) {
            try {
                Runtime.getRuntime().exec(new String[]{"su", "-c", cmd}).waitFor();
            } catch (Exception e) {
                Log.w(TAG, "exec: " + cmd + " → " + e.getMessage());
            }
        }
    }
}

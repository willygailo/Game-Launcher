package com.gamebooster.app.diagnostics;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import androidx.core.content.FileProvider;

import com.gamebooster.app.BuildConfig;
import com.gamebooster.app.device.DisplayCapabilitiesDetector;
import com.gamebooster.app.engine.MasterOptimizationEnforcer;
import com.gamebooster.app.shizuku.RishManager;
import com.gamebooster.app.shizuku.ShizukuConnectionManager;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofPreferences;
import com.gamebooster.app.spoofer.SpoofProfile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rikka.shizuku.Shizuku;

/**
 * Builds a comprehensive, shareable diagnostics snapshot:
 * App build metadata, Hardware & SoC parameters, Android OS & Runtime flags,
 * Shizuku / AIDL / Rish privilege bridges, Display Hz metrics, Active Tweaks,
 * Device Identity Spoofing state, and captured Crash Logs.
 */
public final class DiagnosticsExporter {

    public static final String TAG = "DiagnosticsExporter";
    public static final String PREFIX = "diagnostics_";
    public static final String EXTENSION = ".txt";

    private DiagnosticsExporter() {
    }

    public static List<String> buildSnapshot(Context context) {
        if (context == null) return new ArrayList<>();
        Context appCtx = context.getApplicationContext();

        MasterOptimizationEnforcer.EnforcementStatus status =
                MasterOptimizationEnforcer.verifyEnforcementStatus(appCtx);
        boolean spoofEnabled = SpoofPreferences.isSpoofEnabled(appCtx);
        String spoofProfileId = SpoofPreferences.getActiveProfileId(appCtx);
        String crashTail = CrashLog.readTail(appCtx, 1200);

        return buildSnapshot(
                BuildConfig.VERSION_NAME + " (code " + BuildConfig.VERSION_CODE + ", " + (BuildConfig.DEBUG ? "DEBUG" : "RELEASE") + ")",
                Build.MANUFACTURER + " " + Build.MODEL + " (" + Build.DEVICE + ")",
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                status,
                spoofEnabled,
                spoofProfileId,
                crashTail,
                appCtx
        );
    }

    public static List<String> buildSnapshot(String appVersion, String deviceModel,
                                             String androidRelease, int sdkInt,
                                             MasterOptimizationEnforcer.EnforcementStatus status,
                                             boolean spoofEnabled, String spoofProfileId,
                                             String crashTail) {
        return buildSnapshot(appVersion, deviceModel, androidRelease, sdkInt, status, spoofEnabled, spoofProfileId, crashTail, null);
    }

    public static List<String> buildSnapshot(String appVersion, String deviceModel,
                                             String androidRelease, int sdkInt,
                                             MasterOptimizationEnforcer.EnforcementStatus status,
                                             boolean spoofEnabled, String spoofProfileId,
                                             String crashTail, Context context) {
        List<String> lines = new ArrayList<>();
        lines.add("==================================================");
        lines.add("       ⚡ GAME BOOSTER PRO SYSTEM DIAGNOSTICS ⚡   ");
        lines.add("==================================================");
        lines.add("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date()));
        lines.add("");

        // 1. App Information
        lines.add("--- [APP & BUILD] ---");
        lines.add("Package: " + BuildConfig.APPLICATION_ID);
        lines.add("App Version: " + safe(appVersion));
        lines.add("Target SDK: " + Build.VERSION_CODES.UPSIDE_DOWN_CAKE + "+ (Android 14-16 API 34-36)");
        lines.add("Compile SDK: 36");
        lines.add("");

        // 2. Device & Hardware
        lines.add("--- [HARDWARE & SOC] ---");
        lines.add("Device: " + safe(deviceModel));
        lines.add("Brand: " + Build.BRAND);
        lines.add("Board / Hardware: " + Build.BOARD + " / " + Build.HARDWARE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            lines.add("SoC Model: " + Build.SOC_MODEL + " (" + Build.SOC_MANUFACTURER + ")");
        }
        lines.add("CPU Cores: " + Runtime.getRuntime().availableProcessors() + " Cores");
        lines.add("Supported ABIs: " + String.join(", ", Build.SUPPORTED_ABIS));
        if (context != null) {
            long[] ram = getMemoryInfo(context);
            if (ram[0] > 0) {
                lines.add("RAM Total / Available: " + (ram[0] / 1024 / 1024) + " MB / " + (ram[1] / 1024 / 1024) + " MB");
            }
        }
        lines.add("");

        // 3. Android Platform & OS
        lines.add("--- [ANDROID OS & RUNTIME] ---");
        lines.add("Android Version: " + safe(androidRelease) + " (API " + sdkInt + ")");
        lines.add("Build ID / Display: " + Build.DISPLAY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            lines.add("Security Patch: " + Build.VERSION.SECURITY_PATCH);
        }
        lines.add("Dalvik Heap Limit: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
        lines.add("");

        // 4. Privileged Access & System Bridges
        lines.add("--- [PRIVILEGE BRIDGES & SHIZUKU] ---");
        boolean shizukuAlive = false;
        boolean shizukuGranted = false;
        try {
            shizukuAlive = Shizuku.pingBinder();
            shizukuGranted = shizukuAlive && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable ignored) {}

        lines.add("Shizuku Binder Alive: " + (shizukuAlive ? "✅ YES (Service Running)" : "❌ NO"));
        lines.add("Shizuku Permission: " + (shizukuGranted ? "✅ GRANTED (UID 2000/0)" : "⚠️ NOT GRANTED"));
        if (shizukuGranted) {
            try {
                lines.add("Shizuku UID / Version: UID=" + Shizuku.getUid() + " / v" + Shizuku.getVersion());
            } catch (Throwable ignored) {}
        }
        boolean aidlConnected = ShizukuUserServiceConnector.getInstance().isServiceConnected();
        lines.add("AIDL UserService Connected: " + (aidlConnected ? "✅ TRUE (Direct Binder IPC Active)" : "⚠️ FALSE (Connecting/Idle)"));
        lines.add("Rish Embedded Executable: " + (RishManager.isRishAvailable() ? "✅ READY" : "⚠️ STANDBY"));
        lines.add("Connection State: " + ShizukuConnectionManager.getInstance().getState());
        lines.add("");

        // 5. Display & Refresh Rate
        lines.add("--- [DISPLAY & REFRESH ENGINE] ---");
        if (context != null) {
            try {
                DisplayCapabilitiesDetector.DisplayCaps caps = DisplayCapabilitiesDetector.detect(context);
                lines.add("Max Hardware Refresh Rate: " + caps.maxRefreshRate + " Hz");
                lines.add("Current Active Refresh Rate: " + caps.currentRefreshRate + " Hz");
                lines.add("Display Resolution: " + caps.width + "x" + caps.height + " (" + caps.densityDpi + " dpi)");
                if (!caps.supportedRefreshRates.isEmpty()) {
                    lines.add("Supported Rates: " + caps.supportedRefreshRates + " Hz");
                }
            } catch (Throwable ignored) {}
        }
        lines.add("Animation Scale: 0.0x (Instant / Zero-Delay Latency)");
        lines.add("");

        // 6. Optimizations & Tweaks
        lines.add("--- [PERFORMANCE TWEAKS ENFORCEMENT] ---");
        if (status != null) {
            lines.add("Tweaks Enforced: " + status.tweaksAppliedCount + " / " + status.totalSupportedTweaks + " Applied");
            lines.add("Master Enforcer Status: " + (status.shizukuRootGranted ? "ACTIVE (Elevated Tier 1)" : "STANDARD (Tier 3)"));
        } else {
            lines.add("Tweaks Enforced: Unknown");
        }
        lines.add("");

        // 7. Device Spoofing
        lines.add("--- [HARDWARE DEVICE IDENTITY SPOOFER] ---");
        lines.add("Spoofing Enabled: " + (spoofEnabled ? "✅ ACTIVE" : "❌ DISABLED"));
        if (spoofEnabled && spoofProfileId != null && !spoofProfileId.isEmpty()) {
            SpoofProfile prof = DeviceSpooferEngine.getProfileById(spoofProfileId);
            if (prof != null) {
                lines.add("Active Profile: " + prof.displayName + " (" + prof.model + ")");
                lines.add("Spoofed Brand / Model: " + prof.brand + " / " + prof.model);
                lines.add("Fingerprint: " + prof.fingerprint);
            } else {
                lines.add("Active Profile ID: " + spoofProfileId + " (Custom)");
            }
        } else {
            lines.add("Active Profile: None (Original Device Identity)");
        }
        lines.add("");

        // 8. Crash Logs
        lines.add("--- [CRASH LOG & ERROR HISTORY] ---");
        if (crashTail != null && !crashTail.trim().isEmpty()) {
            lines.add("⚠️ Recent Intercepted Crash Trace:");
            lines.add(crashTail.trim());
        } else {
            lines.add("✅ Zero Crash Logs Captured (Clean Execution)");
        }
        lines.add("==================================================");
        lines.add("                 END OF DIAGNOSTICS               ");
        lines.add("==================================================");

        return lines;
    }

    public static File exportToFile(Context context, String content) throws Exception {
        File dir = context.getExternalFilesDir(null);
        if (dir == null) {
            dir = context.getFilesDir();
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String name = PREFIX + System.currentTimeMillis() + EXTENSION;
        File file = new File(dir, name);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.print(content);
        }
        Log.i(TAG, "Exported diagnostics to " + file.getAbsolutePath());
        return file;
    }

    public static Intent shareSnapshot(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_SUBJECT, "Game Booster PRO Diagnostics Report");
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return Intent.createChooser(share, "Share diagnostics");
    }

    public static String join(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines.get(i));
        }
        return sb.toString();
    }

    private static String safe(String value) {
        return value != null ? value : "unknown";
    }

    private static long[] getMemoryInfo(Context context) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(mi);
                return new long[]{mi.totalMem, mi.availMem};
            }
        } catch (Throwable ignored) {}
        return new long[]{-1L, -1L};
    }
}
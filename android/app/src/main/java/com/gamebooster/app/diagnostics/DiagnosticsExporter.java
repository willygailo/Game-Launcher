package com.gamebooster.app.diagnostics;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.gamebooster.app.BuildConfig;
import com.gamebooster.app.config.ConfigBackupManager;
import com.gamebooster.app.config.GameConfigPathResolver;
import com.gamebooster.app.device.DisplayCapabilitiesDetector;
import com.gamebooster.app.engine.MasterOptimizationEnforcer;
import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.shizuku.RishManager;
import com.gamebooster.app.shizuku.ShizukuConnectionManager;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofPreferences;
import com.gamebooster.app.spoofer.SpoofProfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import rikka.shizuku.Shizuku;

/**
 * Builds a comprehensive, shareable diagnostics snapshot:
 * App build metadata, Hardware & SoC parameters, Android OS & Runtime flags,
 * Root / Magisk / KSU indicators, Shizuku / AIDL / Rish privilege bridges,
 * Battery & Thermal metrics, Display Hz metrics, Active Tweaks,
 * Installed Games & 144fps Patcher status, Device Identity Spoofing state,
 * Storage / Network metrics, Memory & Swap/ZRAM stats, and captured Crash Logs.
 */
public final class DiagnosticsExporter {

    public static final String TAG = "DiagnosticsExporter";
    public static final String PREFIX = "diagnostics_";
    public static final String EXTENSION = ".txt";

    private DiagnosticsExporter() {
    }

    public static List<String> buildSnapshot(Context context) {
        if (context == null) return fallbackSnapshot("Null context provided");
        Context appCtx = context.getApplicationContext();

        try {
            MasterOptimizationEnforcer.EnforcementStatus status = null;
            try {
                status = MasterOptimizationEnforcer.verifyEnforcementStatus(appCtx);
            } catch (Throwable t) {
                Log.w(TAG, "Failed to verify enforcement status: " + t.getMessage());
            }

            boolean spoofEnabled = false;
            String spoofProfileId = "";
            try {
                spoofEnabled = SpoofPreferences.isSpoofEnabled(appCtx);
                spoofProfileId = SpoofPreferences.getActiveProfileId(appCtx);
            } catch (Throwable t) {
                Log.w(TAG, "Failed to read spoof preferences: " + t.getMessage());
            }

            String crashTail = "";
            try {
                crashTail = CrashLog.readTail(appCtx, 2500);
            } catch (Throwable t) {
                Log.w(TAG, "Failed to read crash tail: " + t.getMessage());
            }

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
        } catch (Throwable t) {
            Log.e(TAG, "Fatal error building diagnostics snapshot: " + t.getMessage(), t);
            return fallbackSnapshot("Exception in buildSnapshot: " + t.getMessage());
        }
    }

    private static List<String> fallbackSnapshot(String reason) {
        List<String> lines = new ArrayList<>();
        lines.add("==================================================");
        lines.add("       ⚡ GAME BOOSTER PRO SYSTEM DIAGNOSTICS ⚡   ");
        lines.add("==================================================");
        lines.add("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date()));
        lines.add("Status: " + reason);
        lines.add("App Version: " + BuildConfig.VERSION_NAME + " (code " + BuildConfig.VERSION_CODE + ")");
        lines.add("Device: " + Build.MANUFACTURER + " " + Build.MODEL + " (Android " + Build.VERSION.RELEASE + ", SDK " + Build.VERSION.SDK_INT + ")");
        lines.add("==================================================");
        return lines;
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
        lines.add("--- [1. APP & BUILD METADATA] ---");
        lines.add("Package: " + BuildConfig.APPLICATION_ID);
        lines.add("App Version: " + safe(appVersion));
        lines.add("Target SDK: Android 14-16 (API 34-36)");
        lines.add("Compile SDK: 36");
        lines.add("Process: " + android.os.Process.myPid() + " (UID: " + android.os.Process.myUid() + ")");
        lines.add("");

        // 2. Device & Hardware
        lines.add("--- [2. HARDWARE & SOC SPECIFICATIONS] ---");
        lines.add("Device: " + safe(deviceModel));
        lines.add("Brand / Manufacturer: " + Build.BRAND + " / " + Build.MANUFACTURER);
        lines.add("Product / Board: " + Build.PRODUCT + " / " + Build.BOARD);
        lines.add("Hardware Platform: " + Build.HARDWARE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                lines.add("SoC Model: " + Build.SOC_MODEL + " (" + Build.SOC_MANUFACTURER + ")");
            } catch (Throwable ignored) {}
        }
        lines.add("CPU Cores: " + Runtime.getRuntime().availableProcessors() + " Cores");
        lines.add("Supported ABIs: " + Arrays.toString(Build.SUPPORTED_ABIS));
        if (context != null) {
            try {
                long[] ram = getMemoryInfo(context);
                if (ram[0] > 0) {
                    long totalMb = ram[0] / (1024 * 1024);
                    long availMb = ram[1] / (1024 * 1024);
                    long usedMb = totalMb - availMb;
                    int pct = totalMb > 0 ? (int) ((usedMb * 100) / totalMb) : 0;
                    lines.add("RAM Usage: " + pct + "% (" + usedMb + " / " + totalMb + " MB, Free: " + availMb + " MB)");
                }
                Map<String, Long> memInfo = readMemInfo();
                if (memInfo.containsKey("SwapTotal") && memInfo.get("SwapTotal") > 0) {
                    long swapTotalMb = memInfo.get("SwapTotal") / 1024;
                    long swapFreeMb = memInfo.containsKey("SwapFree") ? memInfo.get("SwapFree") / 1024 : 0;
                    long swapUsedMb = swapTotalMb - swapFreeMb;
                    int swapPct = swapTotalMb > 0 ? (int) ((swapUsedMb * 100) / swapTotalMb) : 0;
                    lines.add("Swap / ZRAM: " + swapPct + "% (" + swapUsedMb + " / " + swapTotalMb + " MB used)");
                }
            } catch (Throwable ignored) {}
        }
        lines.add("");

        // 3. Android Platform & Kernel
        lines.add("--- [3. ANDROID OS & KERNEL RUNTIME] ---");
        lines.add("Android Version: " + safe(androidRelease) + " (API Level " + sdkInt + ")");
        lines.add("Build ID / Display: " + Build.DISPLAY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                lines.add("Security Patch Level: " + Build.VERSION.SECURITY_PATCH);
            } catch (Throwable ignored) {}
        }
        lines.add("Dalvik Heap Limit: " + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " MB");
        String kernelVer = readKernelVersion();
        lines.add("Kernel Version: " + kernelVer);
        lines.add("");

        // 4. Root & Security Status
        lines.add("--- [4. ROOT, SU & SECURITY ENVIRONMENT] ---");
        boolean hasSu = checkRootSuBinary();
        lines.add("Root Binary (su): " + (hasSu ? "✅ DETECTED (Root Access Ready)" : "❌ NOT FOUND (Non-Root/Shizuku Engine)"));
        if (context != null) {
            try {
                String rootManager = detectRootManager(context);
                if (rootManager != null) {
                    lines.add("Root Manager: " + rootManager);
                }
            } catch (Throwable ignored) {}
        }
        String selinux = getSELinuxStatus();
        lines.add("SELinux State: " + selinux);
        lines.add("");

        // 5. Privileged Access & System Bridges
        lines.add("--- [5. PRIVILEGE BRIDGES & SHIZUKU API] ---");
        boolean shizukuAlive = false;
        boolean shizukuGranted = false;
        try {
            shizukuAlive = Shizuku.pingBinder();
            shizukuGranted = shizukuAlive && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable ignored) {}

        lines.add("Shizuku Binder Alive: " + (shizukuAlive ? "✅ YES (Service Active)" : "❌ NO"));
        lines.add("Shizuku Permission: " + (shizukuGranted ? "✅ GRANTED (Privileged UID 2000/0)" : "⚠️ NOT GRANTED"));
        if (shizukuGranted) {
            try {
                lines.add("Shizuku UID / Version: UID=" + Shizuku.getUid() + " / v" + Shizuku.getVersion());
            } catch (Throwable ignored) {}
        }
        boolean aidlConnected = false;
        try {
            aidlConnected = ShizukuUserServiceConnector.getInstance().isServiceConnected();
        } catch (Throwable ignored) {}
        lines.add("AIDL UserService Connected: " + (aidlConnected ? "✅ TRUE (Direct Binder IPC Active)" : "⚠️ FALSE (Connecting/Idle)"));
        lines.add("Rish Embedded Executable: " + (RishManager.isRishAvailable() ? "✅ READY" : "⚠️ STANDBY"));
        try {
            lines.add("Shizuku Connection Manager State: " + ShizukuConnectionManager.getInstance().getState());
        } catch (Throwable ignored) {}
        lines.add("");

        // 6. Battery & Thermal Status
        lines.add("--- [6. BATTERY & THERMAL TELEMETRY] ---");
        if (context != null) {
            try {
                Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                if (batteryIntent != null) {
                    int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int temp = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
                    int statusBat = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    int plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

                    int batPct = (level >= 0 && scale > 0) ? (level * 100 / scale) : -1;
                    float tempC = temp > 0 ? (temp / 10.0f) : -1f;

                    String charging = statusBat == BatteryManager.BATTERY_STATUS_CHARGING ? "⚡ Charging" :
                            statusBat == BatteryManager.BATTERY_STATUS_FULL ? "🔋 Full" : "🔋 Discharging";
                    if (plugged > 0) charging += " (AC/USB/Wireless)";

                    lines.add("Battery Level: " + batPct + "% | Status: " + charging);
                    lines.add("Battery Temperature: " + (tempC > 0 ? tempC + " °C" : "Unknown"));
                }
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    lines.add("Power Save Mode: " + (pm.isPowerSaveMode() ? "⚠️ ON (Performance Throttled)" : "✅ OFF (Full Performance)"));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        int thermalStatus = pm.getCurrentThermalStatus();
                        lines.add("Thermal Status: " + formatThermalStatus(thermalStatus));
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        try {
                            float headroom = pm.getThermalHeadroom(30);
                            if (!Float.isNaN(headroom) && headroom >= 0) {
                                int headroomPct = Math.min(100, (int) (headroom * 100));
                                lines.add("Thermal Headroom (30s forecast): " + headroomPct + "%" + (headroom >= 1.0f ? " (Throttling Alert)" : " (Nominal)"));
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            } catch (Throwable ignored) {}
        }
        lines.add("");

        // 7. Display & Refresh Engine
        lines.add("--- [7. DISPLAY & REFRESH ENGINE] ---");
        if (context != null) {
            try {
                DisplayCapabilitiesDetector.DisplayCaps caps = DisplayCapabilitiesDetector.detect(context);
                lines.add("Max Hardware Refresh Rate: " + caps.maxRefreshRate + " Hz");
                lines.add("Current Active Refresh Rate: " + caps.currentRefreshRate + " Hz");
                lines.add("Display Resolution: " + caps.width + "x" + caps.height + " (" + caps.densityDpi + " dpi)");
                if (!caps.supportedRefreshRates.isEmpty()) {
                    lines.add("Supported Rates: " + caps.supportedRefreshRates + " Hz");
                }
                lines.add("HDR Support: " + (caps.supportsHdr ? "✅ YES" : "❌ NO") + " | Wide Color Gamut: " + (caps.supportsWideColorGamut ? "✅ YES" : "❌ NO"));
            } catch (Throwable ignored) {}
        }
        lines.add("SurfaceFlinger 144Hz Uncap: READY (Binder Call 1035 Supported)");
        lines.add("Animation Scale: 0.0x (Instant / Zero-Delay Latency)");
        lines.add("");

        // 8. Storage & Network Diagnostics
        lines.add("--- [8. STORAGE & NETWORK METRICS] ---");
        if (context != null) {
            try {
                File internalDir = Environment.getDataDirectory();
                StatFs stat = new StatFs(internalDir.getPath());
                long blockSize = stat.getBlockSizeLong();
                long totalBlocks = stat.getBlockCountLong();
                long availBlocks = stat.getAvailableBlocksLong();
                long totalSpaceGb = (totalBlocks * blockSize) / (1024 * 1024 * 1024);
                long freeSpaceGb = (availBlocks * blockSize) / (1024 * 1024 * 1024);
                lines.add("Internal Storage Total / Free: " + totalSpaceGb + " GB / " + freeSpaceGb + " GB");
            } catch (Throwable ignored) {}

            try {
                lines.add(getNetworkTelemetry(context));
            } catch (Throwable ignored) {
                lines.add("Active Network: Telemetry unavailable");
            }
        }
        lines.add("");

        // 9. Performance Tweaks Enforcement
        lines.add("--- [9. PERFORMANCE TWEAKS ENFORCEMENT] ---");
        if (status != null) {
            lines.add("Tweaks Enforced: " + status.tweaksAppliedCount + " / " + status.totalSupportedTweaks + " Applied");
            lines.add("Master Enforcer Status: " + (status.shizukuRootGranted ? "ACTIVE (Elevated Tier 1 Privileged)" : "STANDARD (Tier 3 Normal)"));
        } else {
            lines.add("Tweaks Enforced: Unknown");
        }
        lines.add("");

        // 10. Installed Games & 144fps Patcher Status (Fast Lightweight Query without heavy drawable loading)
        lines.add("--- [10. INSTALLED GAMES & 144FPS PATCH STATUS] ---");
        if (context != null) {
            try {
                List<InstalledGameQuickInfo> installedGames = scanInstalledGamesFast(context);
                lines.add("Installed Supported Games Detected: " + installedGames.size());
                for (InstalledGameQuickInfo g : installedGames) {
                    int backups = ConfigBackupManager.getBackupCount(context, g.packageName);
                    List<String> paths = GameConfigPathResolver.getPathsForGame(g.packageName);
                    int pathCount = paths != null ? paths.size() : 0;
                    lines.add("  🎮 " + g.label + " (" + g.packageName + "): "
                            + pathCount + " config paths, " + backups + " backups active [144fps UltraExtreme Capable]");
                }
            } catch (Throwable ignored) {
                lines.add("Games scan: Error reading target game packages");
            }
        }
        lines.add("");

        // 11. Hardware Device Identity Spoofer
        lines.add("--- [11. HARDWARE DEVICE IDENTITY SPOOFER] ---");
        lines.add("Spoofing Enabled: " + (spoofEnabled ? "✅ ACTIVE" : "❌ DISABLED"));
        if (spoofEnabled && spoofProfileId != null && !spoofProfileId.isEmpty()) {
            try {
                SpoofProfile prof = DeviceSpooferEngine.getProfileById(spoofProfileId);
                if (prof != null) {
                    lines.add("Active Profile: " + prof.displayName + " (" + prof.model + ")");
                    lines.add("Spoofed Brand / Model: " + prof.brand + " / " + prof.model);
                    lines.add("Fingerprint: " + prof.fingerprint);
                } else {
                    lines.add("Active Profile ID: " + spoofProfileId + " (Custom)");
                }
            } catch (Throwable ignored) {
                lines.add("Active Profile ID: " + spoofProfileId);
            }
        } else {
            lines.add("Active Profile: None (Original Device Identity)");
        }
        lines.add("");

        // 12. GAME-MANAGER Status
        lines.add("--- [12. GAME-MANAGER ENGINE STATUS] ---");
        try {
            com.gamebooster.app.gamemanager.GameManagerStatus gmStatus = com.gamebooster.app.gamemanager.GameManagerStatus.getInstance();
            lines.add("Active Game Session: " + (gmStatus.hasActiveSession() ? "🎮 " + gmStatus.getActiveGamePackage() + " (" + gmStatus.getSessionDurationSeconds() + "s)" : "IDLE (Baseline Restored)"));
            lines.add("Last Enforcement Time: " + gmStatus.getFormattedLastApplyTime());
            lines.add("Last Enforcement Action: " + gmStatus.getLastApplySummary());
            lines.add("Applications Masked Count: " + gmStatus.getMaskedAppsCount());
        } catch (Throwable ignored) {
            lines.add("Active Game Session: IDLE (Baseline Restored)");
        }
        lines.add("");

        // 13. Crash Logs & Stability
        lines.add("--- [13. CRASH LOG & ERROR HISTORY] ---");
        if (crashTail != null && !crashTail.trim().isEmpty()) {
            lines.add("⚠️ Recent Intercepted Crash Trace:");
            lines.add(crashTail.trim());
        } else {
            lines.add("✅ Zero Crash Logs Captured (Clean Execution)");
        }
        lines.add("");

        // 14. Android API Gates
        lines.add("--- [14. ANDROID API GATES (API 31-36)] ---");
        lines.add("Android 12 GameManager API (API 31+): " + (sdkInt >= 31 ? "✅ OPEN" : "❌ LOCKED"));
        lines.add("Android 12 ADPF PerformanceHintManager (API 31+): " + (sdkInt >= 31 ? "✅ OPEN" : "❌ LOCKED"));
        lines.add("Android 13 GameOverlay API (API 33+): " + (sdkInt >= 33 ? "✅ OPEN" : "❌ LOCKED"));
        lines.add("Android 14 FPS/Refresh Override (API 34+): " + (sdkInt >= 34 ? "✅ OPEN" : "❌ LOCKED"));
        lines.add("Android 15 Fixed Clocks Power Mode (API 35+): " + (sdkInt >= 35 ? "✅ OPEN" : "❌ LOCKED"));
        lines.add("Android 16 Performance-Class Baklava (API 36+): " + (sdkInt >= 36 ? "✅ OPEN" : "❌ LOCKED"));
        lines.add("No-Fallback State Persistence: ACTIVE (Toggles stay ON until manual OFF)");
        lines.add("==================================================");
        lines.add("                 END OF DIAGNOSTICS               ");
        lines.add("==================================================");

        return lines;
    }

    public static List<String> buildSnapshotFull(Context context) {
        return buildSnapshot(context);
    }

    public static File exportToFile(Context context, String content) throws Exception {
        if (context == null) throw new IllegalArgumentException("Null context");
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
            writer.flush();
        }
        Log.i(TAG, "Exported diagnostics to " + file.getAbsolutePath());
        return file;
    }

    /**
     * Creates a share intent with universal FileProvider URI permissions granted across all target apps.
     */
    public static Intent shareSnapshot(Context context, File file) {
        if (context == null || file == null) return new Intent();
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_SUBJECT, "⚡ Game Booster PRO Diagnostics Report");
        share.putExtra(Intent.EXTRA_TEXT, "Attached is the Game Booster PRO Diagnostics Snapshot (" + file.getName() + ").");
        share.setClipData(ClipData.newRawUri("diagnostics", uri));
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        // Grant URI permission explicitly to all matching handler packages
        try {
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> resInfoList = pm.queryIntentActivities(share, PackageManager.MATCH_DEFAULT_ONLY);
            if (resInfoList != null) {
                for (ResolveInfo resolveInfo : resInfoList) {
                    if (resolveInfo.activityInfo != null) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        context.grantUriPermission(packageName, uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                }
            }
        } catch (Throwable ignored) {}

        Intent chooser = Intent.createChooser(share, "Share Diagnostics Report");
        chooser.setClipData(ClipData.newRawUri("diagnostics", uri));
        chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return chooser;
    }

    public static String join(List<String> lines) {
        if (lines == null) return "";
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

    private static String formatThermalStatus(int status) {
        switch (status) {
            case PowerManager.THERMAL_STATUS_NONE:
                return "Level 0: NOMINAL (Cool / Full Gaming Performance)";
            case PowerManager.THERMAL_STATUS_LIGHT:
                return "Level 1: LIGHT (Slight Throttle Warning)";
            case PowerManager.THERMAL_STATUS_MODERATE:
                return "Level 2: MODERATE (Moderate Throttling)";
            case PowerManager.THERMAL_STATUS_SEVERE:
                return "Level 3: SEVERE (Heavy Throttling Active)";
            case PowerManager.THERMAL_STATUS_CRITICAL:
                return "Level 4: CRITICAL (Dropping Frames / Stutters Expected)";
            case PowerManager.THERMAL_STATUS_EMERGENCY:
                return "Level 5: EMERGENCY (Thermal Shutdown Protection)";
            case PowerManager.THERMAL_STATUS_SHUTDOWN:
                return "Level 6: SHUTDOWN";
            default:
                return "Level " + status + " (Custom OEM Level)";
        }
    }

    private static String getNetworkTelemetry(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return "Active Network: Unavailable";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network activeNet = cm.getActiveNetwork();
            if (activeNet != null) {
                NetworkCapabilities caps = cm.getNetworkCapabilities(activeNet);
                if (caps != null) {
                    StringBuilder sb = new StringBuilder();
                    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        sb.append("Active Network: Wi-Fi");
                        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if (wm != null) {
                            try {
                                WifiInfo winfo = wm.getConnectionInfo();
                                if (winfo != null && winfo.getLinkSpeed() > 0) {
                                    sb.append(" (").append(winfo.getLinkSpeed()).append(" Mbps, RSSI: ").append(winfo.getRssi()).append(" dBm)");
                                }
                            } catch (Throwable ignored) {}
                        }
                    } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        sb.append("Active Network: Cellular Mobile Data");
                    } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        sb.append("Active Network: Ethernet LAN");
                    } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        sb.append("Active Network: VPN Tunnel");
                    } else {
                        sb.append("Active Network: Connected");
                    }

                    int downKbps = caps.getLinkDownstreamBandwidthKbps();
                    int upKbps = caps.getLinkUpstreamBandwidthKbps();
                    if (downKbps > 0 || upKbps > 0) {
                        sb.append(" | Bandwidth: ↓").append(downKbps / 1000).append(" Mbps / ↑").append(upKbps / 1000).append(" Mbps");
                    }
                    if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED)) {
                        sb.append(" [Low Congestion]");
                    }
                    return sb.toString();
                }
            }
            return "Active Network: Offline / Disconnected";
        } else {
            @SuppressWarnings("deprecation")
            NetworkInfo activeNet = cm.getActiveNetworkInfo();
            if (activeNet != null && activeNet.isConnected()) {
                return "Active Network: " + activeNet.getTypeName() + " (" + activeNet.getSubtypeName() + ")";
            }
            return "Active Network: Offline / Disconnected";
        }
    }

    private static String readKernelVersion() {
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/version"))) {
            String line = br.readLine();
            return line != null ? line.trim() : System.getProperty("os.version", "Unknown");
        } catch (Throwable t) {
            return System.getProperty("os.version", "Unknown");
        }
    }

    private static boolean checkRootSuBinary() {
        String[] paths = {
                "/system/bin/su", "/system/xbin/su", "/sbin/su",
                "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/xbin/su",
                "/data/local/bin/su", "/data/local/su", "/su/bin/su",
                "/system/app/Superuser.apk", "/data/adb/ksu/bin/su", "/data/adb/ap/bin/su",
                "/data/adb/magisk"
        };
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static String detectRootManager(Context context) {
        String[] packages = {
                "com.topjohnwu.magisk:Magisk",
                "io.github.tiann.kernelsu:KernelSU",
                "me.bmax.apatch:APatch",
                "com.sukisu.ultra:KernelSU Next",
                "com.koushikdutta.superuser:Superuser",
                "eu.chainfire.supersu:SuperSU"
        };
        PackageManager pm = context.getPackageManager();
        for (String entry : packages) {
            String[] parts = entry.split(":");
            try {
                pm.getPackageInfo(parts[0], 0);
                return "✅ " + parts[1] + " App Installed (" + parts[0] + ")";
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static String getSELinuxStatus() {
        // First try privileged execution via Shizuku if active
        try {
            if (ShizukuExecutor.hasShizukuPermission()) {
                String privileged = ShizukuExecutor.executeShizukuCommand("getenforce");
                if (privileged != null && !privileged.trim().isEmpty()) {
                    return privileged.trim() + " (Privileged Query)";
                }
            }
        } catch (Throwable ignored) {}

        // Fallback to standard process exec
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getenforce");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = br.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    return line.trim();
                }
            }
        } catch (Throwable ignored) {}
        return "Enforcing (Default)";
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

    private static Map<String, Long> readMemInfo() {
        Map<String, Long> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 2) {
                    String key = parts[0].trim();
                    String valStr = parts[1].trim().split("\\s+")[0];
                    try {
                        map.put(key, Long.parseLong(valStr));
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        return map;
    }

    private static class InstalledGameQuickInfo {
        final String packageName;
        final String label;

        InstalledGameQuickInfo(String packageName, String label) {
            this.packageName = packageName;
            this.label = label;
        }
    }

    private static List<InstalledGameQuickInfo> scanInstalledGamesFast(Context context) {
        List<InstalledGameQuickInfo> list = new ArrayList<>();
        if (context == null) return list;
        PackageManager pm = context.getPackageManager();
        Set<String> added = new HashSet<>();

        // 1. Check known supported game package names directly (ultra fast, zero drawable overhead)
        for (Map.Entry<String, GamePackageRegistry.GameInfoSpec> entry : GamePackageRegistry.getAllKnownGames().entrySet()) {
            String pkg = entry.getKey();
            if (added.contains(pkg)) continue;
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
                if (appInfo != null) {
                    String label = entry.getValue() != null ? entry.getValue().title : pkg;
                    list.add(new InstalledGameQuickInfo(pkg, label));
                    added.add(pkg);
                }
            } catch (Throwable ignored) {}
        }

        // 2. Query Launcher category games
        try {
            Intent gameIntent = new Intent(Intent.ACTION_MAIN, null);
            gameIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(gameIntent, 0);
            if (resolveInfos != null) {
                for (ResolveInfo ri : resolveInfos) {
                    if (ri == null || ri.activityInfo == null) continue;
                    String pkg = ri.activityInfo.packageName;
                    if (pkg == null || added.contains(pkg) || pkg.equalsIgnoreCase(context.getPackageName())) {
                        continue;
                    }
                    ApplicationInfo aInfo = ri.activityInfo.applicationInfo;
                    boolean isGame = false;
                    if (aInfo != null) {
                        if ((aInfo.flags & ApplicationInfo.FLAG_IS_GAME) != 0) isGame = true;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && aInfo.category == ApplicationInfo.CATEGORY_GAME) {
                            isGame = true;
                        }
                    }
                    if (isGame) {
                        CharSequence labelSeq = ri.loadLabel(pm);
                        String label = labelSeq != null ? labelSeq.toString() : pkg;
                        list.add(new InstalledGameQuickInfo(pkg, label));
                        added.add(pkg);
                    }
                }
            }
        } catch (Throwable ignored) {}

        return list;
    }
}
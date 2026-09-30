package com.gamebooster.app.engine;

import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.engine.CommandExecutor;

/**
 * AceContainerEvasionEngine — Detects virtual app containers & configures Zygisk bypass.
 *
 * Tencent ACE 2026 detects 30+ container types including:
 *   VirtualAPP, Parallel Space, LBE, Island, Shelter, VMOS, F1VM, X8 Sandbox,
 *   NoxPlayer, BlueStacks, MuMu, GameLoop, and any bind-mount overlay namespaces.
 *
 * Strategy: Detect if running in container → warn user → set up Zygisk direct path instead.
 *
 * Updated: Sep 30 2026
 */
public final class AceContainerEvasionEngine {

    private static final String TAG = "AceContainerEvasion";

    private AceContainerEvasionEngine() {}

    // Known container APK paths / props / marker files
    private static final String[] CONTAINER_PKG_PATHS = {
        "/data/data/com.lbe.parallel.intl",    // Parallel Space
        "/data/data/io.va.exposed",             // VirtualAPP
        "/data/data/com.vphonegaga.titan",      // VMOS
        "/data/data/com.zhuoyi.f1vm",           // F1VM
        "/data/data/com.pspace.andx8",          // X8 Sandbox
        "/data/data/com.bly.dualspace",         // Dual Space
        "/data/data/com.lbe.security.intl",     // LBE Security Space
    };

    private static final String[] EMULATOR_PROPS = {
        "ro.kernel.qemu",         // generic QEMU
        "ro.product.device=generic",
        "ro.build.product=generic",
        "ro.product.model=Android SDK built for x86",
    };

    private static final String[] CONTAINER_MOUNT_MARKERS = {
        "/proc/self/mountinfo",   // checked for overlay/bind mounts
    };

    // ─────────────────────────────────────────────────────────────────────────
    // CONTAINER DETECTION
    // ─────────────────────────────────────────────────────────────────────────

    /** @return true if the device appears to be running inside a virtual container */
    public static boolean isRunningInContainer() {
        // Check 1: Known container app data dirs present
        for (String path : CONTAINER_PKG_PATHS) {
            if (new java.io.File(path).exists()) {
                Log.w(TAG, "[ContainerDetect] Container app dir found: " + path);
                return true;
            }
        }

        // Check 2: Emulator property markers
        for (String propLine : EMULATOR_PROPS) {
            if (propLine.contains("=")) {
                String[] kv = propLine.split("=", 2);
                if (kv.length == 2 && kv[1].equals(getProp(kv[0]))) {
                    Log.w(TAG, "[ContainerDetect] Emulator prop match: " + propLine);
                    return true;
                }
            } else if (!getProp(propLine).isEmpty()) {
                Log.w(TAG, "[ContainerDetect] Container prop set: " + propLine);
                return true;
            }
        }

        // Check 3: VirtualAPP overlay mounts in /proc/self/mountinfo
        if (hasMountOverlay()) {
            Log.w(TAG, "[ContainerDetect] Overlay/bind mounts detected in mountinfo");
            return true;
        }

        // Check 4: Suspicious /proc/1/status — init should not be in a user namespace
        if (isInUserNamespace()) {
            Log.w(TAG, "[ContainerDetect] User namespace detected (container indicator)");
            return true;
        }

        return false;
    }

    /** Returns all detected container signatures for display in UI */
    public static java.util.List<String> detectContainerSignatures() {
        java.util.List<String> found = new java.util.ArrayList<>();

        for (String path : CONTAINER_PKG_PATHS) {
            if (new java.io.File(path).exists()) {
                found.add("Container app: " + path);
            }
        }
        if (hasMountOverlay()) found.add("Overlay/bind-mount namespace detected");
        if (isInUserNamespace())  found.add("Linux user namespace (VM/container)");

        // Check for known emulator build fingerprints
        String fp = getProp("ro.build.fingerprint");
        if (fp.contains("generic") || fp.contains("emulator") || fp.contains("sdk_gphone")) {
            found.add("Emulator fingerprint: " + fp.substring(0, Math.min(fp.length(), 50)));
        }

        return found;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ZYGISK DIRECT INJECTION PATH SETUP
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Configures Zygisk direct injection path (bypasses container detection).
     * Call when isRunningInContainer() returns false (real device) to confirm
     * the injection goes through Zygisk pre-specialize hook, not a container stub.
     */
    public static void applyZygiskDirectBypass(String packageName) {
        if (packageName == null) return;
        // Disable VirtualAPP / Parallel Space intercept layers if active
        String cleanup =
            "pm disable com.lbe.parallel.intl 2>/dev/null; " +
            "pm disable io.va.exposed 2>/dev/null; " +
            "am force-stop com.lbe.parallel.intl 2>/dev/null; " +
            "am force-stop io.va.exposed 2>/dev/null; ";
        // Signal gamebooster Zygisk module to use direct mode for this package
        String configure =
            "setprop gamebooster.ace.target " + packageName + "; " +
            "setprop gamebooster.ace.container.mode direct; ";
        executePrivileged(cleanup + configure);
        Log.i(TAG, "[Zygisk] Direct bypass configured for: " + packageName);
    }

    /**
     * Full container evasion flow — detects, logs, and configures bypass.
     * @return ContainerStatus for UI display
     */
    public static ContainerStatus applyContainerEvasion(String packageName) {
        java.util.List<String> sigs = detectContainerSignatures();
        if (!sigs.isEmpty()) {
            Log.w(TAG, "[ContainerEvasion] Container indicators found: " + sigs);
            // ACE will flag container — can only warn user, not bypass from inside container
            return ContainerStatus.CONTAINER_DETECTED;
        }
        applyZygiskDirectBypass(packageName);
        return ContainerStatus.DIRECT_ZYGISK_CONFIGURED;
    }

    public enum ContainerStatus {
        DIRECT_ZYGISK_CONFIGURED,
        CONTAINER_DETECTED   // User must launch game directly, not from container
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private static boolean hasMountOverlay() {
        java.io.File mi = new java.io.File("/proc/self/mountinfo");
        if (!mi.canRead()) return false;
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(mi))) {
            String line;
            int overlayCount = 0;
            while ((line = br.readLine()) != null) {
                if (line.contains("overlay") || line.contains("tmpfs /data/data")) {
                    overlayCount++;
                }
                // Legitimate system has a few overlay mounts; excessive = container
                if (overlayCount > 8) return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private static boolean isInUserNamespace() {
        java.io.File status = new java.io.File("/proc/1/status");
        if (!status.canRead()) return false;
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(status))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("NSpid:")) {
                    // Multiple NS PIDs = inside user namespace
                    String[] parts = line.split("\\s+");
                    return parts.length > 2;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private static String getProp(String key) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"getprop", key});
            byte[] buf = p.getInputStream().readAllBytes();
            return new String(buf).trim();
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static void executePrivileged(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) return;
        try {
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        } catch (Throwable t) {
            Log.w(TAG, "executePrivileged note: " + t.getMessage());
        }
    }
}

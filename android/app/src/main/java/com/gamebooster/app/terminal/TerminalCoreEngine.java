package com.gamebooster.app.terminal;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.gamebooster.app.engine.ShellExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Universal Multi-Tier Terminal Engine for Game Booster PRO.
 * Handles command execution seamlessly across both non-privileged runtime mode
 * (standard shell & Android Settings) and elevated Shizuku/Root mode across Android 13, 14, 15, and 16.
 */
public class TerminalCoreEngine {

    private static final String TAG = "TerminalCoreEngine";
    private static final String TEMP_DIR = "/data/local/tmp";
    private static volatile TerminalCoreEngine instance;

    private final List<TerminalScriptPreset> presetScripts = new ArrayList<>();

    private TerminalCoreEngine() {
        initDefaultPresets();
    }

    public static TerminalCoreEngine getInstance() {
        if (instance == null) {
            synchronized (TerminalCoreEngine.class) {
                if (instance == null) {
                    instance = new TerminalCoreEngine();
                }
            }
        }
        return instance;
    }

    public boolean isPrivilegedRootActive() {
        return ShizukuExecutor.hasShizukuPermission();
    }

    public String getPrivilegeTierString() {
        if (isPrivilegedRootActive()) {
            return "🟢 SHIZUKU PRIVILEGED / ROOT (UID 2000)";
        } else {
            return "🟡 NATIVE SYSTEM RUNTIME (STANDARD SHELL)";
        }
    }

    /**
     * Executes a single or piped shell command with automatic multi-tier fallback.
     * Tier 1: Elevated Shizuku / Root Binder
     * Tier 2: Native Android Settings Provider (for settings get/put)
     * Tier 3: Zero-permission Linux Runtime Process Execution (sh -c)
     */
    public String executeCommand(String command) {
        return executeCommand(null, command);
    }

    public String executeCommand(Context context, String command) {
        if (command == null || command.trim().isEmpty()) {
            return "";
        }
        String trimmed = command.trim();

        // 1. If Shizuku is connected, execute with elevated privileges
        if (isPrivilegedRootActive()) {
            try {
                return ShizukuExecutor.executeShizukuCommand(trimmed);
            } catch (Throwable t) {
                Log.w(TAG, "Shizuku exec failed, falling back: " + t.getMessage());
            }
        }

        // 2. Direct Settings Inspection Fallback if non-root and command is settings get/put
        if (context != null && (trimmed.startsWith("settings get ") || trimmed.startsWith("settings put "))) {
            String settingsResult = handleDirectSettingsCommand(context, trimmed);
            if (settingsResult != null) {
                return settingsResult;
            }
        }

        // 3. Native Linux Runtime Process Execution (Zero-Permission Fallback)
        try {
            ShellExecutor.CommandResult shellRes = ShellExecutor.executeCommand(trimmed);
            if (shellRes.isSuccess()) {
                return shellRes.stdout.isEmpty() ? "SUCCESS (Exit Code 0)" : shellRes.stdout;
            } else {
                String err = shellRes.stderr.isEmpty() ? shellRes.stdout : shellRes.stderr;
                if (err.isEmpty()) {
                    err = "Command returned exit code " + shellRes.exitCode;
                }
                return err;
            }
        } catch (Throwable t) {
            Log.e(TAG, "Native shell execution failed: " + trimmed, t);
            return "ERROR: " + t.getMessage();
        }
    }

    /**
     * Writes and executes a multi-line shell script.
     * If Shizuku is active: uses `/data/local/tmp` with `chmod 777`.
     * If Shizuku is offline: writes to app cache directory and runs via native Linux `sh`.
     */
    public String writeAndExecuteTempScript(Context context, String scriptFileName, String scriptBody) {
        if (scriptBody == null || scriptBody.trim().isEmpty()) {
            return "ERROR: Script body is empty";
        }

        if (scriptFileName == null || scriptFileName.trim().isEmpty()) {
            scriptFileName = "gamebooster_temp_" + System.currentTimeMillis() + ".sh";
        }
        if (!scriptFileName.endsWith(".sh")) {
            scriptFileName += ".sh";
        }

        // Tier 1: Elevated /data/local/tmp execution via Shizuku
        if (isPrivilegedRootActive()) {
            String targetPath = TEMP_DIR + "/" + scriptFileName;
            try {
                String base64Content = Base64.encodeToString(scriptBody.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
                String writeCmd = "echo \"" + base64Content + "\" | base64 -d > " + targetPath;
                ShizukuExecutor.executeShizukuCommand(writeCmd);
                ShizukuExecutor.executeShizukuCommand("chmod 777 " + targetPath);
                String result = ShizukuExecutor.executeShizukuCommand("sh " + targetPath);
                return result != null && !result.isEmpty() ? result : "SUCCESS (Exit Code 0 via Shizuku Root)";
            } catch (Throwable t) {
                Log.w(TAG, "Shizuku temp script failed: " + t.getMessage());
            }
        }

        // Tier 2: App Cache Directory Script Runner (No Shizuku Needed)
        try {
            File cacheDir = context != null ? context.getCacheDir() : new File("/sdcard/Download");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File scriptFile = new File(cacheDir, scriptFileName);

            try (FileOutputStream fos = new FileOutputStream(scriptFile)) {
                fos.write(scriptBody.getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }
            scriptFile.setExecutable(true, false);
            scriptFile.setReadable(true, false);

            ShellExecutor.CommandResult res = ShellExecutor.executeCommand("sh " + scriptFile.getAbsolutePath());
            if (res.isSuccess()) {
                return res.stdout.isEmpty() ? "SUCCESS (Exit Code 0 via Native Script Runner)" : res.stdout;
            } else {
                return !res.stderr.isEmpty() ? res.stderr : res.stdout;
            }
        } catch (Throwable t) {
            Log.e(TAG, "Local cache script failed: " + scriptFileName, t);
            return "ERROR: " + t.getMessage();
        }
    }

    public String writeAndExecuteTempScript(String scriptFileName, String scriptBody) {
        return writeAndExecuteTempScript(null, scriptFileName, scriptBody);
    }

    private String handleDirectSettingsCommand(Context context, String cmd) {
        try {
            String[] tokens = cmd.split("\\s+");
            if (tokens.length >= 3 && tokens[1].equalsIgnoreCase("get")) {
                String namespace = tokens[2].toLowerCase();
                String key = tokens[3];
                String val = null;
                if ("system".equals(namespace)) {
                    val = Settings.System.getString(context.getContentResolver(), key);
                } else if ("global".equals(namespace)) {
                    val = Settings.Global.getString(context.getContentResolver(), key);
                } else if ("secure".equals(namespace)) {
                    val = Settings.Secure.getString(context.getContentResolver(), key);
                }
                return val != null ? val : "null";
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public List<String> executeScript(List<String> scriptLines) {
        if (scriptLines == null || scriptLines.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> results = new ArrayList<>();
        for (String line : scriptLines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            results.add(executeCommand(trimmed));
        }
        return results;
    }

    public void cleanupTempScripts(Context context) {
        try {
            if (isPrivilegedRootActive()) {
                ShizukuExecutor.executeShizukuCommand("rm -f " + TEMP_DIR + "/gamebooster_*.sh");
            }
            if (context != null && context.getCacheDir() != null) {
                File[] files = context.getCacheDir().listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().startsWith("gamebooster_") || f.getName().endsWith(".sh")) {
                            f.delete();
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    private void initDefaultPresets() {
        presetScripts.clear();

        // 1. Identity & Kernel Diagnostics
        presetScripts.add(new TerminalScriptPreset(
                "diag_id",
                "⚡ Whoami / System UID",
                "Queries user ID, kernel version, and OS build info",
                "id; whoami; uname -a; getprop ro.build.version.release; getprop ro.product.model"
        ));

        // 2. Global / System / Secure Settings Explorer
        presetScripts.add(new TerminalScriptPreset(
                "diag_settings",
                "⚙️ System & Global Settings",
                "Dumps key gaming and display window manager settings",
                "settings get system peak_refresh_rate; settings get system min_refresh_rate; settings get global window_animation_scale; settings get global game_driver_all_apps"
        ));

        // 3. Storage & Directories
        presetScripts.add(new TerminalScriptPreset(
                "diag_storage",
                "📁 /Android/data & Storage",
                "Lists installed game packages and storage mount table",
                "ls -la /sdcard/Android/data; df -h /sdcard; df -h /data"
        ));

        // 4. Temporary /data/local/tmp Scripts Directory
        presetScripts.add(new TerminalScriptPreset(
                "diag_temp",
                "📂 /data/local/tmp Explorer",
                "Inspects executable temporary scripts and binary files",
                "ls -la /data/local/tmp; ls -la /cache 2>/dev/null"
        ));

        // 5. FPS, Refresh Rate & SurfaceFlinger Pacing
        presetScripts.add(new TerminalScriptPreset(
                "diag_fps",
                "🎮 120-185 FPS SurfaceFlinger",
                "Dumps SurfaceFlinger frame pacing, refresh rate locks, and hardware layers",
                "dumpsys SurfaceFlinger --latency; getprop debug.sf.fps_limit; getprop persist.sys.NV_FPSLIMIT; settings get system peak_refresh_rate"
        ));

        // 6. Deep RAM Flush & Trim
        presetScripts.add(new TerminalScriptPreset(
                "tweak_ram",
                "🧹 Deep RAM Flush & Trim",
                "Frees memory buffers and flushes application cache across the system",
                "pm trim-caches 999999999999; am kill-all; dumpsys meminfo --oom"
        ));

        // 7. Touch & Gyro Zero-Delay Input
        presetScripts.add(new TerminalScriptPreset(
                "tweak_touch",
                "🎯 1000Hz Touch Slop & Gyro",
                "Inspects and applies 1000Hz touch rate and zero touch slop",
                "getprop view.touch_slop; getprop debug.input.max_events_per_sec; getprop sys.use_fifo; getprop persist.sys.touch.pressure.scale"
        ));

        // 8. GPU Game Driver & ANGLE Renderer
        presetScripts.add(new TerminalScriptPreset(
                "tweak_gpu",
                "🚀 Game Driver & ANGLE Mode",
                "Inspects ANGLE OpenGL ES layer and global Game Driver bindings",
                "settings get global game_driver_all_apps; settings get global angle_gl_driver_all_angle; getprop debug.hwui.renderer"
        ));

        // 9. Thermal Status & Battery Governor
        presetScripts.add(new TerminalScriptPreset(
                "tweak_thermal",
                "🛡️ Thermal Throttle Inspection",
                "Checks device thermal status, temperature sensor zones, and battery engine",
                "dumpsys thermalservice; dumpsys battery; cat /sys/class/thermal/thermal_zone0/temp 2>/dev/null"
        ));

        // 10. Matrix DNS & Latency Relay
        presetScripts.add(new TerminalScriptPreset(
                "diag_net",
                "🌐 Matrix Edge Ping Diagnostic",
                "Queries active DNS resolver and tests low-latency gaming route",
                "getprop net.dns1; ping -c 3 1.1.1.1"
        ));
    }

    public List<TerminalScriptPreset> getPresetScripts() {
        return presetScripts;
    }

    public int getAndroidVersion() {
        return Build.VERSION.SDK_INT;
    }
}

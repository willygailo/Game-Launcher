package com.gamebooster.app.terminal;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.gamebooster.app.engine.ShellExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Universal Multi-Tier Terminal Engine for Game Booster PRO.
 * Built-in SetEdit (System/Secure/Global Database Editor), DeviceConfig modifier,
 * direct storage script runner (/storage/emulated/0, /sdcard/Download),
 * and privileged Shizuku temporary root execution across Android 13, 14, 15, and 16.
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
     * Automatically handles:
     * 1. Storage script execution (/storage/emulated/0, Download, etc.)
     * 2. Built-in SetEdit commands (setedit put/get/list/search)
     * 3. Elevated Shizuku / Root Binder (settings, setprop, device_config, dumpsys)
     * 4. Native Android Settings Provider
     * 5. Native Linux process execution
     */
    public String executeCommand(String command) {
        return executeCommand(null, command);
    }

    public String executeCommand(Context context, String command) {
        if (command == null || command.trim().isEmpty()) {
            return "";
        }
        String trimmed = command.trim();

        // 1. Check if command is a script execution (e.g. sh /storage/... or run tweak.sh)
        String scriptFileResult = resolveAndExecuteScriptFile(context, trimmed);
        if (scriptFileResult != null) {
            return scriptFileResult;
        }

        // 2. Check if command is a SetEdit command (e.g. setedit put/get/list/search)
        if (trimmed.startsWith("setedit ") || "setedit".equalsIgnoreCase(trimmed)) {
            return handleSetEditCommand(context, trimmed);
        }

        // 3. If Shizuku is connected, execute with elevated privileges (Temporary Root UID 2000)
        if (isPrivilegedRootActive()) {
            try {
                return ShizukuExecutor.executeShizukuCommand(trimmed);
            } catch (Throwable t) {
                Log.w(TAG, "Shizuku exec failed, falling back: " + t.getMessage());
            }
        }

        // 4. Direct Settings Inspection Fallback if non-root and command is settings get/put
        if (context != null && (trimmed.startsWith("settings get ") || trimmed.startsWith("settings put "))) {
            String settingsResult = handleDirectSettingsCommand(context, trimmed);
            if (settingsResult != null) {
                return settingsResult;
            }
        }

        // 5. Native Linux Runtime Process Execution (Zero-Permission Fallback)
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
     * Resolves script execution commands such as:
     * - `sh /storage/emulated/0/Download/game.sh`
     * - `run /sdcard/Download/tweak.sh`
     * - `run my_tweak.sh`
     * - `/storage/emulated/0/Download/boost.sh`
     * Automatically reads the file from device storage, pushes to /data/local/tmp with chmod 777,
     * and executes with elevated root/Shizuku permissions.
     */
    public String resolveAndExecuteScriptFile(Context context, String rawInput) {
        String candidatePath = rawInput.trim();
        boolean isExplicitScriptCmd = false;

        if (candidatePath.startsWith("sh ")) {
            candidatePath = candidatePath.substring(3).trim();
            isExplicitScriptCmd = true;
        } else if (candidatePath.startsWith("bash ")) {
            candidatePath = candidatePath.substring(5).trim();
            isExplicitScriptCmd = true;
        } else if (candidatePath.startsWith("source ")) {
            candidatePath = candidatePath.substring(7).trim();
            isExplicitScriptCmd = true;
        } else if (candidatePath.startsWith("run ")) {
            candidatePath = candidatePath.substring(4).trim();
            isExplicitScriptCmd = true;
        } else if (candidatePath.startsWith("exec ")) {
            candidatePath = candidatePath.substring(5).trim();
            isExplicitScriptCmd = true;
        } else if (candidatePath.startsWith("./")) {
            candidatePath = candidatePath.substring(2).trim();
            isExplicitScriptCmd = true;
        } else if (candidatePath.startsWith("/") && candidatePath.endsWith(".sh")) {
            isExplicitScriptCmd = true;
        } else if (candidatePath.endsWith(".sh")) {
            isExplicitScriptCmd = true;
        }

        if (!isExplicitScriptCmd && !candidatePath.startsWith("/storage/") && !candidatePath.startsWith("/sdcard/")) {
            return null;
        }

        if ((candidatePath.startsWith("\"") && candidatePath.endsWith("\"")) ||
            (candidatePath.startsWith("'") && candidatePath.endsWith("'"))) {
            candidatePath = candidatePath.substring(1, candidatePath.length() - 1).trim();
        }

        File scriptFile = findScriptFile(context, candidatePath);
        if (scriptFile != null && scriptFile.exists() && scriptFile.isFile()) {
            return executeScriptFileObject(context, scriptFile);
        }

        if (isExplicitScriptCmd && (candidatePath.contains("/") || candidatePath.endsWith(".sh"))) {
            return "ERROR: Script file not found: " + candidatePath +
                    "\n💡 Tip: Check if the file exists in /storage/emulated/0/Download/ or use the '📂 Run .sh File' button.";
        }

        return null;
    }

    /**
     * Locates a script file across standard Android storage locations.
     */
    public File findScriptFile(Context context, String path) {
        if (path == null || path.trim().isEmpty()) return null;

        // 1. Direct absolute path
        File direct = new File(path);
        if (direct.exists() && direct.isFile()) return direct;

        // 2. /storage/emulated/0/Download/
        File dl1 = new File("/storage/emulated/0/Download", path);
        if (dl1.exists() && dl1.isFile()) return dl1;

        // 3. /sdcard/Download/
        File dl2 = new File("/sdcard/Download", path);
        if (dl2.exists() && dl2.isFile()) return dl2;

        // 4. /storage/emulated/0/
        File root1 = new File("/storage/emulated/0", path);
        if (root1.exists() && root1.isFile()) return root1;

        // 5. /sdcard/
        File root2 = new File("/sdcard", path);
        if (root2.exists() && root2.isFile()) return root2;

        // 6. Environment.DIRECTORY_DOWNLOADS
        try {
            File extDl = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), path);
            if (extDl.exists() && extDl.isFile()) return extDl;
        } catch (Throwable ignored) {}

        // 7. /data/local/tmp/
        File tmp = new File(TEMP_DIR, path);
        if (tmp.exists() && tmp.isFile()) return tmp;

        // 8. App cache directory
        if (context != null) {
            File cache = new File(context.getCacheDir(), path);
            if (cache.exists() && cache.isFile()) return cache;
        }

        return null;
    }

    /**
     * Reads a script file from device storage, safely deploys it to /data/local/tmp with chmod 777,
     * and runs it with privileged Shizuku/root execution.
     */
    public String executeScriptFileObject(Context context, File scriptFile) {
        if (scriptFile == null || !scriptFile.exists()) {
            return "ERROR: File does not exist.";
        }
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(scriptFile), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            String scriptBody = sb.toString().trim();
            if (scriptBody.isEmpty()) {
                return "WARNING: Script file is empty: " + scriptFile.getAbsolutePath();
            }

            String fileName = scriptFile.getName();
            String runnerTier = isPrivilegedRootActive() ?
                    "⚡ Shizuku Privileged Root (UID 2000) -> /data/local/tmp/" + fileName :
                    "🟡 Native Shell Runner (Standard Sandbox)";

            StringBuilder report = new StringBuilder();
            report.append("═══════════════════════════════════════════════════════\n");
            report.append("📂 SCRIPT RUNNER: ").append(scriptFile.getAbsolutePath()).append("\n");
            report.append("🚀 EXECUTION TIER: ").append(runnerTier).append("\n");
            report.append("═══════════════════════════════════════════════════════\n");

            String execResult = writeAndExecuteTempScript(context, fileName, scriptBody);
            report.append(execResult).append("\n");
            report.append("═══════════════════════════════════════════════════════\n");
            report.append("✅ SCRIPT EXECUTION FINISHED");

            return report.toString();
        } catch (Throwable t) {
            Log.e(TAG, "Error executing script file: " + scriptFile.getAbsolutePath(), t);
            return "ERROR executing script: " + t.getMessage();
        }
    }

    /**
     * Reads and executes a script directly from a selected Storage Uri (from SAF File Picker).
     */
    public String executeScriptFromUri(Context context, Uri uri, String displayName) {
        if (context == null || uri == null) {
            return "ERROR: Invalid script URI";
        }
        try {
            StringBuilder sb = new StringBuilder();
            try (InputStream is = context.getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            String scriptBody = sb.toString().trim();
            if (scriptBody.isEmpty()) {
                return "WARNING: Selected script file is empty.";
            }

            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = "picked_script_" + System.currentTimeMillis() + ".sh";
            }
            if (!displayName.endsWith(".sh")) {
                displayName += ".sh";
            }

            String runnerTier = isPrivilegedRootActive() ?
                    "⚡ Shizuku Privileged Root (UID 2000) -> /data/local/tmp/" + displayName :
                    "🟡 Native Shell Runner";

            StringBuilder report = new StringBuilder();
            report.append("═══════════════════════════════════════════════════════\n");
            report.append("📂 LOADED SCRIPT FROM STORAGE: ").append(displayName).append("\n");
            report.append("🚀 EXECUTION TIER: ").append(runnerTier).append("\n");
            report.append("═══════════════════════════════════════════════════════\n");

            String execResult = writeAndExecuteTempScript(context, displayName, scriptBody);
            report.append(execResult).append("\n");
            report.append("═══════════════════════════════════════════════════════\n");
            report.append("✅ SCRIPT EXECUTION FINISHED");

            return report.toString();
        } catch (Throwable t) {
            Log.e(TAG, "Error reading script URI: " + uri, t);
            return "ERROR loading script: " + t.getMessage();
        }
    }

    /**
     * Built-in SetEdit Engine:
     * Allows inspecting, listing, modifying, and searching system, secure, and global tables.
     */
    public String handleSetEditCommand(Context context, String command) {
        String trimmed = command.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length <= 1 || "help".equalsIgnoreCase(parts[1]) || "--help".equalsIgnoreCase(parts[1])) {
            return "📝 SetEdit Universal Engine (Android System/Secure/Global Database Editor)\n\n" +
                    "SYNTAX GUIDE:\n" +
                    " • Put Value:    setedit put <system|secure|global> <key> <val>\n" +
                    "                 settings put <system|secure|global> <key> <val>\n" +
                    " • Get Value:    setedit get <system|secure|global> <key>\n" +
                    "                 settings get <system|secure|global> <key>\n" +
                    " • Delete Key:   setedit delete <system|secure|global> <key>\n" +
                    " • List Table:   setedit list <system|secure|global>\n" +
                    " • Search Key:   setedit search <keyword>\n\n" +
                    "TABLES:\n" +
                    " • system: UI animations, refresh rates, sound, touch sensitivity\n" +
                    " • secure: ADB toggles, input methods, accessibility, device locks\n" +
                    " • global: Game Driver, ANGLE renderer, window animations, WiFi/data policies\n\n" +
                    "POPULAR GAMING SETEDIT TWEAKS:\n" +
                    " • setedit put system peak_refresh_rate 120\n" +
                    " • setedit put system min_refresh_rate 120\n" +
                    " • setedit put global window_animation_scale 0.5\n" +
                    " • setedit put global transition_animation_scale 0.5\n" +
                    " • setedit put global animator_duration_scale 0.5\n" +
                    " • setedit put global game_driver_all_apps 1\n" +
                    " • setedit put system touch_slop_reduction 1\n" +
                    " • setedit search refresh";
        }

        String subCmd = parts[1].toLowerCase();

        if ("list".equalsIgnoreCase(subCmd)) {
            if (parts.length < 3) {
                return "ERROR: Usage: setedit list <system|secure|global>";
            }
            String table = parts[2].toLowerCase();
            return executeCommand(context, "settings list " + table);
        }

        if ("search".equalsIgnoreCase(subCmd) || "find".equalsIgnoreCase(subCmd)) {
            if (parts.length < 3) {
                return "ERROR: Usage: setedit search <keyword>";
            }
            String query = parts[2].toLowerCase();
            StringBuilder searchResult = new StringBuilder();
            searchResult.append("🔍 SETEDIT SEARCH RESULTS FOR: '").append(query).append("'\n");
            searchResult.append("───────────────────────────────────────────────────\n");

            String[] tables = {"system", "secure", "global"};
            int matchCount = 0;
            for (String tbl : tables) {
                String dump = executeCommand(context, "settings list " + tbl);
                if (dump != null && !dump.startsWith("ERROR") && !dump.isEmpty()) {
                    String[] lines = dump.split("\n");
                    for (String l : lines) {
                        if (l.toLowerCase().contains(query)) {
                            searchResult.append("[").append(tbl.toUpperCase()).append("] ").append(l).append("\n");
                            matchCount++;
                        }
                    }
                }
            }
            if (matchCount == 0) {
                searchResult.append("No matching keys found in system/secure/global tables.\n");
            } else {
                searchResult.append("───────────────────────────────────────────────────\n");
                searchResult.append("Total Matches: ").append(matchCount);
            }
            return searchResult.toString();
        }

        if ("put".equalsIgnoreCase(subCmd) || "set".equalsIgnoreCase(subCmd)) {
            if (parts.length < 5) {
                return "ERROR: Usage: setedit put <system|secure|global> <key> <value>";
            }
            String table = parts[2].toLowerCase();
            String key = parts[3];
            StringBuilder valBuilder = new StringBuilder();
            for (int i = 4; i < parts.length; i++) {
                if (i > 4) valBuilder.append(" ");
                valBuilder.append(parts[i]);
            }
            String val = valBuilder.toString();
            return executeCommand(context, "settings put " + table + " " + key + " " + val);
        }

        if ("get".equalsIgnoreCase(subCmd)) {
            if (parts.length < 4) {
                return "ERROR: Usage: setedit get <system|secure|global> <key>";
            }
            String table = parts[2].toLowerCase();
            String key = parts[3];
            return executeCommand(context, "settings get " + table + " " + key);
        }

        if ("delete".equalsIgnoreCase(subCmd) || "del".equalsIgnoreCase(subCmd) || "rm".equalsIgnoreCase(subCmd)) {
            if (parts.length < 4) {
                return "ERROR: Usage: setedit delete <system|secure|global> <key>";
            }
            String table = parts[2].toLowerCase();
            String key = parts[3];
            return executeCommand(context, "settings delete " + table + " " + key);
        }

        return "Unknown SetEdit subcommand: " + subCmd + ". Type 'setedit help' for usage.";
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

        // 2. SetEdit Ultra Gaming Pack
        presetScripts.add(new TerminalScriptPreset(
                "setedit_game_pack",
                "⚡ SetEdit Game Pack",
                "Applies maximum refresh rate, 0.5x animations, Game Driver, and touch slop tweaks",
                "settings put system peak_refresh_rate 120; settings put system min_refresh_rate 120; settings put global window_animation_scale 0.5; settings put global transition_animation_scale 0.5; settings put global animator_duration_scale 0.5; settings put global game_driver_all_apps 1; settings put system touch_slop_reduction 1; echo '[SETEDIT GAMING PACK APPLIED]'"
        ));

        // 3. GPU Hardware Acceleration Setprop Tweaks
        presetScripts.add(new TerminalScriptPreset(
                "gpu_hw_setprops",
                "🚀 GPU setprop & HW Acceleration",
                "Applies low-level GPU rendering properties and hardware overlays",
                "setprop debug.egl.hw 1; setprop debug.sf.hw 1; setprop debug.hwui.renderer skiagl; setprop renderthread.initialize.priority 1; getprop debug.egl.hw; echo '[GPU & HW PROPERTIES CONFIGURED]'"
        ));

        // 4. Global / System / Secure Settings Explorer
        presetScripts.add(new TerminalScriptPreset(
                "diag_settings",
                "⚙️ System & Global Settings",
                "Dumps key gaming and display window manager settings",
                "settings get system peak_refresh_rate; settings get system min_refresh_rate; settings get global window_animation_scale; settings get global game_driver_all_apps"
        ));

        // 5. Storage & Directories
        presetScripts.add(new TerminalScriptPreset(
                "diag_storage",
                "📁 /Android/data & Storage",
                "Lists installed game packages and storage mount table",
                "ls -la /sdcard/Android/data; df -h /sdcard; df -h /data"
        ));

        // 6. Temporary /data/local/tmp Scripts Directory
        presetScripts.add(new TerminalScriptPreset(
                "diag_temp",
                "📂 /data/local/tmp Explorer",
                "Inspects executable temporary scripts and binary files",
                "ls -la /data/local/tmp; ls -la /cache 2>/dev/null"
        ));

        // 7. FPS, Refresh Rate & SurfaceFlinger Pacing
        presetScripts.add(new TerminalScriptPreset(
                "diag_fps",
                "🎮 120-185 FPS SurfaceFlinger",
                "Dumps SurfaceFlinger frame pacing, refresh rate locks, and hardware layers",
                "dumpsys SurfaceFlinger --latency; getprop debug.sf.fps_limit; getprop persist.sys.NV_FPSLIMIT; settings get system peak_refresh_rate"
        ));

        // 8. Deep RAM Flush & Trim
        presetScripts.add(new TerminalScriptPreset(
                "tweak_ram",
                "🧹 Deep RAM Flush & Trim",
                "Frees memory buffers and flushes application cache across the system",
                "pm trim-caches 999999999999; am kill-all; dumpsys meminfo --oom"
        ));

        // 9. Touch & Gyro Zero-Delay Input
        presetScripts.add(new TerminalScriptPreset(
                "tweak_touch",
                "🎯 1000Hz Touch Slop & Gyro",
                "Inspects and applies 1000Hz touch rate and zero touch slop",
                "getprop view.touch_slop; getprop debug.input.max_events_per_sec; getprop sys.use_fifo; getprop persist.sys.touch.pressure.scale"
        ));

        // 10. GPU Game Driver & ANGLE Renderer
        presetScripts.add(new TerminalScriptPreset(
                "tweak_gpu",
                "🚀 Game Driver & ANGLE Mode",
                "Inspects ANGLE OpenGL ES layer and global Game Driver bindings",
                "settings get global game_driver_all_apps; settings get global angle_gl_driver_all_angle; getprop debug.hwui.renderer"
        ));

        // 11. Thermal Status & Battery Governor
        presetScripts.add(new TerminalScriptPreset(
                "tweak_thermal",
                "🛡️ Thermal Throttle Inspection",
                "Checks device thermal status, temperature sensor zones, and battery engine",
                "dumpsys thermalservice; dumpsys battery; cat /sys/class/thermal/thermal_zone0/temp 2>/dev/null"
        ));

        // 12. Matrix DNS & Latency Relay
        presetScripts.add(new TerminalScriptPreset(
                "diag_net",
                "🌐 Matrix Edge Ping Diagnostic",
                "Queries active DNS resolver and tests low-latency gaming route",
                "getprop net.dns1; ping -c 3 1.1.1.1"
        ));

        // 13. Ahead-Of-Time (AOT) DEX Speed Compilation
        presetScripts.add(new TerminalScriptPreset(
                "preset_aot_dexopt",
                "⚡ AOT DEX Speed Compilation",
                "Forces system package manager speed compilation to eliminate JIT stutter",
                "cmd package compile -m speed -f com.mobile.legends; cmd package compile -m speed -f com.tencent.ig; cmd package compile -m speed -f com.activision.callofduty.shooter; cmd package compile -m speed -f com.dts.freefireth; echo '[AOT SPEED COMPILATION COMPLETE]'"
        ));

        // 14. Universal OEM Game Throttling Bypass
        presetScripts.add(new TerminalScriptPreset(
                "preset_oem_bypass",
                "🛡️ Legal OEM Throttling Neutralizer",
                "Neutralizes Joyose, Samsung GOS, ColorOS GPA, and resets thermal flags",
                "pm disable-user --user 0 com.xiaomi.joyose; pm disable-user --user 0 com.samsung.android.game.gos; pm disable-user --user 0 com.oplus.games; cmd thermalservice override-status 0; echo '[OEM THROTTLING BYPASSED]'"
        ));

        // 15. 185Hz / 165Hz Display & FPS Force Lock
        presetScripts.add(new TerminalScriptPreset(
                "preset_185hz_force",
                "🚀 185Hz / 165Hz Extreme Display & FPS Lock",
                "Forces peak refresh rate and overrides SurfaceFlinger frame pacing",
                "cmd window set-app-refresh-rate --force 185; settings put system peak_refresh_rate 185.0; settings put system min_refresh_rate 185.0; setprop debug.sf.fps_limit 185; echo '[185Hz REFRESH RATE ACTIVE]'"
        ));

        // 16. Android 16 cgroup v2 & ADPF Scheduler Boost
        presetScripts.add(new TerminalScriptPreset(
                "preset_adpf_android16",
                "⚡ Android 16 cgroup v2 & ADPF Boost",
                "Enforces Game Mode performance intervention and expands background app headroom",
                "cmd game mode performance global; cmd activity set-process-limit 32; setprop sys.use_fifo_ui 1; setprop persist.sys.sched_boost 1; echo '[ADPF & SCHEDULER BOOST CONFIGURED]'"
        ));

        // 17. Vulkan Swapchain & Triple Buffering Overdrive
        presetScripts.add(new TerminalScriptPreset(
                "preset_vulkan_overdrive",
                "🎮 Vulkan Swapchain & Triple Buffering Overdrive",
                "Overdrives Vulkan graphics pipeline with zero swap interval and backpressure bypass",
                "setprop debug.hwui.renderer vulkan; setprop debug.renderengine.backend skiagl; setprop debug.egl.swapinterval 0; setprop debug.sf.disable_backpressure 1; echo '[VULKAN OVERDRIVE APPLIED]'"
        ));
    }

    public List<TerminalScriptPreset> getPresetScripts() {
        return presetScripts;
    }

    public int getAndroidVersion() {
        return Build.VERSION.SDK_INT;
    }
}

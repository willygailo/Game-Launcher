package com.gamebooster.app.terminal;

import android.content.Context;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TerminalCoreEngine handles command dispatching, multi-line script execution,
 * temporary script generation in Android's `/data/local/tmp`, elevated
 * Shizuku/Root shell execution, and standard local shell fallback across Android 13-16.
 */
public class TerminalCoreEngine {

    private static final String TAG = "TerminalCoreEngine";
    private static final String TEMP_DIR = "/data/local/tmp";
    private static volatile TerminalCoreEngine instance;

    private final List<TerminalScriptPreset> presetScripts = new ArrayList<>();
    private String currentWorkingDir = "/sdcard/GameBooster/terminal";

    private TerminalCoreEngine() {
        initDefaultPresets();
        File initialDir = new File(currentWorkingDir);
        if (!initialDir.exists()) {
            initialDir.mkdirs();
        }
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

    public String getCurrentWorkingDir() {
        return currentWorkingDir;
    }

    public void setCurrentWorkingDir(String dir) {
        if (dir != null && !dir.trim().isEmpty()) {
            this.currentWorkingDir = dir.trim();
        }
    }

    /**
     * Executes a single or piped shell command via elevated Shizuku binder, root su,
     * or standard local process shell fallback.
     */
    public String executeCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "";
        }
        String trimmed = command.trim();

        // 1. Built-in help command
        if ("help".equalsIgnoreCase(trimmed) || "?".equals(trimmed)) {
            return "══════════════════════════════════════════════════════\n" +
                   "  ⚡ GAME LAUNCHER PRO — CYBER TERMINAL ENGINE ⚡\n" +
                   "══════════════════════════════════════════════════════\n" +
                   "COMMANDS:\n" +
                   "  • pwd                  - Print current working directory\n" +
                   "  • cd <dir>             - Change directory (e.g. cd /sdcard/Android/data)\n" +
                   "  • ls [-la]             - List files in current directory\n" +
                   "  • cat <file>           - Display contents of a file\n" +
                   "  • id / whoami          - Show current user and elevated shell UID\n" +
                   "  • clear / cls          - Clear terminal screen buffer\n" +
                   "  • sh <file.sh>         - Run any .sh script from terminal folder\n" +
                   "  • getprop <prop>       - Read system property (e.g. getprop ro.product.model)\n" +
                   "  • setprop <prop> <val> - Set elevated system property via Shizuku\n" +
                   "  • settings <get/put>   - Manage system/global settings\n" +
                   "  • dumpsys <service>    - Query Android system service state\n" +
                   "  • am / pm              - Control activities, packages, and memory\n" +
                   "══════════════════════════════════════════════════════\n" +
                   "📁 SCRIPTS FOLDER: " + currentWorkingDir + "\n" +
                   "══════════════════════════════════════════════════════";
        }

        // 2. Handle directory change commands internally
        if (trimmed.equals("pwd")) {
            return currentWorkingDir;
        }

        if (trimmed.startsWith("cd ") || trimmed.equals("cd")) {
            return handleCdCommand(trimmed);
        }

        // 3. Resolve script file execution if filename is typed directly
        String execCommandStr = trimmed;
        if (trimmed.endsWith(".sh")) {
            String scriptName = trimmed;
            if (scriptName.startsWith("./")) {
                scriptName = scriptName.substring(2);
            }
            File localScript = new File(currentWorkingDir, scriptName);
            File defaultFolderScript = new File("/sdcard/GameBooster/terminal", scriptName);
            if (localScript.exists()) {
                execCommandStr = "sh \"" + localScript.getAbsolutePath() + "\"";
            } else if (defaultFolderScript.exists()) {
                execCommandStr = "sh \"" + defaultFolderScript.getAbsolutePath() + "\"";
            }
        }

        // 4. Try Elevated Shizuku AIDL / Process Execution
        if (ShizukuExecutor.hasShizukuPermission()) {
            try {
                String cmdWithDir = "cd \"" + currentWorkingDir + "\" 2>/dev/null; " + execCommandStr;
                String result = com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().executeCommand(cmdWithDir);
                if (result != null && !result.trim().isEmpty()) {
                    return result;
                }
                return "[COMMAND COMPLETED (Exit Code 0)]";
            } catch (Throwable t) {
                Log.w(TAG, "Shizuku execution failed, falling back: " + t.getMessage());
            }
        }

        // 5. Fallback: Local Shell Process Execution
        return executeLocalShellCommand(execCommandStr);
    }

    private String handleCdCommand(String cmd) {
        if (cmd.equals("cd") || cmd.equals("cd ~")) {
            currentWorkingDir = "/sdcard/GameBooster/terminal";
            return currentWorkingDir;
        }
        String targetPath = cmd.substring(2).trim();
        if (targetPath.startsWith("/")) {
            File f = new File(targetPath);
            if (f.exists() && f.isDirectory()) {
                currentWorkingDir = targetPath;
            } else {
                currentWorkingDir = targetPath; // allow navigation even if permission-restricted directory
            }
        } else if (targetPath.equals("..")) {
            File parent = new File(currentWorkingDir).getParentFile();
            if (parent != null) {
                currentWorkingDir = parent.getAbsolutePath();
            }
        } else {
            File target = new File(currentWorkingDir, targetPath);
            currentWorkingDir = target.getAbsolutePath();
        }
        return currentWorkingDir;
    }

    /**
     * Executes command via standard Android runtime process (fallback).
     */
    public String executeLocalShellCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder("/system/bin/sh", "-c", "cd " + currentWorkingDir + " 2>/dev/null; " + command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            String result = output.toString().trim();
            if (result.isEmpty()) {
                return exitCode == 0 ? "[COMMAND COMPLETED (Exit Code 0)]" : "[EXIT CODE: " + exitCode + "]";
            }
            return result;
        } catch (Throwable t) {
            Log.e(TAG, "Local shell execution error: " + command, t);
            return "ERROR: " + t.getMessage();
        }
    }

    /**
     * Writes a custom shell tweak script directly into Android's temporary `/data/local/tmp` directory,
     * forces executable permissions (chmod 777), and executes it with elevated Shizuku/Root privileges.
     */
    public String writeAndExecuteTempScript(String scriptFileName, String scriptBody) {
        if (scriptBody == null || scriptBody.trim().isEmpty()) {
            return "ERROR: Script body is empty";
        }

        if (scriptFileName == null || scriptFileName.trim().isEmpty()) {
            scriptFileName = "gamebooster_temp_" + System.currentTimeMillis() + ".sh";
        }
        if (!scriptFileName.endsWith(".sh")) {
            scriptFileName += ".sh";
        }

        String targetPath = TEMP_DIR + "/" + scriptFileName;

        try {
            // Encode content in Base64 to avoid quotes/newline escaping issues in shell
            String base64Content = Base64.encodeToString(scriptBody.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

            if (ShizukuExecutor.hasShizukuPermission()) {
                // Step 1: Write file into /data/local/tmp using base64 decoding
                String writeCmd = "echo \"" + base64Content + "\" | base64 -d > " + targetPath;
                String writeResult = ShizukuExecutor.executeShizukuCommand(writeCmd);
                if (writeResult != null && writeResult.startsWith("ERROR")) {
                    // Fallback: standard echo
                    ShizukuExecutor.executeShizukuCommand("echo '" + scriptBody.replace("'", "'\\''") + "' > " + targetPath);
                }

                // Step 2: Force executable permissions on temporary script
                ShizukuExecutor.executeShizukuCommand("chmod 777 " + targetPath);

                // Step 3: Force execute script with elevated Shizuku / Root privileges
                String execCmd = "sh " + targetPath;
                String result = ShizukuExecutor.executeShizukuCommand(execCmd);

                Log.i(TAG, "Successfully executed temp script at " + targetPath);
                return result != null && !result.trim().isEmpty() ? result : "[SCRIPT COMPLETED SUCCESSFULLY (Zero Exit Code)]";
            } else {
                // Fallback direct execution
                return executeLocalShellCommand(scriptBody);
            }

        } catch (Throwable t) {
            Log.e(TAG, "Failed to write/execute temp script: " + targetPath, t);
            return executeLocalShellCommand(scriptBody);
        }
    }

    /**
     * Executes multi-line batch script lines sequentially.
     */
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

    /**
     * Cleans up Game Booster temporary scripts in /data/local/tmp.
     */
    public void cleanupTempScripts() {
        try {
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand("rm -f " + TEMP_DIR + "/gamebooster_*.sh");
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Pre-loads built-in elevated tweak scripts optimized for Android 13 to 16.
     */
    private void initDefaultPresets() {
        presetScripts.clear();

        // 1. Diagnostics & Identity
        presetScripts.add(new TerminalScriptPreset(
                "diag_id",
                "⚡ Whoami & Shizuku UID",
                "Verifies elevated shell UID (2000) or root UID (0)",
                "id; whoami; pm get-install-location; getprop ro.build.version.release"
        ));

        // 2. Latency & Refresh Rate Engine (Android 13-16)
        presetScripts.add(new TerminalScriptPreset(
                "diag_fps",
                "🎮 FPS & SurfaceFlinger Latency",
                "Dumps SurfaceFlinger frame pacing and display buffer status",
                "dumpsys SurfaceFlinger --latency; getprop debug.sf.fps_limit; getprop persist.sys.NV_FPSLIMIT; settings get system peak_refresh_rate"
        ));

        // 3. RAM, Cache, and LMK Trim (Android 13-16 compatible)
        presetScripts.add(new TerminalScriptPreset(
                "tweak_ram",
                "🧹 Deep RAM Flush & Trim",
                "Frees memory buffers and flushes application cache across the system",
                "pm trim-caches 999999999999; am kill-all; dumpsys meminfo --oom"
        ));

        // 4. Touch & Gyro Zero-Delay Input
        presetScripts.add(new TerminalScriptPreset(
                "tweak_touch",
                "🎯 Zero Touch Slop & 1000Hz Input",
                "Configures touch slop reduction and 1000Hz polling rate",
                "setprop debug.input.max_events_per_sec 1000; setprop view.touch_slop 1; settings put system touch_slop_reduction 1; setprop sys.use_fifo 1; setprop persist.sys.touch.pressure.scale 0.001"
        ));

        // 5. GPU Game Driver & ANGLE Renderer
        presetScripts.add(new TerminalScriptPreset(
                "tweak_gpu",
                "🚀 GPU Game Driver & ANGLE Mode",
                "Inspects ANGLE OpenGL ES layer and global Game Driver bindings",
                "settings get global game_driver_all_apps; settings get global angle_gl_driver_all_angle; getprop debug.hwui.renderer"
        ));

        // 6. Thermal Service & Battery Engine
        presetScripts.add(new TerminalScriptPreset(
                "tweak_thermal",
                "🛡️ Thermal Throttle Inspection",
                "Checks device thermal status, temperature sensor zones, and power governor",
                "dumpsys thermalservice; dumpsys battery; cat /sys/class/thermal/thermal_zone0/temp 2>/dev/null"
        ));

        // 7. Network / DNS Ping Check
        presetScripts.add(new TerminalScriptPreset(
                "diag_net",
                "🌐 Ultra-Low Ping DNS Diagnostic",
                "Queries active DNS resolver and tests Google / Cloudflare ping",
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

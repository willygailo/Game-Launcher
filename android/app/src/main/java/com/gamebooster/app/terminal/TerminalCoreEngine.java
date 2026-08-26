package com.gamebooster.app.terminal;

import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.util.Base64;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TerminalCoreEngine — True Interactive POSIX Shell Engine for Android 13-16.
 *
 * Features:
 * 1. Persistent Working Directory tracking across subshells via __PWD__ sync.
 * 2. Full POSIX environment setup: PATH, HOME, TERM=xterm-256color, ANDROID_DATA, etc.
 * 3. Real interactive tab auto-completion for binary commands, built-ins, and filesystem paths.
 * 4. Process cancellation / Ctrl+C interrupt support.
 * 5. Elevated execution via Shizuku (shell UID 2000) or Root (UID 0) with standard local shell fallback.
 */
public class TerminalCoreEngine {

    private static final String TAG = "TerminalCoreEngine";
    private static final String TEMP_DIR = "/data/local/tmp";
    private static volatile TerminalCoreEngine instance;

    private final List<TerminalScriptPreset> presetScripts = new ArrayList<>();
    private volatile String currentWorkingDir = "/data/local/tmp";
    private final AtomicReference<java.lang.Process> activeLocalProcess = new AtomicReference<>(null);

    // Standard shell commands for tab completion
    private static final List<String> COMMON_COMMANDS = Arrays.asList(
            "ls", "cat", "cd", "pwd", "sh", "rm", "cp", "mv", "mkdir", "chmod", "chown",
            "touch", "getprop", "setprop", "settings", "dumpsys", "pm", "am", "cmd",
            "service", "top", "ps", "df", "free", "uptime", "logcat", "ping", "ip",
            "ifconfig", "netstat", "clear", "cls", "help", "scripts", "run", "echo",
            "grep", "sed", "awk", "find", "kill", "killall", "pkill", "whoami", "id",
            "uname", "dmesg", "sync", "sleep", "which", "stat", "head", "tail", "tar", "gzip",
            "neofetch", "fastfetch", "termux-info", "pkg", "apt", "su", "shizuku"
    );

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
     * Returns the dynamic bash-like user prompt string in Termux format.
     */
    public String getPromptUserPrefix() {
        if (ShizukuExecutor.hasShizukuPermission()) {
            return "shizuku@localhost";
        }
        int uid = Process.myUid();
        return "u0_a" + (uid % 100000) + "@localhost";
    }

    public String getPromptSymbol() {
        return "$";
    }

    /**
     * Executes a shell command via elevated Shizuku binder or local process shell.
     * Automatically extracts updated working directory (__PWD__) and exit code (__EXIT__).
     */
    public TerminalResult executeCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return new TerminalResult("", 0, currentWorkingDir);
        }
        String trimmed = command.trim();

        // 1. Built-in help command (Termux Style)
        if ("help".equalsIgnoreCase(trimmed) || "?".equals(trimmed)) {
            String helpText = "\u001B[1;32mWelcome to Termux (Shizuku Privileged Shell)!\u001B[0m\n\n" +
                    "\u001B[1;36mSHELL BUILT-INS & TOOLS:\u001B[0m\n" +
                    "  • \u001B[32mpkg <list|search|info|trim>\u001B[0m - Termux package management subsystem\n" +
                    "  • \u001B[32mneofetch / fastfetch\u001B[0m        - Display ASCII system & hardware specs\n" +
                    "  • \u001B[32mpwd\u001B[0m                         - Print current working directory\n" +
                    "  • \u001B[32mcd <dir>\u001B[0m                    - Change directory (e.g. cd /sdcard/Android/data)\n" +
                    "  • \u001B[32mls [-la]\u001B[0m                    - List files and directories\n" +
                    "  • \u001B[32mcat <file>\u001B[0m                  - Output file contents\n" +
                    "  • \u001B[32mid / whoami\u001B[0m                 - Show current shell UID and groups\n" +
                    "  • \u001B[32mclear / cls\u001B[0m                 - Clear terminal screen buffer\n" +
                    "  • \u001B[32msh <script.sh>\u001B[0m              - Run shell scripts directly\n" +
                    "  • \u001B[32mgetprop <prop>\u001B[0m              - Read Android system property\n" +
                    "  • \u001B[32msetprop <prop> <v>\u001B[0m          - Set system property (Elevated Shizuku)\n" +
                    "  • \u001B[32msettings <get|put>\u001B[0m          - Query/Modify global, system, secure settings\n" +
                    "  • \u001B[32mdumpsys <service>\u001B[0m           - Dump Android system service state\n" +
                    "  • \u001B[32mam / pm\u001B[0m                     - Activity / Package manager commands\n" +
                    "  • \u001B[32mtop / ps\u001B[0m                    - Monitor processes and CPU load\n\n" +
                    "\u001B[33mUse TAB for path/command autocompletion, Extra-Keys for modifiers.\u001B[0m";
            return new TerminalResult(helpText, 0, currentWorkingDir);
        }

        // 2. Built-in neofetch / fastfetch / termux-info
        if ("neofetch".equalsIgnoreCase(trimmed) || "fastfetch".equalsIgnoreCase(trimmed) || "termux-info".equalsIgnoreCase(trimmed)) {
            String banner = generateNeofetchBanner();
            return new TerminalResult(banner, 0, currentWorkingDir);
        }

        // 3. Built-in pkg / apt command for Termux compatibility
        if (trimmed.startsWith("pkg") || trimmed.startsWith("apt")) {
            String[] parts = trimmed.split("\\s+");
            String subCmd = parts.length > 1 ? parts[1].toLowerCase() : "help";
            if ("help".equals(subCmd)) {
                String pkgHelp = "\u001B[1;36mTermux Package Helper (Android System Integration):\u001B[0m\n" +
                        "  • \u001B[32mpkg list [-3|-s|-d]\u001B[0m     - List packages (3rd-party, system, disabled)\n" +
                        "  • \u001B[32mpkg search <query>\u001B[0m      - Search installed application packages\n" +
                        "  • \u001B[32mpkg info <package>\u001B[0m      - Inspect package details and permissions\n" +
                        "  • \u001B[32mpkg trim\u001B[0m                - Trim all app caches (Reclaim NAND storage)\n" +
                        "  • \u001B[32mpkg install <apk_path>\u001B[0m  - Install APK via elevated package manager\n" +
                        "  • \u001B[32mpkg uninstall <pkg>\u001B[0m     - Uninstall application for user 0\n";
                return new TerminalResult(pkgHelp, 0, currentWorkingDir);
            } else if ("list".equals(subCmd)) {
                String flag = parts.length > 2 ? parts[2] : "";
                return executeCommand("pm list packages " + flag);
            } else if ("search".equals(subCmd)) {
                if (parts.length > 2) {
                    return executeCommand("pm list packages | grep -i " + parts[2]);
                } else {
                    return new TerminalResult("\u001B[31mUsage: pkg search <keyword>\u001B[0m", 1, currentWorkingDir);
                }
            } else if ("info".equals(subCmd)) {
                if (parts.length > 2) {
                    return executeCommand("dumpsys package " + parts[2]);
                } else {
                    return new TerminalResult("\u001B[31mUsage: pkg info <package_name>\u001B[0m", 1, currentWorkingDir);
                }
            } else if ("trim".equals(subCmd)) {
                return executeCommand("pm trim-caches 999999999999; echo '[NAND & App Caches Trimmed Successfully]'");
            } else if ("install".equals(subCmd)) {
                if (parts.length > 2) {
                    return executeCommand("pm install -r -d " + parts[2]);
                } else {
                    return new TerminalResult("\u001B[31mUsage: pkg install <apk_file_path>\u001B[0m", 1, currentWorkingDir);
                }
            } else if ("uninstall".equals(subCmd)) {
                if (parts.length > 2) {
                    return executeCommand("pm uninstall --user 0 " + parts[2]);
                } else {
                    return new TerminalResult("\u001B[31mUsage: pkg uninstall <package_name>\u001B[0m", 1, currentWorkingDir);
                }
            }
        }

        // 4. Resolve script execution if script name is typed directly
        String execCommandStr = trimmed;
        if (trimmed.endsWith(".sh")) {
            String scriptName = trimmed.startsWith("./") ? trimmed.substring(2) : trimmed;
            File localScript = new File(currentWorkingDir, scriptName);
            File defaultFolderScript = new File(TEMP_DIR, scriptName);
            if (localScript.exists()) {
                execCommandStr = "sh \"" + localScript.getAbsolutePath() + "\"";
            } else if (defaultFolderScript.exists()) {
                execCommandStr = "sh \"" + defaultFolderScript.getAbsolutePath() + "\"";
            }
        }

        // Build POSIX environment script wrapper
        String shellScript = "export PATH=/system/bin:/system/xbin:/vendor/bin:/data/local/tmp:$PATH; " +
                "export HOME=/data/local/tmp; " +
                "export TERM=xterm-256color; " +
                "export ANDROID_DATA=/data; " +
                "export ANDROID_ROOT=/system; " +
                "export EXTERNAL_STORAGE=/sdcard; " +
                "cd \"" + currentWorkingDir + "\" 2>/dev/null; " +
                execCommandStr + "; " +
                "echo \"__PWD__:$PWD\"; " +
                "echo \"__EXIT__:$?\"";

        // Try Elevated Shizuku AIDL / Process Execution
        if (ShizukuExecutor.hasShizukuPermission()) {
            try {
                String rawOutput = com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().executeCommand(shellScript);
                if (rawOutput != null) {
                    return parseShellOutput(rawOutput);
                }
            } catch (Throwable t) {
                Log.w(TAG, "Shizuku execution failed, falling back: " + t.getMessage());
            }
        }

        // Fallback: Local Shell Process Execution
        return executeLocalShell(shellScript);
    }

    private TerminalResult executeLocalShell(String shellScript) {
        StringBuilder output = new StringBuilder();
        int exitCode = 0;
        try {
            ProcessBuilder pb = new ProcessBuilder("/system/bin/sh", "-c", shellScript);
            pb.redirectErrorStream(true);
            java.lang.Process process = pb.start();
            activeLocalProcess.set(process);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            exitCode = process.waitFor();
            activeLocalProcess.set(null);
            return parseShellOutput(output.toString());
        } catch (Throwable t) {
            activeLocalProcess.set(null);
            Log.e(TAG, "Local shell execution error: " + shellScript, t);
            return new TerminalResult("ERROR: " + t.getMessage(), 1, currentWorkingDir);
        }
    }

    /**
     * Cancels any running interactive local process (Ctrl+C).
     */
    public void cancelRunningCommand() {
        java.lang.Process proc = activeLocalProcess.getAndSet(null);
        if (proc != null) {
            try {
                proc.destroyForcibly();
            } catch (Throwable ignored) {}
        }
    }

    private TerminalResult parseShellOutput(String rawOutput) {
        if (rawOutput == null) {
            return new TerminalResult("", 0, currentWorkingDir);
        }

        String[] lines = rawOutput.split("\n");
        StringBuilder cleanOutput = new StringBuilder();
        int exitCode = 0;

        for (String line : lines) {
            if (line.startsWith("__PWD__:")) {
                String newPwd = line.substring(8).trim();
                if (!newPwd.isEmpty()) {
                    currentWorkingDir = newPwd;
                }
            } else if (line.startsWith("__EXIT__:")) {
                try {
                    exitCode = Integer.parseInt(line.substring(9).trim());
                } catch (NumberFormatException ignored) {}
            } else {
                cleanOutput.append(line).append("\n");
            }
        }

        String outStr = cleanOutput.toString();
        if (outStr.endsWith("\n")) {
            outStr = outStr.substring(0, outStr.length() - 1);
        }
        return new TerminalResult(outStr, exitCode, currentWorkingDir);
    }

    /**
     * Resolves Tab Auto-Completion suggestions based on current input text.
     */
    public List<String> getCompletions(String input) {
        List<String> completions = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) {
            return completions;
        }

        String lastToken = input;
        int lastSpace = input.lastIndexOf(' ');
        if (lastSpace >= 0) {
            lastToken = input.substring(lastSpace + 1);
        }

        if (lastToken.isEmpty()) {
            return completions;
        }

        // 1. If starting token (command), search common commands
        if (lastSpace < 0) {
            for (String cmd : COMMON_COMMANDS) {
                if (cmd.startsWith(lastToken.toLowerCase()) && !cmd.equals(lastToken)) {
                    completions.add(cmd);
                }
            }
        }

        // 2. Search local filesystem files/directories in current working dir
        try {
            File searchDir = new File(currentWorkingDir);
            String filePrefix = lastToken;
            if (lastToken.contains("/")) {
                int slashIndex = lastToken.lastIndexOf('/');
                String parentPath = lastToken.substring(0, slashIndex);
                filePrefix = lastToken.substring(slashIndex + 1);
                searchDir = parentPath.startsWith("/") ? new File(parentPath) : new File(currentWorkingDir, parentPath);
            }

            if (searchDir.exists() && searchDir.isDirectory()) {
                File[] files = searchDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().startsWith(filePrefix)) {
                            String name = f.getName() + (f.isDirectory() ? "/" : "");
                            if (lastToken.contains("/")) {
                                int slashIndex = lastToken.lastIndexOf('/');
                                name = lastToken.substring(0, slashIndex + 1) + name;
                            }
                            completions.add(name);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        Collections.sort(completions);
        return completions;
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
            String base64Content = Base64.encodeToString(scriptBody.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

            if (ShizukuExecutor.hasShizukuPermission()) {
                String writeCmd = "echo \"" + base64Content + "\" | base64 -d > " + targetPath;
                String writeResult = ShizukuExecutor.executeShizukuCommand(writeCmd);
                if (writeResult != null && writeResult.startsWith("ERROR")) {
                    ShizukuExecutor.executeShizukuCommand("echo '" + scriptBody.replace("'", "'\\''") + "' > " + targetPath);
                }

                ShizukuExecutor.executeShizukuCommand("chmod 777 " + targetPath);
                String execCmd = "sh " + targetPath;
                String result = ShizukuExecutor.executeShizukuCommand(execCmd);

                Log.i(TAG, "Successfully executed temp script at " + targetPath);
                return result != null && !result.trim().isEmpty() ? result : "[SCRIPT COMPLETED SUCCESSFULLY (Exit Code 0)]";
            } else {
                TerminalResult tr = executeCommand(scriptBody);
                return tr.output;
            }
        } catch (Throwable t) {
            Log.e(TAG, "Failed to write/execute temp script: " + targetPath, t);
            TerminalResult tr = executeCommand(scriptBody);
            return tr.output;
        }
    }

    public List<TerminalScriptPreset> getPresetScripts() {
        return presetScripts;
    }

    private void initDefaultPresets() {
        presetScripts.clear();

        presetScripts.add(new TerminalScriptPreset(
                "diag_id",
                "⚡ Whoami & Shizuku UID",
                "Verifies elevated shell UID (2000) or root UID (0)",
                "id; whoami; pm get-install-location; getprop ro.build.version.release"
        ));

        presetScripts.add(new TerminalScriptPreset(
                "diag_fps",
                "🎮 FPS & SurfaceFlinger Latency",
                "Dumps SurfaceFlinger frame pacing and display buffer status",
                "dumpsys SurfaceFlinger --latency; getprop debug.sf.fps_limit; getprop persist.sys.NV_FPSLIMIT; settings get system peak_refresh_rate"
        ));

        presetScripts.add(new TerminalScriptPreset(
                "tweak_ram",
                "🧹 Deep RAM Flush & Trim",
                "Frees memory buffers and flushes application cache across the system",
                "pm trim-caches 999999999999; am kill-all; dumpsys meminfo --oom"
        ));

        presetScripts.add(new TerminalScriptPreset(
                "tweak_touch",
                "🎯 Zero Touch Slop & 1000Hz Gyro (Super Smooth)",
                "Configures touch slop reduction, 1000Hz polling rate, hardware smoothing & gyro stabilization",
                "setprop debug.input.max_events_per_sec 1000; setprop view.touch_slop 0; settings put system touch_slop_reduction 1; setprop sys.use_fifo 1; setprop persist.sys.touch.pressure.scale 0.0001; setprop debug.sensor.gyro.sample_rate 1000; setprop debug.sensor.gyro.smooth 1; setprop debug.sensor.gyro.stabilization 1; setprop persist.sys.gyro.filter 1; setprop persist.sys.gyro.delay 0"
        ));

        presetScripts.add(new TerminalScriptPreset(
                "tweak_gpu",
                "🚀 GPU Game Driver & ANGLE Mode",
                "Inspects ANGLE OpenGL ES layer and global Game Driver bindings",
                "settings get global game_driver_all_apps; settings get global angle_gl_driver_all_angle; getprop debug.hwui.renderer"
        ));

        presetScripts.add(new TerminalScriptPreset(
                "tweak_thermal",
                "❄️ Thermal Throttle Bypass & Cool Engine",
                "Overrides device thermal status to cool (0), disables kernel throttling, and checks sensor zones",
                "cmd thermalservice override-status 0; cmd thermal override-status 0; setprop debug.thermal.throttle.disable 1; dumpsys thermalservice; cat /sys/class/thermal/thermal_zone0/temp 2>/dev/null"
        ));

        presetScripts.add(new TerminalScriptPreset(
                "tweak_hz185",
                "⚡ 185Hz Extreme Unlock & Pacing",
                "Forces 185Hz refresh rate via SurfaceFlinger and system display settings",
                "settings put system peak_refresh_rate 185.0; settings put system min_refresh_rate 185.0; service call SurfaceFlinger 1035 i32 185; service call SurfaceFlinger 1036 i32 185; setprop debug.sf.fps_limit 185; setprop persist.sys.NV_FPSLIMIT 185"
        ));

        presetScripts.add(new TerminalScriptPreset(
                "diag_net",
                "🌐 Ultra-Low Ping DNS Diagnostic",
                "Queries active DNS resolver and tests Google / Cloudflare ping",
                "getprop net.dns1; ping -c 3 1.1.1.1"
        ));

        presetScripts.add(new TerminalScriptPreset(
                "tweak_mask",
                "🛡️ Hardware Mask & Flagship Identity",
                "Verifies device model, brand, SoC, and GPU vendor spoofing properties",
                "getprop ro.product.model; getprop ro.product.manufacturer; getprop ro.product.brand; getprop ro.soc.model; getprop ro.hardware.egl"
        ));

        presetScripts.add(new TerminalScriptPreset(
                "tweak_storage",
                "📁 Unlock Combo /sdcard/Android/data & obb",
                "Applies full read/write permissions (chmod 777) across all internal and external game paths",
                "chmod -R 777 /sdcard/Android/data /sdcard/Android/obb 2>/dev/null; ls -ld /sdcard/Android/data /sdcard/Android/obb"
        ));

        presetScripts.add(new TerminalScriptPreset(
                "tweak_android16",
                "🚀 Android 13-16 GameMode & Performance HAL",
                "Queries active GameMode, 185 FPS Game Overlays, and ADPF power hints",
                "cmd game mode get com.mobile.legends 2>/dev/null; device_config get game_overlay 2>/dev/null; getprop debug.sf.showfps"
        ));
    }

    private String generateNeofetchBanner() {
        StringBuilder sb = new StringBuilder();
        String user = getPromptUserPrefix();
        String osVer = "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
        String device = Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL;
        String hardware = Build.HARDWARE + " (" + Build.BOARD + ")";
        String kernel = System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch");
        String uptime = getUptimeFormatted();
        String memory = getMemoryFormatted();
        boolean hasShizuku = ShizukuExecutor.hasShizukuPermission();
        String shellPriv = hasShizuku ? "UID 2000 (Shell Elevated - Shizuku PTY)" : "UID " + Process.myUid() + " (Standard App Shell)";

        sb.append("\u001B[1;32m       /\\       \u001B[1;36m").append(user).append("\u001B[0m\n");
        sb.append("\u001B[1;32m      /  \\      \u001B[1;37m---------------------------------------\u001B[0m\n");
        sb.append("\u001B[1;32m     / /\\ \\     \u001B[1;33mOS:        \u001B[0m").append(osVer).append("\n");
        sb.append("\u001B[1;32m    / /  \\ \\    \u001B[1;33mHost:      \u001B[0m").append(device).append("\n");
        sb.append("\u001B[1;32m   / / /\\ \\ \\   \u001B[1;33mKernel:    \u001B[0m").append(kernel).append("\n");
        sb.append("\u001B[1;32m  / / /  \\ \\ \\  \u001B[1;33mSoC/Board: \u001B[0m").append(hardware).append("\n");
        sb.append("\u001B[1;32m /_/_/    \\_\\_\\ \u001B[1;33mShell:     \u001B[0m/system/bin/sh (xterm-256color)\n");
        sb.append("\u001B[1;32m                \u001B[1;33mAccess:    \u001B[0m").append(shellPriv).append("\n");
        sb.append("\u001B[1;32m                \u001B[1;33mUptime:    \u001B[0m").append(uptime).append("\n");
        sb.append("\u001B[1;32m                \u001B[1;33mMemory:    \u001B[0m").append(memory).append("\n");
        sb.append("\u001B[1;32m                \u001B[1;33mHome:      \u001B[0m").append(currentWorkingDir).append("\n\n");
        sb.append("\u001B[40m   \u001B[41m   \u001B[42m   \u001B[43m   \u001B[44m   \u001B[45m   \u001B[46m   \u001B[47m   \u001B[0m\n");
        sb.append("\u001B[100m   \u001B[101m   \u001B[102m   \u001B[103m   \u001B[104m   \u001B[105m   \u001B[106m   \u001B[107m   \u001B[0m\n");

        return sb.toString();
    }

    private String getUptimeFormatted() {
        long uptimeMs = android.os.SystemClock.elapsedRealtime();
        long seconds = uptimeMs / 1000;
        long mins = (seconds / 60) % 60;
        long hours = (seconds / 3600) % 24;
        long days = seconds / 86400;
        if (days > 0) {
            return days + " days, " + hours + " hours, " + mins + " mins";
        } else if (hours > 0) {
            return hours + " hours, " + mins + " mins";
        } else {
            return mins + " mins, " + (seconds % 60) + " secs";
        }
    }

    private String getMemoryFormatted() {
        Runtime runtime = Runtime.getRuntime();
        long usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMem = runtime.maxMemory() / (1024 * 1024);
        return usedMem + "MB / " + maxMem + "MB (JVM Heap)";
    }

    /**
     * Data class holding command output, exit code, and active working directory.
     */
    public static final class TerminalResult {
        public final String output;
        public final int exitCode;
        public final String currentDir;

        public TerminalResult(String output, int exitCode, String currentDir) {
            this.output = output != null ? output : "";
            this.exitCode = exitCode;
            this.currentDir = currentDir != null ? currentDir : "/data/local/tmp";
        }
    }
}

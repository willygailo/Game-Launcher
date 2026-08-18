package com.gamebooster.app.terminal;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TerminalFolderManager manages the dedicated scripts/terminal folder on device storage.
 * Supports reading, writing, creating, listing, and executing shell script files (.sh).
 */
public class TerminalFolderManager {

    private static final String TAG = "TerminalFolderManager";
    private static final String SDCARD_DIR = "/sdcard/GameBooster/terminal";
    private static volatile TerminalFolderManager instance;

    private final Context appContext;
    private File terminalDir;

    private TerminalFolderManager(Context context) {
        this.appContext = context.getApplicationContext();
        initTerminalFolder();
    }

    public static TerminalFolderManager getInstance(Context context) {
        if (instance == null) {
            synchronized (TerminalFolderManager.class) {
                if (instance == null) {
                    instance = new TerminalFolderManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the terminal scripts folder and seeds default gaming tweak scripts.
     */
    public synchronized void initTerminalFolder() {
        try {
            // Attempt primary directory in /sdcard/GameBooster/terminal
            File primaryDir = new File(SDCARD_DIR);
            if (!primaryDir.exists()) {
                primaryDir.mkdirs();
            }

            if (primaryDir.exists() && primaryDir.canWrite()) {
                terminalDir = primaryDir;
            } else {
                // Fallback to external files dir or internal files dir
                File extDir = appContext.getExternalFilesDir("terminal");
                if (extDir != null && (extDir.exists() || extDir.mkdirs())) {
                    terminalDir = extDir;
                } else {
                    terminalDir = new File(appContext.getFilesDir(), "terminal");
                    if (!terminalDir.exists()) {
                        terminalDir.mkdirs();
                    }
                }
            }

            seedDefaultScripts();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to initialize terminal folder", t);
            terminalDir = new File(appContext.getFilesDir(), "terminal");
            if (!terminalDir.exists()) {
                terminalDir.mkdirs();
            }
        }
    }

    /**
     * Seeds default shell scripts (.sh) if they don't already exist.
     */
    private void seedDefaultScripts() {
        if (terminalDir == null || !terminalDir.exists()) return;

        createScriptIfNotExists("1_gaming_185fps_unlock.sh",
                "# ==================================================\n" +
                "# GAME BOOSTER PRO - 185Hz / 185 FPS UNLOCK SCRIPT\n" +
                "# ==================================================\n" +
                "settings put system peak_refresh_rate 185.0\n" +
                "settings put system min_refresh_rate 185.0\n" +
                "settings put system user_refresh_rate 185\n" +
                "settings put global peak_refresh_rate 185.0\n" +
                "settings put global min_refresh_rate 185.0\n" +
                "cmd window set-app-refresh-rate global 185\n" +
                "service call SurfaceFlinger 1035 i32 185\n" +
                "service call SurfaceFlinger 1036 i32 185\n" +
                "setprop debug.sf.fps_limit 185\n" +
                "setprop persist.sys.NV_FPSLIMIT 185\n" +
                "echo '[FPS & 185Hz REFRESH RATE UNLOCKED]'\n"
        );

        createScriptIfNotExists("2_ultra_touch_slop_1000hz.sh",
                "# ==================================================\n" +
                "# GAME BOOSTER PRO - ULTRA 1000Hz TOUCH LATENCY\n" +
                "# ==================================================\n" +
                "setprop debug.input.max_events_per_sec 1000\n" +
                "setprop view.touch_slop 1\n" +
                "settings put system touch_slop_reduction 1\n" +
                "setprop sys.use_fifo 1\n" +
                "setprop persist.sys.touch.pressure.scale 0.001\n" +
                "echo '[ZERO TOUCH SLOP & 1000Hz POLLING ACTIVE]'\n"
        );

        createScriptIfNotExists("3_deep_ram_flush_trim.sh",
                "# ==================================================\n" +
                "# GAME BOOSTER PRO - DEEP RAM & CACHE FLUSH\n" +
                "# ==================================================\n" +
                "pm trim-caches 999999999999\n" +
                "am kill-all\n" +
                "echo 3 > /proc/sys/vm/drop_caches 2>/dev/null || true\n" +
                "dumpsys meminfo --oom | head -n 25\n" +
                "echo '[DEEP RAM FLUSH & CACHE TRIM COMPLETE]'\n"
        );

        createScriptIfNotExists("4_unlock_data_obb_storage.sh",
                "# ==================================================\n" +
                "# GAME BOOSTER PRO - UNLOCK ANDROID/DATA & OBB\n" +
                "# ==================================================\n" +
                "chmod -R 777 /sdcard/Android/data 2>/dev/null || true\n" +
                "chmod -R 777 /sdcard/Android/obb 2>/dev/null || true\n" +
                "ls -la /sdcard/Android/data | head -n 15\n" +
                "echo '[STORAGE PERMISSIONS & DIRECTORIES UNLOCKED]'\n"
        );

        createScriptIfNotExists("5_cloudflare_low_ping_dns.sh",
                "# ==================================================\n" +
                "# GAME BOOSTER PRO - 1.1.1.1 LOW-PING GAMING DNS\n" +
                "# ==================================================\n" +
                "settings put global private_dns_mode hostname\n" +
                "settings put global private_dns_specifier one.one.one.one\n" +
                "setprop net.dns1 1.1.1.1\n" +
                "setprop net.dns2 1.0.0.1\n" +
                "ping -c 3 1.1.1.1\n" +
                "echo '[CLOUDFLARE GAMING DNS CONFIGURED]'\n"
        );

        createScriptIfNotExists("6_gpu_angle_vulkan_driver.sh",
                "# ==================================================\n" +
                "# GAME BOOSTER PRO - GPU ANGLE & VULKAN DRIVER\n" +
                "# ==================================================\n" +
                "settings put global angle_gl_driver_all_angle 1\n" +
                "settings put global game_driver_all_apps 1\n" +
                "setprop debug.hwui.renderer vulkan\n" +
                "echo '[GPU ANGLE & VULKAN DRIVER ENABLED]'\n"
        );

        createScriptIfNotExists("7_thermal_throttle_bypass.sh",
                "# ==================================================\n" +
                "# GAME BOOSTER PRO - THERMAL STATUS & GOVERNOR\n" +
                "# ==================================================\n" +
                "dumpsys thermalservice\n" +
                "dumpsys battery\n" +
                "echo '[THERMAL SERVICE INSPECTION COMPLETED]'\n"
        );

        createScriptIfNotExists("8_device_diagnostics_id.sh",
                "# ==================================================\n" +
                "# GAME BOOSTER PRO - DIAGNOSTICS & IDENTITY\n" +
                "# ==================================================\n" +
                "id\n" +
                "whoami\n" +
                "getprop ro.build.version.release\n" +
                "getprop ro.product.model\n" +
                "getprop ro.product.brand\n" +
                "echo '[DEVICE DIAGNOSTICS SUCCESS]'\n"
        );
    }

    private void createScriptIfNotExists(String fileName, String content) {
        if (terminalDir == null) return;
        File file = new File(terminalDir, fileName);
        if (!file.exists()) {
            saveScript(fileName, content);
        }
    }

    public File getTerminalDir() {
        if (terminalDir == null || !terminalDir.exists()) {
            initTerminalFolder();
        }
        return terminalDir;
    }

    public String getTerminalDirPath() {
        return getTerminalDir() != null ? getTerminalDir().getAbsolutePath() : SDCARD_DIR;
    }

    /**
     * Lists all script files (.sh) in the terminal folder.
     */
    public List<File> listScriptFiles() {
        File dir = getTerminalDir();
        if (dir == null || !dir.exists()) {
            return Collections.emptyList();
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".sh") || name.endsWith(".txt"));
        if (files == null) {
            return Collections.emptyList();
        }
        List<File> list = new ArrayList<>(Arrays.asList(files));
        Collections.sort(list, Comparator.comparing(File::getName));
        return list;
    }

    /**
     * Reads contents of a script file.
     */
    public String readScript(File file) {
        if (file == null || !file.exists()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error reading script file: " + file.getAbsolutePath(), t);
        }
        return sb.toString();
    }

    /**
     * Saves or overwrites a script in the terminal folder.
     */
    public boolean saveScript(String fileName, String content) {
        try {
            File dir = getTerminalDir();
            if (dir == null) return false;
            if (!dir.exists()) dir.mkdirs();

            if (!fileName.endsWith(".sh") && !fileName.endsWith(".txt")) {
                fileName += ".sh";
            }
            File targetFile = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }
            targetFile.setReadable(true, false);
            targetFile.setWritable(true, false);
            targetFile.setExecutable(true, false);
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "Error saving script: " + fileName, t);
            return false;
        }
    }

    /**
     * Deletes a script file from the terminal folder.
     */
    public boolean deleteScript(File file) {
        if (file != null && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * Executes a script file via TerminalCoreEngine.
     */
    public String executeScriptFile(File file) {
        if (file == null || !file.exists()) {
            return "ERROR: File does not exist: " + (file != null ? file.getAbsolutePath() : "null");
        }
        String content = readScript(file);
        return TerminalCoreEngine.getInstance().writeAndExecuteTempScript(file.getName(), content);
    }
}

package com.gamebooster.app.shizuku;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Manages the Shizuku 13.5 `rish` (Root/Shizuku Shell) binary and `rish_shizuku.dex`.
 * Provides standalone, elevated ADB/Root shell command execution across Android 13, 14, 15, and 16.
 */
public class RishManager {

    private static final String TAG = "RishManager";
    private static final String DIR_NAME = "shizuku";
    private static final String RISH_BIN_NAME = "rish";
    private static final String RISH_DEX_NAME = "rish_shizuku.dex";

    private static volatile boolean isInitialized = false;
    private static File rishBinFile = null;
    private static File rishDexFile = null;

    /**
     * Initializes and extracts rish and rish_shizuku.dex to internal app storage.
     */
    public static synchronized boolean initialize(Context context) {
        if (context == null) return false;
        if (isInitialized && rishBinFile != null && rishBinFile.exists() && rishDexFile != null && rishDexFile.exists()) {
            return true;
        }

        try {
            Context appContext = context.getApplicationContext();
            File shizukuDir = new File(appContext.getFilesDir(), DIR_NAME);
            if (!shizukuDir.exists()) {
                shizukuDir.mkdirs();
            }

            rishBinFile = new File(shizukuDir, RISH_BIN_NAME);
            rishDexFile = new File(shizukuDir, RISH_DEX_NAME);

            // Extract rish_shizuku.dex if missing or size differs
            if (!rishDexFile.exists() || rishDexFile.length() == 0) {
                // If read-only from previous run, make writable before overwriting
                if (rishDexFile.exists()) {
                    rishDexFile.setWritable(true, true);
                }
                copyAssetToFile(appContext, DIR_NAME + "/" + RISH_DEX_NAME, rishDexFile);
            }

            // Android 14+ (API 34+) requires dex loaded by app_process to be strictly read-only
            if (Build.VERSION.SDK_INT >= 34) {
                rishDexFile.setReadOnly();
            }

            // Generate/write custom rish script with target package pre-configured
            String rishScript = generateRishScript(appContext.getPackageName(), rishDexFile.getAbsolutePath());
            try (FileOutputStream fos = new FileOutputStream(rishBinFile)) {
                fos.write(rishScript.getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }

            // Grant executable permissions
            rishBinFile.setExecutable(true, false);
            rishBinFile.setReadable(true, false);

            isInitialized = true;
            Log.i(TAG, "RishManager 13.5 successfully initialized at: " + rishBinFile.getAbsolutePath());
            return true;

        } catch (Throwable t) {
            Log.e(TAG, "Failed to initialize RishManager", t);
            return false;
        }
    }

    /**
     * Executes a command using Shizuku 13.5 rish shell.
     */
    public static String executeRishCommand(Context context, String command) {
        if (command == null || command.trim().isEmpty()) {
            return "SUCCESS";
        }

        if (!isInitialized) {
            initialize(context);
        }

        if (rishBinFile == null || !rishBinFile.exists()) {
            return "ERROR: rish binary not available";
        }

        Process process = null;
        BufferedReader stdoutReader = null;
        BufferedReader stderrReader = null;

        try {
            String pkgName = context != null ? context.getPackageName() : "com.gamebooster.app";

            ProcessBuilder pb = new ProcessBuilder("sh", rishBinFile.getAbsolutePath(), "-c", command);
            pb.environment().put("RISH_APPLICATION_ID", pkgName);
            pb.directory(rishBinFile.getParentFile());

            process = pb.start();

            stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder stdout = new StringBuilder();
            String line;
            while ((line = stdoutReader.readLine()) != null) {
                stdout.append(line).append("\n");
            }

            stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder stderr = new StringBuilder();
            while ((line = stderrReader.readLine()) != null) {
                stderr.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            String stdoutStr = stdout.toString().trim();
            String stderrStr = stderr.toString().trim();

            if (exitCode == 0) {
                return stdoutStr.isEmpty() ? "SUCCESS" : stdoutStr;
            } else {
                if (!stderrStr.isEmpty()) {
                    return "ERROR: " + stderrStr;
                }
                return stdoutStr.isEmpty() ? "ERROR (exit " + exitCode + ")" : stdoutStr;
            }

        } catch (Throwable t) {
            Log.w(TAG, "executeRishCommand failed: " + t.getMessage());
            return "ERROR: " + t.getMessage();
        } finally {
            try {
                if (stdoutReader != null) stdoutReader.close();
                if (stderrReader != null) stderrReader.close();
                if (process != null) process.destroy();
            } catch (Throwable ignored) {}
        }
    }

    public static boolean isAvailable(Context context) {
        if (!isInitialized) {
            initialize(context);
        }
        return rishBinFile != null && rishBinFile.exists();
    }

    private static String generateRishScript(String pkgName, String dexPath) {
        return "#!/system/bin/sh\n" +
                "DEX=\"" + dexPath + "\"\n" +
                "if [ $(getprop ro.build.version.sdk 2>/dev/null || echo 0) -ge 34 ]; then\n" +
                "  if [ -w \"$DEX\" ]; then\n" +
                "    chmod 400 \"$DEX\" 2>/dev/null\n" +
                "  fi\n" +
                "fi\n" +
                "export RISH_APPLICATION_ID=\"" + pkgName + "\"\n" +
                "exec /system/bin/app_process -Djava.class.path=\"$DEX\" /system/bin --nice-name=rish rikka.shizuku.shell.ShizukuShellLoader \"$@\"\n";
    }

    private static void copyAssetToFile(Context context, String assetPath, File destFile) throws Exception {
        try (InputStream in = context.getAssets().open(assetPath);
             FileOutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
    }
}

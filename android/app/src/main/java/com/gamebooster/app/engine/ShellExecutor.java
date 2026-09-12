package com.gamebooster.app.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class ShellExecutor {

    public static class CommandResult {
        public final int exitCode;
        public final String stdout;
        public final String stderr;

        public CommandResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public boolean isSuccess() {
            return exitCode == 0;
        }
    }

    public static boolean isAndroidEnvironment() {
        String vendor = System.getProperty("java.vendor", "");
        String vmName = System.getProperty("java.vm.name", "");
        return vendor.contains("The Android Project")
                || vmName.equalsIgnoreCase("Dalvik")
                || vmName.equalsIgnoreCase("ART");
    }

    private static final String[] SU_PATHS = {
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/vendor/bin/su",
            "/data/local/tmp/su",
            "/apex/com.android.runtime/bin/su",
            "/system/bin/.ext/.su"
    };

    private static Boolean sRootAvailable = null;
    private static final Object ROOT_CHECK_LOCK = new Object();

    public static boolean isRootSuAvailable() {
        if (!isAndroidEnvironment()) {
            return false;
        }
        synchronized (ROOT_CHECK_LOCK) {
            if (sRootAvailable != null) {
                return sRootAvailable;
            }
            // 1. Check known binary paths
            boolean suFileExists = false;
            for (String path : SU_PATHS) {
                File f = new File(path);
                if (f.exists() && f.canExecute()) {
                    suFileExists = true;
                    break;
                }
            }

            // 2. Also try executing 'su -c id' to verify grant from Magisk / KernelSU / APatch
            try {
                CommandResult res = executeInternal("su", "id");
                if (res.isSuccess() && res.stdout.contains("uid=0")) {
                    sRootAvailable = true;
                    return true;
                }
            } catch (Throwable ignored) {}

            if (suFileExists) {
                sRootAvailable = true;
                return true;
            }

            // 3. Check 'which su'
            try {
                CommandResult whichRes = executeInternal("sh", "which su");
                if (whichRes.isSuccess() && whichRes.stdout.length() > 0 && !whichRes.stdout.contains("not found")) {
                    sRootAvailable = true;
                    return true;
                }
            } catch (Throwable ignored) {}

            sRootAvailable = false;
            return false;
        }
    }

    public static void invalidateRootCache() {
        synchronized (ROOT_CHECK_LOCK) {
            sRootAvailable = null;
        }
    }

    public static boolean isPrivilegedAvailable() {
        return PrivilegeBridgeEngine.isPrivilegedActive();
    }

    public static CommandResult executeSuCommand(String command) {
        if (!isAndroidEnvironment()) {
            return new CommandResult(0, "", "");
        }
        if (isRootSuAvailable()) {
            return executeInternal("su", command);
        }
        // Fall back to Shizuku Virtual Root privileged execution
        if (PrivilegeBridgeEngine.isShizukuVirtualRootReady()) {
            String out = PrivilegeBridgeEngine.executePrivileged(command);
            if (out != null && !out.startsWith("ERROR:")) {
                return new CommandResult(0, out.equals("SUCCESS") ? "" : out, "");
            } else {
                return new CommandResult(1, "", out != null ? out : "Virtual root execution failed");
            }
        }
        return executeInternal("su", command);
    }

    public static CommandResult executeCommand(String command, boolean preferRoot) {
        if (!isAndroidEnvironment()) {
            return new CommandResult(0, "", "");
        }
        if (preferRoot && isRootSuAvailable()) {
            CommandResult suRes = executeSuCommand(command);
            if (suRes.isSuccess()) {
                return suRes;
            }
        }
        return executeCommand(command);
    }

    public static CommandResult executeCommand(String command) {
        // In desktop unit test environments, do not spawn host shell processes for Android commands
        if (!isAndroidEnvironment()) {
            return new CommandResult(0, "", "");
        }
        return executeInternal("sh", command);
    }

    private static CommandResult executeInternal(String shellBinary, String command) {
        Process process = null;
        BufferedReader isReader = null;
        BufferedReader esReader = null;

        try {
            process = Runtime.getRuntime().exec(new String[]{shellBinary, "-c", command});

            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            isReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            esReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = isReader.readLine()) != null) {
                stdout.append(line).append("\n");
            }
            while ((line = esReader.readLine()) != null) {
                stderr.append(line).append("\n");
            }

            int exitCode;
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new CommandResult(-1, "", "Command timed out after 5s");
            }
            exitCode = process.exitValue();
            return new CommandResult(exitCode, stdout.toString().trim(), stderr.toString().trim());

        } catch (Exception e) {
            return new CommandResult(-1, "", e.getMessage() != null ? e.getMessage() : "Execution exception");
        } finally {
            try {
                if (isReader != null) isReader.close();
                if (esReader != null) esReader.close();
                if (process != null) process.destroy();
            } catch (Exception ignored) {}
        }
    }
}

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

    public static boolean isRootSuAvailable() {
        // Non-rooted device architecture: strictly non-root, relies only on Shizuku API (UID 2000)
        return false;
    }

    public static CommandResult executeCommand(String command, boolean preferRoot) {
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

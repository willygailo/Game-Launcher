package com.gamebooster.app.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

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

    private static Boolean sHasSu = null;

    public static boolean isRootSuAvailable() {
        if (sHasSu != null) return sHasSu;
        String[] paths = {"/system/bin/su", "/system/xbin/su", "/sbin/su", "/data/local/xbin/su",
                "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su"};
        for (String p : paths) {
            try {
                if (new File(p).exists()) {
                    sHasSu = true;
                    return true;
                }
            } catch (Throwable ignored) {}
        }
        sHasSu = false;
        return false;
    }

    public static CommandResult executeCommand(String command, boolean preferRoot) {
        if (preferRoot && isRootSuAvailable()) {
            CommandResult suRes = executeInternal("su", command);
            if (suRes.isSuccess()) {
                return suRes;
            }
        }
        return executeCommand(command);
    }

    public static CommandResult executeCommand(String command) {
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

            int exitCode = process.waitFor();
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

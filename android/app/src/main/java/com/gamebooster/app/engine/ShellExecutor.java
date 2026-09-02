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

    public static boolean isRootSuAvailable() {
        try {
            File[] paths = {
                new File("/system/bin/su"),
                new File("/system/xbin/su"),
                new File("/sbin/su"),
                new File("/system/sd/xbin/su"),
                new File("/vendor/bin/su")
            };
            for (File p : paths) {
                if (p.exists() && p.canExecute()) return true;
            }
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    public static CommandResult executeCommand(String command, boolean preferRoot) {
        if (preferRoot && isRootSuAvailable()) {
            CommandResult res = executeInternal("su", command);
            if (res.isSuccess()) return res;
        }
        return executeCommand(command);
    }

    public static CommandResult executeCommand(String command) {
        if (isRootSuAvailable()) {
            CommandResult res = executeInternal("su", command);
            if (res.isSuccess()) return res;
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

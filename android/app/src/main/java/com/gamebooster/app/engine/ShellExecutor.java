package com.gamebooster.app.engine;

import java.io.BufferedReader;
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

    public static boolean isRootAvailable() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "id"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            int exitCode = process.waitFor();
            return exitCode == 0 && line != null && line.contains("uid=0");
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static CommandResult executeCommand(String command, boolean useRoot) {
        if (useRoot && isRootAvailable()) {
            return executeRootCommand(command);
        }
        return executeCommand(command);
    }

    public static CommandResult executeRootCommand(String command) {
        Process process = null;
        BufferedReader isReader = null;
        BufferedReader esReader = null;

        try {
            process = Runtime.getRuntime().exec(new String[]{"su", "-c", command});

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
            return new CommandResult(-1, "", e.getMessage() != null ? e.getMessage() : "Root execution exception");
        } finally {
            try {
                if (isReader != null) isReader.close();
                if (esReader != null) esReader.close();
                if (process != null) process.destroy();
            } catch (Exception ignored) {}
        }
    }

    public static CommandResult executeCommand(String command) {
        Process process = null;
        BufferedReader isReader = null;
        BufferedReader esReader = null;

        try {
            process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});

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

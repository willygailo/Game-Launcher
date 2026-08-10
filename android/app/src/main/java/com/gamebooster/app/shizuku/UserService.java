package com.gamebooster.app.shizuku;

import android.os.Process;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class UserService extends IUserService.Stub {

    private static final String TAG = "UserService";

    public UserService() {
        Log.i(TAG, "UserService initialized under privileged UID=" + Process.myUid());
    }

    @Override
    public void destroy() {
        Log.i(TAG, "UserService destroyed.");
        System.exit(0);
    }

    @Override
    public int getUid() {
        return Process.myUid();
    }

    @Override
    public String execCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "ERROR: Empty command";
        }
        java.lang.Process process = null;
        BufferedReader stdoutReader = null;
        BufferedReader stderrReader = null;
        try {
            // Use /system/bin/sh explicitly for correct PATH as uid 2000 (Shizuku ADB shell)
            // Bare 'sh' may resolve to a different shell binary on some OEM ROMs
            process = Runtime.getRuntime().exec(new String[]{"/system/bin/sh", "-c", command});

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
                return "ERROR: Command failed with exit code " + exitCode + (stderrStr.isEmpty() ? "" : ": " + stderrStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "execCommand exception", e);
            return "ERROR: " + e.getMessage();
        } finally {
            try {
                if (stdoutReader != null) stdoutReader.close();
                if (stderrReader != null) stderrReader.close();
                if (process != null) process.destroy();
            } catch (Exception ignored) {}
        }
    }
}

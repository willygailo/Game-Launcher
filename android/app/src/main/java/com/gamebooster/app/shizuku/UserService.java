package com.gamebooster.app.shizuku;

import android.os.Process;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Privileged AIDL User Service running directly under Shizuku/Root process context (UID 2000 / 0).
 * Provides high-speed direct file I/O, property manipulation, and elevated command execution.
 */
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
            process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});

            stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder stdout = new StringBuilder();
            String line;
            while ((line = stdoutReader.readLine()) != null) {
                stdout.append(line).append("\n");
            }

            stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
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

    @Override
    public boolean writeFile(String path, String content, String mode) {
        if (path == null || content == null) return false;
        try {
            File targetFile = new File(path);
            File parent = targetFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }

            if (mode != null && !mode.trim().isEmpty()) {
                targetFile.setReadable(true, false);
                targetFile.setWritable(true, false);
                targetFile.setExecutable(mode.contains("7") || mode.contains("5"), false);
                Runtime.getRuntime().exec(new String[]{"chmod", mode, path}).waitFor();
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "writeFile failed for: " + path, e);
            return false;
        }
    }

    @Override
    public String readFile(String path) {
        if (path == null) return null;
        try {
            File file = new File(path);
            if (!file.exists() || !file.canRead()) return null;

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "readFile failed for: " + path, e);
            return null;
        }
    }

    @Override
    public boolean deletePath(String path) {
        if (path == null) return false;
        try {
            File file = new File(path);
            if (!file.exists()) return true;
            if (file.isDirectory()) {
                deleteRecursive(file);
            }
            return file.delete();
        } catch (Exception e) {
            Log.e(TAG, "deletePath failed for: " + path, e);
            return false;
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory == null || !fileOrDirectory.exists()) return;
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }

    @Override
    public boolean ensureDir(String dirPath) {
        if (dirPath == null) return false;
        try {
            File dir = new File(dirPath);
            if (dir.exists()) return true;
            boolean ok = dir.mkdirs();
            if (ok) {
                dir.setReadable(true, false);
                dir.setWritable(true, false);
                dir.setExecutable(true, false);
            }
            return ok || dir.exists();
        } catch (Exception e) {
            Log.e(TAG, "ensureDir failed for: " + dirPath, e);
            return false;
        }
    }

    @Override
    public boolean setProperty(String key, String value) {
        if (key == null || value == null) return false;
        try {
            java.lang.Process p = Runtime.getRuntime().exec(new String[]{"setprop", key, value});
            return p.waitFor() == 0;
        } catch (Exception e) {
            Log.e(TAG, "setProperty failed for " + key, e);
            return false;
        }
    }

    @Override
    public String getProperty(String key) {
        if (key == null) return "";
        try {
            java.lang.Process p = Runtime.getRuntime().exec(new String[]{"getprop", key});
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            String line = reader.readLine();
            reader.close();
            p.waitFor();
            return line != null ? line.trim() : "";
        } catch (Exception e) {
            Log.e(TAG, "getProperty failed for " + key, e);
            return "";
        }
    }
}
